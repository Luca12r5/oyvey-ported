// Minecraft version metadata: manifest, version JSON, rule evaluation,
// library/classpath resolution and launch argument construction.

import { posix } from 'node:path';

export const VERSION_MANIFEST = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json';
export const RESOURCES = 'https://resources.download.minecraft.net';
export const LIBRARIES = 'https://libraries.minecraft.net';

export type OsName = 'windows' | 'osx' | 'linux';

export interface Env {
  os: OsName;
  arch: 'x86' | 'x64' | 'arm64';
  osVersion: string;
  features: Record<string, boolean>;
}

export interface Rule {
  action: 'allow' | 'disallow';
  os?: { name?: string; arch?: string; version?: string };
  features?: Record<string, boolean>;
}

export interface Artifact { path: string; sha1: string; size: number; url: string }

export interface Library {
  name: string;
  downloads?: { artifact?: Artifact; classifiers?: Record<string, Artifact> };
  natives?: Record<string, string>;
  rules?: Rule[];
  extract?: { exclude?: string[] };
  /** Fabric-style libraries: maven base url (+ optional hashes). */
  url?: string;
  sha1?: string;
  size?: number;
}

export type Argument = string | { rules?: Rule[]; value: string | string[] };

export interface VersionJson {
  id: string;
  inheritsFrom?: string;
  type?: string;
  mainClass: string;
  minecraftArguments?: string;
  arguments?: { game?: Argument[]; jvm?: Argument[] };
  libraries: Library[];
  assetIndex?: { id: string; sha1: string; size: number; url: string; totalSize?: number };
  assets?: string;
  downloads?: { client?: { sha1: string; size: number; url: string } };
  javaVersion?: { component: string; majorVersion: number };
  logging?: { client?: { argument: string; file: { id: string; sha1: string; size: number; url: string }; type: string } };
}

export interface ManifestEntry { id: string; type: string; url: string; sha1: string; releaseTime: string }
export interface Manifest { latest: { release: string; snapshot: string }; versions: ManifestEntry[] }

export function ruleAllows(rules: Rule[] | undefined, env: Env): boolean {
  if (!rules || rules.length === 0) return true;
  let allowed = false;
  for (const r of rules) {
    let match = true;
    if (r.os) {
      if (r.os.name && r.os.name !== env.os) match = false;
      if (r.os.arch && r.os.arch !== env.arch) match = false;
      if (r.os.version) {
        try {
          if (!new RegExp(r.os.version).test(env.osVersion)) match = false;
        } catch {
          match = false;
        }
      }
    }
    if (r.features) {
      for (const [k, v] of Object.entries(r.features)) if ((env.features[k] ?? false) !== v) match = false;
    }
    if (match) allowed = r.action === 'allow';
  }
  return allowed;
}

/** "group:artifact:version[:classifier]" -> maven relative path. */
export function mavenPath(name: string, ext = 'jar'): string {
  const [group, artifact, version, classifier] = name.split(':');
  if (!group || !artifact || !version) throw new Error(`invalid maven coordinate ${name}`);
  const file = `${artifact}-${version}${classifier ? `-${classifier}` : ''}.${ext}`;
  return posix.join(...group.split('.'), artifact, version, file);
}

/** Library identity without version, used to let a child profile override a parent's library. */
function libKey(name: string): string {
  const p = name.split(':');
  return `${p[0]}:${p[1]}${p[3] ? `:${p[3]}` : ''}`;
}

/** Merges a child profile (e.g. Fabric) into its parent (vanilla). */
export function mergeVersions(parent: VersionJson, child: VersionJson): VersionJson {
  const childKeys = new Set(child.libraries.map((l) => libKey(l.name)));
  return {
    ...parent,
    ...child,
    id: child.id,
    inheritsFrom: undefined,
    mainClass: child.mainClass ?? parent.mainClass,
    libraries: [...child.libraries, ...parent.libraries.filter((l) => !childKeys.has(libKey(l.name)))],
    arguments: {
      game: [...(parent.arguments?.game ?? []), ...(child.arguments?.game ?? [])],
      jvm: [...(parent.arguments?.jvm ?? []), ...(child.arguments?.jvm ?? [])],
    },
    assetIndex: child.assetIndex ?? parent.assetIndex,
    downloads: child.downloads ?? parent.downloads,
    javaVersion: child.javaVersion ?? parent.javaVersion,
    logging: child.logging ?? parent.logging,
  };
}

export interface ResolvedFile {
  /** Path relative to the libraries directory. */
  path: string;
  url: string;
  sha1: string | null;
  size: number | null;
  native: boolean;
  excludes?: string[];
}

