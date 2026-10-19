// Small synthesized sounds for the VR lobby (no audio files): UI ticks and
// clicks, window open/close swooshes, and a calm outdoor ambience (soft wind,
// birds now and then).

export interface Sfx {
  hover(): void;
  click(): void;
  open(): void;
  close(): void;
  /** Starts wind + birds; returns a stopper. */
  ambience(): () => void;
}

export function makeSfx(ctx: AudioContext, out: AudioNode): Sfx {
  const tone = (f0: number, f1: number, dur: number, vol: number, type: OscillatorType = 'sine', at = 0) => {
    try {
      const t = ctx.currentTime + at;
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.type = type;
      o.frequency.setValueAtTime(f0, t);
      o.frequency.exponentialRampToValueAtTime(f1, t + dur);
      g.gain.setValueAtTime(0.0001, t);
      g.gain.exponentialRampToValueAtTime(vol, t + 0.008);
      g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
      o.connect(g).connect(out);
      o.start(t);
      o.stop(t + dur + 0.02);
    } catch { /* audio unavailable */ }
  };
  let noise: AudioBuffer | null = null;
  const noiseBuf = () => {
    if (noise) return noise;
    noise = ctx.createBuffer(1, ctx.sampleRate * 2, ctx.sampleRate);
    const d = noise.getChannelData(0);
    for (let i = 0; i < d.length; i++) d[i] = Math.random() * 2 - 1;
    return noise;
  };
  const swoosh = (f0: number, f1: number, dur: number, vol: number) => {
    try {
      const t = ctx.currentTime;
      const s = ctx.createBufferSource();
      s.buffer = noiseBuf();
      const bp = ctx.createBiquadFilter();
      bp.type = 'bandpass';
      bp.Q.value = 1.4;
      bp.frequency.setValueAtTime(f0, t);
      bp.frequency.exponentialRampToValueAtTime(f1, t + dur);
      const g = ctx.createGain();
      g.gain.setValueAtTime(0.0001, t);
      g.gain.exponentialRampToValueAtTime(vol, t + dur * 0.35);
      g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
      s.connect(bp).connect(g).connect(out);
      s.start(t);
      s.stop(t + dur + 0.05);
    } catch { /* audio unavailable */ }
  };
  let lastHover = 0;
  return {
    hover() { const n = performance.now(); if (n - lastHover < 60) return; lastHover = n; tone(1500, 1700, 0.035, 0.025); },
    click() { tone(900, 600, 0.06, 0.07, 'triangle'); tone(1400, 1300, 0.05, 0.035, 'sine', 0.035); },
    open() { swoosh(400, 2400, 0.32, 0.09); tone(660, 990, 0.18, 0.03, 'sine', 0.08); },
    close() { swoosh(2200, 350, 0.28, 0.07); },
    ambience() {
      let stopped = false;
      let wind: AudioBufferSourceNode | null = null;
      try {
        wind = ctx.createBufferSource();
        wind.buffer = noiseBuf();
        wind.loop = true;
        const lp = ctx.createBiquadFilter();
        lp.type = 'lowpass';
        lp.frequency.value = 380;
        const g = ctx.createGain();
        g.gain.value = 0.035;
        const lfo = ctx.createOscillator(), lg = ctx.createGain();
        lfo.frequency.value = 0.08;
        lg.gain.value = 0.02;
        lfo.connect(lg).connect(g.gain);
        lfo.start();
        wind.connect(lp).connect(g).connect(out);
        wind.start();
      } catch { /* audio unavailable */ }
      // A bird: two to five quick chirps from a random side.
      const bird = () => {
        if (stopped) return;
        try {
          const pan = ctx.createStereoPanner();
          pan.pan.value = Math.random() * 1.6 - 0.8;
          pan.connect(out);
          const base = 2600 + Math.random() * 1600;
          const n = 2 + Math.floor(Math.random() * 4);
          for (let i = 0; i < n; i++) {
            const t = ctx.currentTime + i * (0.11 + Math.random() * 0.05);
            const o = ctx.createOscillator(), g = ctx.createGain();
            o.frequency.setValueAtTime(base, t);
            o.frequency.exponentialRampToValueAtTime(base * (1.25 + Math.random() * 0.3), t + 0.06);
            g.gain.setValueAtTime(0.0001, t);
            g.gain.exponentialRampToValueAtTime(0.022, t + 0.01);
            g.gain.exponentialRampToValueAtTime(0.0001, t + 0.08);
            o.connect(g).connect(pan);
            o.start(t);
            o.stop(t + 0.1);
          }
        } catch { /* audio unavailable */ }
        timer = window.setTimeout(bird, 2500 + Math.random() * 6000);
      };
      let timer = window.setTimeout(bird, 1800);
      return () => { stopped = true; clearTimeout(timer); try { wind?.stop(); } catch { /* already stopped */ } };
    },
  };
}
