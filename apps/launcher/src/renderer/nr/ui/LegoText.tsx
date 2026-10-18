// Text built from LEGO bricks (SVG, sharp at any size): every pixel of a 5×7
// letter is a 1×1 brick with a lit top face, a shaded side, a rounded stud and
// a glossy edge. The bricks snap in one after another (CSS animation with
// per-brick delay); a light sweep runs over them on hover.
import { memo, useId, useMemo } from 'react';

const FONT: Record<string, string[]> = {
  A: ['01110', '10001', '10001', '11111', '10001', '10001', '10001'],
  C: ['01111', '10000', '10000', '10000', '10000', '10000', '01111'],
  E: ['11111', '10000', '10000', '11110', '10000', '10000', '11111'],
  G: ['01111', '10000', '10000', '10011', '10001', '10001', '01111'],
  H: ['10001', '10001', '10001', '11111', '10001', '10001', '10001'],
  L: ['10000', '10000', '10000', '10000', '10000', '10000', '11111'],
  N: ['10001', '11001', '10101', '10101', '10011', '10001', '10001'],
  O: ['01110', '10001', '10001', '10001', '10001', '10001', '01110'],
  R: ['11110', '10001', '10001', '11110', '10100', '10010', '10001'],
  U: ['10001', '10001', '10001', '10001', '10001', '10001', '01110'],
  ' ': ['000', '000', '000', '000', '000', '000', '000'],
};

/** Classic brick colours. Any other value is taken as a CSS hex colour (e.g. the accent). */
export const BRICK_COLORS: Record<string, string> = {
  red: '#d01012', yellow: '#f2cd37', blue: '#0055bf', green: '#237841', white: '#e9ecef', grey: '#8a9097', black: '#2b2b2b', orange: '#fe8a18', pink: '#e4659d', purple: '#8a4dd8',
};

function shade(hex: string, f: number): string {
  const m = /^#?([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(hex);
  if (!m) return hex;
  const ch = (v: string) => {
    const n = parseInt(v, 16);
    return Math.round(f >= 0 ? n + (255 - n) * f : n * (1 + f)).toString(16).padStart(2, '0');
  };
  return `#${ch(m[1]!)}${ch(m[2]!)}${ch(m[3]!)}`;
}

interface Brick { x: number; y: number; c: string; top: boolean; side: boolean; i: number }

export interface LegoTextProps {
  text: string;
  /** One colour per letter (cycled): a BRICK_COLORS name or a hex colour. */
  colors?: string[];
  height?: number;
  animate?: boolean;
  className?: string;
}

const S = 10;
const D = 3.4;

export const LegoText = memo(function LegoText({ text, colors = ['red', 'yellow', 'blue', 'green'], height = 32, animate = true, className }: LegoTextProps) {
  const uid = useId().replace(/:/g, '');
  const { bricks, w } = useMemo(() => {
    const out: Brick[] = [];
    let ox = 0;
    let n = 0;
    [...text.toUpperCase()].forEach((ch, li) => {
      const g = FONT[ch] ?? FONT[' ']!;
      const name = colors[li % colors.length] ?? 'red';
      const col = BRICK_COLORS[name] ?? name;
      const filled = (x: number, y: number) => g[y]?.[x] === '1';
      g.forEach((row, y) => [...row].forEach((v, x) => {
        if (v === '1') out.push({ x: ox + x, y, c: col, top: !filled(x, y - 1), side: !filled(x + 1, y), i: n++ });
      }));
      ox += (g[0]?.length ?? 5) + 1;
    });
    return { bricks: out, w: ox - 1 };
  }, [text, colors.join(',')]);

  const vw = w * S + D + 1;
  const vh = 7 * S + D + 1;
  const width = (height * vw) / vh;
  return (
    <svg
      className={`lx-legotext ${animate ? 'lx-build' : ''} ${className ?? ''}`}
      viewBox={`0 0 ${vw} ${vh}`}
      // Inline size: page CSS for icons must not shrink the lettering (it overlapped the next word).
      style={{ width: `${width}px`, height: `${height}px`, minWidth: `${width}px`, flex: 'none' }}
      role="img"
      aria-label={text}
    >
      <defs>
        <linearGradient id={`g${uid}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#fff" stopOpacity=".35" />
          <stop offset=".45" stopColor="#fff" stopOpacity="0" />
          <stop offset="1" stopColor="#000" stopOpacity=".22" />
        </linearGradient>
        <linearGradient id={`s${uid}`} x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stopColor="#fff" stopOpacity="0" />
          <stop offset=".5" stopColor="#fff" stopOpacity=".55" />
          <stop offset="1" stopColor="#fff" stopOpacity="0" />
        </linearGradient>
        <clipPath id={`c${uid}`}>
          {bricks.map((b) => <rect key={b.i} x={b.x * S} y={b.y * S + D - D} width={S + D} height={S + D} />)}
        </clipPath>
      </defs>
      {bricks.map((b) => {
        const x = b.x * S, y = b.y * S + D;
        const top = shade(b.c, 0.28), side = shade(b.c, -0.38), edge = shade(b.c, -0.55);
        return (
          <g key={b.i} className="lx-brick-cell" style={{ animationDelay: `${(b.i * 11) % 900}ms` }}>
            {b.side && <polygon points={`${x + S},${y} ${x + S + D},${y - D} ${x + S + D},${y + S - D} ${x + S},${y + S}`} fill={side} />}
            {b.top && <polygon points={`${x},${y} ${x + D},${y - D} ${x + S + D},${y - D} ${x + S},${y}`} fill={top} />}
            <rect x={x} y={y} width={S} height={S} rx={1.2} fill={b.c} stroke={edge} strokeWidth="0.6" />
            <rect x={x} y={y} width={S} height={S} rx={1.2} fill={`url(#g${uid})`} />
            {b.top && (
              <>
                <ellipse cx={x + S / 2 + D / 2} cy={y - D / 2 + 0.2} rx={3.1} ry={1.25} fill={side} />
                <ellipse cx={x + S / 2 + D / 2} cy={y - D / 2 - 0.5} rx={3.1} ry={1.25} fill={top} />
                <ellipse cx={x + S / 2 + D / 2 - 1} cy={y - D / 2 - 0.8} rx={1.2} ry={0.45} fill="#fff" opacity=".6" />
              </>
            )}
          </g>
        );
      })}
      <g clipPath={`url(#c${uid})`} pointerEvents="none">
        <rect className="lx-sweep" x={-vw * 0.5} y="0" width={vw * 0.5} height={vh} fill={`url(#s${uid})`} />
      </g>
    </svg>
  );
});
