import { api, fmtDate, html, mount, raw, state } from '../core.js';
import { pixel } from '../icons.js';

const RELEASES = 'https://github.com/Luca12r5/oyvey-ported/releases/latest';

export async function home(el) {
  const [catalog, news] = await Promise.all([api('GET', '/api/public/catalog'), api('GET', '/api/public/news').catch(() => ({ news: [] }))]);
  const cosmetics = catalog.items.filter((i) => i.kind === 'cosmetic').length;
  mount(el, html`
    <section class="hero">
      <div>
        <span class="badge">Minecraft 1.8 bis neueste · Fabric · Quilt · Forge · NeoForge</span>
        <h1>Dein Minecraft. Dein Launcher.</h1>
        <p class="lead">LEGO Launcher startet Minecraft mit dem LEGO Client: HUD, Performance-Werkzeuge, ${cosmetics} 3D-Cosmetics, Pets, Fahrzeuge, Emotes, Minispiele, Freunde und ein eigenes Konto mit Coins und Battle Pass.</p>
        <div class="row"><a class="btn primary" href="/download" data-link>Herunterladen</a><a class="btn" href="/features" data-link>Was ist drin?</a></div>
      </div>
      <figure class="art"><div class="glow" aria-hidden="true"></div><img src="/assets/shot-play.webp" alt="Startbildschirm des LEGO Launchers" width="1280" height="800"></figure>
    </section>
    <section class="grid cols-4">
      <div class="card"><div class="kpi">${catalog.items.length}</div><div class="muted">Cosmetics &amp; Name Tags</div></div>
      <div class="card"><div class="kpi">101</div><div class="muted">Launcher-Designs</div></div>
      <div class="card"><div class="kpi">${catalog.nameTags.length}</div><div class="muted">Name-Tag-Stile</div></div>
      <div class="card"><div class="kpi">15</div><div class="muted">Minispiele im Client</div></div>
    </section>
    <h2 class="spaced">Neuigkeiten</h2>
    ${news.news.length ? html`<div class="grid cols-2">${news.news.slice(0, 4).map((n) => html`<article class="card"><div class="muted small">${fmtDate(n.created_at)} · ${n.kind}</div><h3>${n.title}</h3><p>${n.body.slice(0, 220)}${n.body.length > 220 ? '…' : ''}</p></article>`)}</div>` : html`<div class="empty">Noch keine News.</div>`}
  `);
}

const FEATURES = [
  ['rocket', 'Eigener Launcher', 'Microsoft-Anmeldung über den offiziellen Login, Versions- und Profilverwaltung, Fabric-Installation, Logs und Absturzberichte.'],
  ['flame', 'Performance', 'Entity-Culling, Partikel-Limit, Hintergrund-FPS, Leistungsmonitor. Messbar, ohne Registry-Tweaks oder Systemeingriffe.'],
  ['package', 'Module & HUD', 'Rund 85 Client-Module mit Einstellungen, Tastenbelegung und HUD-Editor – nur faire Funktionen, keine Cheats.'],
  ['cape', 'Cosmetics', '211 eigene 3D-Cosmetics (Capes, Flügel, Hüte, Rucksäcke, Auren, Pets, Fahrzeuge) plus 100 Name-Tag-Stile.'],
  ['heart', 'Freunde & Chat', 'Freundschaftsanfragen, Online-Status mit Datenschutz, private Nachrichten, Partys mit Party-Chat.'],
  ['creeper', 'Minispiele', 'Tetris, Snake, 2048, Asteroids, Sudoku, Solar Smash und mehr direkt im Client.'],
  ['trophy', 'Battle Pass & Quests', 'Tägliche Belohnungen, tägliche und wöchentliche Quests, kostenlose und Premium-Stufen.'],
  ['lock', 'Sicher', 'Dein Minecraft-Token verlässt nie deinen PC: Die Anmeldung beim LEGO-Server nutzt dasselbe Verfahren wie Minecraft-Server.'],
];

export async function features(el) {
  mount(el, html`<h1>Features</h1>
    <p class="muted">Was heute funktioniert. Geplante Funktionen stehen in der Roadmap im Repository.</p>
    <div class="grid cols-3">${FEATURES.map(([i, t, d]) => html`<div class="card"><div class="feature-icon">${pixel(i, 'md')}</div><h3>${t}</h3><p class="muted">${d}</p></div>`)}</div>`);
}

