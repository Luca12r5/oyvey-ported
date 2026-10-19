// The LEGO client's real 3D cosmetics in the launcher: geometry and textures are
// exported from the client's own model code (tools/cosmetics3d/export.mjs) and
// attached to the skinview3d player the same way the game attaches them (head
// items to the head, wings/back/aura to the body, knives to the right arm, pets
// and vehicles next to / under the player). Animated items cycle their frames.
import {
  AdditiveBlending, BufferAttribute, BufferGeometry, DoubleSide, Group, LinearMipmapLinearFilter, Mesh, MeshBasicMaterial, MeshStandardMaterial,
  NearestFilter, SRGBColorSpace, TextureLoader, Box3, Vector3, type Material, type Texture,
} from 'three';
import type { SkinViewer } from 'skinview3d';

const BASE = 'cosmetics3d/';

interface GroupInfo { tex: string; glow: boolean; quads: number }
interface ItemFile { id: string; slot: string; frames: GroupInfo[][] }
export interface CosmeticModel { id: string; slot: string; frames: Group[] }

let index: Promise<{ items: { id: string; slot: string; frames: number }[]; textures: Record<string, string> }> | null = null;
function loadIndex() {
  index ??= fetch(`${BASE}index.json`).then((r) => (r.ok ? r.json() : { items: [], textures: {} })).catch(() => ({ items: [], textures: {} }));
  return index;
}

/** Ids that have a real 3D model. */
export async function has3D(id: string): Promise<boolean> {
  return (await loadIndex()).items.some((i) => i.id === id);
}

const texCache = new Map<string, Texture>();
const loader = new TextureLoader();
function texture(key: string, file: string | undefined): Texture | null {
  if (!file) return null;
  let t = texCache.get(key);
  if (!t) {
    t = loader.load(`${BASE}textures/${file}`);
    t.colorSpace = SRGBColorSpace;
    t.flipY = false;
    t.magFilter = NearestFilter;
    t.minFilter = LinearMipmapLinearFilter;
    texCache.set(key, t);
  }
  return t;
}

const modelCache = new Map<string, Promise<CosmeticModel | null>>();

