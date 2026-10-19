// Line icons for the VR dock and window bar, drawn straight onto a panel canvas
// (so they stay sharp at any panel resolution).

export type VrIcon = 'grid' | 'compass' | 'keyboard' | 'star' | 'gear' | 'recenter' | 'power' | 'wifi' | 'back' | 'forward' | 'reload' | 'home' | 'volume' | 'mute' | 'minimize' | 'expand' | 'close' | 'search' | 'user';

export function drawIcon(c: CanvasRenderingContext2D, name: VrIcon, cx: number, cy: number, size: number, color: string): void {
  const s = size / 24;
  c.save();
  c.translate(cx - 12 * s, cy - 12 * s);
  c.scale(s, s);
  c.strokeStyle = color;
  c.fillStyle = color;
  c.lineWidth = 2;
  c.lineCap = 'round';
  c.lineJoin = 'round';
  const p = new Path2D();
  switch (name) {
    case 'grid':
      for (const [x, y] of [[3, 3], [13, 3], [3, 13], [13, 13]]) p.roundRect(x!, y!, 8, 8, 2);
      c.stroke(p);
      break;
    case 'compass':
      p.arc(12, 12, 9.5, 0, Math.PI * 2);
      c.stroke(p);
      c.beginPath(); c.moveTo(15.5, 8.5); c.lineTo(10.5, 10.5); c.lineTo(8.5, 15.5); c.lineTo(13.5, 13.5); c.closePath(); c.fill();
      break;
    case 'keyboard':
      p.roundRect(2, 6, 20, 12, 2.5);
      c.stroke(p);
      for (let i = 0; i < 5; i++) c.fillRect(5 + i * 3.2, 9, 1.8, 1.8);
      for (let i = 0; i < 4; i++) c.fillRect(6.6 + i * 3.2, 12, 1.8, 1.8);
      c.fillRect(8, 15, 8, 1.6);
      break;
    case 'star': {
      c.beginPath();
      for (let i = 0; i < 10; i++) { const a = -Math.PI / 2 + (i * Math.PI) / 5, r = i % 2 ? 4.2 : 9.5; c.lineTo(12 + Math.cos(a) * r, 12.6 + Math.sin(a) * r); }
      c.closePath();
      c.stroke();
      break;
    }
    case 'gear': {
      c.beginPath();
      for (let i = 0; i < 16; i++) { const a = (i * Math.PI) / 8 + Math.PI / 16, r = i % 2 ? 7 : 9.6; c.lineTo(12 + Math.cos(a) * r, 12 + Math.sin(a) * r); }
      c.closePath();
      c.stroke();
      c.beginPath(); c.arc(12, 12, 3, 0, Math.PI * 2); c.stroke();
      break;
    }
    case 'recenter':
      c.beginPath(); c.arc(12, 12, 8, -Math.PI * 0.35, Math.PI * 1.35); c.stroke();
      c.beginPath(); c.moveTo(17.5, 2.5); c.lineTo(17.4, 6.8); c.lineTo(13.2, 6.6); c.stroke();
      c.beginPath(); c.arc(12, 12, 2, 0, Math.PI * 2); c.fill();
      break;
    case 'power':
      c.beginPath(); c.arc(12, 13, 8, -Math.PI * 0.3, Math.PI * 1.3); c.stroke();
      c.beginPath(); c.moveTo(12, 3); c.lineTo(12, 12); c.stroke();
      break;
    case 'wifi':
      for (const r of [3, 7, 11]) { c.beginPath(); c.arc(12, 20, r, -Math.PI * 0.78, -Math.PI * 0.22); c.stroke(); }
      c.beginPath(); c.arc(12, 20, 1.4, 0, Math.PI * 2); c.fill();
      break;
    case 'back': c.beginPath(); c.moveTo(15, 5); c.lineTo(8, 12); c.lineTo(15, 19); c.stroke(); break;
    case 'forward': c.beginPath(); c.moveTo(9, 5); c.lineTo(16, 12); c.lineTo(9, 19); c.stroke(); break;
    case 'reload':
      c.beginPath(); c.arc(12, 12, 7.5, -Math.PI * 0.15, Math.PI * 1.55); c.stroke();
      c.beginPath(); c.moveTo(19.6, 4.5); c.lineTo(19.4, 9.6); c.lineTo(14.4, 9); c.stroke();
      break;
    case 'home':
      c.beginPath(); c.moveTo(3.5, 11.5); c.lineTo(12, 4); c.lineTo(20.5, 11.5); c.stroke();
      c.beginPath(); c.moveTo(6, 10); c.lineTo(6, 20); c.lineTo(18, 20); c.lineTo(18, 10); c.stroke();
      break;
    case 'volume':
    case 'mute':
      c.beginPath(); c.moveTo(3, 9); c.lineTo(7, 9); c.lineTo(12, 4.5); c.lineTo(12, 19.5); c.lineTo(7, 15); c.lineTo(3, 15); c.closePath(); c.stroke();
      if (name === 'volume') { c.beginPath(); c.arc(13, 12, 4, -0.9, 0.9); c.stroke(); c.beginPath(); c.arc(13, 12, 8, -0.9, 0.9); c.stroke(); }
      else { c.beginPath(); c.moveTo(16, 9); c.lineTo(21, 15); c.moveTo(21, 9); c.lineTo(16, 15); c.stroke(); }
      break;
    case 'minimize': c.beginPath(); c.moveTo(5, 12); c.lineTo(19, 12); c.stroke(); break;
    case 'expand':
      c.beginPath(); c.moveTo(4, 9); c.lineTo(4, 4); c.lineTo(9, 4); c.moveTo(15, 4); c.lineTo(20, 4); c.lineTo(20, 9); c.moveTo(20, 15); c.lineTo(20, 20); c.lineTo(15, 20); c.moveTo(9, 20); c.lineTo(4, 20); c.lineTo(4, 15); c.stroke();
      break;
    case 'close': c.beginPath(); c.moveTo(6, 6); c.lineTo(18, 18); c.moveTo(18, 6); c.lineTo(6, 18); c.stroke(); break;
    case 'search': c.beginPath(); c.arc(10.5, 10.5, 6.5, 0, Math.PI * 2); c.stroke(); c.beginPath(); c.moveTo(15.5, 15.5); c.lineTo(20.5, 20.5); c.stroke(); break;
    case 'user': c.beginPath(); c.arc(12, 8.5, 4, 0, Math.PI * 2); c.stroke(); c.beginPath(); c.arc(12, 22, 8, Math.PI * 1.15, Math.PI * 1.85); c.stroke(); break;
  }
  c.restore();
}
