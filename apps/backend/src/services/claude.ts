// Claude API client for the optional AI features (cape designer, photo → skin).
// The SDK is loaded on first use, so the server starts without it when no
// ANTHROPIC_API_KEY is configured.

import type Anthropic from '@anthropic-ai/sdk';

const clients = new Map<string, Promise<Anthropic>>();

export function claudeClient(apiKey: string): Promise<Anthropic> {
  let c = clients.get(apiKey);
  if (!c) {
    c = import('@anthropic-ai/sdk').then(({ default: Sdk }) => new Sdk({ apiKey, maxRetries: 1, timeout: 60_000 }));
    clients.set(apiKey, c);
  }
  return c;
}
