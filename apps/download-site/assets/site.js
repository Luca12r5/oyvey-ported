// Download page: finds the newest launcher installer in the GitHub releases,
// shows version, size and SHA-256 (when GitHub provides an asset digest), and
// draws a light animated brick background.
import { CONFIG } from '../config.js';

const $ = (id) => document.getElementById(id);
const releasesUrl = `https://github.com/${CONFIG.repo}/releases`;

function siteLinks() {
  $('allReleases').href = releasesUrl;
  $('source').href = `https://github.com/${CONFIG.repo}`;
  document.querySelectorAll('[data-site]').forEach((a) => {
    if (!CONFIG.website) return;
    a.href = `${CONFIG.website.replace(/\/$/, '')}/${a.dataset.site}`;
    a.hidden = false;
  });
  if (CONFIG.website) $('faqFeedback').textContent = `Im Launcher unter „Feedback“ oder auf ${CONFIG.website.replace(/^https:\/\//, '')}/feedback.`;
}

async function latestInstaller() {
  const res = await fetch(`https://api.github.com/repos/${CONFIG.repo}/releases?per_page=20`, { headers: { Accept: 'application/vnd.github+json' } });
  if (!res.ok) throw new Error(`GitHub ${res.status}`);
  for (const rel of await res.json()) {
    if (rel.draft || rel.prerelease) continue;
    const asset = rel.assets.find((a) => /^LEGO-Launcher-Setup-.+\.exe$/.test(a.name));
    if (asset) return { rel, asset };
  }
  return null;
}

async function setupDownload() {
  const btn = $('download');
  const meta = $('dlMeta');
  try {
    const found = await latestInstaller();
    if (!found) {
      btn.href = releasesUrl;
      btn.dataset.state = 'none';
      meta.textContent = 'Noch keine Version veröffentlicht – bald verfügbar.';
      return;
    }
    const { rel, asset } = found;
    btn.href = asset.browser_download_url;
    btn.dataset.state = 'ready';
    meta.textContent = `${rel.tag_name.replace(/^launcher-/, '')} · ${(asset.size / 1048576).toFixed(0)} MB · ${new Date(rel.published_at).toLocaleDateString('de-DE')}`;
    if (typeof asset.digest === 'string' && asset.digest.startsWith('sha256:')) {
      $('hashValue').textContent = asset.digest.slice(7);
      $('hash').hidden = false;
    }
  } catch {
    btn.href = releasesUrl;
    btn.dataset.state = 'error';
    meta.textContent = 'Zu den GitHub-Releases';
  }
}

function background() {
  const c = $('bg');
  const ctx = c.getContext('2d');
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const colors = ['#d946ef', '#8b5cf6', '#e3000b', '#f2cd37', '#0055bf'];
  let bricks = [];
  const resize = () => {
    const dpr = Math.min(2, devicePixelRatio || 1);
    c.width = innerWidth * dpr; c.height = innerHeight * dpr;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    bricks = Array.from({ length: Math.round(innerWidth / 70) }, (_, i) => ({ x: Math.random() * innerWidth, y: Math.random() * innerHeight, s: 8 + Math.random() * 14, v: 0.08 + Math.random() * 0.25, r: Math.random() * 6, c: colors[i % colors.length] }));
  };
  const draw = (t) => {
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (const b of bricks) {
      if (!reduce) { b.y -= b.v; if (b.y < -40) { b.y = innerHeight + 40; b.x = Math.random() * innerWidth; } }
      ctx.save(); ctx.translate(b.x, b.y); ctx.rotate(Math.sin(t * 0.0003 + b.r) * 0.5); ctx.globalAlpha = 0.16; ctx.fillStyle = b.c;
      ctx.beginPath(); ctx.roundRect(-b.s, -b.s * 0.6, b.s * 2, b.s * 1.2, b.s * 0.15); ctx.fill();
      ctx.fillRect(-b.s * 0.82, -b.s * 0.86, b.s * 0.6, b.s * 0.3); ctx.fillRect(b.s * 0.22, -b.s * 0.86, b.s * 0.6, b.s * 0.3);
      ctx.restore();
    }
    if (!reduce) requestAnimationFrame(draw);
  };
  addEventListener('resize', resize);
  resize();
  requestAnimationFrame(draw);
}

siteLinks();
void setupDownload();
background();