/** Loads one cosmetic (null when there is no 3D model for it). */
export function loadCosmetic(id: string, skinTexture: () => Texture | null): Promise<CosmeticModel | null> {
  let p = modelCache.get(id);
  if (!p) {
    p = (async () => {
      const idx = await loadIndex();
      if (!idx.items.some((i) => i.id === id)) return null;
      const res = await fetch(`${BASE}items/${id}.bin.gz`);
      if (!res.ok || !res.body) return null;
      const buf = await new Response(res.body.pipeThrough(new DecompressionStream('gzip'))).arrayBuffer();
      const dv = new DataView(buf);
      if (String.fromCharCode(dv.getUint8(0), dv.getUint8(1), dv.getUint8(2), dv.getUint8(3)) !== 'LC3D') return null;
      const hl = dv.getUint32(4, true);
      const head = JSON.parse(new TextDecoder().decode(new Uint8Array(buf, 8, hl))) as ItemFile;
      let off = 8 + hl + ((8 + hl) % 2);
      const frames = head.frames.map((groups) => {
        const g = new Group();
        for (const info of groups) {
          const n = info.quads;
          const pos = new Int16Array(buf.slice(off, off + n * 24)); off += n * 24;
          const uv = new Int16Array(buf.slice(off, off + n * 16)); off += n * 16;
          const col = new Uint8Array(buf, off, n * 4); off += n * 4;
          const P = new Float32Array(n * 6 * 3), U = new Float32Array(n * 6 * 2), C = new Float32Array(n * 6 * 4);
          let transparent = false;
          for (let q = 0; q < n; q++) {
            // quad v0 v1 v2 v3 -> triangles (0,1,2) (0,2,3)
            [0, 1, 2, 0, 2, 3].forEach((v, k) => {
              const o = q * 6 + k;
              P[o * 3] = pos[q * 12 + v * 3]! / 100; P[o * 3 + 1] = pos[q * 12 + v * 3 + 1]! / 100; P[o * 3 + 2] = pos[q * 12 + v * 3 + 2]! / 100;
              U[o * 2] = uv[q * 8 + v * 2]! / 4096; U[o * 2 + 1] = uv[q * 8 + v * 2 + 1]! / 4096;
              C[o * 4] = col[q * 4]! / 255; C[o * 4 + 1] = col[q * 4 + 1]! / 255; C[o * 4 + 2] = col[q * 4 + 2]! / 255; C[o * 4 + 3] = col[q * 4 + 3]! / 255;
            });
            if (col[q * 4 + 3]! < 255) transparent = true;
          }
          const geo = new BufferGeometry();
          geo.setAttribute('position', new BufferAttribute(P, 3));
          geo.setAttribute('uv', new BufferAttribute(U, 2));
          geo.setAttribute('color', new BufferAttribute(C, 4));
          geo.computeVertexNormals();
          const map = info.tex === '@skin' ? skinTexture() : texture(info.tex, idx.textures[info.tex]);
          const mat: Material = info.glow
            ? new MeshBasicMaterial({ map, vertexColors: true, transparent: true, blending: AdditiveBlending, depthWrite: false, side: DoubleSide, toneMapped: false })
            : new MeshStandardMaterial({ map, vertexColors: true, transparent, alphaTest: 0.08, side: DoubleSide, roughness: 0.55, metalness: 0.08 });
          g.add(new Mesh(geo, mat));
        }
        return g;
      });
      return { id: head.id, slot: head.slot, frames };
    })().catch((e) => { console.warn('cosmetic 3D', id, e); return null; });
    modelCache.set(id, p);
  }
  return p;
}

/** Equipped cosmetics on a skinview3d player; returns a disposer. */
export function wearCosmetics(viewer: SkinViewer, ids: string[]): () => void {
  const skin = viewer.playerObject.skin;
  const holders: Group[] = [];
  let stop = false;
  const skinTex = () => (skin.head.innerLayer as Mesh).material ? ((skin.head.innerLayer as Mesh).material as MeshStandardMaterial).map ?? null : null;
  for (const id of ids) {
    void loadCosmetic(id, skinTex).then((m) => {
      if (!m || stop) return;
      const holder = new Group();
      holder.name = `cos:${id}`;
      // Each frame is a copy of the model at a moment of its animation.
      const frames = m.frames.map((f) => f.clone());
      frames.forEach((f, i) => { f.visible = i === 0; holder.add(f); });
      holder.userData.frames = frames;
      const parent = m.slot === 'hat' || m.slot === 'face' ? skin.head : m.slot === 'knife' ? skin.rightArm : m.slot === 'pet' || m.slot === 'vehicle' ? viewer.playerObject : skin.body;
      if (parent === skin.body) holder.position.set(0, 6, 0); // body group sits 6 below the neck pivot
      if (m.slot === 'pet' || m.slot === 'vehicle') {
        const box = new Box3().setFromObject(frames[0]!);
        const size = box.getSize(new Vector3());
        const scale = m.slot === 'pet' ? Math.min(1, 14 / Math.max(size.x, size.y, size.z, 1)) : Math.min(1, 30 / Math.max(size.x, size.z, 1));
        holder.scale.setScalar(scale);
        holder.position.set(m.slot === 'pet' ? 15 : 0, -24 - box.min.y * scale, m.slot === 'pet' ? 4 : 0);
      }
      parent.add(holder);
      holders.push(holder);
      viewer.canvas.dataset.cos = String(holders.length);
    });
  }
  let t0 = performance.now(), raf = 0;
  const tick = (t: number) => {
    raf = requestAnimationFrame(tick);
    const k = Math.floor((t - t0) / 350);
    for (const h of holders) {
      const fr = h.userData.frames as Group[];
      if (fr.length < 2) continue;
      // ping-pong so the loop has no jump
      const n = fr.length, cyc = 2 * n - 2, i = k % cyc, f = i < n ? i : cyc - i;
      fr.forEach((g, j) => { g.visible = j === f; });
    }
  };
  raf = requestAnimationFrame(tick);
  return () => {
    stop = true;
    cancelAnimationFrame(raf);
    for (const h of holders) h.removeFromParent();
    t0 = 0;
  };
}

