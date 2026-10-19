// Skin preview before saving: the skin as a turnable 3D model (drag to rotate,
// wheel to zoom, poses), Classic/Slim, a name, and for skins made from a photo
// the description (colours, hair, clothes) to adjust before saving.
import { useEffect, useMemo, useRef, useState } from 'react';
import { ACCESSORIES, GLASSES, HAIR_STYLES, TOP_KINDS, TOP_PATTERNS, renderSkin, sanitizeSkinSpec, type SkinSpec } from '@lego/shared';
import { createAvatar, type Avatar } from '../../avatar.ts';
import { Icon } from '../lib/iconify.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { useThemeStore } from '../store/theme.ts';

export interface SkinDialogProps {
  title: string;
  /** A ready skin PNG (upload) … */
  data?: string;
  /** … or a description to paint and edit (photo → skin). */
  spec?: SkinSpec;
  note?: string;
  name: string;
  variant: 'classic' | 'slim';
  onSave: (data: string, variant: 'classic' | 'slim', name: string) => Promise<void> | void;
  onClose: () => void;
}

function specToPng(spec: SkinSpec): string {
  const c = document.createElement('canvas');
  c.width = c.height = 64;
  c.getContext('2d')!.putImageData(new ImageData(new Uint8ClampedArray(renderSkin(spec)), 64, 64), 0, 0);
  return c.toDataURL('image/png');
}

