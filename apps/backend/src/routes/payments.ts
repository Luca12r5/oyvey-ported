// Real-money purchases through Stripe Checkout. Card data never touches this
// server: Stripe hosts the payment page, and only signed webhook events grant
// anything. Each event id is stored once, so retried deliveries are no-ops.
//
// Configure STRIPE_SECRET_KEY and STRIPE_WEBHOOK_SECRET; without them the
// endpoints answer 503 and the shop only offers credit purchases.

import { createHmac } from 'node:crypto';
import { currentSeason, getProduct, type Product } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import type { Db } from '../db.ts';
import { badRequest, HttpError, notFound } from '../http.ts';
import { obj, safeEqual, str } from '../security.ts';
import { audit } from '../services/audit.ts';
import { applyCredits } from '../services/credits.ts';
import { getUser } from '../services/users.ts';

const SIGNATURE_TOLERANCE_S = 300;

/** Verifies a Stripe-Signature header (scheme v1, HMAC-SHA256). */
export function verifyStripeSignature(rawBody: Buffer, header: string, secret: string, nowSec = Math.floor(Date.now() / 1000)): boolean {
  const parts = Object.fromEntries(header.split(',').map((p) => p.split('=') as [string, string]).filter((p) => p.length === 2));
  const t = Number(parts.t);
  if (!Number.isFinite(t) || Math.abs(nowSec - t) > SIGNATURE_TOLERANCE_S) return false;
  const expected = createHmac('sha256', secret).update(`${t}.`).update(rawBody).digest('hex');
  const sigs = header.split(',').filter((p) => p.startsWith('v1=')).map((p) => p.slice(3));
  return sigs.some((s) => safeEqual(s, expected));
}

/** Grants a product once per payment event. Returns false for duplicates. */
export function fulfill(db: Db, eventId: string, userId: string, product: Product, amount: number, currency: string): boolean {
  return db.tx(() => {
    const ins = db.run(
      'INSERT OR IGNORE INTO payments(event_id, user_id, product_id, amount, currency, created_at) VALUES (:e, :u, :p, :a, :c, :t)',
      { e: eventId, u: userId, p: product.id, a: amount, c: currency, t: Date.now() },
    );
    if (ins.changes === 0) return false;
    if (product.kind === 'credits' && product.credits) {
      applyCredits(db, { userId, delta: product.credits, kind: 'payment', reason: `Purchase ${product.id}`, ref: eventId, idemKey: `stripe:${eventId}` });
    } else if (product.kind === 'pass') {
      const season = currentSeason();
      if (season) {
        db.run(
          `INSERT INTO pass_progress(user_id, season_id, xp, premium) VALUES (:u, :s, 0, 1)
           ON CONFLICT(user_id, season_id) DO UPDATE SET premium = 1`,
          { u: userId, s: season.id },
        );
      }
    } else if (product.kind === 'subscription' && product.tier && product.periodDays) {
      const now = Date.now();
      const cur = db.get<{ expires_at: number; tier: string }>('SELECT expires_at, tier FROM subscriptions WHERE user_id = :u', { u: userId });
      const base = cur && cur.expires_at > now && cur.tier === product.tier ? cur.expires_at : now;
      const expires = base + product.periodDays * 86_400_000;
      db.run(
        `INSERT INTO subscriptions(user_id, tier, expires_at, updated_at) VALUES (:u, :t, :e, :n)
         ON CONFLICT(user_id) DO UPDATE SET tier = :t, expires_at = :e, updated_at = :n`,
        { u: userId, t: product.tier, e: expires, n: now },
      );
      const stipend = product.tier === 'lego_plus_plus' ? 1000 : 400;
      applyCredits(db, { userId, delta: stipend, kind: 'subscription', reason: `${product.name} period credits`, ref: eventId, idemKey: `stripe-stipend:${eventId}` });
    }
    audit(db, null, 'payment.fulfilled', userId, { eventId, product: product.id, amount, currency }, null);
    return true;
  });
}

