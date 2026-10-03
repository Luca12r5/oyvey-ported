// Applies a theme to the document and stores custom themes.

import { getTheme, THEMES, validateTheme, type Theme } from '@lego/shared';

export function allThemes(custom: unknown[]): Theme[] {
  const ok = custom.map((c) => validateTheme(c)).filter((v): v is { ok: true; theme: Theme } => v.ok).map((v) => v.theme);
  return [...THEMES, ...ok];
}

export function findTheme(id: string, custom: unknown[]): Theme {
  return allThemes(custom).find((t) => t.id === id) ?? getTheme('lego-graphite') ?? getTheme('nightfall')!;
}

export function applyTheme(t: Theme, opts: { reducedMotion: boolean; uiScale: number }): void {
  const r = document.documentElement.style;
  const c = t.colors;
  const vars: Record<string, string> = {
    bg: c.bg, bg2: c.bg2, surface: c.surface, 'surface-hover': c.surfaceHover, border: c.border, text: c.text, muted: c.textMuted,
    accent: c.accent, accent2: c.accent2, success: c.success, warning: c.warning, danger: c.danger,
    radius: `${t.radius}px`, blur: `${t.blur}px`, 'surface-alpha': String(t.surfaceAlpha), 'border-w': `${t.borderWidth}px`,
    'bg-intensity': String(t.background.intensity), scale: String(Math.max(0.75, Math.min(2, t.scale * opts.uiScale))),
  };
  for (const [k, v] of Object.entries(vars)) r.setProperty(`--${k}`, v);
  if (t.background.kind === 'image' && t.background.image) r.setProperty('--bg-image', `url("${t.background.image}")`);
  const b = document.body;
  b.className = [`font-${t.font}`, `shadow-${t.shadow}`, `motion-${t.motion}`, `bg-${t.background.kind}`, opts.reducedMotion ? 'reduced' : ''].filter(Boolean).join(' ');
  document.documentElement.style.colorScheme = t.mode;
}