export async function download(el) {
  mount(el, html`<h1>Download</h1>
    <div class="grid cols-2">
      <div class="card stack"><h2>Windows</h2><p>Installer (NSIS) für Windows 10/11, 64-Bit. Der Launcher prüft Updates selbst und verifiziert jede Datei per SHA-512.</p>
        <a class="btn primary" href="${RELEASES}" rel="noopener">Zu den Releases</a>
        <p class="muted small">Ein Microsoft-Konto mit Minecraft: Java Edition wird benötigt. Ohne Code-Signing-Zertifikat zeigt Windows SmartScreen eine Warnung an.</p></div>
      <div class="card stack"><h2>Nur der Client</h2><p>Du nutzt schon einen anderen Launcher? Lade die Mod-Datei <code>legoclient-*.jar</code> und lege sie mit Fabric API in deinen <code>mods</code>-Ordner (Minecraft 1.21.11, Fabric Loader ≥ 0.16).</p>
        <a class="btn" href="${RELEASES}" rel="noopener">Mod herunterladen</a></div>
    </div>`);
}

export async function news(el) {
  const data = await api('GET', '/api/public/news?kind=news');
  mount(el, html`<h1>News</h1>${data.news.length ? html`<div class="stack">${data.news.map((n) => html`<article class="card"><div class="muted small">${fmtDate(n.created_at)}${n.author ? ` · ${n.author}` : ''}</div><h2>${n.title}</h2><p class="prewrap">${n.body}</p></article>`)}</div>` : html`<div class="empty">Noch keine News.</div>`}
    <p><a href="/changelog" data-link>Changelog ansehen →</a></p>`);
}

export async function changelog(el) {
  const data = await api('GET', '/api/public/news?kind=changelog');
  mount(el, html`<h1>Changelog</h1>${data.news.length ? html`<div class="stack">${data.news.map((n) => html`<article class="card"><div class="muted small">${fmtDate(n.created_at)}</div><h2>${n.title}</h2><p>${n.body}</p></article>`)}</div>` : html`<div class="empty">Noch keine Einträge.</div>`}`);
}

export async function status(el) {
  let s;
  try { s = await api('GET', '/api/public/status'); } catch { s = null; }
  mount(el, html`<h1>Status</h1><div class="card stack">
    <div class="row"><span class="dot ${s?.ok ? 'online' : ''}"></span><b>API</b><span class="muted">${s?.ok ? 'erreichbar' : 'nicht erreichbar'}</span></div>
    <div class="row"><span class="dot ${s && !s.maintenance ? 'online' : 'away'}"></span><b>Wartungsmodus</b><span class="muted">${s?.maintenance ? 'aktiv' : 'aus'}</span></div>
    <div class="row"><span class="dot ${s?.paymentsEnabled ? 'online' : ''}"></span><b>Zahlungen</b><span class="muted">${s?.paymentsEnabled ? 'aktiv' : 'nicht konfiguriert'}</span></div>
    <div class="muted small">API-Version ${s?.apiVersion ?? '–'} · Serverzeit ${fmtDate(s?.time)}</div></div>`);
}

export async function leaderboards(el) {
  const games = [['tennis', 'Tennis'], ['tetris', 'Tetris'], ['snake', 'Snake'], ['g2048', '2048'], ['asteroids', 'Asteroids'], ['flappy', 'Flappy'], ['breakout', 'Breakout'], ['reaction', 'Reaktion'], ['aimtrainer', 'Aim Trainer'], ['cpstest', 'CPS-Test']];
  mount(el, html`<h1>Ranglisten</h1><div class="tabs" id="tabs">${games.map(([id, n], i) => html`<button data-game="${id}" class="${i === 0 ? 'active' : ''}">${n}</button>`)}</div><div id="board"></div>
    <p class="muted small">Ergebnisse werden vom Client gemeldet. Nur Spieler mit öffentlichem Profil erscheinen.</p>`);
  const load = async (game) => {
    const d = await api('GET', `/api/public/leaderboard/${game}`);
    mount(el.querySelector('#board'), d.entries.length ? html`<div class="card table-wrap"><table><thead><tr><th>#</th><th>Spieler</th><th>Bestwert</th><th>Siege</th></tr></thead><tbody>${d.entries.map((e, i) => html`<tr><td>${i + 1}</td><td><a href="/u/${e.name}" data-link>${e.name}</a></td><td>${e.score}</td><td>${e.wins}</td></tr>`)}</tbody></table></div>` : html`<div class="empty">Noch keine Ergebnisse.</div>`);
  };
  el.querySelector('#tabs').addEventListener('click', (e) => {
    const b = e.target.closest('button');
    if (!b) return;
    el.querySelectorAll('#tabs button').forEach((x) => x.classList.toggle('active', x === b));
    void load(b.dataset.game);
  });
  await load(games[0][0]);
}