function form(data: Record<string, string>): string {
  return Object.entries(data).map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join('&');
}

export function registerPayments(app: App): void {
  const r = app.router;

  r.post('/api/payments/checkout', async (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const key = app.config.stripeSecretKey;
    if (!key || !app.config.stripeWebhookSecret) throw new HttpError(503, 'payments_disabled', 'Payments are not configured on this server');
    const product = getProduct(str(obj(ctx.body), 'productId', 2, 64));
    if (!product) throw notFound('Unknown product');
    const origin = app.config.publicOrigin;
    const fields: Record<string, string> = {
      mode: product.kind === 'subscription' ? 'subscription' : 'payment',
      client_reference_id: u.id,
      success_url: `${origin}/shop?checkout=success`,
      cancel_url: `${origin}/shop?checkout=cancel`,
      'line_items[0][quantity]': '1',
      'line_items[0][price_data][currency]': 'eur',
      'line_items[0][price_data][unit_amount]': String(product.priceCents),
      'line_items[0][price_data][product_data][name]': product.name,
      'metadata[user_id]': u.id,
      'metadata[product_id]': product.id,
    };
    if (product.kind === 'subscription') {
      fields['line_items[0][price_data][recurring][interval]'] = 'month';
      fields['subscription_data[metadata][user_id]'] = u.id;
      fields['subscription_data[metadata][product_id]'] = product.id;
    }
    const res = await app.fetch('https://api.stripe.com/v1/checkout/sessions', {
      method: 'POST',
      headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/x-www-form-urlencoded', 'Idempotency-Key': `${u.id}:${product.id}:${Math.floor(Date.now() / 60_000)}` },
      body: form(fields),
      signal: AbortSignal.timeout(10_000),
    });
    const data = (await res.json()) as { url?: string; error?: { message?: string } };
    if (!res.ok || !data.url) throw new HttpError(502, 'stripe_error', data.error?.message ?? 'Checkout could not be created');
    return { url: data.url };
  }, { rate: ['checkout', 10] });

  r.post('/api/payments/webhook', (ctx: AppCtx) => {
    const secret = app.config.stripeWebhookSecret;
    if (!secret) throw new HttpError(503, 'payments_disabled');
    const sig = String(ctx.req.headers['stripe-signature'] ?? '');
    if (!ctx.rawBody || !verifyStripeSignature(ctx.rawBody, sig, secret)) throw new HttpError(400, 'invalid_signature');
    const event = JSON.parse(ctx.rawBody.toString('utf8')) as { id: string; type: string; data: { object: Record<string, any> } };
    const o = event.data.object;
    let meta: Record<string, string> | undefined;
    let amount = 0;
    let currency = 'eur';
    if (event.type === 'checkout.session.completed') {
      if (o.payment_status !== 'paid') return { received: true, ignored: 'unpaid' };
      meta = o.metadata;
      amount = o.amount_total ?? 0;
      currency = o.currency ?? 'eur';
    } else if (event.type === 'invoice.paid' && o.billing_reason === 'subscription_cycle') {
      meta = o.subscription_details?.metadata ?? o.parent?.subscription_details?.metadata;
      amount = o.amount_paid ?? 0;
      currency = o.currency ?? 'eur';
    } else {
      return { received: true, ignored: event.type };
    }
    const product = meta?.product_id ? getProduct(meta.product_id) : undefined;
    const user = meta?.user_id ? getUser(app.db, meta.user_id) : undefined;
    if (!product || !user) throw badRequest('unknown_metadata');
    const granted = fulfill(app.db, event.id, user.id, product, amount, currency);
    if (granted) app.events.send(user.id, 'purchase', { product: product.id });
    return { received: true, granted };
  }, { auth: 'none', rawBody: true, anyContentType: true, bodyLimit: 1024 * 1024 });
}
