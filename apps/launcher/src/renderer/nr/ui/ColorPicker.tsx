// Custom colour picker in the style of the NoRisk launcher's ColorPickerModal
// (GPL-3.0, NoRiskClient team; see NOTICE.md): a saturation/brightness field, a
// hue bar, Hex and RGB fields. Changed: colours are previewed live while you
// drag (onPreview), recently used colours are remembered, keyboard/touch work.
import { useCallback, useEffect, useRef, useState } from 'react';
import { Button } from './buttons/Button.tsx';

export interface ColorPickerProps {
  title: string;
  value: string;
  /** Called while dragging (live preview). */
  onPreview?: (hex: string) => void;
  onApply: (hex: string) => void;
  onClose: () => void;
}

function hsvToRgb(h: number, s: number, v: number) {
  const c = v * s, x = c * (1 - Math.abs(((h / 60) % 2) - 1)), m = v - c;
  const [r, g, b] = h < 60 ? [c, x, 0] : h < 120 ? [x, c, 0] : h < 180 ? [0, c, x] : h < 240 ? [0, x, c] : h < 300 ? [x, 0, c] : [c, 0, x];
  return { r: Math.round((r + m) * 255), g: Math.round((g + m) * 255), b: Math.round((b + m) * 255) };
}

function rgbToHsv(r: number, g: number, b: number) {
  r /= 255; g /= 255; b /= 255;
  const max = Math.max(r, g, b), min = Math.min(r, g, b), d = max - min;
  let h = 0;
  if (d) h = max === r ? ((g - b) / d + (g < b ? 6 : 0)) * 60 : max === g ? ((b - r) / d + 2) * 60 : ((r - g) / d + 4) * 60;
  return { h, s: max ? d / max : 0, v: max };
}