export async function support(el) {
  mount(el, html`<h1>Support</h1><div class="grid cols-2">
    <div class="card prose"><h2>Häufige Fragen</h2>
      <h3>Der Login schlägt fehl</h3><p>Du brauchst ein Microsoft-Konto, das Minecraft: Java Edition besitzt. Der Launcher nutzt den offiziellen Microsoft-Anmeldedialog.</p>
      <h3>Wie melde ich mich auf der Website an?</h3><p>Öffne im Launcher <i>Profil → Website-Anmeldung</i> und gib den 8-stelligen Code hier unter <a href="/login" data-link>Anmelden</a> ein.</p>
      <h3>Sehen andere meine Cosmetics?</h3><p>Ja, wenn sie den LEGO Client nutzen und mit dem LEGO-Server verbunden sind. Spieler anderer Clients sehen sie nicht.</p>
      <h3>Wird mein Spiel schneller?</h3><p>Die Performance-Module reduzieren Rechenarbeit (z. B. unsichtbare Entities). Wie viel das bringt, hängt von Hardware und Welt ab – miss es mit dem Leistungsmonitor.</p></div>
    <div class="card stack"><h2>Ticket erstellen</h2><p>Für Fehler, Abstürze oder Kontoprobleme. Logs und Screenshots kannst du anhängen.</p>
      ${state.user ? html`<a class="btn primary" href="/feedback/new?kind=support" data-link>Ticket erstellen</a>` : html`<a class="btn primary" href="/login" data-link>Anmelden, um ein Ticket zu erstellen</a>`}</div></div>`);
}

const LEGAL = {
  imprint: ['Impressum', raw(`<p><b>Platzhalter:</b> Vor dem Veröffentlichen müssen hier die Angaben des Betreibers stehen (Name, Anschrift, Kontakt; in Deutschland gemäß § 5 DDG).</p>`)],
  privacy: ['Datenschutzerklärung', raw(`<p>Diese Seite beschreibt, welche Daten der LEGO-Server tatsächlich speichert:</p><ul>
    <li>Minecraft-UUID und -Name (aus der Anmeldung über den Mojang-Sessionserver). Passwörter und Microsoft-/Minecraft-Tokens werden <b>nicht</b> an den LEGO-Server übertragen.</li>
    <li>Sitzungen (nur als SHA-256-Hash), Zeitpunkt der letzten Nutzung und Browser-/Launcher-Kennung.</li>
    <li>Freundeslisten, Blockierungen, private Nachrichten, Party-Chat, Meldungen.</li>
    <li>Coins-Buchungen, gekaufte und ausgerüstete Gegenstände, Fortschritt, Spielergebnisse.</li>
    <li>Feedback und freiwillig angehängte Dateien.</li>
    <li>Bei Käufen: Ereignis-ID und Betrag vom Zahlungsanbieter. Kartendaten verarbeitet ausschließlich der Zahlungsanbieter.</li></ul>
    <p>Wer deinen Status, dein Profil und deine Aktivität sieht, stellst du unter <a href="/account" data-link>Konto → Datenschutz</a> ein.</p>
    <p><b>Platzhalter:</b> Verantwortlicher, Rechtsgrundlagen, Speicherdauer, Betroffenenrechte und Kontakt müssen vom Betreiber ergänzt werden.</p>`)],
  terms: ['Nutzungsbedingungen', raw(`<p><b>Platzhalter:</b> Nutzungsbedingungen, Regeln für Käufe virtueller Güter (Widerrufsrecht, Altersfreigaben, Elternzustimmung) und Verhaltensregeln müssen vom Betreiber festgelegt und rechtlich geprüft werden.</p>
    <p>LEGO Launcher ist ein Fanprojekt und steht in keiner Verbindung zu Mojang Studios, Microsoft oder der LEGO Group.</p>`)],
};

export async function legal(el, which) {
  const [title, body] = LEGAL[which];
  mount(el, html`<h1>${title}</h1><div class="card prose">${body}</div>`);
}
