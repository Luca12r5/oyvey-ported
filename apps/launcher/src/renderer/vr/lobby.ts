// VR mode: a Halloween lobby you step into. Real WebXR (immersive-vr) when a
// headset/OpenXR runtime is available, otherwise the same world as a desktop
// preview (mouse look). Inside: a spatial click GUI (client modules, launcher
// and lobby settings), a real web browser (offscreen Chromium, SafeSearch)
// and a virtual keyboard. Panels can be pointed at, clicked, grabbed, moved
// and resized. The entry is a headset that flies onto your face.

import * as THREE from 'three';
import { NOCTURNE } from '@lego/shared';
import { api, errorToast, toast } from '../ui.ts';
import { retheme, state } from '../app.ts';
import { dotTexture, portal } from '../intro3d.ts';
import { batFlock, forestInstances, fogLayers, moon, pumpkinPatch, stoneMat, tombstone, wisps, type Ctx } from '../bg3d.ts';
import { Panel, palette, round, type Pal } from './panel.ts';
import type { Settings } from '../../core/settings.ts';
import type { VrbState } from '../../ipc-types.ts';

export async function xrSupported(): Promise<boolean> {
  try {
    const xr = (navigator as Navigator & { xr?: { isSessionSupported(m: string): Promise<boolean> } }).xr;
    return !!xr && (await xr.isSessionSupported('immersive-vr'));
  } catch {
    return false;
  }
}

export interface VrOptions { xr: boolean; sound: boolean; quality: number }

/** Something in the world that pointers can hit and grab. */
interface Spatial { root: THREE.Group; targets: THREE.Mesh[]; home: () => void }

const ease = (t: number) => (t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2);
const clamp01 = (x: number) => Math.max(0, Math.min(1, x));

function headsetModel(pal: Pal): { group: THREE.Group; lensMat: THREE.MeshBasicMaterial } {
  const g = new THREE.Group();
  const shell = new THREE.MeshStandardMaterial({ color: '#16101f', roughness: 0.35, metalness: 0.4 });
  const trim = new THREE.MeshStandardMaterial({ color: pal.accent, emissive: new THREE.Color(pal.accent), emissiveIntensity: 0.6, roughness: 0.4 });
  const body = new THREE.Mesh(new THREE.BoxGeometry(0.19, 0.1, 0.09), shell);
  const visor = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.105, 0.012), trim);
  visor.position.z = 0.046;
  const lensMat = new THREE.MeshBasicMaterial({ color: new THREE.Color(pal.accent2).multiplyScalar(1.5), toneMapped: false });
  for (const x of [-0.042, 0.042]) {
    const lens = new THREE.Mesh(new THREE.CylinderGeometry(0.027, 0.027, 0.01, 32), lensMat);
    lens.rotation.x = Math.PI / 2;
    lens.position.set(x, 0, -0.047);
    g.add(lens);
  }
  const strap = new THREE.Mesh(new THREE.TorusGeometry(0.11, 0.012, 10, 48, Math.PI), shell);
  strap.rotation.set(Math.PI / 2, 0, 0);
  strap.position.z = -0.05;
  strap.scale.set(1, 1.3, 1);
  g.add(body, visor, strap);
  return { group: g, lensMat };
}

