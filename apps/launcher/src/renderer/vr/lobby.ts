// VR mode. Two ways in:
//  • real VR (WebXR immersive-vr) with a headset/OpenXR runtime: controllers
//    or tracked hands, stick locomotion, snap turn, teleport;
//  • visual VR on the desktop: walked with WASD + mouse (pointer lock), Space
//    to jump, E for the menu; two dark hands with pointer rays in view.
// The lobby is a sunny Minecraft-style valley (vr/world.ts) with a floating
// browser window, a dock (time, fps, apps) and the click GUI. Panels and props
// (LEGO bricks) can be grabbed, moved, pulled closer, pushed away, scaled and
// thrown. Panel layout is remembered. In XR your arms use your own skin.

import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js';
import { XRHandModelFactory } from 'three/examples/jsm/webxr/XRHandModelFactory.js';
import { api, errorToast, toast } from '../ui.ts';
import { retheme, state } from '../app.ts';
import { brickGeometry } from '../introcave.ts';
import { DEFAULT_SKIN, headFromSkin } from '../avatar.ts';
import { Panel, palette, panelSounds, round, type Pal } from './panel.ts';
import { buildWorld } from './world.ts';
import { aimRay, makeHands } from './hands.ts';
import { makeSfx } from './sfx.ts';
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

/** Something pointers can hit and grab. */
interface Spatial { name: string; root: THREE.Group; targets: THREE.Mesh[]; home: () => void }
interface Prop { mesh: THREE.Object3D; hit: THREE.Mesh; vel: THREE.Vector3; spin: THREE.Vector3; held: boolean; radius: number; home: THREE.Vector3 }

const ease = (t: number) => (t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2);
const clamp01 = (x: number) => Math.max(0, Math.min(1, x));
const BW = 1600, BH = 1000;
const LAYOUT_KEY = 'lego.vr.layout';

// ---- the player's arms from their Minecraft skin ------------------------------------

function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((res, rej) => { const i = new Image(); i.onload = () => res(i); i.onerror = rej; i.src = src; });
}

/** Box materials (+x, -x, +y, -y, +z, -z) cut from a 64×64 skin. */
function armMaterials(img: HTMLImageElement, right: boolean, slim: boolean): THREE.MeshStandardMaterial[] {
  const w = slim ? 3 : 4;
  const [bx, by] = right ? [40, 16] : [32, 48];
  const legacy = img.height === 32 && !right;
  const ox = legacy ? 40 : bx, oy = legacy ? 16 : by;
  const faces: [number, number, number, number][] = [
    [ox + w + 4, oy + 4, 4, 12], // +x (outer side)
    [ox, oy + 4, 4, 12], // -x (inner side)
    [ox + 4, oy, w, 4], // top
    [ox + 4 + w, oy, w, 4], // bottom (hand)
    [ox + 4, oy + 4, w, 12], // front
    [ox + 8 + w, oy + 4, w, 12], // back
  ];
  return faces.map(([x, y, fw, fh]) => {
    const c = document.createElement('canvas');
    c.width = fw * 8;
    c.height = fh * 8;
    const g = c.getContext('2d')!;
    g.imageSmoothingEnabled = false;
    g.drawImage(img, x, y, fw, fh, 0, 0, c.width, c.height);
    const t = new THREE.CanvasTexture(c);
    t.colorSpace = THREE.SRGBColorSpace;
    t.magFilter = THREE.NearestFilter;
    t.minFilter = THREE.NearestFilter;
    return new THREE.MeshStandardMaterial({ map: t, roughness: 0.8 });
  });
}

/** A Minecraft-style arm (12 px long) whose pivot is the shoulder; the hand points along -Z. */
function makeArm(mats: THREE.Material[], slim: boolean): THREE.Group {
  const px = 0.06;
  const geo = new THREE.BoxGeometry((slim ? 3 : 4) * px, 4 * px, 12 * px);
  // Box faces: rotate so the long axis is Z (front face looks up, hand at -Z).
  const mesh = new THREE.Mesh(geo, [mats[0]!, mats[1]!, mats[4]!, mats[5]!, mats[2]!, mats[3]!]);
  mesh.position.z = -6 * px;
  mesh.castShadow = true;
  const g = new THREE.Group();
  g.add(mesh);
  return g;
}

function headsetModel(): THREE.Group {
  const g = new THREE.Group();
  const shell = new THREE.MeshPhysicalMaterial({ color: '#1b1820', roughness: 0.32, metalness: 0.1, clearcoat: 0.8, clearcoatRoughness: 0.25 });
  const foam = new THREE.MeshStandardMaterial({ color: '#2a2630', roughness: 1 });
  const strapMat = new THREE.MeshStandardMaterial({ color: '#141216', roughness: 0.9 });
  const body = new THREE.Mesh(new RoundedBoxGeometry(0.19, 0.105, 0.085, 4, 0.03), shell);
  const pad = new THREE.Mesh(new RoundedBoxGeometry(0.175, 0.09, 0.03, 3, 0.012), foam);
  pad.position.z = -0.05;
  const lensMat = new THREE.MeshPhysicalMaterial({ color: '#101820', roughness: 0.05, metalness: 0.3, clearcoat: 1, transmission: 0.2 });
  for (const x of [-0.033, 0.033]) {
    const lens = new THREE.Mesh(new THREE.CylinderGeometry(0.022, 0.022, 0.004, 32), lensMat);
    lens.rotation.x = Math.PI / 2;
    lens.position.set(x, 0, -0.066);
    g.add(lens);
  }
  const strap = new THREE.Mesh(new THREE.TorusGeometry(0.105, 0.008, 8, 48, Math.PI), strapMat);
  strap.rotation.set(Math.PI / 2, 0, 0);
  strap.position.z = -0.06;
  strap.scale.set(1, 1.45, 1);
  const logo = new THREE.Mesh(new THREE.CircleGeometry(0.01, 20), new THREE.MeshBasicMaterial({ color: '#ff7a1a' }));
  logo.position.set(0.07, 0.03, 0.0431);
  g.add(body, pad, strap, logo);
  g.traverse((o) => { (o as THREE.Mesh).castShadow = true; });
  return g;
}

/** Dark rounded window body with a soft drop shadow (behind the browser). */
function windowFrame(w: number, h: number, r: number): THREE.Group {
  const shape = new THREE.Shape();
  shape.moveTo(-w / 2 + r, -h / 2);
  shape.lineTo(w / 2 - r, -h / 2); shape.quadraticCurveTo(w / 2, -h / 2, w / 2, -h / 2 + r);
  shape.lineTo(w / 2, h / 2 - r); shape.quadraticCurveTo(w / 2, h / 2, w / 2 - r, h / 2);
  shape.lineTo(-w / 2 + r, h / 2); shape.quadraticCurveTo(-w / 2, h / 2, -w / 2, h / 2 - r);
  shape.lineTo(-w / 2, -h / 2 + r); shape.quadraticCurveTo(-w / 2, -h / 2, -w / 2 + r, -h / 2);
  const g = new THREE.Group();
  const body = new THREE.Mesh(new THREE.ShapeGeometry(shape, 8), new THREE.MeshBasicMaterial({ color: '#1f2026', toneMapped: false }));
  const sc = document.createElement('canvas');
  sc.width = 256; sc.height = Math.round((256 * h) / w);
  const sg = sc.getContext('2d')!;
  sg.filter = 'blur(10px)';
  sg.fillStyle = 'rgba(0,0,0,.5)';
  sg.beginPath(); sg.roundRect(16, 20, sc.width - 32, sc.height - 32, 18); sg.fill();
  const shadow = new THREE.Mesh(new THREE.PlaneGeometry(w * 1.12, h * 1.12), new THREE.MeshBasicMaterial({ map: new THREE.CanvasTexture(sc), transparent: true, depthWrite: false, toneMapped: false }));
  shadow.position.set(0, -0.03, -0.012);
  g.add(shadow, body);
  return g;
}

function drawSearch(c: CanvasRenderingContext2D, x: number, y: number): void {
  c.save();
  c.strokeStyle = 'rgba(255,255,255,.6)';
  c.lineWidth = 3.5;
  c.beginPath(); c.arc(x - 3, y - 3, 11, 0, Math.PI * 2); c.stroke();
  c.beginPath(); c.moveTo(x + 5, y + 5); c.lineTo(x + 14, y + 14); c.stroke();
  c.restore();
}

