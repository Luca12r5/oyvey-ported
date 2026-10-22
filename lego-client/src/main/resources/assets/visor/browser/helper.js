// Runs in every frame of every page the headset browser shows (also in embedded frames).
//
// - Links and popups stay in the one visible tab, printing is off.
// - Fullscreen is emulated: the real browser window is parked off screen and real fullscreen
//   would pull it onto the monitor, over the game. A page asking for fullscreen gets its element
//   lifted into the top layer, filling the page, and the fullscreen API reports it as the
//   fullscreen element; the headset then shows the page as a big cinema screen. Embedded frames
//   pass their fullscreen up to the frame element in their parent.
// - The headset reads the state of the page's main video (window.__visor.state()) and controls
//   it, including "VR capture": the raw video is lifted on top of everything, stretched over the
//   whole page, so the page stream carries the full 360° picture for the headset to wrap around
//   the player.
// - The volume of all video and audio is scaled by the headset's browser volume.
(() => {
  const VOLUME = %VOLUME%;
  if (window.__visor) {
    window.__visor.setVolume(VOLUME);
    return;
  }
  const visor = { volume: VOLUME, setVolume(value) { visor.volume = value; } };
  Object.defineProperty(window, '__visor', { value: visor });
  const TOP = window === window.top;
  const define = (target, name, descriptor) => {
    try { Object.defineProperty(target, name, { configurable: true, ...descriptor }); } catch (e) {}
  };

  // ---------------------------------------------------------------------------------------------
  // One tab: links, forms and window.open stay here.

  define(window, 'open', {
    writable: true,
    value: function (url) {
      if (url) { try { location.href = new URL(String(url), location.href).href; } catch (e) {} }
      return null;
    },
  });
  const sameTab = element => {
    if (element && element.target && !/^_(self|top|parent)$/i.test(element.target)) element.target = '_self';
  };
  document.addEventListener('click', event => {
    const link = event.target && event.target.closest ? event.target.closest('a[target]') : null;
    sameTab(link);
  }, true);
  document.addEventListener('submit', event => sameTab(event.target), true);
  try { window.print = function () {}; } catch (e) {}

  // Picture-in-picture would open a real window on the desktop.
  define(HTMLVideoElement.prototype, 'requestPictureInPicture', {
    writable: true,
    value: function () { return Promise.reject(new DOMException('Picture-in-picture is not available', 'NotSupportedError')); },
  });
  define(Document.prototype, 'pictureInPictureEnabled', { get() { return false; } });
  if ('documentPictureInPicture' in window) define(window, 'documentPictureInPicture', { value: undefined });

  // ---------------------------------------------------------------------------------------------
  // Lifting elements into the top layer (fullscreen and VR capture).

  const FS = 'data-visor-fs';
  const VR = 'data-visor-vr';
  const sheet = new CSSStyleSheet();
  sheet.replaceSync(`
video::-webkit-media-controls-fullscreen-button { display: none !important; }
:root[data-visor-fs-root], :root[data-visor-vr-root] { overflow: hidden !important; }
:not(:root)[data-visor-fs] {
  position: fixed !important; inset: 0 !important; margin: 0 !important; box-sizing: border-box !important;
  min-width: 0 !important; max-width: none !important; min-height: 0 !important; max-height: none !important;
  width: 100% !important; height: 100% !important; transform: none !important; translate: none !important;
  scale: none !important; rotate: none !important; z-index: 2147483647 !important; object-fit: contain;
}
:where(:not(:root)[data-visor-fs]) { padding: 0; border: 0; background-color: transparent; color: inherit; overflow: visible; }
iframe[data-visor-fs] { border: none !important; padding: 0 !important; }
[data-visor-fs]::backdrop, [data-visor-vr]::backdrop { background: #000 !important; }
[data-visor-vr] {
  position: fixed !important; left: 0 !important; top: 0 !important; right: auto !important; bottom: auto !important;
  width: 100vw !important; height: 100vh !important; margin: 0 !important; padding: 0 !important; border: 0 !important;
  box-sizing: border-box !important; min-width: 0 !important; max-width: none !important; min-height: 0 !important;
  max-height: none !important; object-fit: fill !important; object-position: 0 0 !important; transform: none !important;
  translate: none !important; scale: none !important; rotate: none !important; display: block !important;
  visibility: visible !important; opacity: 1 !important; filter: none !important; clip-path: none !important;
  mask: none !important; background: #000 !important; z-index: 2147483647 !important; pointer-events: none !important;
}
video[data-visor-vr="top"] { height: 200vh !important; }
video[data-visor-vr="left"] { width: 200vw !important; }
`);
  const adopt = root => {
    try {
      if (root && root.adoptedStyleSheets && !root.adoptedStyleSheets.includes(sheet)) {
        root.adoptedStyleSheets = [...root.adoptedStyleSheets, sheet];
      }
    } catch (e) {}
  };

  // Elements in the top layer, with what they looked like before: element -> { count, popover }.
  const lifted = new Map();
  const isOpen = element => { try { return element.matches(':popover-open'); } catch (e) { return false; } };
  const show = element => {
    try {
      if (!element.hasAttribute('popover')) element.setAttribute('popover', 'manual');
      if (!isOpen(element)) element.showPopover();
    } catch (e) {}
  };
  const lift = (element, attribute, value) => {
    adopt(document);
    const root = element.getRootNode();
    if (root !== document) adopt(root);
    element.setAttribute(attribute, value || '');
    if (element === document.documentElement) return;
    let record = lifted.get(element);
    if (!record) {
      record = { count: 0, popover: element.getAttribute('popover'), open: isOpen(element) };
      lifted.set(element, record);
    }
    record.count++;
    show(element);
  };
  const drop = (element, attribute) => {
    element.removeAttribute(attribute);
    const record = lifted.get(element);
    if (!record || --record.count > 0) return;
    lifted.delete(element);
    if (!record.open) {
      try { element.hidePopover(); } catch (e) {}
      if (record.popover === null) element.removeAttribute('popover');
      else element.setAttribute('popover', record.popover);
    }
  };
  // The most recently shown element of the top layer is drawn on top.
  const raise = element => {
    if (!element || !lifted.has(element)) return;
    try { element.hidePopover(); } catch (e) {}
    show(element);
  };

  // Messages between a frame and its parent, invisible to the page's own message listeners.
  const post = (target, message) => {
    try { target.postMessage({ __visorMessage: true, ...message }, '*'); } catch (e) {}
  };
  const frameOf = source => {
    if (!source) return null;
    for (const frame of document.querySelectorAll('iframe, frame')) {
      try { if (frame.contentWindow === source) return frame; } catch (e) {}
    }
    return null;
  };

  // ---------------------------------------------------------------------------------------------
  // Emulated fullscreen.

  const full = { element: null, prefixed: false, frame: null };
  // The real API, for the safety net below.
  const realElement = (Object.getOwnPropertyDescriptor(Document.prototype, 'fullscreenElement') || {}).get;
  const realExit = Document.prototype.exitFullscreen;

  const retarget = (element, root) => {
    let node = element;
    while (node) {
      const nodeRoot = node.getRootNode();
      if (nodeRoot === root) return node;
      if (!(nodeRoot instanceof ShadowRoot)) return null;
      node = nodeRoot.host;
    }
    return null;
  };
  const fireChange = (element, prefixed) => {
    const target = element && element.isConnected ? element : document;
    try { target.dispatchEvent(new Event(prefixed ? 'webkitfullscreenchange' : 'fullscreenchange', { bubbles: true, composed: true })); } catch (e) {}
    try { window.dispatchEvent(new Event('resize')); } catch (e) {}
  };

  const enterFullscreen = (element, prefixed, frame) => {
    if (full.element === element) return;
    if (full.element) leaveFullscreen(true, false);
    full.element = element;
    full.prefixed = prefixed;
    full.frame = frame || null;
    document.documentElement.setAttribute('data-visor-fs-root', '');
    lift(element, FS);
    raise(capture.element);
    fireChange(element, prefixed);
    if (!TOP) post(parent, { type: 'fullscreen', on: true });
  };

  // tellFrame: also end the fullscreen inside the embedded frame that asked for it.
  const leaveFullscreen = (tellFrame, tellParent) => {
    const element = full.element;
    if (!element) return;
    const frame = full.frame;
    full.element = null;
    full.frame = null;
    drop(element, FS);
    document.documentElement.removeAttribute('data-visor-fs-root');
    fireChange(element, full.prefixed);
    if (frame && tellFrame) post(frame.contentWindow, { type: 'leave-fullscreen' });
    if (!TOP && tellParent) post(parent, { type: 'fullscreen', on: false });
  };

  // Like the real API, the promise settles after the change event, on the next frame.
  const settle = resolve => {
    requestAnimationFrame(() => resolve());
    setTimeout(() => resolve(), 100);
  };
  const request = prefixed => function () {
    const element = this;
    const promise = new Promise((resolve, reject) => {
      if (!(element instanceof Element) || !element.isConnected) {
        reject(new TypeError('Failed to execute \'requestFullscreen\' on \'Element\': the element is not connected'));
        return;
      }
      enterFullscreen(element, prefixed, null);
      settle(resolve);
    });
    if (prefixed) { promise.catch(() => {}); return undefined; }
    return promise;
  };
  define(Element.prototype, 'requestFullscreen', { writable: true, value: request(false) });
  define(Element.prototype, 'webkitRequestFullscreen', { writable: true, value: request(true) });
  define(Element.prototype, 'webkitRequestFullScreen', { writable: true, value: request(true) });
  define(Document.prototype, 'exitFullscreen', {
    writable: true,
    value: function () {
      return new Promise((resolve, reject) => {
        if (!full.element) { reject(new TypeError('Failed to execute \'exitFullscreen\' on \'Document\': Document not active')); return; }
        leaveFullscreen(true, true);
        settle(resolve);
      });
    },
  });
  const leave = function () { leaveFullscreen(true, true); };
  define(Document.prototype, 'webkitExitFullscreen', { writable: true, value: leave });
  define(Document.prototype, 'webkitCancelFullScreen', { writable: true, value: leave });
  const current = { get() { return retarget(full.element, this); } };
  define(Document.prototype, 'fullscreenElement', current);
  define(Document.prototype, 'webkitFullscreenElement', current);
  define(Document.prototype, 'webkitCurrentFullScreenElement', current);
  define(ShadowRoot.prototype, 'fullscreenElement', current);
  define(Document.prototype, 'fullscreen', { get() { return !!full.element; } });
  define(Document.prototype, 'webkitIsFullScreen', { get() { return !!full.element; } });
  define(Document.prototype, 'fullscreenEnabled', { get() { return true; } });
  define(Document.prototype, 'webkitFullscreenEnabled', { get() { return true; } });
  define(HTMLVideoElement.prototype, 'webkitEnterFullscreen', { writable: true, value: function () { enterFullscreen(this, true, null); } });
  define(HTMLVideoElement.prototype, 'webkitEnterFullScreen', { writable: true, value: function () { enterFullscreen(this, true, null); } });
  define(HTMLVideoElement.prototype, 'webkitExitFullscreen', { writable: true, value: function () { if (full.element === this) leave(); } });
  define(HTMLVideoElement.prototype, 'webkitSupportsFullscreen', { get() { return true; } });
  define(HTMLVideoElement.prototype, 'webkitDisplayingFullscreen', { get() { return full.element === this; } });

  // Should anything still reach real fullscreen (a menu of the browser's own video controls), it
  // is ended at once and turned into the emulated one.
  document.addEventListener('fullscreenchange', event => {
    if (!event.isTrusted || !realElement || !realExit) return;
    const element = realElement.call(document);
    if (!element) return;
    try { realExit.call(document).catch(() => {}); } catch (e) {}
    if (element !== document.documentElement || !full.element) enterFullscreen(element, false, null);
  }, true);

  // The browser's own video controls would use real fullscreen: their fullscreen and
  // picture-in-picture buttons go, and a double click toggles the emulated fullscreen instead.
  const prepare = video => {
    adopt(video.getRootNode());
    try {
      video.disablePictureInPicture = true;
      if (video.controlsList) {
        video.controlsList.add('nofullscreen');
        video.controlsList.add('noremoteplayback');
      }
    } catch (e) {}
  };
  document.addEventListener('dblclick', event => {
    const video = event.target;
    if (video instanceof HTMLVideoElement && video.controls) {
      event.preventDefault();
      if (full.element === video) leave(); else enterFullscreen(video, false, null);
    }
  }, true);

  // Pages that don't handle the keyboard in fullscreen themselves still get the usual video keys.
  window.addEventListener('keydown', event => {
    if (!full.element || event.defaultPrevented || event.ctrlKey || event.altKey || event.metaKey) return;
    const target = event.target;
    if (target && (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName))) return;
    const video = full.element instanceof HTMLVideoElement ? full.element : full.element.querySelector && full.element.querySelector('video');
    if (!video) return;
    switch (event.key) {
      case ' ': case 'k': case 'K': if (video.paused) video.play().catch(() => {}); else video.pause(); break;
      case 'ArrowLeft': case 'j': case 'J': seekBy(video, event.key === 'ArrowLeft' ? -5 : -10); break;
      case 'ArrowRight': case 'l': case 'L': seekBy(video, event.key === 'ArrowRight' ? 5 : 10); break;
      case 'm': case 'M': video.muted = !video.muted; break;
      case 'f': case 'F': leave(); break;
      default: return;
    }
    event.preventDefault();
  });

  // ---------------------------------------------------------------------------------------------
  // The page's main video.

  const seekBy = (video, seconds) => {
    if (!isFinite(video.duration)) return;
    video.currentTime = Math.max(0, Math.min(video.duration - 0.1, video.currentTime + seconds));
  };
  const area = video => video.videoWidth * video.videoHeight;
  const bestVideo = () => {
    let best = null;
    let bestScore = 0;
    for (const video of document.querySelectorAll('video')) {
      prepare(video);
      if (video.readyState < 1 || video.videoWidth < 64) continue;
      const score = area(video) + (video.paused ? 0 : 1e9) + (capture.element === video ? 2e9 : 0);
      if (score > bestScore) { best = video; bestScore = score; }
    }
    return best;
  };

  const youtube = video => {
    const player = video.closest && video.closest('.html5-video-player');
    if (!player || typeof player.getPlayerResponse !== 'function') return null;
    const info = { ad: player.classList.contains('ad-showing'), sph: player.classList.contains('ytp-webgl-spherical'), player };
    try {
      const response = player.getPlayerResponse();
      info.id = response && response.videoDetails ? response.videoDetails.videoId : '';
      info.title = response && response.videoDetails ? response.videoDetails.title : '';
      const formats = response && response.streamingData ? response.streamingData.adaptiveFormats || [] : [];
      const format = formats.find(f => f.projectionType && String(f.mimeType || '').startsWith('video'));
      info.projection = format ? format.projectionType : '';
      info.stereo = format && format.stereoLayout ? String(format.stereoLayout) : '';
    } catch (e) {}
    const skip = document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button');
    info.skip = !!(skip && skip.offsetParent !== null);
    return info;
  };

  // VR layout of the video: '360', '360tb' (top-bottom 3D), '180', '180sbs' (side-by-side 3D) or
  // '' for a normal flat video. sure = known from the site (YouTube), not guessed.
  const vrFormat = (video, yt) => {
    if (yt && yt.projection) {
      switch (yt.projection) {
        case 'EQUIRECTANGULAR': return { format: '360', sure: true };
        case 'EQUIRECTANGULAR_THREED_TOP_BOTTOM': return { format: '360tb', sure: true };
        case 'MESH': return { format: /LEFT_RIGHT/i.test(yt.stereo) ? '180sbs' : '180', sure: true };
        default: return { format: '', sure: true };
      }
    }
    const aspect = video.videoWidth / video.videoHeight;
    const text = document.title.toLowerCase();
    const hidden = video.offsetWidth < 2 || video.offsetHeight < 2 || getComputedStyle(video).visibility === 'hidden'
      || getComputedStyle(video).opacity === '0';
    const drawn = hidden && !!document.querySelector('canvas');
    const hinted = /360|vr180|180°|\bvr\b/.test(text);
    let format = '';
    const half = /\b180\b|vr180|180°/.test(text);
    if (Math.abs(aspect - 2) < 0.04) format = half ? '180sbs' : '360';
    else if (Math.abs(aspect - 1) < 0.03) format = half ? '180' : '360tb';
    return { format, sure: false, likely: !!format && (drawn || hinted) };
  };

  const describe = video => {
    if (!video) return null;
    const yt = youtube(video);
    const guess = vrFormat(video, yt);
    return {
      w: video.videoWidth, h: video.videoHeight,
      t: Math.round(video.currentTime * 10) / 10,
      d: isFinite(video.duration) ? Math.round(video.duration * 10) / 10 : 0,
      p: video.paused ? 1 : 0, m: video.muted ? 1 : 0, e: video.ended ? 1 : 0,
      f: guess.format, sure: guess.sure ? 1 : (guess.likely ? 2 : 0),
      ad: yt && yt.ad ? 1 : 0, skip: yt && yt.skip ? 1 : 0, yt: yt ? 1 : 0,
      id: (yt && yt.id) || (video.currentSrc || '').slice(0, 200),
      title: ((yt && yt.title) || document.title || '').slice(0, 160),
    };
  };

  // Reports of embedded frames that have a video: frame element -> description.
  const reports = new Map();
  let lastReport = '';
  const bestMedia = () => {
    const local = bestVideo();
    let media = local ? describe(local) : null;
    let source = local;
    for (const [frame, report] of reports) {
      if (!frame.isConnected) { reports.delete(frame); continue; }
      const score = report.w * report.h + (report.p ? 0 : 1e9) + (capture.frame === frame ? 2e9 : 0);
      const ours = media ? media.w * media.h + (media.p ? 0 : 1e9) + (capture.element ? 2e9 : 0) : -1;
      if (score > ours) { media = report; source = frame; }
    }
    return { media, source };
  };
  if (!TOP) {
    // Embedded frames tell their parent about their video twice a second.
    setInterval(() => {
      const { media } = bestMedia();
      const text = media ? JSON.stringify(media) : '';
      if (text !== lastReport) {
        lastReport = text;
        post(parent, { type: 'media', media });
      }
    }, 500);
  }

  // ---------------------------------------------------------------------------------------------
  // VR capture: the raw video, stretched over the whole page, above everything else.

  const capture = { element: null, frame: null, region: 'full' };

  const startCapture = (region, quality) => {
    stopCapture();
    const { media, source } = bestMedia();
    if (!media || !source) return null;
    capture.region = region;
    if (source instanceof HTMLIFrameElement || source instanceof HTMLFrameElement) {
      post(source.contentWindow, { type: 'capture', on: true, region, quality });
      liftFrame(source);
    } else {
      document.documentElement.setAttribute('data-visor-vr-root', '');
      lift(source, VR, region === 'full' ? '' : region);
      capture.element = source;
      const yt = youtube(source);
      if (yt && yt.player && typeof yt.player.setPlaybackQualityRange === 'function') {
        // The sharpest the stream can carry: 1440p for the standard quality, 4K for high.
        try {
          const levels = yt.player.getAvailableQualityLevels();
          const best = quality === 'high' ? ['hd2160', 'hd1440'] : ['hd1440'];
          const pick = best.find(level => levels.includes(level)) || levels.find(level => level !== 'auto' && level !== 'highres' && level !== 'hd2880' && level !== 'hd2160');
          if (pick) yt.player.setPlaybackQualityRange(pick, pick);
        } catch (e) {}
      }
    }
    if (!TOP) post(parent, { type: 'capture-lift', on: true });
    return media;
  };
  const liftFrame = frame => {
    if (capture.frame === frame) return;
    document.documentElement.setAttribute('data-visor-vr-root', '');
    capture.frame = frame;
    lift(frame, VR, '');
  };
  const stopCapture = () => {
    if (capture.element) {
      drop(capture.element, VR);
      capture.element = null;
    }
    if (capture.frame) {
      post(capture.frame.contentWindow, { type: 'capture', on: false });
      drop(capture.frame, VR);
      capture.frame = null;
    }
    document.documentElement.removeAttribute('data-visor-vr-root');
  };

  // ---------------------------------------------------------------------------------------------
  // Commands from the headset for the main video (forwarded to the frame that has it).

  const command = (name, a, b) => {
    const { media, source } = bestMedia();
    if (!media || !source) return false;
    if (!(source instanceof HTMLVideoElement)) {
      post(source.contentWindow, { type: 'command', name, a, b });
      return true;
    }
    const video = source;
    const yt = youtube(video);
    const player = yt && yt.player;
    switch (name) {
      case 'toggle':
        if (video.paused) { if (player && player.playVideo) player.playVideo(); else video.play().catch(() => {}); }
        else if (player && player.pauseVideo) player.pauseVideo(); else video.pause();
        break;
      case 'seek': seekBy(video, a); break;
      case 'seekTo':
        if (isFinite(video.duration)) {
          const time = Math.max(0, Math.min(video.duration - 0.1, a * video.duration));
          if (player && player.seekTo) player.seekTo(time, true); else video.currentTime = time;
        }
        break;
      case 'mute':
        if (player && player.isMuted) { if (player.isMuted()) player.unMute(); else player.mute(); }
        else video.muted = !video.muted;
        break;
      case 'skip': {
        const button = document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button');
        if (button) button.click();
        break;
      }
      case 'look':
        // Spherical sound of YouTube follows its own camera: keep it facing where the player looks.
        if (player && typeof player.setSphericalProperties === 'function' && yt.sph) {
          try { player.setSphericalProperties({ yaw: ((a % 360) + 360) % 360, pitch: b }); } catch (e) {}
        }
        break;
      case 'fullscreen': {
        // YouTube picks its own fullscreen element and layout through its own button.
        const button = player ? document.querySelector('.ytp-fullscreen-button, button.fullscreen-icon') : null;
        if (button) button.click();
        else if (full.element) leaveFullscreen(true, true);
        else enterFullscreen(video, false, null);
        break;
      }
      default: return false;
    }
    return true;
  };

  // ---------------------------------------------------------------------------------------------
  // Messages from parent and embedded frames.

  window.addEventListener('message', event => {
    const data = event.data;
    if (!data || typeof data !== 'object' || data.__visorMessage !== true) return;
    event.stopImmediatePropagation();
    const fromParent = event.source === window.parent && !TOP;
    const frame = fromParent ? null : frameOf(event.source);
    switch (data.type) {
      case 'fullscreen':
        if (!frame) return;
        if (data.on) enterFullscreen(frame, false, frame);
        else if (full.element === frame) leaveFullscreen(false, true);
        break;
      case 'leave-fullscreen':
        if (fromParent) leaveFullscreen(true, false);
        break;
      case 'media':
        if (!frame) return;
        if (data.media) reports.set(frame, data.media); else reports.delete(frame);
        break;
      case 'capture':
        if (!fromParent) return;
        if (data.on) startCapture(data.region, data.quality); else stopCapture();
        break;
      case 'capture-lift':
        if (!frame) return;
        if (data.on) liftFrame(frame);
        break;
      case 'command':
        if (fromParent) command(data.name, data.a, data.b);
        break;
      default:
    }
  }, true);

  // ---------------------------------------------------------------------------------------------
  // API for the headset (top frame).

  visor.state = () => {
    const { media } = bestMedia();
    return JSON.stringify({ fs: full.element ? 1 : 0, vr: capture.element || capture.frame ? 1 : 0, m: media });
  };
  visor.exitFullscreen = () => leaveFullscreen(true, true);
  visor.startCapture = (region, quality) => JSON.stringify(startCapture(region || 'full', quality || 'standard'));
  visor.stopCapture = () => stopCapture();
  visor.command = (name, a, b) => command(name, a, b);

  // ---------------------------------------------------------------------------------------------
  // Volume: every video and audio element plays at its own volume times the headset's.

  const descriptor = Object.getOwnPropertyDescriptor(HTMLMediaElement.prototype, 'volume');
  if (descriptor && descriptor.get && descriptor.set) {
    const own = new WeakMap();
    const apply = media => {
      const wanted = own.has(media) ? own.get(media) : 1;
      try { descriptor.set.call(media, Math.max(0, Math.min(1, wanted * visor.volume))); } catch (e) {}
    };
    Object.defineProperty(HTMLMediaElement.prototype, 'volume', {
      configurable: true,
      enumerable: descriptor.enumerable,
      get() { return own.has(this) ? own.get(this) : descriptor.get.call(this); },
      set(value) {
        const number = Number(value);
        if (!(number >= 0 && number <= 1)) { descriptor.set.call(this, value); return; }
        own.set(this, number);
        apply(this);
      },
    });
    visor.setVolume = value => { visor.volume = value; document.querySelectorAll('video, audio').forEach(apply); };
    document.addEventListener('play', event => { if (event.target instanceof HTMLMediaElement) apply(event.target); }, true);
    document.addEventListener('loadedmetadata', event => {
      if (event.target instanceof HTMLMediaElement) apply(event.target);
      if (event.target instanceof HTMLVideoElement) prepare(event.target);
    }, true);
  }
})()
