// Cape editor: design an own cape from data (background, symbols, text, border,
// shine) with a live 3D preview on the own skin. "Mit KI erstellen" turns a
// description into a design (server AI when configured, otherwise the built-in
// offline generator); everything stays editable afterwards.
import { useEffect, useMemo, useRef, useState } from 'react';
import { SkinViewer } from 'skinview3d';
import { CAPE_BG_TYPES, CAPE_MAX_SYMBOLS, CAPE_PRESETS, CAPE_SYMBOLS, CAPE_SYMBOL_IDS, DEFAULT_CAPE, sanitizeCapeSpec, type CapeBgType, type CapeSpec, type CapeSymbol } from '@lego/shared';
import { capeFrontUrl, capeSheetUrl } from '../../capeart.ts';
import { DEFAULT_SKIN } from '../../avatar.ts';
import { Icon } from '../lib/iconify.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { useThemeStore } from '../store/theme.ts';

export interface CapeEditorProps {
  initial: CapeSpec | null;
  skin: string | null;
  variant: 'classic' | 'slim';
  /** True when the LEGO server can store the cape (others see it in game). */
  online: boolean;
  onAi: (prompt: string, seed: number) => Promise<{ spec: CapeSpec; source: 'server' | 'offline'; left?: number }>;
  onSave: (spec: CapeSpec) => Promise<void>;
  onClose: () => void;
}

const BG_NAMES: Record<CapeBgType, string> = { solid: 'Einfarbig', gradient: 'Verlauf', radial: 'Leuchten', stripes: 'Streifen', checker: 'Karo', galaxy: 'Galaxie', waves: 'Wellen', split: 'Geteilt' };

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="flex items-center justify-between gap-3 py-1.5">
      <span className="text-sm text-white/80 font-minecraft whitespace-nowrap">{label}</span>
      <span className="flex items-center gap-2 min-w-0">{children}</span>
    </label>
  );
}

function Slider({ value, min, max, step = 1, onChange, unit = '' }: { value: number; min: number; max: number; step?: number; onChange: (v: number) => void; unit?: string }) {
  return (
    <>
      <input type="range" min={min} max={max} step={step} value={value} onChange={(e) => onChange(Number(e.target.value))} className="w-36" />
      <span className="text-xs text-white/60 w-12 text-right font-mono">{step < 1 ? Math.round(value * 100) : value}{unit}</span>
    </>
  );
}

function Section({ title, children, right }: { title: string; children: React.ReactNode; right?: React.ReactNode }) {
  return (
    <section className="border border-white/10 bg-black/25 p-3 mb-3" style={{ borderRadius: 'var(--border-radius)' }}>
      <div className="flex items-center justify-between mb-2"><h3 className="font-smallcaps text-white text-base m-0">{title}</h3>{right}</div>
      {children}
    </section>
  );
}