const FAVORITES: { name: string; url: string }[] = [
  { name: 'Google', url: 'https://www.google.com/' },
  { name: 'Minecraft Wiki', url: 'https://minecraft.wiki/' },
  { name: 'Modrinth', url: 'https://modrinth.com/' },
  { name: 'Wikipedia', url: 'https://de.wikipedia.org/' },
  { name: 'LEGO', url: 'https://www.lego.com/' },
];

// ---- sound -------------------------------------------------------------------------

function breath(ctx: AudioContext, out: AudioNode, at: number): void {
  const len = ctx.sampleRate;
  const b = ctx.createBuffer(1, len, ctx.sampleRate);
  const d = b.getChannelData(0);
  for (let i = 0; i < len; i++) d[i] = Math.random() * 2 - 1;
  for (const [t, dur, f] of [[at, 1.1, 700], [at + 1.4, 1.3, 500]] as const) {
    const s = ctx.createBufferSource();
    s.buffer = b;
    s.loop = true;
    const bp = ctx.createBiquadFilter();
    bp.type = 'bandpass';
    bp.frequency.value = f;
    bp.Q.value = 0.7;
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, ctx.currentTime + t);
    g.gain.exponentialRampToValueAtTime(0.18, ctx.currentTime + t + dur * 0.4);
    g.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + t + dur);
    s.connect(bp).connect(g).connect(out);
    s.start(ctx.currentTime + t);
    s.stop(ctx.currentTime + t + dur + 0.05);
  }
}

// ---- the lobby ----------------------------------------------------------------------