/** Libraries that apply on this OS, including natives, deduplicated. */
export function resolveLibraries(v: VersionJson, env: Env): ResolvedFile[] {
  const out = new Map<string, ResolvedFile>();
  for (const lib of v.libraries) {
    if (!ruleAllows(lib.rules, env)) continue;
    if (lib.downloads?.artifact) {
      const a = lib.downloads.artifact;
      // Modern natives are separate libraries whose name ends in :natives-<os>.
      const native = /:natives-/.test(lib.name);
      out.set(a.path, { path: a.path, url: a.url, sha1: a.sha1, size: a.size, native });
    } else if (!lib.downloads && lib.url !== undefined) {
      const path = mavenPath(lib.name);
      out.set(path, { path, url: `${lib.url.replace(/\/?$/, '/')}${path}`, sha1: lib.sha1 ?? null, size: lib.size ?? null, native: false });
    } else if (!lib.downloads) {
      const path = mavenPath(lib.name);
      out.set(path, { path, url: `${LIBRARIES}/${path}`, sha1: lib.sha1 ?? null, size: lib.size ?? null, native: false });
    }
    // Legacy natives (pre-1.19): classifier per OS.
    if (lib.natives?.[env.os] && lib.downloads?.classifiers) {
      const key = lib.natives[env.os]!.replace('${arch}', env.arch === 'x86' ? '32' : '64');
      const c = lib.downloads.classifiers[key];
      if (c) out.set(c.path, { path: c.path, url: c.url, sha1: c.sha1, size: c.size, native: true, excludes: lib.extract?.exclude });
    }
  }
  return [...out.values()];
}

export interface LaunchContext {
  version: VersionJson;
  env: Env;
  javaPath: string;
  gameDir: string;
  assetsDir: string;
  librariesDir: string;
  nativesDir: string;
  clientJar: string;
  classpathSeparator: string;
  player: { name: string; uuid: string; accessToken: string; xuid: string | null; userType: 'msa' };
  launcherName: string;
  launcherVersion: string;
  memoryMb: { min: number; max: number };
  extraJvmArgs: string[];
  resolution?: { width: number; height: number };
  loggingConfigPath?: string;
}

function expand(args: Argument[] | undefined, env: Env): string[] {
  const out: string[] = [];
  for (const a of args ?? []) {
    if (typeof a === 'string') out.push(a);
    else if (ruleAllows(a.rules, env)) out.push(...(Array.isArray(a.value) ? a.value : [a.value]));
  }
  return out;
}

export function buildClasspath(ctx: LaunchContext): string[] {
  const libs = resolveLibraries(ctx.version, ctx.env).map((l) => posix.join(ctx.librariesDir.replace(/\\/g, '/'), l.path));
  return [...libs, ctx.clientJar];
}

/** Full argv after the java executable. Substitutes ${placeholders}. */
export function buildArguments(ctx: LaunchContext): string[] {
  const v = ctx.version;
  const cp = buildClasspath(ctx).join(ctx.classpathSeparator);
  const vars: Record<string, string> = {
    auth_player_name: ctx.player.name,
    version_name: v.id,
    game_directory: ctx.gameDir,
    assets_root: ctx.assetsDir,
    game_assets: ctx.assetsDir,
    assets_index_name: v.assetIndex?.id ?? v.assets ?? 'legacy',
    auth_uuid: ctx.player.uuid,
    auth_access_token: ctx.player.accessToken,
    auth_session: ctx.player.accessToken,
    clientid: '',
    auth_xuid: ctx.player.xuid ?? '',
    user_type: ctx.player.userType,
    user_properties: '{}',
    version_type: v.type ?? 'release',
    natives_directory: ctx.nativesDir,
    launcher_name: ctx.launcherName,
    launcher_version: ctx.launcherVersion,
    classpath: cp,
    classpath_separator: ctx.classpathSeparator,
    library_directory: ctx.librariesDir,
    resolution_width: String(ctx.resolution?.width ?? 1280),
    resolution_height: String(ctx.resolution?.height ?? 720),
  };
  const sub = (s: string) => s.replace(/\$\{([a-zA-Z_]+)\}/g, (m, k: string) => (k in vars ? vars[k]! : m));

  let jvm = expand(v.arguments?.jvm, ctx.env).map(sub);
  if (!v.arguments?.jvm) {
    jvm = [`-Djava.library.path=${ctx.nativesDir}`, '-cp', cp];
  }
  if (ctx.loggingConfigPath && v.logging?.client) jvm.push(v.logging.client.argument.replace('${path}', ctx.loggingConfigPath));
  const memory = [`-Xms${ctx.memoryMb.min}M`, `-Xmx${ctx.memoryMb.max}M`];
  const game = v.arguments?.game ? expand(v.arguments.game, ctx.env).map(sub) : (v.minecraftArguments ?? '').split(' ').filter(Boolean).map(sub);
  // Drop quick-play / demo flags whose values were not provided.
  const cleaned: string[] = [];
  for (let i = 0; i < game.length; i++) {
    const a = game[i]!;
    if (a.startsWith('--quickPlay') && (game[i + 1] ?? '').includes('${')) { i++; continue; }
    if (a.includes('${')) continue;
    cleaned.push(a);
  }
  return [...memory, ...ctx.extraJvmArgs, ...jvm, v.mainClass, ...cleaned];
}

/** Masks the access token for logs. */
export function redactArgs(args: string[], token: string): string[] {
  return args.map((a) => (token && a.includes(token) ? a.split(token).join('<redacted>') : a));
}

export function currentEnv(platform: NodeJS.Platform, arch: string, release: string): Env {
  const os: OsName = platform === 'win32' ? 'windows' : platform === 'darwin' ? 'osx' : 'linux';
  return { os, arch: arch === 'arm64' ? 'arm64' : arch === 'ia32' ? 'x86' : 'x64', osVersion: release, features: { has_custom_resolution: true } };
}
