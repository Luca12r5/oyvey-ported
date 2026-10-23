// Crash doctor: reads a Minecraft log / crash report, names the cause in plain
// words and proposes a fix the launcher can apply (and undo) by itself. Works
// offline with known patterns of Fabric, Mixin, the JVM and the GPU drivers;
// no API key, nothing leaves the PC.

export interface ModInfo { filename: string; id?: string | null; title?: string | null; enabled: boolean }

export type Fix =
  | { kind: 'disable-mod'; filename: string }
  | { kind: 'install-mod'; slug: string }
  | { kind: 'set-memory'; mb: number }
  | { kind: 'redownload' }
  | { kind: 'reauth' };

export interface Diagnosis {
  id: string;
  title: string;
  detail: string;
  /** null = nothing the launcher can change (e.g. GPU driver). */
  fix: Fix | null;
  fixLabel?: string;
  confidence: 'high' | 'medium';
}

/** Dependencies that can be installed from Modrinth by their mod id. */
const KNOWN_DEPS: Record<string, string> = {
  'fabric-api': 'fabric-api', fabric: 'fabric-api', 'fabric-language-kotlin': 'fabric-language-kotlin', 'cloth-config': 'cloth-config', 'cloth-config2': 'cloth-config',
  architectury: 'architectury-api', 'modmenu': 'modmenu', 'yet_another_config_lib_v3': 'yacl', yacl: 'yacl', 'geckolib': 'geckolib', 'owo': 'owo-lib', 'sodium': 'sodium', 'iris': 'iris', 'indium': 'indium', 'balm-fabric': 'balm', 'forgeconfigapiport': 'forge-config-api-port', 'puzzleslib': 'puzzles-lib', 'collective': 'collective', 'resourcefullib': 'resourceful-lib', 'trinkets': 'trinkets', 'lithium': 'lithium',
};

function modFor(mods: ModInfo[], idOrName: string): ModInfo | undefined {
  const k = idOrName.toLowerCase().replace(/[^a-z0-9]/g, '');
  const norm = (s: string | null | undefined) => (s ?? '').toLowerCase().replace(/[^a-z0-9]/g, '');
  return mods.find((m) => m.enabled && (norm(m.id) === k || norm(m.title) === k))
    ?? mods.find((m) => m.enabled && norm(m.filename.replace(/\.jar$/, '')).startsWith(k) && k.length >= 3);
}