export function enterVr(opts: VrOptions): Promise<void> {
  return new Promise((resolveExit) => {
    const pal = palette();
    let visual = !opts.xr;
    const root = document.createElement('div');
    root.className = `vr-root ${visual ? 'visual' : ''}`;
    const canvas = document.createElement('canvas');
    const hud = document.createElement('div');
    hud.className = 'vr-hud';
    const help = document.createElement('div');
    help.className = 'vr-help';
    help.textContent = 'VR aktiv · Trigger: klicken · Griff: greifen · rechter Stick: näher/weiter oder drehen · linker Stick: laufen · A: teleportieren';
    help.hidden = visual;
    // Key hints like on a headset's desktop view (light up while pressed).
    const keysEl = document.createElement('div');
    keysEl.className = 'vr-keys';
    keysEl.innerHTML = `<div class="r"><span></span><kbd data-k="KeyW">W</kbd><span></span></div><div class="r"><kbd data-k="KeyA">A</kbd><kbd data-k="KeyS">S</kbd><kbd data-k="KeyD">D</kbd></div><div class="r m"><kbd data-k="m0">LMB</kbd><kbd data-k="m2">RMB</kbd></div><div class="hint">E Menü · Shift rennen · Esc verlassen</div>`;
    keysEl.hidden = !visual;
    const keyLight = (k: string, on: boolean) => keysEl.querySelector(`[data-k="${k}"]`)?.classList.toggle('on', on);
    const exitBtn = document.createElement('button');
    exitBtn.className = 'intro-btn';
    exitBtn.type = 'button';
    exitBtn.textContent = 'VR verlassen';
    hud.append(help, exitBtn);
    hud.prepend(keysEl);
    const lens = document.createElement('div');
    lens.className = 'vr-lens';
    const lensFog = document.createElement('div');
    lensFog.className = 'vr-fog';
    const cross = document.createElement('div');
    cross.className = 'vr-cross';
    root.append(canvas, lensFog, lens, cross, hud);
    document.body.append(root);

    const q = opts.quality;
    const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, powerPreference: 'high-performance' });
    renderer.setPixelRatio(Math.min(devicePixelRatio || 1, q >= 0.9 ? 2 : 1.5));
    renderer.setSize(innerWidth, innerHeight, false);
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 0.9;
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.shadowMap.enabled = q > 0.45 && !state.info.e2e; // software GL in CI is too slow for shadows
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    renderer.xr.enabled = opts.xr;
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(visual ? 74 : 70, innerWidth / Math.max(1, innerHeight), 0.03, 300);
    camera.position.set(0, 1.62, 0);
    const rig = new THREE.Group();
    rig.add(camera);
    scene.add(rig);
    renderer.toneMappingExposure = 1.0;
    const world = buildWorld(scene, renderer.shadowMap.enabled);

    // ---- sound
    const listener = new THREE.AudioListener();
    camera.add(listener);
    let soundOn = opts.sound;
    listener.setMasterVolume(soundOn ? 0.8 : 0);
    const actx = listener.context;
    const sfx = makeSfx(actx, listener.getInput());
    panelSounds.hover = sfx.hover;
    panelSounds.click = sfx.click;
    const stopAmbience = sfx.ambience();
    try { breath(actx, listener.getInput(), 0.6); } catch { /* audio unavailable */ }

    // ---- arms (own skin), headset, entry
    const arms = new THREE.Group();
    camera.add(arms);
    let armR: THREE.Group | null = null, armL: THREE.Group | null = null;
    const loadArms = async (skin: string | null, slim: boolean) => {
      try {
        const img = await loadImage(skin ?? DEFAULT_SKIN);
        arms.clear();
        if (!visual) {
          armR = makeArm(armMaterials(img, true, slim && !!skin), slim && !!skin);
          armL = makeArm(armMaterials(img, false, slim && !!skin), slim && !!skin);
          arms.add(armR, armL);
        }
        // XR: a forearm on each controller (hidden while hands are tracked).
        for (const [i, e] of ctls.entries()) {
          if (e.arm) e.grip.remove(e.arm);
          e.arm = makeArm(armMaterials(img, i === 1, slim && !!skin), slim && !!skin);
          e.arm.children[0]!.position.set(0, -0.02, 0.32);
          e.grip.add(e.arm);
        }
      } catch { /* keep no arms */ }
    };
    void loadArms(state.skin.skin, state.skin.variant === 'slim');
    const headset = headsetModel();
    camera.add(headset);
    const hands = makeHands(pal.accent);
    if (visual) { camera.add(hands.root); scene.add(hands.rayR, hands.rayL, hands.dot); }
    let headImg: HTMLImageElement | null = null;
    void headFromSkin(state.skin.skin, 64).then(loadImage).then((i) => { headImg = i; dock.redraw(); }).catch(() => {});

    // ---- panels
    const spatials: Spatial[] = [];
    const camYaw = () => { const d = new THREE.Vector3(); camera.getWorldDirection(d); return Math.atan2(-d.x, -d.z); };
    const headPos = () => { const v = new THREE.Vector3(); camera.getWorldPosition(v); return v; };
    const place = (g: THREE.Object3D, yawOffset: number, dist: number, y: number, tilt = 0) => {
      const yaw = camYaw() + yawOffset;
      const h = headPos();
      const gy = rig.position.y + y;
      g.position.set(h.x + Math.sin(yaw) * -dist, gy, h.z + Math.cos(yaw) * -dist);
      g.lookAt(new THREE.Vector3(h.x, gy + tilt, h.z));
    };

    let tab: 'modules' | 'launcher' | 'lobby' | 'skin' = 'modules';
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
    const menu = new Panel(900, 1120, 0.72, (pn) => {
      pn.bg('LEGO VR');
      const tabs: [typeof tab, string][] = [['modules', 'Module'], ['launcher', 'Launcher'], ['lobby', 'Lobby'], ['skin', 'Skin']];
      tabs.forEach(([id, l], i) => pn.button(`tab-${id}`, 40 + i * 207, 110, 195, 70, l, () => { tab = id; pn.redraw(); }, { active: tab === id }));
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
        pn.toggle('intro', x, y + 172, wdt, 'Startanimation', s.introEnabled, () => void setSetting({ introEnabled: !s.introEnabled }));
      } else if (tab === 'lobby') {
        pn.toggle('sound', x, y, wdt, 'Klang', soundOn, () => setSound(!soundOn), 'Wind, Vögel und Klick-Geräusche');
        pn.toggle('browser', x, y + 86, wdt, 'Browser', browser.root.visible, () => show(browser, !browser.root.visible));
        pn.toggle('keyboard', x, y + 172, wdt, 'Tastatur', keyboard.root.visible, () => show(keyboard, !keyboard.root.visible));
        pn.toggle('arms', x, y + 258, wdt, visual ? 'Hände zeigen' : 'Arme zeigen', visual ? hands.root.visible : arms.visible, () => { if (visual) hands.root.visible = !hands.root.visible; else arms.visible = !arms.visible; pn.redraw(); });
        pn.button('arrange', x, y + 370, wdt, 76, 'Panels neu anordnen', () => { try { localStorage.removeItem(LAYOUT_KEY); } catch { /* storage blocked */ } spatials.forEach((s) => s.home()); });
        pn.button('props', x, y + 460, wdt, 76, 'Steine zurücklegen', () => props.forEach((p) => { p.mesh.position.copy(p.home); p.vel.set(0, 0, 0); p.spin.set(0, 0, 0); }));
      } else {
        pn.text('Dein Körper in VR', x, y + 20, { size: 26 });
        const lib = state.settings.skinLibrary ?? [];
        const opts2: { id: string; label: string; skin: string | null; slim: boolean }[] = [
          { id: 'own', label: state.auth.signedIn ? `Mein Skin (${state.auth.name})` : state.settings.guestName ? `Skin von ${state.settings.guestName}` : 'Mein Skin', skin: state.skin.skin, slim: state.skin.variant === 'slim' },
          { id: 'lego', label: 'LEGO-Figur', skin: null, slim: false },
          ...lib.slice(0, 6).map((s) => ({ id: s.id, label: s.name, skin: s.data, slim: s.variant === 'slim' })),
        ];
        opts2.forEach((o, i) => pn.button(`sk-${o.id}`, x, y + 70 + i * 86, wdt, 74, o.label, () => void loadArms(o.skin, o.slim)));
      }
      pn.button('reset', 40, 1000, 400, 80, 'Ansicht zurücksetzen', () => resetView());
      pn.button('exit', 460, 1000, 400, 80, 'VR verlassen', () => exit(), { primary: true });
    }, pal);
    const menuS: Spatial = { name: 'menu', root: menu.group, targets: [menu.mesh], home: () => place(menu.group, 0.72, 1.6, 1.45) };
    spatials.push(menuS);
    scene.add(menu.group);

    // Browser: tab strip + bar (flat), curved page below.
    let bstate: VrbState = { url: '', title: '', canBack: false, canForward: false, loading: true };
    let address = '';
    let sugg: { title: string; url: string }[] = [];
    let focus: 'page' | 'address' | null = null;
    const bw = 1.75;
    const nav = async (action: Parameters<typeof api.vrBrowserNav>[0], text?: string) => {
      try { bstate = await api.vrBrowserNav(action, text); } catch (e) { errorToast(e); }
      bar.redraw();
    };
    let big = false;
    // Title bar like a floating headset window: tabs, then back/forward/reload,
    // the address pill, sound, home, and minimise/fullscreen/close.
    const bar = new Panel(1600, 196, bw, (pn) => {
      const cx = pn.ctx;
      cx.beginPath();
      cx.roundRect(0, 0, 1600, 196, [34, 34, 0, 0]);
      cx.fillStyle = '#1f2026';
      cx.fill();
      const tabs = bstate.tabs?.length ? bstate.tabs : [{ title: bstate.title, url: bstate.url }];
      tabs.forEach((tb, i) => {
        const tx = 24 + i * 300, act = i === (bstate.active ?? 0);
        round(cx, tx, 14, 290, 54, 16);
        cx.fillStyle = act ? 'rgba(255,255,255,.13)' : 'rgba(255,255,255,.04)';
        cx.fill();
        pn.text(tb.title || 'Neuer Tab', tx + 20, 41, { size: 22, color: act ? pal.text : pal.muted, max: tabs.length > 1 ? 220 : 250 });
        pn.regions.push({ id: `tab-${i}`, x: tx, y: 14, w: tabs.length > 1 ? 240 : 290, h: 54, onClick: () => void nav('tab', String(i)) });
        if (tabs.length > 1 && act) pn.iconBtn(`tclose-${i}`, tx + 244, 20, 42, 'close', () => void nav('closeTab'));
      });
      if (tabs.length < 4) pn.iconBtn('newtab', 24 + tabs.length * 300, 16, 50, 'expand', () => void nav('newTab'));
      pn.text(`${Math.round((bstate.zoom ?? 1) * 100)}%`, 1540, 41, { size: 20, color: pal.muted, align: 'center' });
      const y0 = 86, sz = 84;
      pn.iconBtn('back', 24, y0, sz, 'back', () => void nav('back'), { color: bstate.canBack ? pal.text : pal.muted });
      pn.iconBtn('fwd', 116, y0, sz, 'forward', () => void nav('forward'), { color: bstate.canForward ? pal.text : pal.muted });
      pn.iconBtn('reload', 208, y0, sz, 'reload', () => void nav('reload'));
      const ax = 312, aw = 820;
      round(cx, ax, y0 + 4, aw, 76, 38);
      cx.fillStyle = focus === 'address' ? 'rgba(255,255,255,.16)' : 'rgba(255,255,255,.08)';
      cx.fill();
      if (focus === 'address') { cx.lineWidth = 3; cx.strokeStyle = pal.accent; cx.stroke(); }
      drawSearch(cx, ax + 40, y0 + 42);
      const shown = focus === 'address' ? `${address}|` : (bstate.url ? bstate.url.replace(/^https:\/\/(www\.)?/, '') : 'Mit Google suchen oder Adresse eingeben');
      pn.text(shown, ax + 76, y0 + 42, { size: 28, color: focus === 'address' || bstate.url ? pal.text : pal.muted, max: aw - 110 });
      pn.regions.push({ id: 'addr', x: ax, y: y0, w: aw, h: 84, onClick: () => { focus = 'address'; address = ''; sugg = []; show(keyboard, true); pn.redraw(); kb.redraw(); sp.redraw(); } });
      pn.iconBtn('vol', 1150, y0, sz, soundOn ? 'volume' : 'mute', () => setSound(!soundOn));
      pn.iconBtn('home', 1242, y0, sz, 'home', () => void nav('home'));
      cx.fillStyle = 'rgba(255,255,255,.12)';
      cx.fillRect(1340, y0 + 18, 2, 48);
      pn.iconBtn('min', 1316 + 40, y0, sz, 'minimize', () => show(browser, false));
      pn.iconBtn('big', 1408 + 40, y0, sz, 'expand', () => { big = !big; browser.root.userData.pop = { t0: performance.now(), from: browser.root.scale.x, to: big ? 1.45 : 1 }; sfx.open(); }, { active: big });
      pn.iconBtn('close', 1500, y0, sz, 'close', () => show(browser, false));
      if (bstate.loading) { cx.fillStyle = pal.accent; cx.fillRect(0, 190, 1600 * (0.25 + ((performance.now() / 900) % 1) * 0.75), 6); }
    }, pal);
    bar.group.children[0]!.visible = false; // the window frame below casts the shadow
    // Suggestions dropdown (from this session's history only).
    const sp = new Panel(1600, 330, bw, (pn) => {
      if (focus !== 'address' || !sugg.length) { pn.ctx.clearRect(0, 0, 1600, 330); return; }
      round(pn.ctx, 0, 0, 1600, 66 * sugg.length + 12, 18);
      pn.ctx.fillStyle = 'rgba(20,18,26,.98)';
      pn.ctx.fill();
      sugg.forEach((s, i) => pn.button(`sg-${i}`, 8, 6 + i * 66, 1584, 60, `${s.title} — ${s.url.replace(/^https:\/\/(www\.)?/, '')}`, () => { focus = 'page'; sugg = []; void nav('go', s.url); sp.redraw(); }, { size: 24 }));
    }, pal);
    // Flat page in a rounded window frame.
    const pageH = (bw * BH) / BW;
    const pageGeo = new THREE.PlaneGeometry(bw, pageH);
    const pageTex = new THREE.Texture();
    pageTex.colorSpace = THREE.SRGBColorSpace;
    pageTex.anisotropy = 8;
    const page = new THREE.Mesh(pageGeo, new THREE.MeshBasicMaterial({ map: pageTex, color: '#1a1820', toneMapped: false, side: THREE.DoubleSide }));
    const browserRoot = new THREE.Group();
    const barH = (bw * 196) / BW;
    bar.group.position.y = pageH / 2 + barH / 2;
    sp.group.position.set(0, pageH / 2 - (bw * 330) / BW / 2 - 0.01, 0.02);
    sp.group.children[0]!.visible = false; // no frame behind the dropdown
    const winFrame = windowFrame(bw + 0.03, pageH + barH + 0.03, 0.05);
    winFrame.position.set(0, barH / 2, -0.004);
    browserRoot.add(winFrame, bar.group, page, sp.group);
    const browser: Spatial = { name: 'browser', root: browserRoot, targets: [bar.mesh, page, sp.mesh], home: () => place(browserRoot, -0.12, 2.15, 1.75) };
    spatials.push(browser);
    scene.add(browserRoot);
    let suggTimer = 0;
    const refreshSugg = () => { clearTimeout(suggTimer); suggTimer = window.setTimeout(async () => { sugg = await api.vrBrowserSuggest(address).catch(() => []); sp.redraw(); }, 120); };

    // Virtual keyboard.
    let shift = false;
    const ROWS = ['1234567890ß', 'qwertzuiopü', 'asdfghjklöä', 'yxcvbnm,.-'];
    const keyClick = () => {
      try {
        const o = actx.createOscillator(), g = actx.createGain();
        o.type = 'square'; o.frequency.value = 1800;
        g.gain.setValueAtTime(0.03, actx.currentTime); g.gain.exponentialRampToValueAtTime(0.0001, actx.currentTime + 0.03);
        o.connect(g).connect(listener.getInput()); o.start(); o.stop(actx.currentTime + 0.04);
      } catch { /* no audio */ }
    };
    const typeKey = (k: string) => {
      keyClick();
      if (focus === 'address') {
        if (k === 'Backspace') address = address.slice(0, -1);
        else if (k === 'Enter') { void nav('go', address); focus = 'page'; sugg = []; }
        else if (k.length === 1) address += k;
        bar.redraw();
        refreshSugg();
      } else {
        if (k.length === 1) api.vrBrowserInput({ type: 'char', keyCode: k });
        else { api.vrBrowserInput({ type: 'keyDown', keyCode: k }); api.vrBrowserInput({ type: 'keyUp', keyCode: k }); }
      }
    };
    const kb = new Panel(1100, 470, 0.85, (pn) => {
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
    const keyboard: Spatial = { name: 'keyboard', root: kb.group, targets: [kb.mesh], home: () => place(kb.group, -0.12, 1.05, 1.0, 1.2) };
    keyboard.root.visible = false;
    spatials.push(keyboard);
    scene.add(kb.group);

    // Opening/closing windows: a short pop with a swoosh.
    const show = (sp2: Spatial, on: boolean) => {
      if (sp2.root.visible === on && !sp2.root.userData.closing) return;
      const base = sp2 === browser && big ? 1.45 : 1;
      if (on) {
        if (!sp2.root.visible) sp2.home();
        sp2.root.visible = true;
        delete sp2.root.userData.closing;
        sp2.root.userData.pop = { t0: performance.now(), from: 0.6 * base, to: base };
        sfx.open();
      } else {
        sp2.root.userData.pop = { t0: performance.now(), from: sp2.root.scale.x, to: 0.001 };
        sp2.root.userData.closing = true;
        sfx.close();
      }
      if (sp2 === keyboard && !on && focus === 'address') { focus = null; bar.redraw(); }
      menu.redraw(); dock.redraw();
    };
    const setSound = (on: boolean) => { soundOn = on; listener.setMasterVolume(on ? 0.8 : 0); menu.redraw(); bar.redraw(); };

    // Favourites: quick links for the browser.
    const fav = new Panel(620, 560, 0.5, (pn) => {
      pn.bg('Favoriten');
      FAVORITES.forEach((f, i) => pn.button(`fav-${i}`, 36, 126 + i * 84, 548, 70, f.name, () => { show(browser, true); void nav('go', f.url); show(favS, false); }));
    }, pal);
    const favS: Spatial = { name: 'fav', root: fav.group, targets: [fav.mesh], home: () => place(fav.group, 0.62, 1.5, 1.2) };
    fav.group.visible = false;
    spatials.push(favS);
    scene.add(fav.group);

    // Dock: time, connection, fps · ms, your head, apps.
    let fps = 60, ms = 16, fpsFrames = 0, fpsAt = performance.now();
    const DW = 1500, DH = 150;
    const dock = new Panel(DW, DH, 1.0, (pn) => {
      const c = pn.ctx;
      round(c, 0, 0, DW, DH - 18, 66);
      c.fillStyle = 'rgba(30,31,38,.96)';
      c.fill();
      c.lineWidth = 2;
      c.strokeStyle = 'rgba(255,255,255,.1)';
      c.stroke();
      const mid = (DH - 18) / 2;
      const now = new Date();
      pn.text(`${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`, 52, mid, { size: 36, weight: 700 });
      pn.iconBtn('wifi', 160, mid - 28, 56, 'wifi', () => toast(navigator.onLine ? 'Online' : 'Offline'), { color: navigator.onLine ? pal.text : pal.muted });
      pn.text(`${fps} fps · ${ms} ms`, 236, mid, { size: 24, color: pal.muted });
      c.fillStyle = 'rgba(255,255,255,.12)';
      c.fillRect(430, 34, 2, mid * 2 - 68);
      // own head
      const hx = 470, hs = 76;
      c.save();
      round(c, hx, mid - hs / 2, hs, hs, 18);
      c.clip();
      if (headImg) { c.imageSmoothingEnabled = false; c.drawImage(headImg, hx, mid - hs / 2, hs, hs); } else { c.fillStyle = 'rgba(255,255,255,.1)'; c.fillRect(hx, mid - hs / 2, hs, hs); }
      c.restore();
      if (pn.hover === 'me') { round(c, hx - 4, mid - hs / 2 - 4, hs + 8, hs + 8, 22); c.lineWidth = 4; c.strokeStyle = pal.accent; c.stroke(); }
      pn.regions.push({ id: 'me', x: hx, y: mid - hs / 2, w: hs, h: hs, onClick: () => { tab = 'skin'; show(menuS, true); menu.redraw(); } });
      const apps: [string, Parameters<typeof pn.iconBtn>[4], () => void, boolean][] = [
        ['apps', 'grid', () => { const was = tab === 'modules' && menu.group.visible && !menu.group.userData.closing; tab = 'modules'; show(menuS, !was); menu.redraw(); }, menu.group.visible && !menu.group.userData.closing],
        ['web', 'compass', () => show(browser, !browser.root.visible || !!browser.root.userData.closing), browser.root.visible && !browser.root.userData.closing],
        ['keys', 'keyboard', () => show(keyboard, !keyboard.root.visible || !!keyboard.root.userData.closing), keyboard.root.visible && !keyboard.root.userData.closing],
        ['favs', 'star', () => show(favS, !fav.group.visible || !!fav.group.userData.closing), fav.group.visible && !fav.group.userData.closing],
      ];
      apps.forEach(([id, ic, fn, open], i) => pn.iconBtn(`d-${id}`, 590 + i * 104, mid - 42, 84, ic, fn, { dot: open }));
      c.fillStyle = 'rgba(255,255,255,.12)';
      c.fillRect(1018, 34, 2, mid * 2 - 68);
      pn.iconBtn('d-recenter', 1050, mid - 42, 84, 'recenter', () => resetView());
      pn.iconBtn('d-settings', 1154, mid - 42, 84, 'gear', () => { tab = 'lobby'; show(menuS, true); menu.redraw(); });
      pn.iconBtn('d-exit', 1300, mid - 42, 84, 'power', () => exit(), { color: '#ff8a8a' });
    }, pal);
    dock.group.children[0]!.visible = false;
    const dockS: Spatial = { name: 'dock', root: dock.group, targets: [dock.mesh], home: () => place(dock.group, 0, 1.1, 1.13, 0.5) };
    spatials.push(dockS);
    scene.add(dock.group);

    // Live frames of the offscreen browser.
    let lastBitmap: ImageBitmap | null = null;
    const offFrame = api.on('vrb-frame', (buf: Uint8Array) => {
      void createImageBitmap(new Blob([buf as BlobPart], { type: 'image/jpeg' }), { imageOrientation: 'flipY' }).then((bmp) => {
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

    // ---- grabbable props with simple physics
    const props: Prop[] = [];
    const bGeo = brickGeometry(3);
    [['#c91a1a', 1.2, -1.0], ['#f2b632', 1.6, -0.5], ['#1e5aa8', -1.4, -0.8], ['#237841', -1.9, -0.2]].forEach(([color, x, z]) => {
      const m = new THREE.Mesh(bGeo, new THREE.MeshPhysicalMaterial({ color: color as string, roughness: 0.28, clearcoat: 0.6, clearcoatRoughness: 0.3 }));
      m.scale.setScalar(0.32);
      m.position.set(x as number, 0.12, z as number);
      m.castShadow = m.receiveShadow = true;
      scene.add(m);
      props.push({ mesh: m, hit: m, vel: new THREE.Vector3(), spin: new THREE.Vector3(), held: false, radius: 0.12, home: m.position.clone() });
    });

    // Remembered panel layout (positions relative to the play area).
    const saveLayout = () => {
      try {
        const data: Record<string, { p: number[]; q: number[]; s: number; v: boolean }> = {};
        for (const s of spatials) data[s.name] = { p: s.root.position.toArray(), q: s.root.quaternion.toArray() as number[], s: s.root.scale.x, v: s.root.visible };
        localStorage.setItem(LAYOUT_KEY, JSON.stringify(data));
      } catch { /* storage blocked */ }
    };
    const loadLayout = (): boolean => {
      try {
        const data = JSON.parse(localStorage.getItem(LAYOUT_KEY) ?? 'null') as Record<string, { p: number[]; q: number[]; s: number; v: boolean }> | null;
        if (!data) return false;
        for (const s of spatials) {
          const d = data[s.name];
          if (!d) { s.home(); continue; }
          s.root.position.fromArray(d.p);
          s.root.quaternion.fromArray(d.q);
          s.root.userData.scale = Math.max(0.5, Math.min(2.5, d.s));
        }
        return true;
      } catch { return false; }
    };

    const resetView = () => {
      yaw = 0; pitch = 0;
      rig.position.set(0, 0, 0);
      try { localStorage.removeItem(LAYOUT_KEY); } catch { /* storage blocked */ }
      spatials.forEach((s) => { s.home(); s.root.scale.setScalar(1); });
    };

    // ---- pointer routing
    const ray = new THREE.Raycaster();
    const targets = () => [...spatials.filter((s) => s.root.visible).flatMap((s) => s.targets), ...props.map((p) => p.hit)];
    const ownerOf = (m: THREE.Object3D) => spatials.find((s) => s.targets.includes(m as THREE.Mesh));
    const propOf = (m: THREE.Object3D) => props.find((p) => p.hit === m);
    let lastMove = 0;
    let scrollVel = 0;
    let scrollAt: { x: number; y: number } | null = null;
    const route = (kind: 'move' | 'down' | 'up' | 'wheel', hit: THREE.Intersection | undefined, deltaY = 0, ctrl = false) => {
      const obj = hit?.object as THREE.Mesh | undefined;
      const pnl = obj?.userData.panel as Panel | undefined;
      for (const s of [menu, bar, kb, sp, dock, fav]) if (s !== pnl) s.pointer('move', null);
      if (!hit || !hit.uv) {
        if (kind === 'down' && obj && propOf(obj)) { const p = propOf(obj)!; p.vel.addScaledVector(ray.ray.direction, 3).add(new THREE.Vector3(0, 1.5, 0)); sfx.click(); }
        return false;
      }
      if (obj === page) {
        const x = hit.uv.x * BW, y = (1 - hit.uv.y) * BH;
        if (kind === 'move') { const now = performance.now(); if (now - lastMove > 33) { lastMove = now; api.vrBrowserInput({ type: 'mouseMove', x, y }); } }
        else if (kind === 'wheel') {
          if (ctrl) void nav(deltaY < 0 ? 'zoomIn' : 'zoomOut');
          else { scrollVel += deltaY * 6; scrollAt = { x, y }; }
        } else {
          api.vrBrowserInput({ type: kind === 'down' ? 'mouseDown' : 'mouseUp', x, y });
          if (kind === 'down' && focus !== 'page') { focus = 'page'; sugg = []; bar.redraw(); sp.redraw(); }
        }
        return true;
      }
      if (pnl) {
        if (kind === 'wheel') { const s = ownerOf(obj!); if (s) { s.root.scale.multiplyScalar(deltaY > 0 ? 0.93 : 1.07); s.root.scale.clampScalar(0.5, 2.5); } }
        else pnl.pointer(kind, hit.uv);
        return true;
      }
      if (kind === 'down' && obj && propOf(obj)) { const p = propOf(obj)!; p.vel.addScaledVector(ray.ray.direction, 3).add(new THREE.Vector3(0, 1.5, 0)); sfx.click(); return true; }
      return false;
    };

    // ---- desktop (visual VR): pointer lock, WASD, jump, grab with the right button
    let yaw = 0, pitch = 0;
    const mouse = new THREE.Vector2();
    let looking = false, lx = 0, ly = 0;
    const keys = new Set<string>();
    let vy = 0, jumpY = 0, walkPhase = 0;
    type Grab = { s?: Spatial; p?: Prop; dist: number; lastPos: THREE.Vector3; vel: THREE.Vector3 };
    let grab: Grab | null = null;
    let swing = 0;
    let pointAmt = 0;
    let dockFollow = false;
    let aim: THREE.Intersection | undefined;
    let pokeAt = 0;
    const locked = () => document.pointerLockElement === canvas;
    const cast = (e?: MouseEvent) => {
      if (locked() || !e) mouse.set(0, 0);
      else mouse.set((e.clientX / innerWidth) * 2 - 1, -(e.clientY / innerHeight) * 2 + 1);
      ray.setFromCamera(mouse, camera);
      return ray.intersectObjects(targets(), false)[0];
    };
    const startGrab = (hit: THREE.Intersection) => {
      const p = propOf(hit.object);
      const s = p ? undefined : ownerOf(hit.object);
      if (!p && !s) return false;
      grab = { s, p, dist: hit.distance, lastPos: hit.point.clone(), vel: new THREE.Vector3() };
      if (p) p.held = true;
      return true;
    };
    const endGrab = () => {
      if (!grab) return;
      if (grab.p) { grab.p.held = false; grab.p.vel.copy(grab.vel).clampLength(0, 9); }
      else saveLayout();
      grab = null;
    };
    const onDown = (e: MouseEvent) => {
      keyLight(`m${e.button}`, true);
      if (e.button === 0) pokeAt = performance.now();
      if (opts.xr || !ready) return;
      if (visual && !locked() && e.button === 0 && !cast(e)) { void canvas.requestPointerLock(); return; }
      const hit = cast(e);
      if (e.button === 2) {
        if (hit && startGrab(hit)) return;
        if (!locked()) { looking = true; lx = e.clientX; ly = e.clientY; }
        return;
      }
      if (e.button === 0) { swing = 1; if (!route('down', hit) && !locked()) { looking = true; lx = e.clientX; ly = e.clientY; } }
    };
    const onMove = (e: MouseEvent) => {
      if (opts.xr || !ready) return;
      if (locked()) {
        yaw -= e.movementX * 0.0022;
        pitch = Math.max(-1.35, Math.min(1.35, pitch - e.movementY * 0.0022));
      } else if (looking) {
        yaw -= (e.clientX - lx) * 0.004;
        pitch = Math.max(-1.3, Math.min(1.3, pitch - (e.clientY - ly) * 0.004));
        lx = e.clientX; ly = e.clientY;
        return;
      }
      if (grab) return; // follows in the frame loop
      const hit = cast(e);
      route('move', hit);
      aim = hit;
      canvas.style.cursor = locked() ? 'none' : hit ? 'pointer' : 'grab';
      cross.classList.toggle('hot', !!hit);
    };
    const onUp = (e: MouseEvent) => {
      keyLight(`m${e.button}`, false);
      if (opts.xr) return;
      if (e.button === 2 && grab) { endGrab(); return; }
      if (looking) { looking = false; if (e.button === 2) return; }
      if (e.button === 0) route('up', cast(e));
    };
    const onWheel = (e: WheelEvent) => {
      if (opts.xr || !ready) return;
      e.preventDefault();
      if (grab) {
        if (e.shiftKey && grab.s) { grab.s.root.scale.multiplyScalar(e.deltaY > 0 ? 0.93 : 1.07); grab.s.root.scale.clampScalar(0.5, 2.5); }
        else grab.dist = Math.max(0.5, Math.min(8, grab.dist * (e.deltaY > 0 ? 1.08 : 0.92)));
        return;
      }
      route('wheel', cast(e), e.deltaY, e.ctrlKey);
    };
    const SPECIAL: Record<string, string> = { Backspace: 'Backspace', Enter: 'Enter', Tab: 'Tab', ArrowLeft: 'Left', ArrowRight: 'Right', ArrowUp: 'Up', ArrowDown: 'Down', Delete: 'Delete', Home: 'Home', End: 'End', PageUp: 'PageUp', PageDown: 'PageDown' };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') { e.preventDefault(); if (focus) { focus = null; sugg = []; bar.redraw(); sp.redraw(); } else exit(); return; }
      if (focus) {
        e.preventDefault();
        e.stopPropagation();
        if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) typeKey(e.key);
        else if (SPECIAL[e.key]) typeKey(SPECIAL[e.key]!);
        return;
      }
      if (e.code === 'KeyE') { if (menu.group.visible && !menu.group.userData.closing) show(menuS, false); else { show(menuS, true); place(menu.group, 0, 1.3, 1.5); } return; }
      if (e.code === 'Space' && jumpY <= 0.0001) { vy = 4.2; e.preventDefault(); }
      keys.add(e.code);
      keyLight(e.code, true);
    };
    const onKeyUp = (e: KeyboardEvent) => { keys.delete(e.code); keyLight(e.code, false); };
    canvas.addEventListener('mousedown', onDown);
    addEventListener('mousemove', onMove);
    addEventListener('mouseup', onUp);
    canvas.addEventListener('wheel', onWheel, { passive: false });
    canvas.addEventListener('contextmenu', (e) => e.preventDefault());
    addEventListener('keydown', onKey, true);
    addEventListener('keyup', onKeyUp, true);
    exitBtn.addEventListener('click', () => exit());

    // ---- XR: controllers / hands
    type Ctl = { c: THREE.Group; grip: THREE.Group; line: THREE.Line; arm: THREE.Group | null; hand: THREE.Group; grab: { s?: Spatial; p?: Prop; offset: THREE.Matrix4; last: THREE.Vector3; vel: THREE.Vector3 } | null; hover: THREE.Intersection | undefined; turnReady: boolean; aWas: boolean };
    const ctls: Ctl[] = [];
    let twoHand: { s: Spatial; d0: number; s0: number } | null = null;
    if (opts.xr) {
      const handFactory = new XRHandModelFactory();
      for (let i = 0; i < 2; i++) {
        const ctl = renderer.xr.getController(i);
        const grip = renderer.xr.getControllerGrip(i);
        const hand = renderer.xr.getHand(i);
        hand.add(handFactory.createHandModel(hand, 'boxes'));
        const line = new THREE.Line(new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(), new THREE.Vector3(0, 0, -1)]), new THREE.LineBasicMaterial({ color: '#ffd8a8', transparent: true, opacity: 0.6 }));
        line.scale.z = 4;
        ctl.add(line);
        rig.add(ctl, grip, hand);
        const entry: Ctl = { c: ctl, grip, line, arm: null, hand, grab: null, hover: undefined, turnReady: true, aWas: false };
        ctl.addEventListener('selectstart', () => route('down', entry.hover));
        ctl.addEventListener('selectend', () => route('up', entry.hover));
        ctl.addEventListener('squeezestart', () => {
          if (!entry.hover) return;
          const p = propOf(entry.hover.object);
          const s = p ? undefined : ownerOf(entry.hover.object);
          const obj = p ? p.mesh : s?.root;
          if (!obj) return;
          if (p) p.held = true;
          entry.grab = { s, p, offset: new THREE.Matrix4().copy(ctl.matrixWorld).invert().multiply(obj.matrixWorld), last: new THREE.Vector3().setFromMatrixPosition(ctl.matrixWorld), vel: new THREE.Vector3() };
          const other = ctls.find((x) => x !== entry && x.grab?.s && x.grab.s === s);
          if (s && other) twoHand = { s, d0: new THREE.Vector3().setFromMatrixPosition(ctl.matrixWorld).distanceTo(new THREE.Vector3().setFromMatrixPosition(other.c.matrixWorld)), s0: s.root.scale.x };
        });
        ctl.addEventListener('squeezeend', () => {
          if (entry.grab?.p) { entry.grab.p.held = false; entry.grab.p.vel.copy(entry.grab.vel).clampLength(0, 9); }
          if (entry.grab?.s) saveLayout();
          entry.grab = null;
          twoHand = null;
        });
        ctls.push(entry);
      }
    }

    // ---- frame loop
    let t = 0;
    let last = performance.now();
    let ready = false;
    let leaving = false;
    let leftAt = 0;
    const leftFor = () => (performance.now() - leftAt) / 1000;
    const startAt = performance.now();
    const wall = () => (performance.now() - startAt) / 1000;
    const tmpM = new THREE.Matrix4(), tmpQ = new THREE.Quaternion(), tmpS = new THREE.Vector3(), tmpV = new THREE.Vector3();
    const groundPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);
    spatials.forEach((s) => s.root.scale.setScalar(0.001));
    let teleportMark: THREE.Mesh | null = null;
    if (opts.xr) {
      teleportMark = new THREE.Mesh(new THREE.RingGeometry(0.18, 0.24, 32), new THREE.MeshBasicMaterial({ color: '#ffb050', transparent: true, opacity: 0.8 }));
      teleportMark.rotation.x = -Math.PI / 2;
      teleportMark.visible = false;
      scene.add(teleportMark);
    }

    const frame = (now: number) => {
      const dt = Math.min(0.05, (now - last) / 1000);
      last = now;
      t += dt;
      const et = wall();

      // Desktop movement.
      if (!opts.xr) {
        if (ready) {
          const f = (keys.has('KeyW') || keys.has('ArrowUp') ? 1 : 0) - (keys.has('KeyS') || keys.has('ArrowDown') ? 1 : 0);
          const r = (keys.has('KeyD') || keys.has('ArrowRight') ? 1 : 0) - (keys.has('KeyA') || keys.has('ArrowLeft') ? 1 : 0);
          const speed = keys.has('ShiftLeft') || keys.has('ShiftRight') ? 5.2 : 2.6;
          if (f || r) {
            const len = Math.hypot(f, r);
            rig.position.x += ((-Math.sin(yaw) * f + Math.cos(yaw) * r) / len) * speed * dt;
            rig.position.z += ((-Math.cos(yaw) * f - Math.sin(yaw) * r) / len) * speed * dt;
            const d = Math.hypot(rig.position.x, rig.position.z);
            if (d > 24) { rig.position.x *= 24 / d; rig.position.z *= 24 / d; }
            walkPhase += dt * speed * 2.4;
          }
          vy -= 11 * dt;
          jumpY = Math.max(0, jumpY + vy * dt);
          if (jumpY === 0) vy = Math.max(0, vy);
        }
        // Walk over the hills: the rig follows the ground under you.
        const gy = world.heightAt(rig.position.x, rig.position.z);
        rig.position.y += (gy - rig.position.y) * Math.min(1, dt * 10);
        const bob = Math.sin(walkPhase) * 0.035;
        camera.position.set(Math.cos(walkPhase * 0.5) * 0.02, 1.62 + jumpY + Math.abs(bob), 0);
        camera.rotation.set(pitch, yaw, Math.sin(walkPhase * 0.5) * 0.008, 'YXZ');
        camera.updateMatrixWorld();
      }

      // Entry: your hands bring the headset up to your face, black, lenses open.
      const k = clamp01((et - 0.5) / 2.0);
      headset.visible = et < 2.75;
      headset.position.set(0, -0.28 * (1 - ease(k)), -0.55 + ease(k) * 0.5);
      headset.rotation.set(-0.5 * (1 - ease(k)), Math.PI, 0);
      const black = clamp01((et - 2.35) / 0.35) * (1 - clamp01((et - 2.9) / 0.15));
      const open = clamp01((et - 2.9) / 0.9);
      lens.style.setProperty('--open', String(ease(open)));
      lensFog.style.opacity = String(et < 2.9 ? 0 : 1 - clamp01((et - 3.1) / 1.8));
      lens.style.opacity = et < 2.9 ? String(black) : open < 1 ? '1' : '0';
      lens.classList.toggle('opening', et >= 2.9);
      if (!ready && et > 3.0) {
        ready = true;
        if (!loadLayout()) spatials.forEach((s) => s.home());
      }
      spatials.forEach((s, i) => {
        if (!leaving && s.root.visible) {
          const a = ease(clamp01((et - 3.2 - i * 0.15) / 0.6));
          const target = (s.root.userData.scale as number | undefined) ?? 1;
          if (a < 1) s.root.scale.setScalar(Math.max(0.001, a * target));
          else if (s.root.userData.scale) { s.root.scale.setScalar(target); delete s.root.userData.scale; }
        }
        // Window open/close pop (slight overshoot when opening).
        const pop = s.root.userData.pop as { t0: number; from: number; to: number } | undefined;
        if (pop && et > 3.9) {
          const kk = clamp01((now - pop.t0) / 280);
          const e2 = pop.to > pop.from ? 1 + 2.2 * (kk - 1) ** 3 + 1.2 * (kk - 1) ** 2 : ease(kk);
          s.root.scale.setScalar(Math.max(0.001, pop.from + (pop.to - pop.from) * e2));
          if (kk >= 1) {
            delete s.root.userData.pop;
            if (s.root.userData.closing) { s.root.visible = false; delete s.root.userData.closing; s.root.scale.setScalar(pop.from); menu.redraw(); dock.redraw(); }
          }
        }
      });

      // Arms: during the entry they hold the headset; afterwards first-person pose (desktop) or controllers (XR).
      if (armR && armL) {
        const hold = 1 - clamp01((et - 2.6) / 0.4);
        const idle = Math.sin(t * 1.6) * 0.012;
        swing = Math.max(0, swing - dt * 4);
        const sw = Math.sin(swing * Math.PI) * 0.5;
        const walkSw = Math.sin(walkPhase) * 0.06;
        if (opts.xr && !hold) { armR.visible = armL.visible = false; } else {
          // Shoulder positions in camera space; arms point forward/up.
          armR.position.set(lerp(0.36, 0.16, hold), lerp(-0.42, -0.5, hold) + idle - (grab ? 0.05 : 0), lerp(0.05, -0.05, hold));
          armL.position.set(lerp(-0.36, -0.16, hold), lerp(-0.42, -0.5, hold) + idle, lerp(0.05, -0.05, hold));
          armR.rotation.set(lerp(0.75 + walkSw - sw, 1.25 * (0.4 + 0.6 * ease(k)), hold), lerp(0.25, 0.55, hold), 0);
          armL.rotation.set(lerp(0.6 - walkSw, 1.25 * (0.4 + 0.6 * ease(k)), hold), lerp(-0.25, -0.55, hold), 0);
          if (grab && !hold) armR.rotation.x += 0.35;
        }
      }

      // Visual VR: gloved hands with rays. Right hand aims where the mouse points.
      if (visual) {
        const hold = 1 - clamp01((et - 2.6) / 0.4);
        const idle = Math.sin(t * 1.6) * 0.006;
        const poke = Math.max(0, 1 - (now - pokeAt) / 200);
        pointAmt += ((aim ? 1 : 0) - pointAmt) * Math.min(1, dt * 12);
        const walk = Math.sin(walkPhase) * 0.012;
        hands.right.group.position.set(lerp(0.29, 0.085, hold), lerp(-0.29, -0.14 + 0.1 * ease(k), hold) + idle + walk, lerp(-0.5 - poke * 0.04, -0.36, hold));
        hands.left.group.position.set(lerp(-0.3, -0.085, hold), lerp(-0.3, -0.14 + 0.1 * ease(k), hold) - idle - walk, lerp(-0.5, -0.36, hold));
        hands.right.pose(pointAmt, poke);
        hands.left.pose(0.25, 0);
        // Desired aim point (world): what the mouse hits, else 4 m along the mouse ray.
        ray.setFromCamera(new THREE.Vector2(locked() ? 0 : mouse.x, locked() ? 0 : mouse.y), camera);
        const tgt = aim ? aim.point : tmpV.copy(ray.ray.origin).addScaledVector(ray.ray.direction, 4);
        const hp = hands.right.group.getWorldPosition(tmpS);
        tmpM.lookAt(hp, tgt, camera.up);
        tmpQ.setFromRotationMatrix(tmpM);
        const parentQ = camera.getWorldQuaternion(new THREE.Quaternion()).invert();
        const want = parentQ.multiply(tmpQ);
        if (hold > 0) want.slerp(new THREE.Quaternion().setFromEuler(new THREE.Euler(0.9, 0.35, 1.2)), hold);
        hands.right.group.quaternion.slerp(want, Math.min(1, dt * 14));
        hands.left.group.quaternion.slerp(hold > 0 ? new THREE.Quaternion().setFromEuler(new THREE.Euler(0.9, -0.35, -1.2)) : new THREE.Quaternion().setFromEuler(new THREE.Euler(0.12, 0.1, 0)), Math.min(1, dt * 8));
        const showRays = hands.root.visible && hold <= 0 && ready && !leaving;
        hands.rayR.visible = hands.rayL.visible = showRays;
        hands.dot.visible = showRays && !!aim;
        if (showRays) {
          const tipR = hands.right.tip(new THREE.Vector3());
          aimRay(hands.rayR, tipR, tgt);
          const tipL = hands.left.tip(new THREE.Vector3());
          const dirL = new THREE.Vector3(0, 0, -1).applyQuaternion(hands.left.group.getWorldQuaternion(new THREE.Quaternion()));
          aimRay(hands.rayL, tipL, tipL.clone().addScaledVector(dirL, 1.4));
          if (aim) {
            hands.dot.position.copy(aim.point);
            (hands.dot.userData.halo as THREE.Mesh).lookAt(headPos());
            hands.dot.scale.setScalar(1 + poke * 0.6);
          }
        }
      }

      // The dock drifts back below your view when you turn away from it.
      if (ready && !leaving && dock.group.visible && grab?.s !== dockS) {
        const h = headPos();
        const dyaw = Math.atan2(-(dock.group.position.x - h.x), -(dock.group.position.z - h.z));
        let diff = camYaw() - dyaw;
        diff = Math.atan2(Math.sin(diff), Math.cos(diff));
        if (Math.abs(diff) > 1.0) dockFollow = true;
        if (dockFollow) {
          const yw = camYaw();
          const target = new THREE.Vector3(h.x - Math.sin(yw) * 1.1, rig.position.y + 1.13, h.z - Math.cos(yw) * 1.1);
          dock.group.position.lerp(target, Math.min(1, dt * 4));
          dock.group.lookAt(h.x, dock.group.position.y + 0.5, h.z);
          if (dock.group.position.distanceTo(target) < 0.02) { dockFollow = false; saveLayout(); }
        }
      }
      // Dock clock and frame rate.
      fpsFrames++;
      if (now - fpsAt > 1000) { fps = Math.round((fpsFrames * 1000) / (now - fpsAt)); ms = Math.round((now - fpsAt) / Math.max(1, fpsFrames)); fpsFrames = 0; fpsAt = now; dock.redraw(); root.dataset.fps = String(fps); }

      // Desktop grabbing follows the view ray.
      if (grab && !opts.xr) {
        ray.setFromCamera(new THREE.Vector2(locked() ? 0 : mouse.x, locked() ? 0 : mouse.y), camera);
        const target = ray.ray.origin.clone().addScaledVector(ray.ray.direction, grab.dist);
        if (grab.p) { grab.vel.copy(target).sub(grab.p.mesh.position).divideScalar(Math.max(dt, 0.001)); grab.p.mesh.position.lerp(target, 0.5); }
        if (grab.s) { grab.s.root.position.lerp(target, 0.5); grab.s.root.lookAt(headPos().x, grab.s.root.position.y, headPos().z); }
      }

      // XR controllers: grab, two-hand scale, locomotion, snap turn, teleport.
      const session = renderer.xr.getSession();
      for (const [i, e] of ctls.entries()) {
        const src = session?.inputSources[i];
        const gp = src?.gamepad;
        const ax = gp?.axes ?? [];
        const handed = src?.handedness ?? (i ? 'right' : 'left');
        ray.ray.origin.setFromMatrixPosition(e.c.matrixWorld);
        ray.ray.direction.set(0, 0, -1).applyQuaternion(tmpQ.setFromRotationMatrix(e.c.matrixWorld));
        e.line.visible = !src?.hand;
        if (e.arm) e.arm.visible = !src?.hand && arms.visible;
        if (e.grab) {
          const p = tmpV.setFromMatrixPosition(e.c.matrixWorld);
          e.grab.vel.copy(p).sub(e.grab.last).divideScalar(Math.max(dt, 0.001));
          e.grab.last.copy(p);
          const obj = e.grab.p ? e.grab.p.mesh : e.grab.s!.root;
          const sc = obj.scale.x;
          tmpM.multiplyMatrices(e.c.matrixWorld, e.grab.offset);
          tmpM.decompose(obj.position, obj.quaternion, tmpS);
          obj.scale.setScalar(sc);
          if (Math.abs(ax[3] ?? 0) > 0.2) e.grab.offset.premultiply(new THREE.Matrix4().makeTranslation(0, 0, (ax[3] ?? 0) * dt * 1.2));
          if (twoHand && e.grab.s === twoHand.s) {
            const o = ctls.find((x) => x !== e);
            if (o) { const d = p.distanceTo(tmpS.setFromMatrixPosition(o.c.matrixWorld)); twoHand.s.root.scale.setScalar(Math.max(0.4, Math.min(3, twoHand.s0 * (d / Math.max(0.05, twoHand.d0))))); }
          }
          continue;
        }
        e.hover = ray.intersectObjects(targets(), false)[0];
        e.line.scale.z = e.hover ? e.hover.distance : 4;
        if (ready) {
          route('move', e.hover);
          if (e.hover?.object === page && Math.abs(ax[3] ?? 0) > 0.15) { scrollVel += (ax[3] ?? 0) * 1600 * dt; const uv = e.hover.uv!; scrollAt = { x: uv.x * BW, y: (1 - uv.y) * BH }; }
          if (handed === 'left') {
            // Smooth locomotion relative to head direction.
            const mx = ax[2] ?? 0, my = ax[3] ?? 0;
            if (Math.hypot(mx, my) > 0.2) {
              // Forward = where the head looks (flattened), right = 90° clockwise of it.
              const hy = camYaw();
              const fwd = -my, side = mx;
              rig.position.x += (-Math.sin(hy) * fwd + Math.cos(hy) * side) * 2.2 * dt;
              rig.position.z += (-Math.cos(hy) * fwd - Math.sin(hy) * side) * 2.2 * dt;
            }
          } else {
            const tx = ax[2] ?? 0;
            if (Math.abs(tx) > 0.7 && e.turnReady && !(e.hover?.object === page)) {
              e.turnReady = false;
              const head = headPos();
              rig.position.sub(head).applyAxisAngle(new THREE.Vector3(0, 1, 0), tx > 0 ? -Math.PI / 6 : Math.PI / 6).add(head);
              rig.rotation.y += tx > 0 ? -Math.PI / 6 : Math.PI / 6;
            } else if (Math.abs(tx) < 0.3) e.turnReady = true;
            // Teleport with A: aim at the ground.
            const a = !!gp?.buttons[4]?.pressed;
            const hitG = ray.ray.intersectPlane(groundPlane, tmpS);
            if (teleportMark) { teleportMark.visible = a && !!hitG && hitG.length() < 14; if (hitG) teleportMark.position.set(hitG.x, 0.01, hitG.z); }
            if (e.aWas && !a && hitG && hitG.length() < 14) { const head = headPos(); rig.position.x += hitG.x - head.x; rig.position.z += hitG.z - head.z; }
            e.aWas = a;
          }
        }
      }

      // Browser scrolling with inertia.
      if (Math.abs(scrollVel) > 1 && scrollAt) {
        api.vrBrowserInput({ type: 'mouseWheel', x: scrollAt.x, y: scrollAt.y, deltaY: scrollVel * dt * 4 });
        scrollVel *= Math.pow(0.02, dt);
      } else scrollVel = 0;

      // Props: gravity, ground/platform contact, bounce, friction.
      for (const p of props) {
        if (p.held) continue;
        if (p.vel.lengthSq() < 1e-6 && p.mesh.position.y <= p.radius + 0.001 && p.spin.lengthSq() < 1e-6) continue;
        p.vel.y -= 9.81 * dt;
        p.mesh.position.addScaledVector(p.vel, dt);
        p.mesh.rotation.x += p.spin.x * dt;
        p.mesh.rotation.y += p.spin.y * dt;
        const floorY = world.heightAt(p.mesh.position.x, p.mesh.position.z);
        if (p.mesh.position.y < floorY + p.radius) {
          p.mesh.position.y = floorY + p.radius;
          if (p.vel.y < -0.6) { p.vel.y *= -0.35; p.spin.multiplyScalar(0.6); } else { p.vel.y = 0; p.spin.multiplyScalar(0.85); }
          p.vel.x *= 0.86; p.vel.z *= 0.86;
          if (p.vel.lengthSq() < 0.002) p.vel.set(0, 0, 0);
          if (p.spin.lengthSq() < 0.01) p.spin.set(0, 0, 0);
        }
        const d = Math.hypot(p.mesh.position.x, p.mesh.position.z);
        if (d > 30) { p.mesh.position.x *= 30 / d; p.mesh.position.z *= 30 / d; p.vel.x *= -0.4; p.vel.z *= -0.4; }
      }

      world.update(t);
      if (bstate.loading) bar.redraw();

      renderer.render(scene, camera);
      root.dataset.t = et.toFixed(1);
      root.dataset.browser = browser.root.visible && !browser.root.userData.closing ? '1' : '0';
      if (state.info.e2e && ready) {
        // Screen position of the dock's browser button, for the UI test.
        const lp = new THREE.Vector3(((590 + 104 + 42) / DW - 0.5) * dock.widthM, (0.5 - (DH - 18) / 2 / DH) * (dock.widthM * DH / DW), 0);
        const sp3 = dock.mesh.localToWorld(lp).project(camera);
        root.dataset.compass = `${Math.round((sp3.x + 1) / 2 * innerWidth)},${Math.round((1 - sp3.y) / 2 * innerHeight)}`;
      }
      if (leaving && leftFor() > 1.0) finish();
    };
    const lerp = (a: number, b: number, k2: number) => a + (b - a) * k2;

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
        toast(`VR-Brille konnte nicht gestartet werden (${e instanceof Error ? e.message : String(e)}). Es läuft der visuelle VR-Modus.`, 'error');
        opts.xr = false;
        renderer.xr.enabled = false;
        root.classList.add('visual');
        visual = true;
        help.hidden = true;
        keysEl.hidden = false;
        if (!hands.root.parent) { camera.add(hands.root); scene.add(hands.rayR, hands.rayL, hands.dot); }
      });
    }

    let done = false;
    function exit(): void {
      if (leaving) return;
      leaving = true;
      leftAt = performance.now();
      if (locked()) document.exitPointerLock();
      setTimeout(finish, 1300);
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
      clearTimeout(suggTimer);
      void actx.close().catch(() => {});
      removeEventListener('mousemove', onMove);
      removeEventListener('mouseup', onUp);
      removeEventListener('keydown', onKey, true);
      removeEventListener('keyup', onKeyUp, true);
      removeEventListener('resize', resize);
      [menu, bar, kb, sp, dock, fav].forEach((x) => x.dispose());
      stopAmbience();
      panelSounds.hover = panelSounds.click = undefined;
      world.dispose();
      hands.dispose();
      scene.traverse((o) => {
        const m = o as THREE.Mesh;
        m.geometry?.dispose();
        const mats = Array.isArray(m.material) ? m.material : m.material ? [m.material] : [];
        mats.forEach((x) => { (x as THREE.MeshBasicMaterial).map?.dispose(); x.dispose(); });
      });
      bGeo.dispose();
      renderer.dispose();
      renderer.forceContextLoss();
      root.remove();
      resolveExit();
    }
  });
}

export function vrRequirements(): string[] {
  return [
    'Visueller VR-Modus: läuft auf jedem PC – mit Tastatur (WASD, Leertaste, E) und Maus.',
    'Echtes VR: eine PC-VR-Brille (z. B. Meta Quest per Link/Air Link, Valve Index, HTC Vive, Windows Mixed Reality).',
    'Dazu eine aktive OpenXR-Laufzeit (Meta Quest Link, SteamVR oder Windows Mixed Reality) als Standard eingestellt.',
  ];
}

export type { Pal };
