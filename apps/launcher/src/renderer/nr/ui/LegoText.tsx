// Text built from LEGO bricks (SVG, sharp at any size): every pixel of a 5×7
// letter is a 1×1 brick with a lit top, a shaded side and a stud. The bricks
// snap in one after another (CSS animation with per-brick delay).
import { memo, useMemo } from 'react';

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

/** Classic brick colours (front, top, side). */
const COLORS: Record<string, [string, string, string]> = {
  red: ['#d01012', '#ff3b30', '#8f0a0b'],
  yellow: ['#f2cd37', '#ffe46b', '#b8961c'],
  blue: ['#0055bf', '#2f7fe6', '#003b85'],
  green: ['#237841', '#3fa363', '#155229'],
  white: ['#e9ecef', '#ffffff', '#a9aeb4'],
  grey: ['#8a9097', '#b3b8bd', '#5b6066'],
};

interface Brick { x: number; y: number; c: [string, string, string]; top: boolean; side: boolean; i: number }

export interface LegoTextProps {
  text: string;
  /** One colour name per letter (cycled). */
  colors?: (keyof typeof COLORS)[];
  height?: number;
  animate?: boolean;
  className?: string;
}

const S = 10;
const D = 3;

export const LegoText = memo(function LegoText({ text, colors = ['red', 'yellow', 'blue', 'green'], height = 32, animate = true, className }: LegoTextProps) {
  const { bricks, w } = useMemo(() => {
    const out: Brick[] = [];
    let ox = 0;
    let n = 0;
    [...text.toUpperCase()].forEach((ch, li) => {
      const g = FONT[ch] ?? FONT[' ']!;
      const col = COLORS[colors[li % colors.length]!] ?? COLORS.red!;
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
  return (
    <svg className={`lx-legotext ${animate ? 'lx-build' : ''} ${className ?? ''}`} viewBox={`0 0 ${vw} ${vh}`} height={height} width={(height * vw) / vh} role="img" aria-label={text}>
      {bricks.map((b) => {
        const x = b.x * S, y = b.y * S + D;
        return (
          <g key={b.i} className="lx-brick-cell" style={{ animationDelay: `${(b.i * 11) % 900}ms` }}>
            <rect x={x} y={y} width={S} height={S} fill={b.c[0]} stroke="rgba(0,0,0,0.35)" strokeWidth="0.5" />
            {b.top && <polygon points={`${x},${y} ${x + D},${y - D} ${x + S + D},${y - D} ${x + S},${y}`} fill={b.c[1]} />}
            {b.side && <polygon points={`${x + S},${y} ${x + S + D},${y - D} ${x + S + D},${y + S - D} ${x + S},${y + S}`} fill={b.c[2]} />}
            {b.top && <ellipse cx={x + S / 2 + D / 2} cy={y - D / 2 - 0.6} rx={2.8} ry={1.1} fill={b.c[1]} stroke={b.c[2]} strokeWidth="0.5" />}
            <rect x={x + 1} y={y + 1} width={S - 2} height={1.2} fill="rgba(255,255,255,0.28)" />
          </g>
        );
      })}
    </svg>
  );
});