export function analyzeLog(text: string, ctx: { mods: ModInfo[]; memoryMb: number }): Diagnosis[] {
  const out: Diagnosis[] = [];
  const seen = new Set<string>();
  const add = (d: Diagnosis) => { if (!seen.has(d.id)) { seen.add(d.id); out.push(d); } };

  // 1) Fabric dependency resolution.
  const depRe = /Mod '([^']+)' \(([\w.-]+)\) [^\s]+ requires (?:version [^\s]+(?: or later)? of|any version of|version [^\s]+ of) (?:mod )?'?([\w.-]+)'?(?: \([\w.-]+\))?(?:[^,\n]*?, which is missing)?/g;
  for (const m of text.matchAll(depRe)) {
    const [, name, id, dep] = m as unknown as [string, string, string, string];
    const missing = /which is missing/.test(m[0]);
    const slug = KNOWN_DEPS[dep.toLowerCase()];
    if (dep === 'minecraft' || dep === 'fabricloader' || dep === 'java') {
      const mod = modFor(ctx.mods, id);
      add({ id: `ver-${id}`, title: `${name} passt nicht zu dieser Version`, detail: `${name} braucht eine andere ${dep === 'minecraft' ? 'Minecraft' : dep === 'java' ? 'Java' : 'Fabric-Loader'}-Version.`, fix: mod ? { kind: 'disable-mod', filename: mod.filename } : null, fixLabel: mod ? `${name} deaktivieren` : undefined, confidence: 'high' });
    } else if (missing && slug) {
      add({ id: `dep-${dep}`, title: `${dep} fehlt`, detail: `${name} braucht den Mod „${dep}“, der nicht installiert ist.`, fix: { kind: 'install-mod', slug }, fixLabel: `${dep} installieren`, confidence: 'high' });
    } else {
      const mod = modFor(ctx.mods, id);
      add({ id: `dep-${id}-${dep}`, title: `${name}: Abhängigkeit „${dep}“ fehlt oder ist zu alt`, detail: 'Ohne diese Abhängigkeit startet Fabric nicht.', fix: mod ? { kind: 'disable-mod', filename: mod.filename } : null, fixLabel: mod ? `${name} deaktivieren` : undefined, confidence: 'high' });
    }
  }
  const incompat = /Mod '([^']+)' \(([\w.-]+)\)[^\n]*?is incompatible with/g;
  for (const m of text.matchAll(incompat)) {
    const mod = modFor(ctx.mods, m[2]!);
    add({ id: `inc-${m[2]}`, title: `${m[1]} ist inkompatibel`, detail: 'Fabric meldet, dass dieser Mod mit einem anderen nicht zusammen läuft.', fix: mod ? { kind: 'disable-mod', filename: mod.filename } : null, fixLabel: mod ? `${m[1]} deaktivieren` : undefined, confidence: 'high' });
  }

  // 2) Duplicate mods.
  const dup = /(?:Duplicate(?:, conflicting)? mods? found|found multiple times|Mod ID `?([\w.-]+)`? (?:is )?(?:provided|found) (?:multiple times|by multiple))/i.exec(text);
  if (dup) {
    const byId = new Map<string, ModInfo[]>();
    for (const m of ctx.mods.filter((x) => x.enabled)) {
      const k = (m.id ?? m.filename.replace(/[-_ ]?v?\d.*$/, '')).toLowerCase();
      byId.set(k, [...(byId.get(k) ?? []), m]);
    }
    const twice = [...byId.values()].find((l) => l.length > 1);
    const victim = twice ? [...twice].sort((a, b) => a.filename.localeCompare(b.filename))[0] : undefined;
    add({ id: 'dup', title: 'Ein Mod ist doppelt installiert', detail: victim ? `„${victim.title ?? victim.filename}“ liegt mehrfach im Mods-Ordner.` : 'Ein Mod liegt in zwei Versionen im Mods-Ordner.', fix: victim ? { kind: 'disable-mod', filename: victim.filename } : null, fixLabel: victim ? 'Ältere Kopie deaktivieren' : undefined, confidence: victim ? 'high' : 'medium' });
  }

  // 3) Mixin failures name the mod in the config name or in the error text.
  const mixin = /(?:Mixin apply for mod ([\w.-]+) failed|from mod ([\w.-]+)\]|mixins?\.([\w-]+)\.json[^\n]*(?:failed|InvalidInjectionException|MixinApplyError)|Critical injection failure[^\n]*?([\w-]+)\.mixins\.json)/i.exec(text);
  if (mixin || /MixinApplyError|InvalidInjectionException|InjectionError/.test(text)) {
    const id = mixin ? (mixin[1] ?? mixin[2] ?? mixin[3] ?? mixin[4])! : '';
    const mod = id ? modFor(ctx.mods, id) : undefined;
    if (!(mod?.filename.toLowerCase().includes('lego'))) {
      add({ id: `mixin-${id}`, title: mod ? `${mod.title ?? id} ist mit dieser Version nicht kompatibel` : 'Ein Mod konnte nicht geladen werden', detail: 'Ein Mod verändert Minecraft-Code, der in dieser Version anders aussieht (Mixin-Fehler).', fix: mod ? { kind: 'disable-mod', filename: mod.filename } : null, fixLabel: mod ? `${mod.title ?? id} deaktivieren` : undefined, confidence: mod ? 'high' : 'medium' });
    }
  }

  // 4) Wrong Java (class file version 65 = Java 21 …).
  const cv = /compiled by a more recent version of the Java Runtime \(class file version (\d+)/.exec(text);
  if (cv || /UnsupportedClassVersionError/.test(text)) {
    const need = cv ? Number(cv[1]) - 44 : null;
    add({ id: 'java', title: 'Falsche Java-Version', detail: `${need ? `Ein Mod braucht Java ${need}` : 'Ein Mod braucht eine neuere Java-Version'} – er ist für eine neuere Minecraft-Version gebaut. Der Launcher nimmt automatisch das Java, das zu deiner Minecraft-Version passt; entferne zuletzt hinzugefügte Mods oder nimm deren passende Version.`, fix: null, confidence: 'high' });
  }

  // 5) Out of memory.
  if (/java\.lang\.OutOfMemoryError|Could not reserve enough space for object heap/.test(text)) {
    const reserve = /Could not reserve enough space/.test(text);
    const mb = reserve ? Math.max(2048, Math.round((ctx.memoryMb * 0.6) / 512) * 512) : Math.min(12288, Math.ceil((ctx.memoryMb * 1.5) / 512) * 512);
    add({ id: 'mem', title: reserve ? 'Zu viel Arbeitsspeicher eingestellt' : 'Zu wenig Arbeitsspeicher', detail: reserve ? 'Java bekommt den eingestellten RAM nicht – der PC hat nicht so viel frei.' : 'Minecraft ist der Speicher ausgegangen.', fix: { kind: 'set-memory', mb }, fixLabel: `RAM auf ${mb} MB setzen`, confidence: 'high' });
  }

  // 6) Graphics driver.
  if (/GLFW error 6554[23]|WGL: The driver does not appear to support OpenGL|Pixel format not accelerated|No OpenGL context|EXCEPTION_ACCESS_VIOLATION[\s\S]{0,400}(atio6axx|nvoglv|ig\d+icd|ig7icd)/i.test(text)) {
    add({ id: 'gpu', title: 'Problem mit dem Grafiktreiber', detail: 'OpenGL konnte nicht gestartet werden. Aktualisiere den Grafiktreiber (NVIDIA, AMD oder Intel) und starte den PC neu. Laptops: Minecraft auf die starke Grafikkarte stellen.', fix: null, confidence: 'high' });
  }

  // 7) Broken files.
  const zip = /(?:ZipException|invalid LOC header|zip END header not found|Error in opening zip file|Failed to read mod file)[^\n]*?([\w.+-]+\.jar)?/i.exec(text);
  if (zip) {
    const mod = zip[1] ? ctx.mods.find((m) => m.filename === zip[1] || m.filename === `${zip[1]}.disabled`) : undefined;
    add({ id: 'zip', title: 'Eine Datei ist beschädigt', detail: mod ? `Die Datei ${mod.filename} ist kaputt (unvollständiger Download).` : 'Eine Spiel- oder Mod-Datei ist kaputt (unvollständiger Download).', fix: mod ? { kind: 'disable-mod', filename: mod.filename } : { kind: 'redownload' }, fixLabel: mod ? 'Kaputten Mod deaktivieren' : 'Dateien prüfen und neu laden', confidence: 'medium' });
  }

  // 8) Session.
  if (/Invalid session|Failed to login: Invalid session|401 Unauthorized.*session/i.test(text)) {
    add({ id: 'auth', title: 'Anmeldung abgelaufen', detail: 'Die Minecraft-Sitzung ist nicht mehr gültig.', fix: { kind: 'reauth' }, fixLabel: 'Neu anmelden', confidence: 'high' });
  }

  // 9) Vivecraft: the VR runtime did not start (SteamVR / OpenXR / Quest Link).
  if (/org\.vivecraft|Vivecraft/.test(text) && /(VR ?Init(?:ialization)?(?:Error| failed)|Failed to (?:initialize|init) (?:OpenVR|OpenXR|VR)|openvr_api|XR_ERROR_[A-Z_]+|No HMD|HMD not found|VRInitError_\w+)/i.test(text)) {
    add({ id: 'vr-runtime', title: 'VR konnte nicht gestartet werden', detail: 'Vivecraft findet keine laufende VR-Brille. Starte zuerst SteamVR (Quest: erst Quest Link oder Air Link verbinden, dann SteamVR), setze die Brille auf und starte dann Minecraft. In SteamVR unter Einstellungen → OpenXR SteamVR als OpenXR-Runtime festlegen.', fix: null, confidence: 'high' });
  }

  // 10) Fallback: a mod jar that shows up in the crashing stack trace.
  if (!out.length) {
    const jar = /at [\w.$]+\([^)]*\) ~\[([\w.+-]+\.jar)/.exec(text) ?? /Suspected Mods?: ([^\n(]+)/.exec(text);
    if (jar) {
      const mod = ctx.mods.find((m) => m.filename === jar[1]) ?? modFor(ctx.mods, jar[1]!.trim());
      if (mod && !mod.filename.toLowerCase().includes('lego')) {
        add({ id: `stack-${mod.filename}`, title: `Vermutlich verursacht durch ${mod.title ?? mod.filename}`, detail: 'Dieser Mod steht ganz oben im Fehlerbericht.', fix: { kind: 'disable-mod', filename: mod.filename }, fixLabel: 'Mod testweise deaktivieren', confidence: 'medium' });
      }
    }
  }
  return out;
}

/**
 * Excerpt of the crash for the AI check: the crash report and the end of the log, at most
 * ~60 000 characters, with personal data removed (user folder names, tokens, e-mails, IPs).
 */
export function crashExcerpt(report: string, log: string, max = 58_000): string {
  const clean = (s: string) => s
    .replace(/([A-Za-z]:\\(?:Users|Dokumente und Einstellungen|Documents and Settings)\\)[^\\"'\n]+/gi, '$1<user>')
    .replace(/(\/(?:home|Users)\/)[^/\s"']+/g, '$1<user>')
    .replace(/(--accessToken|--uuid|--xuid|--clientId|accessToken|session(?:Id)?)([=:\s]+)[^\s,;"']+/gi, '$1$2<hidden>')
    .replace(/\b[\w.+-]+@[\w-]+\.[\w.-]+\b/g, '<email>')
    .replace(/\b(?:\d{1,3}\.){3}\d{1,3}\b/g, (ip) => (/^(127|0)\./.test(ip) ? ip : '<ip>'))
    .replace(/\beyJ[\w-]{10,}\.[\w-]{10,}\.[\w-]{10,}\b/g, '<token>');
  const r = clean(report).slice(0, Math.round(max * 0.55));
  const rest = Math.max(4000, max - r.length);
  const l = clean(log);
  return (r ? `${r}\n----- latest.log (end) -----\n` : '') + l.slice(-rest);
}