export function enterVr(opts: VrOptions): Promise<void> {
  return new Promise((resolveExit) => {
    const pal = palette();
    const root = document.createElement('div');
    root.className = 'vr-root';
    const canvas = document.createElement('canvas');
    const hud = document.createElement('div');
    hud.className = 'vr-hud';
    const help = document.createElement('div');
    help.className = 'vr-help';
    help.textContent = opts.xr
      ? 'VR aktiv · Trigger: klicken · Griff: Panel greifen · Stick: näher/weiter, größer/kleiner'
      : 'Desktop-VR-Vorschau · Rechte Maustaste ziehen: umsehen · Linksklick: bedienen · Mausrad: scrollen/Größe · Shift+Ziehen: Panel bewegen · Esc: verlassen';
    const exitBtn = document.createElement('button');
    exitBtn.className = 'intro-btn';
    exitBtn.type = 'button';
    exitBtn.textContent = 'VR verlassen';
    hud.append(help, exitBtn);
    const flash = document.createElement('div');
    flash.className = 'intro-flash';
    root.append(canvas, flash, hud);
    document.body.append(root);

    const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, powerPreference: 'high-performance' });
    renderer.setPixelRatio(Math.min(devicePixelRatio || 1, opts.quality >= 0.9 ? 2 : 1.5));
    renderer.setSize(innerWidth, innerHeight, false);
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.05;
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.xr.enabled = opts.xr;
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(NOCTURNE.void);
    const fog = new THREE.FogExp2(NOCTURNE.void, 0.9);
    scene.fog = fog;
    const camera = new THREE.PerspectiveCamera(70, innerWidth / Math.max(1, innerHeight), 0.03, 300);
    camera.position.set(0, 1.6, 0);
    const rig = new THREE.Group();
    rig.add(camera);
    scene.add(rig);
    const dot = dotTexture();

    // ---- the lobby ----------------------------------------------------------
    const c: Ctx = { scene, camera, density: Math.max(0.3, state.settings.fx.particles), q: opts.quality, pal: { bg: pal.bg, accent: pal.accent, accent2: pal.accent2 }, dot };
    const floor = new THREE.Mesh(new THREE.CircleGeometry(60, 64), new THREE.MeshStandardMaterial({ color: '#1a1426', roughness: 1 }));
    floor.rotation.x = -Math.PI / 2;
    const plat = new THREE.Mesh(new THREE.CylinderGeometry(3.2, 3.4, 0.12, 64), new THREE.MeshStandardMaterial({ color: '#1e1729', roughness: 0.9 }));
    plat.position.y = -0.06;
    const rune = new THREE.Mesh(new THREE.TorusGeometry(2.9, 0.025, 8, 160), new THREE.MeshBasicMaterial({ color: new THREE.Color(pal.accent).multiplyScalar(1.8), toneMapped: false }));
    rune.rotation.x = -Math.PI / 2;
    rune.position.y = 0.005;
    scene.add(floor, plat, rune);
    const sm = stoneMat();
    for (let i = 0; i < 16; i++) {
      const a = (i / 16) * Math.PI * 2 + 0.2;
      const r = 7 + (i % 3) * 1.6;
      const t = tombstone(sm);
      t.position.set(Math.cos(a) * r, 0, Math.sin(a) * r);
      t.lookAt(0, 0, 0);
      scene.add(t);
    }
    const trees: THREE.Vector3[] = [];
    const hs: number[] = [];
    for (let i = 0; i < 22; i++) { const a = (i / 22) * Math.PI * 2; const r = 16 + (i % 4) * 3; trees.push(new THREE.Vector3(Math.cos(a) * r, 0, Math.sin(a) * r)); hs.push(9 + (i % 5) * 2); }
    forestInstances(c, trees, hs);
    moon(c, new THREE.Vector3(-20, 26, -60), 6);
    const patch = pumpkinPatch(c, Array.from({ length: 8 }, (_, i) => { const a = (i / 8) * Math.PI * 2 + Math.PI / 8; return new THREE.Vector3(Math.cos(a) * 4.2, 0.35, Math.sin(a) * 4.2); }), 3);
    const moonLight = new THREE.DirectionalLight('#a796ff', 1.1);
    moonLight.position.set(-20, 26, -30);
    const hemi = new THREE.HemisphereLight('#5b4890', '#120a18', 0.9);
    // Warm lantern above the platform so the lobby reads well in the headset.
    const lantern = new THREE.PointLight(NOCTURNE.ember, 7, 12, 2);
    lantern.position.set(0, 3.2, 0);
    scene.add(moonLight, hemi, lantern);
    const p = portal(2.2);
    p.group.position.set(0, 2.4, 6.5);
    p.group.lookAt(0, 2.4, 0);
    scene.add(p.group);
    const fogU = fogLayers(c, 0.15, '#b9a6ff');
    const w = wisps(c, 40 + 80 * c.density, { x: 12, y: [0.4, 3.5], z: [-12, 12] }, [NOCTURNE.slime, pal.accent, pal.accent2], 0.06);
    const bats = batFlock(c, 7, new THREE.Vector3(0, 9, 0), 10);

    // ---- spatial sound --------------------------------------------------------
    const listener = new THREE.AudioListener();
    camera.add(listener);
    let soundOn = opts.sound;
    listener.setMasterVolume(soundOn ? 0.8 : 0);
    const hum = new THREE.PositionalAudio(listener);
    const actx = listener.context;
    const oscs: OscillatorNode[] = [];
    try {
      const mix = actx.createGain();
      mix.gain.value = 0.25;
      for (const f of [55, 82.5, 110.6]) { const o = actx.createOscillator(); o.type = f > 100 ? 'triangle' : 'sine'; o.frequency.value = f; o.connect(mix); o.start(); oscs.push(o); }
      hum.setNodeSource(oscs[0]!);
      hum.disconnect();
      mix.connect(hum.panner);
      hum.setRefDistance(1.5);
      hum.setRolloffFactor(1.4);
      p.group.add(hum);
    } catch { /* audio unavailable */ }

    // ---- headset entry animation ---------------------------------------------
    const headset = headsetModel(pal);
    camera.add(headset.group);
    const flashSphere = new THREE.Mesh(new THREE.SphereGeometry(0.2, 16, 12), new THREE.MeshBasicMaterial({ color: new THREE.Color('#f1e9ff').multiplyScalar(2), transparent: true, opacity: 0, side: THREE.BackSide, depthTest: false, toneMapped: false }));
    flashSphere.renderOrder = 999;
    camera.add(flashSphere);
    const keyLight = new THREE.PointLight(pal.accent, 2, 2, 2);
    camera.add(keyLight);

    // ---- panels ------------------------------------------------------------------
    const spatials: Spatial[] = [];
    const place = (g: THREE.Object3D, yawOffset: number, dist: number, y: number, tilt = 0) => {
      const yaw = camYaw() + yawOffset;
      g.position.set(Math.sin(yaw) * -dist, y, Math.cos(yaw) * -dist).add(new THREE.Vector3(camera.position.x, 0, camera.position.z));
      g.lookAt(new THREE.Vector3(camera.position.x, y + tilt, camera.position.z));
    };
    const camYaw = () => { const d = new THREE.Vector3(); camera.getWorldDirection(d); return Math.atan2(-d.x, -d.z); };

    // Click GUI (menu)
    let tab: 'modules' | 'launcher' | 'lobby' = 'modules';
    let modules: { id: string; on: boolean }[] = [];
    let modAvailable = false;
    let modPage = 0;
    const profile = state.settings.profiles.find((x) => x.id === state.settings.selectedProfile && x.legoClient) ?? state.settings.profiles.find((x) => x.legoClient) ?? null;
    const loadModules = async () => {
      if (!profile) { modAvailable = false; return; }
      try { const r = await api.clientModules(profile.id); modAvailable = r.available; modules = r.modules; } catch { modAvailable = false; }
      menu.redraw();
    };
    const setSetting = async (patch: Partial<Settings>) => {
      try { state.settings = await api.setSettings(patch); retheme(); } catch (e) { errorToast(e); }
      menu.redraw();
    };
    const menu = new Panel(900, 1120, 1.0, (pn) => {
      pn.bg('LEGO VR');
      pn.text('Click GUI', 860, 64, { size: 22, color: pal.muted, align: 'right' });
      const tabs: [typeof tab, string][] = [['modules', 'Module'], ['launcher', 'Launcher'], ['lobby', 'Lobby']];
      tabs.forEach(([id, l], i) => pn.button(`tab-${id}`, 40 + i * 280, 110, 260, 70, l, () => { tab = id; pn.redraw(); }, { active: tab === id }));
      const x = 40, wdt = 820;
      let y = 210;
      if (tab === 'modules') {
        if (!profile) pn.text('Kein Profil mit LEGO Client.', x, y + 30, { color: pal.muted });
        else if (!modAvailable || !modules.length) {
          pn.text(`Profil: ${profile.name}`, x, y + 20, { size: 24, color: pal.muted });
          pn.text('Starte das LEGO-Client-Profil einmal –', x, y + 80, { size: 26 });
          pn.text('danach erscheinen hier deine Module.', x, y + 120, { size: 26 });
        } else {
          pn.text(`${profile.name} · gilt ab dem nächsten Spielstart`, x, y + 16, { size: 22, color: pal.muted, max: wdt });
          y += 44;
          const per = 8, pages = Math.ceil(modules.length / per);
          modPage = Math.min(modPage, pages - 1);
          modules.slice(modPage * per, modPage * per + per).forEach((m, i) => pn.toggle(`m-${m.id}`, x, y + i * 84, wdt, m.id.replace(/[_-]/g, ' ').replace(/\b\w/g, (ch) => ch.toUpperCase()), m.on, async () => {
            try { modules = await api.setClientModule(profile.id, m.id, !m.on); } catch (e) { errorToast(e); }
            pn.redraw();
          }));
          pn.button('mprev', x, 900, 120, 64, '◀', () => { modPage = Math.max(0, modPage - 1); pn.redraw(); });
          pn.text(`${modPage + 1} / ${pages}`, x + wdt / 2, 932, { align: 'center', color: pal.muted });
          pn.button('mnext', x + wdt - 120, 900, 120, 64, '▶', () => { modPage = Math.min(pages - 1, modPage + 1); pn.redraw(); });
        }
      } else if (tab === 'launcher') {
        const s = state.settings;
        pn.toggle('glow', x, y, wdt, 'Glühen (Bloom)', s.fx.glow, () => void setSetting({ fx: { ...s.fx, glow: !s.fx.glow } }), 'Leuchtende Lichter im Launcher');
        pn.toggle('fog', x, y + 86, wdt, 'Nebel', s.fx.fog, () => void setSetting({ fx: { ...s.fx, fog: !s.fx.fog } }), 'Bodennebel in den 3D-Hintergründen');
        pn.toggle('parallax', x, y + 172, wdt, 'Parallax', s.fx.parallax, () => void setSetting({ fx: { ...s.fx, parallax: !s.fx.parallax } }), 'Kamera folgt der Maus');
        pn.toggle('intro', x, y + 258, wdt, 'Startanimation', s.introEnabled, () => void setSetting({ introEnabled: !s.introEnabled }));
        pn.toggle('snow', x, y + 344, wdt, 'Schnee', s.snow, () => void setSetting({ snow: !s.snow }));
        pn.text(`Partikel: ${Math.round(s.fx.particles * 100)} %`, x, y + 470, { size: 26 });
        pn.button('pminus', x + 520, y + 436, 140, 68, '−', () => void setSetting({ fx: { ...s.fx, particles: Math.max(0, +(s.fx.particles - 0.1).toFixed(2)) } }));
        pn.button('pplus', x + 680, y + 436, 140, 68, '+', () => void setSetting({ fx: { ...s.fx, particles: Math.min(1, +(s.fx.particles + 0.1).toFixed(2)) } }));
      } else {
        pn.toggle('sound', x, y, wdt, 'Raumklang', soundOn, () => { soundOn = !soundOn; listener.setMasterVolume(soundOn ? 0.8 : 0); pn.redraw(); }, 'Summen des Portals aus seiner Richtung');
        pn.toggle('browser', x, y + 86, wdt, 'Browser', browser.root.visible, () => { browser.root.visible = !browser.root.visible; pn.redraw(); });
        pn.toggle('keyboard', x, y + 172, wdt, 'Tastatur', keyboard.root.visible, () => { keyboard.root.visible = !keyboard.root.visible; pn.redraw(); });
        pn.button('arrange', x, y + 290, wdt, 76, 'Panels neu anordnen', () => spatials.forEach((s) => s.home()));
      }
      pn.button('reset', 40, 1000, 400, 80, 'Ansicht zurücksetzen', () => resetView());
      pn.button('exit', 460, 1000, 400, 80, 'VR verlassen', () => exit(), { primary: true });
    }, pal);
    const menuS: Spatial = { root: menu.group, targets: [menu.mesh], home: () => place(menu.group, 0.62, 1.7, 1.45) };
    spatials.push(menuS);
    scene.add(menu.group);

    // Browser: address bar panel + live page texture.
    let bstate: VrbState = { url: '', title: '', canBack: false, canForward: false, loading: true };
    let address = '';
    let focus: 'page' | 'address' | null = null;
    const bw = 1.7;
    const bar = new Panel(1280, 120, bw, (pn) => {
      const cx = pn.ctx;
      round(cx, 0, 0, 1280, 120, 30);
      cx.fillStyle = 'rgba(14,10,22,.96)';
      cx.fill();
      pn.button('back', 16, 22, 86, 76, '◀', () => void nav('back'), { size: 30 });
      pn.button('fwd', 110, 22, 86, 76, '▶', () => void nav('forward'), { size: 30 });
      pn.button('reload', 204, 22, 86, 76, bstate.loading ? '…' : '⟳', () => void nav('reload'), { size: 30 });
      pn.button('home', 298, 22, 86, 76, '⌂', () => void nav('home'), { size: 30 });
      const ax = 400, aw = 860;
      round(cx, ax, 22, aw, 76, 38);
      cx.fillStyle = focus === 'address' ? 'rgba(255,255,255,.14)' : 'rgba(255,255,255,.07)';
      cx.fill();
      if (focus === 'address') { cx.lineWidth = 3; cx.strokeStyle = pal.accent; cx.stroke(); }
      const shown = focus === 'address' ? `${address}|` : (bstate.title ? `${bstate.title} — ${bstate.url.replace(/^https:\/\/(www\.)?/, '')}` : 'Suchen oder Adresse eingeben');
      pn.text(shown, ax + 30, 60, { size: 28, color: focus === 'address' || bstate.title ? pal.text : pal.muted, max: aw - 60 });
      pn.regions.push({ id: 'addr', x: ax, y: 22, w: aw, h: 76, onClick: () => { focus = 'address'; address = ''; keyboard.root.visible = true; pn.redraw(); kb.redraw(); } });
    }, pal);
    const pageTex = new THREE.Texture();
    pageTex.colorSpace = THREE.SRGBColorSpace;
    const pageH = (bw * 800) / 1280;
    const page = new THREE.Mesh(new THREE.PlaneGeometry(bw, pageH), new THREE.MeshBasicMaterial({ map: pageTex, color: '#1a1424', toneMapped: false }));
    const browserRoot = new THREE.Group();
    bar.group.position.y = pageH / 2 + (bw * 120) / 1280 / 2 + 0.02;
    browserRoot.add(bar.group, page);
    const browserFrame = new THREE.Mesh(new THREE.PlaneGeometry(bw + 0.05, pageH + 0.05), new THREE.MeshBasicMaterial({ color: new THREE.Color(pal.accent).multiplyScalar(0.8), transparent: true, opacity: 0.4, toneMapped: false }));
    browserFrame.position.z = -0.006;
    browserRoot.add(browserFrame);
    const browser: Spatial = { root: browserRoot, targets: [bar.mesh, page], home: () => place(browserRoot, -0.38, 2.1, 1.75) };
    spatials.push(browser);
    scene.add(browserRoot);
    const nav = async (action: 'back' | 'forward' | 'reload' | 'home' | 'go', text?: string) => {
      try { bstate = await api.vrBrowserNav(action, text); } catch (e) { errorToast(e); }
      bar.redraw();
    };

    // Virtual keyboard (types into the address bar or the page).
    let shift = false;
    const ROWS = ['1234567890ß', 'qwertzuiopü', 'asdfghjklöä', 'yxcvbnm,.-'];
    const typeKey = (k: string) => {
      if (focus === 'address') {
        if (k === 'Backspace') address = address.slice(0, -1);
        else if (k === 'Enter') { void nav('go', address); focus = 'page'; }
        else if (k.length === 1) address += k;
        bar.redraw();
      } else {
        if (k.length === 1) { api.vrBrowserInput({ type: 'char', keyCode: k }); }
        else { api.vrBrowserInput({ type: 'keyDown', keyCode: k }); api.vrBrowserInput({ type: 'keyUp', keyCode: k }); }
      }
    };
    const kb = new Panel(1100, 470, 1.2, (pn) => {
      pn.bg();
      ROWS.forEach((row, ri) => [...row].forEach((ch, ci) => {
        const label = shift ? ch.toUpperCase() : ch;
        pn.button(`k-${ri}-${ci}`, 24 + ri * 30 + ci * 94, 24 + ri * 88, 86, 78, label, () => { typeKey(label); if (shift) { shift = false; pn.redraw(); } });
      }));
      pn.button('shift', 24, 376, 150, 76, '⇧', () => { shift = !shift; pn.redraw(); }, { active: shift });
      pn.button('space', 186, 376, 470, 76, 'Leertaste', () => typeKey(' '));
      pn.button('@', 668, 376, 80, 76, '@', () => typeKey('@'));
      pn.button('bs', 760, 376, 150, 76, '⌫', () => typeKey('Backspace'));
      pn.button('enter', 922, 376, 154, 76, '⏎', () => typeKey('Enter'), { primary: true });
    }, pal);
    const keyboard: Spatial = { root: kb.group, targets: [kb.mesh], home: () => place(kb.group, -0.3, 1.25, 0.95, 1.2) };
    keyboard.root.visible = false;
    spatials.push(keyboard);
    scene.add(kb.group);

    // Live page frames from the offscreen browser.
    let lastBitmap: ImageBitmap | null = null;
    const offFrame = api.on('vrb-frame', (buf: Uint8Array) => {
      void createImageBitmap(new Blob([buf as BlobPart], { type: 'image/jpeg' })).then((bmp) => {
        lastBitmap?.close();
        lastBitmap = bmp;
        pageTex.image = bmp;
        pageTex.needsUpdate = true;
        (page.material as THREE.MeshBasicMaterial).color.set('#ffffff');
      }).catch(() => {});
    });
    const offState = api.on('vrb-state', (s: VrbState) => { bstate = s; bar.redraw(); });
    void api.vrBrowserOpen().then((r) => { bstate = r.state; bar.redraw(); }).catch((e) => errorToast(e));
    void loadModules();

    const resetView = () => {
      if (!opts.xr) { yaw = 0; pitch = 0; }
      spatials.forEach((s) => s.home());
    };

    // ---- pointers: desktop mouse and XR controllers ------------------------------
    const ray = new THREE.Raycaster();
    const targets = () => spatials.filter((s) => s.root.visible).flatMap((s) => s.targets);
    const ownerOf = (m: THREE.Object3D) => spatials.find((s) => s.targets.includes(m as THREE.Mesh))!;
    let lastMove = 0;
    /** Routes a pointer event to whatever the ray hits. */
    const route = (kind: 'move' | 'down' | 'up' | 'wheel', hit: THREE.Intersection | undefined, deltaY = 0) => {
      const obj = hit?.object as THREE.Mesh | undefined;
      const pnl = obj?.userData.panel as Panel | undefined;
      for (const s of [menu, bar, kb]) if (s !== pnl) s.pointer('move', null);
      if (!hit || !hit.uv) return false;
      if (obj === page) {
        const x = hit.uv.x * 1280, y = (1 - hit.uv.y) * 800;
        if (kind === 'move') { const now = performance.now(); if (now - lastMove > 33) { lastMove = now; api.vrBrowserInput({ type: 'mouseMove', x, y }); } }
        else if (kind === 'wheel') api.vrBrowserInput({ type: 'mouseWheel', x, y, deltaY });
        else {
          api.vrBrowserInput({ type: kind === 'down' ? 'mouseDown' : 'mouseUp', x, y });
          if (kind === 'down' && focus !== 'page') { focus = 'page'; bar.redraw(); }
        }
        return true;
      }
      if (pnl) {
        if (kind === 'wheel') { const s = ownerOf(obj!); s.root.scale.multiplyScalar(deltaY > 0 ? 0.93 : 1.07); s.root.scale.clampScalar(0.5, 2.5); }
        else pnl.pointer(kind, hit.uv);
        return true;
      }
      return false;
    };

    // Desktop: mouse look with the right button, panel grabbing with Shift / middle button.
    let yaw = 0, pitch = 0;
    const mouse = new THREE.Vector2();
    let looking = false, lx = 0, ly = 0;
    let grab: { s: Spatial; dist: number } | null = null;
    const castFromMouse = (e: MouseEvent) => {
      mouse.set((e.clientX / innerWidth) * 2 - 1, -(e.clientY / innerHeight) * 2 + 1);
      ray.setFromCamera(mouse, camera);
      return ray.intersectObjects(targets(), false)[0];
    };
    const onDown = (e: MouseEvent) => {
      if (opts.xr || !ready) return;
      const hit = castFromMouse(e);
      if (e.button === 2) { looking = true; lx = e.clientX; ly = e.clientY; return; }
      if (hit && (e.shiftKey || e.button === 1)) { grab = { s: ownerOf(hit.object), dist: hit.distance }; return; }
      if (e.button === 0) { if (!route('down', hit)) { looking = true; lx = e.clientX; ly = e.clientY; } }
    };
    const onMove = (e: MouseEvent) => {
      if (opts.xr || !ready) return;
      if (looking) {
        yaw -= (e.clientX - lx) * 0.004;
        pitch = Math.max(-1.2, Math.min(1.2, pitch - (e.clientY - ly) * 0.004));
        lx = e.clientX; ly = e.clientY;
        return;
      }
      const hit = castFromMouse(e);
      if (grab) {
        grab.s.root.position.copy(ray.ray.origin).addScaledVector(ray.ray.direction, grab.dist);
        grab.s.root.lookAt(camera.position.x, grab.s.root.position.y, camera.position.z);
        return;
      }
      route('move', hit);
      canvas.style.cursor = hit ? 'pointer' : 'grab';
    };
    const onUp = (e: MouseEvent) => {
      if (opts.xr) return;
      if (grab) { grab = null; return; }
      if (looking) { looking = false; if (e.button === 2) return; }
      route('up', castFromMouse(e));
    };
    const onWheel = (e: WheelEvent) => { if (!opts.xr && ready) { e.preventDefault(); route('wheel', castFromMouse(e), e.deltaY); } };
    const SPECIAL: Record<string, string> = { Backspace: 'Backspace', Enter: 'Enter', Tab: 'Tab', ArrowLeft: 'Left', ArrowRight: 'Right', ArrowUp: 'Up', ArrowDown: 'Down', Delete: 'Delete', Home: 'Home', End: 'End', PageUp: 'PageUp', PageDown: 'PageDown' };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') { e.preventDefault(); if (focus) { focus = null; bar.redraw(); } else exit(); return; }
      if (!focus) return;
      e.preventDefault();
      e.stopPropagation();
      if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) typeKey(e.key);
      else if (SPECIAL[e.key]) typeKey(SPECIAL[e.key]!);
    };
    canvas.addEventListener('mousedown', onDown);
    addEventListener('mousemove', onMove);
    addEventListener('mouseup', onUp);
    canvas.addEventListener('wheel', onWheel, { passive: false });
    canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    addEventListener('keydown', onKey, true);
    exitBtn.addEventListener('click', () => exit());

    // XR controllers: laser pointers, trigger = click, grip = grab, stick = distance/size.
    const controllers: { c: THREE.Group; line: THREE.Line; grab: { s: Spatial; offset: THREE.Matrix4 } | null; hover: THREE.Intersection | undefined }[] = [];
    if (opts.xr) {
      for (let i = 0; i < 2; i++) {
        const ctl = renderer.xr.getController(i);
        const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(), new THREE.Vector3(0, 0, -1)]), new THREE.LineBasicMaterial({ color: new THREE.Color(pal.accent).multiplyScalar(2), toneMapped: false }));
        line.scale.z = 4;
        ctl.add(line);
        rig.add(ctl);
        const entry = { c: ctl, line, grab: null as { s: Spatial; offset: THREE.Matrix4 } | null, hover: undefined as THREE.Intersection | undefined };
        ctl.addEventListener('selectstart', () => route('down', entry.hover));
        ctl.addEventListener('selectend', () => route('up', entry.hover));
        ctl.addEventListener('squeezestart', () => {
          if (!entry.hover) return;
          const s = ownerOf(entry.hover.object);
          entry.grab = { s, offset: new THREE.Matrix4().copy(ctl.matrixWorld).invert().multiply(s.root.matrixWorld) };
        });
        ctl.addEventListener('squeezeend', () => { entry.grab = null; });
        controllers.push(entry);
      }
    }

    // ---- frame loop ---------------------------------------------------------------
    let t = 0;
    let last = performance.now();
    let ready = false;
    let leaving = -1;
    let leftAt = 0;
    const leftFor = () => (performance.now() - leftAt) / 1000;
    const ENTRY = 4.2;
    const tmpM = new THREE.Matrix4();
    const tmpQ = new THREE.Quaternion();
    const tmpS = new THREE.Vector3();
    spatials.forEach((s) => s.root.scale.setScalar(0.001));
    const frame = (now: number) => {
      const dt = Math.min(0.1, (now - last) / 1000);
      last = now;
      t += dt;
      if (!opts.xr) {
        camera.rotation.set(pitch, yaw, 0, 'YXZ');
        camera.updateMatrixWorld();
      }
      // Entry: headset flies onto the face, lens flash, fog lifts, panels open.
      const k = clamp01(t / 2.6);
      headset.group.visible = t < 3.2;
      headset.group.position.set(Math.sin(t * 1.3) * 0.15 * (1 - k), -0.05 * (1 - k), -1.6 + ease(k) * 1.58);
      headset.group.rotation.set(0.3 * (1 - k), Math.PI + Math.sin(t * 0.9) * 0.6 * (1 - k), 0.2 * (1 - k));
      headset.lensMat.color.set(pal.accent2).multiplyScalar(1.5 + ease(k) * 4);
      keyLight.intensity = 2 + 6 * k;
      const fl = clamp01((t - 2.4) / 0.5) * (1 - clamp01((t - 3.0) / 0.9));
      (flashSphere.material as THREE.MeshBasicMaterial).opacity = fl;
      flash.style.opacity = String(fl * 0.4);
      fog.density = leaving >= 0 ? 0.04 + clamp01(leftFor() / 0.9) * 0.9 : 0.9 - clamp01((t - 2.8) / 1.6) * 0.86;
      if (!ready && t > 2.9) { ready = true; spatials.forEach((s) => s.home()); }
      spatials.forEach((s, i) => { if (leaving < 0 && s.root.visible) { const a = ease(clamp01((t - 3.1 - i * 0.18) / 0.7)); if (a < 1) s.root.scale.setScalar(Math.max(0.001, a)); } });
      // World motion.
      p.update(t, 1);
      p.group.rotation.z = t * 0.05;
      rune.rotation.z = t * 0.05;
      fogU(t); w(t); bats(t); patch.update(t);
      // XR controllers.
      for (const e of controllers) {
        ray.ray.origin.setFromMatrixPosition(e.c.matrixWorld);
        ray.ray.direction.set(0, 0, -1).applyQuaternion(tmpQ.setFromRotationMatrix(e.c.matrixWorld));
        if (e.grab) {
          tmpM.multiplyMatrices(e.c.matrixWorld, e.grab.offset);
          tmpM.decompose(e.grab.s.root.position, e.grab.s.root.quaternion, tmpS);
          const gp = (renderer.xr.getSession()?.inputSources[controllers.indexOf(e)]?.gamepad);
          const ax = gp?.axes ?? [];
          if (Math.abs(ax[3] ?? 0) > 0.2) e.grab.offset.premultiply(new THREE.Matrix4().makeTranslation(0, 0, (ax[3] ?? 0) * dt * 0.8));
          if (Math.abs(ax[2] ?? 0) > 0.2) { const sc = e.grab.s.root.scale.x * (1 + (ax[2] ?? 0) * dt); e.grab.s.root.scale.setScalar(Math.max(0.5, Math.min(2.5, sc))); }
          continue;
        }
        e.hover = ray.intersectObjects(targets(), false)[0];
        e.line.scale.z = e.hover ? e.hover.distance : 4;
        if (ready) route('move', e.hover);
      }
      renderer.render(scene, camera);
      root.dataset.t = t.toFixed(1);
      if (leaving >= 0 && leftFor() > 1.0) finish();
    };

    const resize = () => { renderer.setSize(innerWidth, innerHeight, false); camera.aspect = innerWidth / Math.max(1, innerHeight); camera.updateProjectionMatrix(); };
    addEventListener('resize', resize);
    renderer.setAnimationLoop(frame);

    if (opts.xr) {
      const xr = (navigator as Navigator & { xr?: { requestSession(m: string, o: object): Promise<XRSession> } }).xr!;
      xr.requestSession('immersive-vr', { optionalFeatures: ['local-floor', 'bounded-floor', 'hand-tracking'] }).then(async (session) => {
        renderer.xr.setReferenceSpaceType('local-floor');
        await renderer.xr.setSession(session);
        session.addEventListener('end', () => exit());
      }).catch((e: unknown) => {
        toast(`VR-Brille konnte nicht gestartet werden (${e instanceof Error ? e.message : String(e)}). Es läuft die Desktop-Vorschau.`, 'error');
        opts.xr = false;
        renderer.xr.enabled = false;
        help.textContent = 'Desktop-VR-Vorschau · Rechte Maustaste ziehen: umsehen · Linksklick: bedienen · Mausrad: scrollen/Größe · Shift+Ziehen: Panel bewegen · Esc: verlassen';
      });
    }

    let done = false;
    function exit(): void {
      if (leaving >= 0) return;
      leaving = t;
      leftAt = performance.now();
      // Wall-clock safety net: closes even if frames stall (slow GPU, hidden window).
      setTimeout(finish, 1300);
      headset.group.visible = false;
      root.classList.add('out');
    }
    function finish(): void {
      if (done) return;
      done = true;
      renderer.setAnimationLoop(null);
      void renderer.xr.getSession()?.end().catch(() => {});
      offFrame();
      offState();
      api.vrBrowserClose();
      lastBitmap?.close();
      oscs.forEach((o) => { try { o.stop(); } catch { /* already stopped */ } });
      void actx.close().catch(() => {});
      removeEventListener('mousemove', onMove);
      removeEventListener('mouseup', onUp);
      removeEventListener('keydown', onKey, true);
      removeEventListener('resize', resize);
      [menu, bar, kb].forEach((x) => x.dispose());
      scene.traverse((o) => {
        const m = o as THREE.Mesh;
        m.geometry?.dispose();
        const mats = Array.isArray(m.material) ? m.material : m.material ? [m.material] : [];
        mats.forEach((x) => { (x as THREE.MeshBasicMaterial).map?.dispose(); x.dispose(); });
      });
      dot.dispose();
      renderer.dispose();
      renderer.forceContextLoss();
      root.remove();
      resolveExit();
    }
  });
}

/** Little helper used by the VR page to describe the setup. */
export function vrRequirements(): string[] {
  return [
    'Eine PC-VR-Brille (z. B. Meta Quest per Link/Air Link, Valve Index, HTC Vive, Windows Mixed Reality).',
    'Eine aktive OpenXR-Laufzeit (Meta Quest Link, SteamVR oder Windows Mixed Reality) als Standard eingestellt.',
    'Ohne Brille startet automatisch die Desktop-VR-Vorschau mit derselben Welt.',
  ];
}
