// Local Minecraft servers (see core/server.ts): files under <userData>/servers/<id>, one
// running process per server, its console lines go to the page.

import { spawn, type ChildProcess } from 'node:child_process';
import { mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { randomUUID } from 'node:crypto';
import { downloadAll } from '../core/download.ts';
import { fetchJson, type FetchLike } from '../core/net.ts';
import { installJava, layout, vanillaVersion } from '../core/install.ts';
import type { Env } from '../core/mojang.ts';
import { eulaFile, sanitizeServer, SERVER_DEFAULT, serverArgs, serverProperties, validCommand, type LocalServer } from '../core/server.ts';

const FABRIC_META = 'https://meta.fabricmc.net/v2';

export class ServerManager {
  private list: LocalServer[] | null = null;
  private procs = new Map<string, ChildProcess>();
  private logs = new Map<string, string[]>();

  private root: string;
  private gameRoot: string;
  private env: Env;
  private fetch: FetchLike;
  private emit: (ev: string, data: unknown) => void;

  constructor(root: string, gameRoot: string, env: Env, f: FetchLike, emit: (ev: string, data: unknown) => void) {
    this.root = root;
    this.gameRoot = gameRoot;
    this.env = env;
    this.fetch = f;
    this.emit = emit;
  }

  private file() { return join(this.root, 'servers.json'); }
  dir(id: string) { return join(this.root, 'servers', id); }

  async all(): Promise<(LocalServer & { running: boolean })[]> {
    this.list ??= await readFile(this.file(), 'utf8').then((t) => JSON.parse(t) as LocalServer[]).catch(() => []);
    return this.list.map((s) => ({ ...s, running: this.procs.has(s.id) }));
  }

  private async save(): Promise<void> {
    await mkdir(this.root, { recursive: true });
    await writeFile(this.file(), JSON.stringify(this.list ?? [], null, 2));
  }

  private get(id: string): LocalServer {
    const s = this.list?.find((x) => x.id === id);
    if (!s) throw new Error('Server nicht gefunden');
    return s;
  }

  async upsert(raw: unknown): Promise<LocalServer> {
    await this.all();
    const id = (raw as { id?: unknown })?.id;
    const old = typeof id === 'string' ? this.list!.find((x) => x.id === id) : undefined;
    const s = sanitizeServer(raw, old ?? { ...SERVER_DEFAULT, id: randomUUID(), createdAt: Date.now() });
    this.list = old ? this.list!.map((x) => (x.id === s.id ? s : x)) : [...this.list!, s];
    await this.save();
    return s;
  }

  async remove(id: string): Promise<void> {
    await this.all();
    if (this.procs.has(id)) throw new Error('Erst den Server stoppen.');
    this.get(id);
    this.list = this.list!.filter((x) => x.id !== id);
    await this.save();
    await rm(this.dir(id), { recursive: true, force: true });
  }

  log(id: string): string[] { return this.logs.get(id) ?? []; }

  private line(id: string, line: string): void {
    const l = this.logs.get(id) ?? [];
    l.push(line);
    if (l.length > 3000) l.splice(0, l.length - 2500);
    this.logs.set(id, l);
    this.emit('server-log', { id, line });
  }

  /** Downloads what is missing (Java, server jar) and starts the server. */
  async start(id: string): Promise<void> {
    await this.all();
    const s = this.get(id);
    if (this.procs.has(id)) return;
    if (!s.eulaAccepted) throw new Error('Bitte zuerst die Minecraft-EULA akzeptieren.');
    const dir = this.dir(id);
    await mkdir(dir, { recursive: true });
    const l = layout(this.gameRoot);
    const say = (t: string) => this.line(id, `[LEGO] ${t}`);
    say(`Minecraft ${s.gameVersion} (${s.loader}) wird vorbereitet …`);
    const v = await vanillaVersion(this.fetch, l, s.gameVersion);
    const java = await installJava(this.fetch, l, v.javaVersion?.component ?? 'jre-legacy', this.env, (step) => say(step));
    const srv = v.downloads?.server;
    if (!srv) throw new Error(`Für Minecraft ${s.gameVersion} gibt es keinen offiziellen Server.`);
    const vanillaJar = join(dir, `minecraft_server.${s.gameVersion}.jar`);
    await downloadAll(this.fetch, [{ url: srv.url, dest: vanillaJar, sha1: srv.sha1, size: srv.size, label: 'server.jar' }]);
    let jar = vanillaJar;
    if (s.loader === 'fabric') {
      const loaders = await fetchJson<{ loader: { version: string; stable: boolean } }[]>(this.fetch, `${FABRIC_META}/versions/loader/${encodeURIComponent(s.gameVersion)}`);
      const installers = await fetchJson<{ version: string; stable: boolean }[]>(this.fetch, `${FABRIC_META}/versions/installer`);
      const lv = (loaders.find((x) => x.loader.stable) ?? loaders[0])?.loader.version;
      const iv = (installers.find((x) => x.stable) ?? installers[0])?.version;
      if (!lv || !iv) throw new Error(`Fabric gibt es nicht für Minecraft ${s.gameVersion}.`);
      jar = join(dir, `fabric-server-${s.gameVersion}-${lv}.jar`);
      const res = await this.fetch(`${FABRIC_META}/versions/loader/${encodeURIComponent(s.gameVersion)}/${lv}/${iv}/server/jar`);
      if (!res.ok) throw new Error(`Fabric-Server-Download fehlgeschlagen (${res.status})`);
      await writeFile(jar, Buffer.from(await res.arrayBuffer()));
      // Fabric's launcher loads the vanilla jar from server.jar next to it.
      await writeFile(join(dir, 'fabric-server-launcher.properties'), `serverJar=minecraft_server.${s.gameVersion}.jar\n`);
    }
    await writeFile(join(dir, 'eula.txt'), eulaFile());
    const props = join(dir, 'server.properties');
    await writeFile(props, serverProperties(await readFile(props, 'utf8').catch(() => ''), s));
    say('Server startet …');
    const p = spawn(java.replace(/javaw\.exe$/, 'java.exe'), serverArgs(s, jar), { cwd: dir, stdio: ['pipe', 'pipe', 'pipe'], windowsHide: true });
    this.procs.set(id, p);
    this.emit('server-state', { id, running: true });
    let rest = '';
    const onData = (d: Buffer) => {
      const t = rest + d.toString('utf8');
      const parts = t.split(/\r?\n/);
      rest = parts.pop() ?? '';
      for (const x of parts) if (x) this.line(id, x);
    };
    p.stdout!.on('data', onData);
    p.stderr!.on('data', onData);
    p.on('exit', (code) => {
      this.procs.delete(id);
      say(`Server beendet (Code ${code ?? '?'})`);
      this.emit('server-state', { id, running: false });
    });
    p.on('error', (e) => say(`Fehler: ${e.message}`));
  }

  command(id: string, cmd: unknown): void {
    const p = this.procs.get(id);
    if (!p || !validCommand(cmd)) return;
    this.line(id, `> ${cmd}`);
    p.stdin!.write(`${cmd.replace(/^\//, '')}\n`);
  }

  /** Saves the world and stops; kills the process if it does not end within 40 s. */
  stop(id: string): void {
    const p = this.procs.get(id);
    if (!p) return;
    p.stdin!.write('stop\n');
    setTimeout(() => { if (this.procs.get(id) === p) p.kill(); }, 40_000).unref();
  }

  stopAll(): void { for (const id of this.procs.keys()) this.stop(id); }
}
