// From the NoRisk Client launcher (GPL-3.0): class-name helper used by every component.
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}

/** `#rrggbb` → `r, g, b` (used for rgba() strings in inline styles). */
export function rgbOf(hex: string): { r: number; g: number; b: number } {
  const m = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return m ? { r: parseInt(m[1]!, 16), g: parseInt(m[2]!, 16), b: parseInt(m[3]!, 16) } : { r: 79, g: 142, b: 255 };
}

export function rgba(hex: string, a: number): string {
  const { r, g, b } = rgbOf(hex);
  return `rgba(${r}, ${g}, ${b}, ${a})`;
}
