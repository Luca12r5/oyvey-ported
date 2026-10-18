// Minimal stand-in for the few gsap.to() calls in the ported NoRisk components:
// CSS transitions on inline styles. (The launcher does not ship gsap.)
type Vars = { [k: string]: unknown; duration?: number; ease?: string; onComplete?: () => void };

const EASE: Record<string, string> = {
  'power2.out': 'cubic-bezier(.33,1,.68,1)',
  'power2.inOut': 'cubic-bezier(.65,0,.35,1)',
  'power3.out': 'cubic-bezier(.22,1,.36,1)',
  none: 'linear',
};

function to(el: unknown, v: Vars): void {
  if (!(el instanceof HTMLElement)) return;
  const d = v.duration ?? 0.3;
  const ease = v.ease?.startsWith('elastic') || v.ease?.startsWith('back') ? 'cubic-bezier(.34,1.56,.64,1)' : (EASE[v.ease ?? ''] ?? 'ease');
  el.style.transition = `all ${d}s ${ease}`;
  for (const [k, val] of Object.entries(v)) {
    if (k === 'duration' || k === 'ease' || k === 'onComplete') continue;
    if (k === 'x' || k === 'y') el.style.translate = k === 'x' ? `${val}px 0` : `0 ${val}px`;
    else if (k === 'scale') el.style.scale = String(val);
    else (el.style as unknown as Record<string, string>)[k] = String(val);
  }
  if (v.onComplete) setTimeout(v.onComplete, d * 1000);
}

export const gsap = { to };
