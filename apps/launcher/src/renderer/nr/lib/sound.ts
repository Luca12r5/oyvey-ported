// Tiny synthesized LEGO "click" (two short filtered noise bursts) for buttons.
// Off when the user disabled UI sounds; never louder than a soft tick.
let ctx: AudioContext | null = null;
let enabled = true;

export function setUiSounds(on: boolean): void { enabled = on; }

export function brickClick(brick: boolean): void {
  if (!enabled) return;
  try {
    ctx ??= new AudioContext();
    const t0 = ctx.currentTime;
    const hit = (at: number, freq: number, gain: number) => {
      const len = Math.floor(ctx!.sampleRate * 0.03);
      const buf = ctx!.createBuffer(1, len, ctx!.sampleRate);
      const d = buf.getChannelData(0);
      for (let i = 0; i < len; i++) d[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / len, 6);
      const src = ctx!.createBufferSource();
      src.buffer = buf;
      const f = ctx!.createBiquadFilter();
      f.type = 'bandpass';
      f.frequency.value = freq;
      f.Q.value = 2.2;
      const g = ctx!.createGain();
      g.gain.value = gain;
      src.connect(f).connect(g).connect(ctx!.destination);
      src.start(t0 + at);
    };
    hit(0, brick ? 2400 : 3400, brick ? 0.22 : 0.08);
    if (brick) hit(0.045, 1700, 0.16);
  } catch { /* audio not available */ }
}

/** Whether UI/emote sounds are on. */
export function uiSoundsOn(): boolean { return enabled; }
