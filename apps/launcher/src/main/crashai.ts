// Crash doctor AI with the player's own Claude API key: the launcher asks Claude directly
// (same schema, instructions and answer check as the LEGO server, from @lego/shared).
// The log is sanitized before (crashExcerpt) and only sent when the player clicks.

import Anthropic from '@anthropic-ai/sdk';
import { CRASH_AI_SCHEMA, CRASH_AI_SYSTEM, crashAiUserMessage, sanitizeCrashResult, type CrashAiInput, type CrashAiResult } from '@lego/shared';

export const CRASH_AI_MODEL = 'claude-opus-5-5';

/** A plausible Anthropic API key (format check only; the API decides if it works). */
export function validAiKey(key: unknown): key is string {
  return typeof key === 'string' && /^sk-ant-[A-Za-z0-9_\-]{20,300}$/.test(key.trim());
}

function client(apiKey: string): Anthropic {
  return new Anthropic({ apiKey, maxRetries: 1, timeout: 90_000 });
}

export async function localCrashAi(apiKey: string, input: CrashAiInput): Promise<CrashAiResult> {
  const res = await client(apiKey).beta.messages.create({
    model: CRASH_AI_MODEL,
    max_tokens: 4000,
    output_config: { effort: 'low', format: { type: 'json_schema', schema: CRASH_AI_SCHEMA } },
    betas: ['server-side-fallback-2026-07-01'],
    fallbacks: 'default',
    system: CRASH_AI_SYSTEM,
    messages: [{ role: 'user', content: crashAiUserMessage(input) }],
  });
  if (res.stop_reason === 'refusal') throw new Error('Die KI hat die Anfrage abgelehnt.');
  const block = res.content.find((b) => b.type === 'text');
  if (!block || block.type !== 'text') throw new Error('Die KI hat keine Antwort geliefert.');
  return sanitizeCrashResult(JSON.parse(block.text), input.mods);
}

/** Readable German error for the dialog (no key or request details in it). */
export function aiError(e: unknown): string {
  if (e instanceof Anthropic.AuthenticationError) return 'Der API-Schlüssel ist ungültig. Prüfe ihn unter Optionen → KI.';
  if (e instanceof Anthropic.PermissionDeniedError) return 'Dieser API-Schlüssel darf das Modell nicht nutzen.';
  if (e instanceof Anthropic.RateLimitError) return 'Zu viele Anfragen gerade. Versuch es in einer Minute noch einmal.';
  if (e instanceof Anthropic.APIConnectionError) return 'Keine Verbindung zur KI. Bist du online?';
  if (e instanceof Anthropic.APIError) return `KI-Fehler (${e.status ?? '?'}).`;
  return e instanceof Error ? e.message : String(e);
}

/** Cheap check that the key works (counts a token, no model output). */
export async function testAiKey(apiKey: string): Promise<void> {
  await client(apiKey).messages.countTokens({ model: CRASH_AI_MODEL, messages: [{ role: 'user', content: 'test' }] });
}
