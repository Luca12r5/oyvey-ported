// Turns a player's cape description into a CapeSpec with Claude (structured
// output). The API key lives only in the server environment (ANTHROPIC_API_KEY);
// the answer is validated with the same sanitizer as every other spec.

import Anthropic from '@anthropic-ai/sdk';
import { CAPE_BG_TYPES, CAPE_MAX_SYMBOLS, CAPE_SYMBOLS, CAPE_SYMBOL_IDS, sanitizeCapeSpec, type CapeSpec } from '@lego/shared';

export type CapeAi = (prompt: string) => Promise<CapeSpec>;

const color = { type: 'string', description: 'Hex colour like #a1b2c3' };
/** Structured-output schema (strict JSON: every object closed, every field required). */
const SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['bg', 'symbols', 'border', 'text', 'shine'],
  properties: {
    bg: {
      type: 'object', additionalProperties: false, required: ['type', 'colors', 'angle'],
      properties: { type: { type: 'string', enum: [...CAPE_BG_TYPES] }, colors: { type: 'array', items: color }, angle: { type: 'integer' } },
    },
    symbols: {
      type: 'array',
      items: {
        type: 'object', additionalProperties: false, required: ['id', 'x', 'y', 'size', 'rot', 'color', 'glow'],
        properties: { id: { type: 'string', enum: CAPE_SYMBOL_IDS }, x: { type: 'number' }, y: { type: 'number' }, size: { type: 'number' }, rot: { type: 'integer' }, color, glow: { type: 'boolean' } },
      },
    },
    border: { type: 'object', additionalProperties: false, required: ['width', 'color'], properties: { width: { type: 'integer' }, color } },
    text: {
      type: 'object', additionalProperties: false, required: ['value', 'color', 'y', 'size'],
      properties: { value: { type: 'string' }, color, y: { type: 'number' }, size: { type: 'integer' } },
    },
    shine: { type: 'boolean' },
  },
};

const SYSTEM = `You design Minecraft capes for the LEGO Launcher. A cape front is 10x16 pixels (portrait, 80x128 texture pixels).
Return a design that matches the player's description, looks modern and readable, and has strong contrast.
Rules:
- bg.type: ${CAPE_BG_TYPES.join(', ')}. bg.colors: 2 or 3 hex colours (galaxy uses 3: deep base, space, nebula). bg.angle 0-359 (gradient/stripes direction; 90 = top to bottom).
- symbols: 0 to ${CAPE_MAX_SYMBOLS}. id is one of: ${Object.entries(CAPE_SYMBOLS).map(([id, s]) => `${id} (${s.name})`).join(', ')}.
  x, y are the centre in 0..1 of the front (y 0 = top), size is the diameter as a share of the width (0.1..1), rot in degrees (multiples of 15), glow adds a soft halo.
  One main symbol of size 0.5-0.7 around y 0.4 usually looks best; small symbols (0.12-0.3) are accents.
- border.width 0-8 texture pixels (0 = none).
- text: only if the player asks for text or a name. Max 10 characters A-Z 0-9 and ! ? - + . # *. Otherwise use value "".
- shine: a soft diagonal highlight.
Never put offensive, political or hateful content on a cape.`;

export function claudeCapeAi(apiKey: string, model: string): CapeAi {
  const client = new Anthropic({ apiKey, maxRetries: 1, timeout: 60_000 });
  return async (prompt) => {
    const res = await client.beta.messages.create({
      model,
      max_tokens: 4000,
      output_config: { effort: 'low', format: { type: 'json_schema', schema: SCHEMA } },
      betas: ['server-side-fallback-2026-07-01'],
      fallbacks: 'default',
      system: SYSTEM,
      messages: [{ role: 'user', content: `Cape description from the player:\n<description>\n${prompt}\n</description>` }],
    });
    if (res.stop_reason === 'refusal') throw new Error('refused');
    const text = res.content.find((b) => b.type === 'text');
    if (!text || text.type !== 'text') throw new Error('no design returned');
    const raw = JSON.parse(text.text) as Record<string, unknown>;
    const t = raw.text as { value?: string } | undefined;
    return sanitizeCapeSpec({ ...raw, text: t && t.value ? t : null });
  };
}