const HAIR_NAMES: Record<string, string> = { short: 'Kurz', buzz: 'Sehr kurz', long: 'Lang', curly: 'Lockig', ponytail: 'Zopf', bald: 'Glatze' };
const TOP_NAMES: Record<string, string> = { tshirt: 'T-Shirt', hoodie: 'Hoodie', shirt: 'Hemd', jacket: 'Jacke' };
const PATTERN_NAMES: Record<string, string> = { none: 'Einfarbig', stripes: 'Streifen', logo: 'Logo' };
const GLASSES_NAMES: Record<string, string> = { none: 'Keine', normal: 'Brille', sun: 'Sonnenbrille' };
const ACC_NAMES: Record<string, string> = { none: 'Nichts', cap: 'Kappe', beanie: 'Mütze', headphones: 'Kopfhörer' };

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return <label className="flex items-center justify-between gap-3 py-1"><span className="text-sm text-white/85">{label}</span><span className="flex items-center gap-2">{children}</span></label>;
}
function Select<T extends string>({ value, options, names, onChange }: { value: T; options: readonly T[]; names: Record<string, string>; onChange: (v: T) => void }) {
  return <select value={value} onChange={(e) => onChange(e.target.value as T)} className="px-2 py-1 text-white text-sm" style={{ background: 'rgba(0,0,0,.45)', border: '1px solid rgba(255,255,255,.2)', borderRadius: 'calc(var(--border-radius) * .6)' }}>{options.map((o) => <option key={o} value={o}>{names[o] ?? o}</option>)}</select>;
}
function Color({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  return <input type="color" value={value} onChange={(e) => onChange(e.target.value)} className="w-9 h-8" />;
}

export function SkinDialog(p: SkinDialogProps) {
  const accent = useThemeStore((s) => s.accentColor);
  const [spec, setSpec] = useState<SkinSpec | null>(p.spec ? sanitizeSkinSpec(p.spec) : null);
  const [variant, setVariant] = useState<'classic' | 'slim'>(p.spec?.slim ? 'slim' : p.variant);
  const [name, setName] = useState(p.name);
  const [anim, setAnim] = useState<'idle' | 'walk' | 'wave' | 'spin'>('idle');
  const [busy, setBusy] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const avatarRef = useRef<Avatar | null>(null);
  const data = useMemo(() => (spec ? specToPng({ ...spec, slim: variant === 'slim' }) : p.data ?? null), [spec, variant, p.data]);
  const upd = (patch: Partial<SkinSpec>) => setSpec((s) => (s ? sanitizeSkinSpec({ ...s, ...patch }) : s));
  const updTop = (patch: Partial<SkinSpec['top']>) => setSpec((s) => (s ? sanitizeSkinSpec({ ...s, top: { ...s.top, ...patch } }) : s));

  useEffect(() => {
    try { avatarRef.current = createAvatar(canvasRef.current!, false); } catch { /* no WebGL */ }
    return () => { avatarRef.current?.dispose(); avatarRef.current = null; };
  }, []);
  useEffect(() => { if (data) void avatarRef.current?.setSkin(data, variant, null); }, [data, variant]);
  useEffect(() => { avatarRef.current?.play(anim); }, [anim]);

  return (
    <div id="skinDialog" className="nr fixed inset-0 z-[900] flex items-center justify-center bg-black/70 backdrop-blur-sm p-6" onMouseDown={(e) => { if (e.target === e.currentTarget) p.onClose(); }}>
      <div className="relative w-full max-w-[920px] max-h-full flex flex-col border-2 overflow-hidden" style={{ borderColor: `${accent.value}80`, background: 'color-mix(in srgb, var(--accent) 7%, #06070b)', borderRadius: 'var(--border-radius)', boxShadow: `0 0 30px ${accent.value}40` }}>
        <header className="flex items-center justify-between px-5 py-3 border-b-2" style={{ borderColor: `${accent.value}40` }}>
          <h2 className="font-minecraft text-2xl text-white m-0">{p.title}</h2>
          <button className="text-white/70 hover:text-white" aria-label="Schließen" onClick={p.onClose}><Icon icon="pixel:window-close-solid" className="w-5 h-5" /></button>
        </header>
        <div className="flex-1 min-h-0 flex gap-5 p-5 overflow-hidden">
          <div className="flex flex-col items-center gap-3 w-[340px] flex-shrink-0">
            <div className="relative w-[320px] h-[420px] border border-white/10" style={{ borderRadius: 'var(--border-radius)', background: `radial-gradient(ellipse at 50% 75%, ${accent.value}30, transparent 65%)` }}>
              <canvas id="skinDialogView" ref={canvasRef} className="absolute inset-0 w-full h-full" />
            </div>
            <div className="flex flex-wrap justify-center gap-1.5">
              {(['idle', 'walk', 'wave', 'spin'] as const).map((a) => (
                <button key={a} onClick={() => setAnim(a)} className="px-2.5 py-1 text-sm font-minecraft border text-white" style={{ borderRadius: 'calc(var(--border-radius) * .6)', borderColor: anim === a ? accent.value : 'rgba(255,255,255,.15)', background: anim === a ? `${accent.value}30` : 'transparent' }}>{{ idle: 'Stehen', walk: 'Gehen', wave: 'Winken', spin: 'Drehen' }[a]}</button>
              ))}
            </div>
            <p className="text-xs text-white/75 m-0 text-center">Ziehen dreht die Figur, Mausrad zoomt.</p>
          </div>
          <div className="flex-1 min-w-0 overflow-y-auto pr-1">
            {p.note && <p className="text-sm text-white/85 mt-0 mb-3">{p.note}</p>}
            <Row label="Name"><input id="skinName" value={name} maxLength={40} onChange={(e) => setName(e.target.value)} className="w-56 px-3 py-1.5 text-white" style={{ background: 'rgba(0,0,0,.45)', border: '1px solid rgba(255,255,255,.2)', borderRadius: 'calc(var(--border-radius) * .6)' }} /></Row>
            <Row label="Modell">
              {(['classic', 'slim'] as const).map((v) => <button key={v} data-variant={v} onClick={() => setVariant(v)} className="px-3 py-1 text-sm font-minecraft border text-white" style={{ borderColor: variant === v ? accent.value : 'rgba(255,255,255,.15)', background: variant === v ? `${accent.value}30` : 'transparent' }}>{v === 'classic' ? 'Classic' : 'Slim'}</button>)}
            </Row>
            {spec && (
              <div id="skinSpec" className="mt-3 border-t border-white/10 pt-3">
                <Row label="Haut"><Color value={spec.skin} onChange={(v) => upd({ skin: v })} /></Row>
                <Row label="Haare"><Select value={spec.hairStyle} options={HAIR_STYLES} names={HAIR_NAMES} onChange={(v) => upd({ hairStyle: v })} /><Color value={spec.hair} onChange={(v) => upd({ hair: v })} /></Row>
                <Row label="Augen"><Color value={spec.eyes} onChange={(v) => upd({ eyes: v })} /></Row>
                <Row label="Bart"><input type="checkbox" checked={spec.beard} onChange={(e) => upd({ beard: e.target.checked })} /></Row>
                <Row label="Brille"><Select value={spec.glasses} options={GLASSES} names={GLASSES_NAMES} onChange={(v) => upd({ glasses: v })} /></Row>
                <Row label="Oberteil"><Select value={spec.top.kind} options={TOP_KINDS} names={TOP_NAMES} onChange={(v) => updTop({ kind: v })} /><Color value={spec.top.color} onChange={(v) => updTop({ color: v })} /><Color value={spec.top.color2} onChange={(v) => updTop({ color2: v })} /></Row>
                <Row label="Muster"><Select value={spec.top.pattern} options={TOP_PATTERNS} names={PATTERN_NAMES} onChange={(v) => updTop({ pattern: v })} /></Row>
                <Row label="Hose"><Color value={spec.pants} onChange={(v) => upd({ pants: v })} /></Row>
                <Row label="Schuhe"><Color value={spec.shoes} onChange={(v) => upd({ shoes: v })} /></Row>
                <Row label="Kopf"><Select value={spec.accessory} options={ACCESSORIES} names={ACC_NAMES} onChange={(v) => upd({ accessory: v })} />{spec.accessory !== 'none' && <Color value={spec.accessoryColor} onChange={(v) => upd({ accessoryColor: v })} />}</Row>
              </div>
            )}
          </div>
        </div>
        <footer className="flex items-center justify-end gap-2 px-5 py-3 border-t-2" style={{ borderColor: `${accent.value}40` }}>
          <Button size="sm" variant="ghost" onClick={p.onClose}>Abbrechen</Button>
          <Button id="skinSave" size="sm" disabled={busy || !data} onClick={() => { if (!data) return; setBusy(true); void Promise.resolve(p.onSave(data, variant, name.trim() || p.name)).finally(() => setBusy(false)); }}>In Bibliothek speichern</Button>
        </footer>
      </div>
    </div>
  );
}