export function CapeEditor(p: CapeEditorProps) {
  const accent = useThemeStore((s) => s.accentColor);
  const [spec, setSpec] = useState<CapeSpec>(() => sanitizeCapeSpec(p.initial ?? DEFAULT_CAPE));
  const [sel, setSel] = useState(0);
  const [prompt, setPrompt] = useState('');
  const [busy, setBusy] = useState(false);
  const [note, setNote] = useState('');
  const seed = useRef(1);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const viewerRef = useRef<SkinViewer | null>(null);
  const front = useMemo(() => capeFrontUrl(spec), [spec]);
  const specRef = useRef(spec);
  specRef.current = spec;
  const icons = useMemo(() => Object.fromEntries(CAPE_SYMBOL_IDS.map((id) => [id, capeFrontUrl({ ...DEFAULT_CAPE, bg: { type: 'solid', colors: ['#1a1a1f', '#1a1a1f'], angle: 0 }, symbols: [{ id, x: 0.5, y: 0.5, size: 0.95, rot: 0, color: '#ffffff', glow: false }], border: { width: 0, color: '#000000' }, text: null, shine: false })])), []);

  const update = (patch: Partial<CapeSpec>) => setSpec((s) => sanitizeCapeSpec({ ...s, ...patch }));
  const setSym = (i: number, patch: Partial<CapeSymbol>) => setSpec((s) => sanitizeCapeSpec({ ...s, symbols: s.symbols.map((x, k) => (k === i ? { ...x, ...patch } : x)) }));

  // 3D preview: the own skin seen from behind, slowly turning.
  useEffect(() => {
    const c = canvasRef.current;
    if (!c) return;
    let v: SkinViewer | null = null;
    try {
      v = new SkinViewer({ canvas: c, width: 300, height: 400, pixelRatio: Math.min(2, devicePixelRatio || 1), zoom: 0.85, fov: 40 });
      v.renderer.setClearColor(0x000000, 0);
      v.playerWrapper.rotation.y = Math.PI * 0.82;
      v.autoRotate = true;
      v.autoRotateSpeed = 0.6;
      v.controls.enableZoom = false;
      viewerRef.current = v;
      void v.loadSkin(p.skin ?? DEFAULT_SKIN, { model: p.skin && p.variant === 'slim' ? 'slim' : 'default' }).then(() => { if (viewerRef.current === v) void v!.loadCape(capeSheetUrl(specRef.current)); });
    } catch { /* no WebGL: the flat front stays */ }
    return () => { viewerRef.current = null; v?.dispose(); };
  }, [p.skin, p.variant]);
  useEffect(() => {
    const v = viewerRef.current;
    if (v) void v.loadCape(capeSheetUrl(spec));
  }, [spec]);

  const ai = async (newSeed = false) => {
    if (!prompt.trim()) return setNote('Beschreibe dein Cape, z. B. „lila Galaxie mit goldener Krone“.');
    if (newSeed) seed.current++;
    setBusy(true);
    setNote('');
    try {
      const r = await p.onAi(prompt.trim(), seed.current);
      setSpec(sanitizeCapeSpec(r.spec));
      setSel(0);
      setNote(r.source === 'server' ? `Von der KI erstellt${r.left != null ? ` · heute noch ${r.left}` : ''} – jetzt nach Lust anpassen.` : 'Mit dem eingebauten Generator erstellt (offline) – jetzt nach Lust anpassen.');
    } finally { setBusy(false); }
  };

  const s = spec.symbols[sel];
  const pickOnFront = (e: React.MouseEvent<HTMLImageElement>) => {
    if (!s) return;
    const r = e.currentTarget.getBoundingClientRect();
    setSym(sel, { x: (e.clientX - r.left) / r.width, y: (e.clientY - r.top) / r.height });
  };

  return (
    <div id="capeEditor" className="nr fixed inset-0 z-[900] flex items-center justify-center bg-black/70 backdrop-blur-sm p-6" onMouseDown={(e) => { if (e.target === e.currentTarget) p.onClose(); }}>
      <div className="relative w-full max-w-[1100px] max-h-full flex flex-col border-2 overflow-hidden" style={{ borderColor: `${accent.value}80`, background: 'color-mix(in srgb, var(--accent) 7%, #06070b)', borderRadius: 'var(--border-radius)', boxShadow: `0 0 30px ${accent.value}40` }}>
        <header className="flex items-center justify-between px-5 py-3 border-b-2" style={{ borderColor: `${accent.value}40` }}>
          <h2 className="font-minecraft text-2xl text-white m-0">Cape-Editor</h2>
          <button className="text-white/60 hover:text-white" aria-label="Schließen" onClick={p.onClose}><Icon icon="pixel:window-close-solid" className="w-5 h-5" /></button>
        </header>
        <div className="flex-1 min-h-0 flex gap-5 p-5 overflow-hidden">
          <div className="flex flex-col items-center gap-3 w-[320px] flex-shrink-0">
            <div className="relative w-[300px] h-[400px] border border-white/10 bg-black/30" style={{ borderRadius: 'var(--border-radius)' }}>
              <canvas ref={canvasRef} className="absolute inset-0 w-full h-full" />
            </div>
            <div className="flex items-start gap-3">
              <img id="capeFront" src={front} alt="Vorderseite" onClick={pickOnFront} className="w-[100px] h-[160px] border border-white/20 cursor-crosshair" style={{ imageRendering: 'pixelated' }} title="Klicken: gewähltes Symbol hierhin setzen" />
              <p className="text-xs text-white/60 font-minecraft m-0 max-w-[180px]">Klick auf die Vorderseite setzt das gewählte Symbol an diese Stelle.</p>
            </div>
          </div>

          <div className="flex-1 min-w-0 overflow-y-auto pr-1">
            <Section title="Mit KI erstellen">
              <div className="flex gap-2">
                <input id="capePrompt" value={prompt} onChange={(e) => setPrompt(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') void ai(); }} maxLength={300} placeholder="z. B. lila Galaxie mit goldener Krone und &quot;KING&quot;" className="flex-1 min-w-0 bg-black/40 border border-white/15 px-3 py-2 text-white" />
                <Button id="capeAi" size="sm" disabled={busy} onClick={() => void ai()}>{busy ? '…' : 'Erstellen'}</Button>
                <Button id="capeAgain" size="sm" variant="ghost" disabled={busy} onClick={() => void ai(true)}>Nochmal</Button>
              </div>
              {note && <p id="capeNote" className="text-xs text-white/70 mt-2 mb-0">{note}</p>}
              <div className="flex flex-wrap gap-1.5 mt-3">
                {Object.entries(CAPE_PRESETS).slice(0, 10).map(([id, pr]) => (
                  <button key={id} title={`Vorlage: ${pr.name}`} onClick={() => { setSpec(pr.spec); setSel(0); }} className="w-8 h-[52px] border border-white/10 hover:border-white/40 overflow-hidden"><img src={capeFrontUrl(pr.spec)} alt="" className="w-full h-full" style={{ imageRendering: 'pixelated' }} /></button>
                ))}
              </div>
            </Section>

            <Section title="Hintergrund">
              <div className="flex flex-wrap gap-1.5 mb-2">
                {CAPE_BG_TYPES.map((t) => (
                  <button key={t} data-bgtype={t} onClick={() => update({ bg: { ...spec.bg, type: t, colors: t === 'galaxy' && spec.bg.colors.length < 3 ? [...spec.bg.colors, '#be185d'] : spec.bg.colors } })}
                    className="px-2.5 py-1 text-sm font-minecraft border" style={{ borderColor: spec.bg.type === t ? accent.value : 'rgba(255,255,255,.12)', background: spec.bg.type === t ? `${accent.value}30` : 'transparent', color: '#fff' }}>{BG_NAMES[t]}</button>
                ))}
              </div>
              <Row label="Farben">
                {[0, 1, 2].map((i) => (
                  <input key={i} type="color" value={spec.bg.colors[i] ?? '#000000'} disabled={i === 2 && spec.bg.colors.length < 3 && false} onChange={(e) => { const c = [...spec.bg.colors]; c[i] = e.target.value; update({ bg: { ...spec.bg, colors: c } }); }} className="w-9 h-8" style={{ opacity: i < spec.bg.colors.length ? 1 : 0.4 }} />
                ))}
              </Row>
              <Row label="Richtung"><Slider value={spec.bg.angle} min={0} max={345} step={15} onChange={(v) => update({ bg: { ...spec.bg, angle: v } })} unit="°" /></Row>
            </Section>

            <Section title={`Symbole (${spec.symbols.length}/${CAPE_MAX_SYMBOLS})`}>
              <div className="grid grid-cols-[repeat(auto-fill,minmax(40px,1fr))] gap-1.5 mb-3">
                {CAPE_SYMBOL_IDS.map((id) => (
                  <button key={id} data-sym={id} title={`${CAPE_SYMBOLS[id]!.name} hinzufügen`} disabled={spec.symbols.length >= CAPE_MAX_SYMBOLS}
                    onClick={() => { const n = spec.symbols.length; update({ symbols: [...spec.symbols, { id, x: 0.5, y: n ? 0.72 : 0.42, size: n ? 0.3 : 0.55, rot: 0, color: '#ffffff', glow: true }] }); setSel(n); }}
                    className="h-10 border border-white/10 hover:border-white/40 overflow-hidden disabled:opacity-30"><img src={icons[id]} alt={CAPE_SYMBOLS[id]!.name} className="w-full h-full object-cover" style={{ imageRendering: 'pixelated', objectPosition: 'center 50%' }} /></button>
                ))}
              </div>
              {spec.symbols.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mb-2">
                  {spec.symbols.map((x, i) => (
                    <button key={i} data-symsel={i} onClick={() => setSel(i)} className="px-2 py-1 text-xs font-minecraft border text-white" style={{ borderColor: i === sel ? accent.value : 'rgba(255,255,255,.12)', background: i === sel ? `${accent.value}30` : 'transparent' }}>{CAPE_SYMBOLS[x.id]!.name}</button>
                  ))}
                </div>
              )}
              {s && (
                <div id="symEdit">
                  <Row label="Farbe"><input type="color" value={s.color} onChange={(e) => setSym(sel, { color: e.target.value })} className="w-9 h-8" /><label className="flex items-center gap-1 text-xs text-white/70"><input type="checkbox" checked={s.glow} onChange={(e) => setSym(sel, { glow: e.target.checked })} /> Leuchten</label></Row>
                  <Row label="Größe"><Slider value={s.size} min={0.1} max={1} step={0.01} onChange={(v) => setSym(sel, { size: v })} unit=" %" /></Row>
                  <Row label="Seitlich"><Slider value={s.x} min={0} max={1} step={0.01} onChange={(v) => setSym(sel, { x: v })} unit=" %" /></Row>
                  <Row label="Höhe"><Slider value={s.y} min={0} max={1} step={0.01} onChange={(v) => setSym(sel, { y: v })} unit=" %" /></Row>
                  <Row label="Drehung"><Slider value={s.rot} min={0} max={345} step={15} onChange={(v) => setSym(sel, { rot: v })} unit="°" /></Row>
                  <div className="flex justify-end"><Button size="sm" variant="destructive" onClick={() => { update({ symbols: spec.symbols.filter((_, k) => k !== sel) }); setSel(0); }}>Entfernen</Button></div>
                </div>
              )}
            </Section>

            <Section title="Text, Rand & Glanz">
              <Row label="Text">
                <input id="capeText" value={spec.text?.value ?? ''} maxLength={10} placeholder="max. 10 Zeichen" onChange={(e) => update({ text: e.target.value ? { value: e.target.value, color: spec.text?.color ?? '#ffffff', y: spec.text?.y ?? 0.84, size: spec.text?.size ?? 2 } : null })} className="w-40 bg-black/40 border border-white/15 px-2 py-1 text-white uppercase" />
                {spec.text && <input type="color" value={spec.text.color} onChange={(e) => update({ text: { ...spec.text!, color: e.target.value } })} className="w-9 h-8" />}
              </Row>
              {spec.text && <Row label="Textgröße"><Slider value={spec.text.size} min={1} max={3} onChange={(v) => update({ text: { ...spec.text!, size: v } })} /></Row>}
              {spec.text && <Row label="Texthöhe"><Slider value={spec.text.y} min={0} max={1} step={0.01} onChange={(v) => update({ text: { ...spec.text!, y: v } })} unit=" %" /></Row>}
              <Row label="Rand"><Slider value={spec.border.width} min={0} max={8} onChange={(v) => update({ border: { ...spec.border, width: v } })} unit=" px" /><input type="color" value={spec.border.color} onChange={(e) => update({ border: { ...spec.border, color: e.target.value } })} className="w-9 h-8" /></Row>
              <Row label="Glanz"><input type="checkbox" checked={spec.shine} onChange={(e) => update({ shine: e.target.checked })} /></Row>
            </Section>
          </div>
        </div>
        <footer className="flex items-center justify-between gap-3 px-5 py-3 border-t-2" style={{ borderColor: `${accent.value}40` }}>
          <span className="text-xs text-white/60 font-minecraft">{p.online ? 'Gespeichert auf dem LEGO-Server – andere LEGO-Spieler sehen dein Cape im Spiel.' : 'Offline: wird lokal gespeichert. Mit LEGO-Server verbinden, damit andere es sehen.'}</span>
          <div className="flex gap-2">
            <Button size="sm" variant="ghost" onClick={p.onClose}>Abbrechen</Button>
            <Button id="capeSave" size="sm" disabled={busy} onClick={() => { setBusy(true); void p.onSave(spec).finally(() => setBusy(false)); }}>Speichern & anlegen</Button>
          </div>
        </footer>
      </div>
    </div>
  );
}
