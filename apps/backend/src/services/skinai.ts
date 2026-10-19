// Photo → Minecraft skin: Claude looks at the photo and fills a SkinSpec (skin
// tone, hair, eyes, clothes, accessory); the shared generator paints the skin.
// The photo is only sent to the API for this one request and never stored.

import { ACCESSORIES, GLASSES, HAIR_STYLES, TOP_KINDS, TOP_PATTERNS, sanitizeSkinSpec, type SkinSpec } from '@lego/shared';
import { claudeClient } from './claude.ts';

export type SkinAi = (image: { mediaType: 'image/jpeg' | 'image/png' | 'image/webp'; base64: string }) => Promise<SkinSpec>;

const color = { type: 'string', description: 'Hex colour like #a1b2c3' };
const SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['skin', 'hair', 'hairStyle', 'eyes', 'beard', 'glasses', 'top', 'pants', 'shoes', 'accessory', 'accessoryColor', 'slim'],
  properties: {
    skin: color, hair: color, hairStyle: { type: 'string', enum: [...HAIR_STYLES] }, eyes: color, beard: { type: 'boolean' },
    glasses: { type: 'string', enum: [...GLASSES] },
    top: {
      type: 'object', additionalProperties: false, required: ['kind', 'color', 'color2', 'pattern'],
      properties: { kind: { type: 'string', enum: [...TOP_KINDS] }, color, color2: color, pattern: { type: 'string', enum: [...TOP_PATTERNS] } },
    },
    pants: color, shoes: color, accessory: { type: 'string', enum: [...ACCESSORIES] }, accessoryColor: color, slim: { type: 'boolean' },
  },
};

const SYSTEM = `You turn a photo of a person into a description for a 64x64 Minecraft skin.
Describe only what is visible: skin tone, hair colour and style, eye colour, beard, glasses, the top (kind, main colour, a second colour for details, pattern), trousers, shoes and one head accessory.
If a part is not visible (e.g. only a face), choose plausible matching clothes. "slim" means thin arms; pick it only if asked or clearly fitting.
Use natural, slightly saturated colours that read well on a small pixel skin.
Never identify the person and never describe anything beyond these clothing and appearance fields.`;

export function claudeSkinAi(apiKey: string, model: string): SkinAi {
  return async (image) => {
    const client = await claudeClient(apiKey);
    const res = await client.beta.messages.create({
      model,
      max_tokens: 3000,
      output_config: { effort: 'low', format: { type: 'json_schema', schema: SCHEMA } },
      betas: ['server-side-fallback-2026-07-01'],
      fallbacks: 'default',
      system: SYSTEM,
      messages: [{
        role: 'user',
        content: [
          { type: 'image', source: { type: 'base64', media_type: image.mediaType, data: image.base64 } },
          { type: 'text', text: 'Create the skin description for this photo.' },
        ],
      }],
    });
    if (res.stop_reason === 'refusal') throw new Error('refused');
    const text = res.content.find((b) => b.type === 'text');
    if (!text || text.type !== 'text') throw new Error('no description returned');
    return sanitizeSkinSpec(JSON.parse(text.text));
  };
}