// ---------------------------------------------------------------- posters

let posterViewer: SkinViewer | null = null;
const posterCache = new Map<string, string>();
// Newest request first: cards that just scrolled into view render before older ones.
const posterJobs: (() => Promise<void>)[] = [];
let posterBusy = false;
function pumpPosters(): void {
  if (posterBusy) return;
  const job = posterJobs.pop();
  if (!job) return;
  posterBusy = true;
  void job().finally(() => { posterBusy = false; pumpPosters(); });
}

async function texturesReady(root: Group): Promise<void> {
  const maps: Texture[] = [];
  root.traverse((o) => { const m = (o as Mesh).material as MeshStandardMaterial | undefined; if (m?.map) maps.push(m.map); });
  const until = performance.now() + 2000;
  while (maps.some((t) => !(t.image as HTMLImageElement | undefined)?.complete) && performance.now() < until) await new Promise((r) => setTimeout(r, 30));
}

/** PNG of the item worn by a mannequin (cached), for shop and collection cards. */
export function cosmeticPoster(id: string, slot: string, skin: string, w = 220, h = 220): Promise<string | null> {
  const key = `${id}|${w}x${h}`;
  const hit = posterCache.get(key);
  if (hit) return Promise.resolve(hit);
  return new Promise<string | null>((resolve) => { posterJobs.push(() => render().then(resolve, () => resolve(null))); pumpPosters(); });
  async function render(): Promise<string | null> {
    if (!(await has3D(id))) return null;
    const { SkinViewer: Viewer } = await import('skinview3d');
    if (!posterViewer) {
      const canvas = document.createElement('canvas');
      posterViewer = new Viewer({ canvas, width: w, height: h, pixelRatio: Math.min(2, devicePixelRatio || 1), preserveDrawingBuffer: true, zoom: 0.9, fov: 35 });
      posterViewer.renderer.setClearColor(0x000000, 0);
      posterViewer.autoRotate = false;
      posterViewer.animation = null;
      posterViewer.globalLight.intensity = 2.4;
      posterViewer.cameraLight.intensity = 0.9;
      await posterViewer.loadSkin(skin);
    }
    const v = posterViewer;
    v.setSize(w, h);
    const back = slot === 'wings' || slot === 'back';
    v.playerWrapper.rotation.y = back ? Math.PI * 0.8 : slot === 'knife' ? -0.6 : 0.45;
    const head = slot === 'hat' || slot === 'face';
    const ty = head ? 10 : slot === 'pet' || slot === 'vehicle' ? -12 : -6;
    v.camera.position.set(0, ty, head ? 30 : slot === 'vehicle' ? 100 : slot === 'pet' ? 78 : 72);
    v.controls.target.set(slot === 'pet' ? 8 : 0, ty, 0);
    v.controls.update();
    v.playerObject.position.y = head ? -2 : 0;
    const off = wearCosmetics(v, [id]);
    const until = performance.now() + 3000;
    let holder: Group | undefined;
    while (!(holder = v.scene.getObjectByName(`cos:${id}`) as Group | undefined) && performance.now() < until) await new Promise((r) => setTimeout(r, 20));
    if (holder) await texturesReady(holder);
    v.render();
    const url = v.canvas.toDataURL('image/png');
    off();
    if (posterCache.size > 300) posterCache.clear();
    posterCache.set(key, url);
    return url;
  }
}