const toHex = (r: number, g: number, b: number) => `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1).toUpperCase()}`;
const fromHex = (hex: string) => {
  const m = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return m ? { r: parseInt(m[1]!, 16), g: parseInt(m[2]!, 16), b: parseInt(m[3]!, 16) } : { r: 255, g: 122, b: 26 };
};
const RECENT_KEY = 'lego.recentColors';
const recent = (): string[] => { try { return (JSON.parse(localStorage.getItem(RECENT_KEY) ?? '[]') as string[]).filter((c) => /^#[0-9a-f]{6}$/i.test(c)).slice(0, 8); } catch { return []; } };

export function ColorPicker(p: ColorPickerProps) {
  const [hsv, setHsv] = useState(() => { const c = fromHex(p.value); return rgbToHsv(c.r, c.g, c.b); });
  const rgb = hsvToRgb(hsv.h, hsv.s, hsv.v);
  const hex = toHex(rgb.r, rgb.g, rgb.b);
  const [hexText, setHexText] = useState(hex);
  const [drag, setDrag] = useState<'sv' | 'hue' | null>(null);
  const svRef = useRef<HTMLDivElement>(null);
  const hueRef = useRef<HTMLDivElement>(null);

  useEffect(() => { setHexText(hex); p.onPreview?.(hex); }, [hex]); // eslint-disable-line react-hooks/exhaustive-deps

  const pick = useCallback((kind: 'sv' | 'hue', x: number, y: number) => {
    const el = kind === 'sv' ? svRef.current : hueRef.current;
    if (!el) return;
    const r = el.getBoundingClientRect();
    const fx = Math.max(0, Math.min(1, (x - r.left) / r.width)), fy = Math.max(0, Math.min(1, (y - r.top) / r.height));
    setHsv((o) => (kind === 'sv' ? { ...o, s: fx, v: 1 - fy } : { ...o, h: fy * 359.99 }));
  }, []);

  useEffect(() => {
    if (!drag) return;
    const move = (e: PointerEvent) => pick(drag, e.clientX, e.clientY);
    const up = () => setDrag(null);
    addEventListener('pointermove', move);
    addEventListener('pointerup', up);
    return () => { removeEventListener('pointermove', move); removeEventListener('pointerup', up); };
  }, [drag, pick]);

  const apply = () => {
    try { localStorage.setItem(RECENT_KEY, JSON.stringify([hex, ...recent().filter((c) => c.toUpperCase() !== hex)].slice(0, 8))); } catch { /* storage blocked */ }
    p.onApply(hex);
  };
  const setRgb = (k: 'r' | 'g' | 'b', v: string) => { const n = { ...rgb, [k]: Math.max(0, Math.min(255, parseInt(v, 10) || 0)) }; setHsv(rgbToHsv(n.r, n.g, n.b)); };
  const nudge = (e: React.KeyboardEvent, kind: 'sv' | 'hue') => {
    const step = e.shiftKey ? 0.1 : 0.02;
    const d = { ArrowLeft: [-step, 0], ArrowRight: [step, 0], ArrowUp: [0, step], ArrowDown: [0, -step] }[e.key];
    if (!d) return;
    e.preventDefault();
    setHsv((o) => (kind === 'sv' ? { ...o, s: Math.max(0, Math.min(1, o.s + d[0]!)), v: Math.max(0, Math.min(1, o.v + d[1]!)) } : { ...o, h: Math.max(0, Math.min(359.99, o.h - d[1]! * 360)) }));
  };
  const input = 'w-full px-3 py-2 text-white focus:outline-none';
  const field = { background: 'rgba(0,0,0,.5)', border: '1px solid rgba(255,255,255,.28)', borderRadius: 'calc(var(--border-radius) * .6)', fontSize: 15 } as const;

  return (
    <div id="colorPicker" className="nr fixed inset-0 z-[950] flex items-center justify-center bg-black/70 backdrop-blur-sm p-6" onMouseDown={(e) => { if (e.target === e.currentTarget) p.onClose(); }}>
      <div className="w-full max-w-[560px] border-2 p-6 space-y-5" style={{ borderColor: `${hex}90`, background: 'color-mix(in srgb, var(--accent) 7%, #08090d)', borderRadius: 'var(--border-radius)', boxShadow: `0 0 34px ${hex}40` }}>
        <h2 className="font-minecraft text-2xl text-white m-0">{p.title}</h2>
        <div className="flex gap-5">
          <div ref={svRef} id="cpSv" tabIndex={0} role="slider" aria-label="Sättigung und Helligkeit" onKeyDown={(e) => nudge(e, 'sv')}
            className="relative flex-1 h-52 cursor-crosshair border border-white/20 touch-none" style={{ background: `hsl(${hsv.h}, 100%, 50%)`, borderRadius: 'calc(var(--border-radius) * .7)' }}
            onPointerDown={(e) => { e.preventDefault(); setDrag('sv'); pick('sv', e.clientX, e.clientY); }}>
            <div className="absolute inset-0" style={{ borderRadius: 'inherit', background: 'linear-gradient(to top, #000, transparent), linear-gradient(to right, #fff, transparent)' }} />
            <div className="absolute w-5 h-5 border-2 border-white rounded-full shadow-lg -translate-x-1/2 -translate-y-1/2 pointer-events-none" style={{ left: `${hsv.s * 100}%`, top: `${(1 - hsv.v) * 100}%`, backgroundColor: hex }} />
          </div>
          <div ref={hueRef} id="cpHue" tabIndex={0} role="slider" aria-label="Farbton" onKeyDown={(e) => nudge(e, 'hue')}
            className="relative w-9 h-52 cursor-pointer border border-white/20 touch-none" style={{ borderRadius: 'calc(var(--border-radius) * .7)', background: 'linear-gradient(to bottom, #f00, #ff0, #0f0, #0ff, #00f, #f0f, #f00)' }}
            onPointerDown={(e) => { e.preventDefault(); setDrag('hue'); pick('hue', e.clientX, e.clientY); }}>
            <div className="absolute -left-1 -right-1 h-2 bg-white border border-black -translate-y-1/2 pointer-events-none rounded-sm" style={{ top: `${(hsv.h / 359.99) * 100}%` }} />
          </div>
        </div>
        <div className="grid grid-cols-4 gap-3">
          <label className="block"><span className="block text-sm text-white/80 mb-1">Hex</span>
            <input id="cpHex" className={input} value={hexText} onChange={(e) => { setHexText(e.target.value); if (/^#?[0-9a-f]{6}$/i.test(e.target.value)) { const c = fromHex(e.target.value); setHsv(rgbToHsv(c.r, c.g, c.b)); } }} style={field} /></label>
          {(['r', 'g', 'b'] as const).map((k) => (
            <label key={k} className="block"><span className="block text-sm text-white/80 mb-1">{k.toUpperCase()}</span>
              <input type="number" min={0} max={255} className={input} value={rgb[k]} onChange={(e) => setRgb(k, e.target.value)} style={field} /></label>
          ))}
        </div>
        {recent().length > 0 && (
          <div className="flex items-center gap-2"><span className="text-sm text-white/80 mr-1">Zuletzt</span>
            {recent().map((c) => <button key={c} title={c} className="w-7 h-7 border border-white/30" style={{ background: c, borderRadius: 'calc(var(--border-radius) * .5)' }} onClick={() => { const v = fromHex(c); setHsv(rgbToHsv(v.r, v.g, v.b)); }} />)}
          </div>
        )}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 border-2 border-white/25 shadow-lg" style={{ backgroundColor: hex, borderRadius: 'calc(var(--border-radius) * .7)' }} />
            <span className="text-white text-base">{hex}</span>
          </div>
          <div className="flex gap-2">
            <Button size="sm" variant="ghost" onClick={p.onClose}>Abbrechen</Button>
            <Button id="cpApply" size="sm" onClick={apply}>Übernehmen</Button>
          </div>
        </div>
      </div>
    </div>
  );
}
