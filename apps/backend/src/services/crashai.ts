// Crash doctor with AI on the LEGO server: reads a Minecraft crash report / log excerpt (sent
// by the launcher only when the player asks for it) and returns the likely causes with one fix
// each. Schema, instructions and the answer check live in @lego/shared (crash-ai.ts).

import { CRASH_AI_SCHEMA, CRASH_AI_SYSTEM, crashAiUserMessage, sanitizeCrashResult, type CrashAiInput, type CrashAiResult } from '@lego/shared';
import { claudeClient } from './claude.ts';

export { sanitizeCrashResult };
export type { CrashAiInput, CrashAiResult, CrashCause, CrashFix, CrashMod } from '@lego/shared';
export type CrashAi = (input: CrashAiInput) => Promise<CrashAiResult>;

export function claudeCrashAi(apiKey: string, model: string): CrashAi {
  return async (input) => {
    const client = await claudeClient(apiKey);
    const res = await client.beta.messages.create({
      model,
      max_tokens: 4000,
      output_config: { effort: 'low', format: { type: 'json_schema', schema: CRASH_AI_SCHEMA } },
      betas: ['server-side-fallback-2026-07-01'],
      fallbacks: 'default',
      system: CRASH_AI_SYSTEM,
      messages: [{
        role: 'user',
        content: crashAiUserMessage(input),
      }],
    });
    if (res.stop_reason === 'refusal') throw new Error('refused');
    const block = res.content.find((b) => b.type === 'text');
    if (!block || block.type !== 'text') throw new Error('no answer');
    return sanitizeCrashResult(JSON.parse(block.text), input.mods);
  };
}
