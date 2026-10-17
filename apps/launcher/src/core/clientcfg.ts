// Reads and edits the LEGO client's module config (<gameDir>/config/legoclient.json)
// so the launcher (and its VR click GUI) can switch client modules. The client
// picks the change up on its next start.

export interface ClientModule { id: string; on: boolean }

export function listModules(json: string): ClientModule[] {
  try {
    const cfg = JSON.parse(json) as { modules?: Record<string, { on?: unknown }> };
    if (!cfg.modules || typeof cfg.modules !== 'object') return [];
    return Object.entries(cfg.modules)
      .filter(([id]) => /^[a-z0-9_.-]{1,48}$/i.test(id))
      .map(([id, m]) => ({ id, on: m?.on === true }))
      .sort((a, b) => a.id.localeCompare(b.id));
  } catch {
    return [];
  }
}

/** Returns the new file content with only `modules.<id>.on` changed, or null if the module is unknown. */
export function setModule(json: string, id: string, on: boolean): string | null {
  let cfg: { modules?: Record<string, Record<string, unknown>> };
  try { cfg = JSON.parse(json); } catch { return null; }
  const m = cfg.modules?.[id];
  if (!m || typeof m !== 'object') return null;
  m.on = on;
  return JSON.stringify(cfg, null, 2);
}
