// Ported from the NoRisk Client launcher (GPL-3.0): components/modrinth/v2/ModrinthFilterSidebarV2.tsx
// (AccordionItem + FilterOption), fed with the LEGO launcher's filter lists.
import { useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import type { AccentColor } from '../store/theme.ts';

function AccordionItem({ title, count, defaultOpen = false, children }: { title: string; count: number; defaultOpen?: boolean; children: React.ReactNode }) {
  const [isOpen, setIsOpen] = useState(defaultOpen || count > 0);
  return (
    <div className="relative overflow-hidden transition-all duration-300 rounded-lg bg-black/20 border border-white/10 hover:border-white/20">
      <button onClick={() => setIsOpen(!isOpen)} className={cn('w-full px-3 py-2.5 text-left font-smallcaps text-white flex justify-between items-center focus:outline-none transition-colors text-base hover:bg-white/5 relative z-10', isOpen && 'border-b border-white/10')}>
        <div className="flex items-center gap-2"><span className="truncate mr-2">{title}</span>{count > 0 && <span className="text-xs text-white/60 font-minecraft">({count})</span>}</div>
        <Icon icon={isOpen ? 'solar:alt-arrow-up-bold' : 'solar:alt-arrow-down-bold'} className="w-4 h-4 flex-shrink-0" />
      </button>
      {isOpen && <div className="p-2 text-sm relative z-10">{children}</div>}
    </div>
  );
}

function FilterOption({ label, isSelected, onClick, accentColor }: { label: string; isSelected: boolean; onClick: () => void; accentColor: AccentColor }) {
  return (
    <button
      onClick={onClick}
      className={cn('w-full flex items-center justify-between p-1.5 text-sm font-smallcaps transition-colors duration-200 cursor-pointer rounded-md mb-1 bg-black/20 border border-white/10 hover:border-white/20', isSelected ? 'text-white' : 'text-gray-300 hover:text-white')}
      style={{ backgroundColor: isSelected ? `${accentColor.value}20` : undefined, borderColor: isSelected ? `${accentColor.value}60` : undefined }}
    >
      <span className="flex items-center flex-grow text-left"><span className="truncate">{label}</span></span>
      {isSelected && <Icon icon="solar:check-circle-bold" className="w-4 h-4 flex-shrink-0 ml-2" style={{ color: accentColor.value }} />}
    </button>
  );
}

export interface FilterSidebarProps {
  accentColor: AccentColor;
  versions: string[];
  selectedVersions: string[];
  onVersionToggle: (v: string) => void;
  loaders: string[] | null;
  selectedLoaders: string[];
  onLoaderToggle: (l: string) => void;
  categories: string[];
  selectedCategories: string[];
  onCategoryToggle: (c: string) => void;
  env: 'client' | 'server' | null;
  onEnv: ((e: 'client' | 'server' | null) => void) | null;
}

export function ModrinthFilterSidebarV2(p: FilterSidebarProps) {
  const [vq, setVq] = useState('');
  const [showAll, setShowAll] = useState(false);
  const shownVersions = p.versions.filter((v) => !vq || v.includes(vq)).slice(0, showAll || vq ? 200 : 14);
  return (
    <div className="filters-sidebar w-64 flex-shrink-0 overflow-y-auto space-y-2 pr-1" id="filters">
      <AccordionItem title="Game Versions" count={p.selectedVersions.length} defaultOpen>
        <div className="flex items-center gap-2 bg-black/30 rounded-md border border-white/10 px-2 py-1.5 mb-2">
          <Icon icon="solar:magnifer-bold" className="w-3.5 h-3.5 text-white/50" />
          <input value={vq} onChange={(e) => setVq(e.target.value)} placeholder="Version suchen …" className="bg-transparent text-white placeholder-white/50 text-xs flex-1 min-w-0 outline-none" />
        </div>
        {shownVersions.map((v) => <FilterOption key={v} label={v} isSelected={p.selectedVersions.includes(v)} onClick={() => p.onVersionToggle(v)} accentColor={p.accentColor} />)}
        {!vq && p.versions.length > 14 && <button className="w-full text-xs text-white/60 hover:text-white font-minecraft py-1" onClick={() => setShowAll(!showAll)}>{showAll ? 'Weniger zeigen' : 'Alle Versionen zeigen'}</button>}
      </AccordionItem>
      {p.loaders && (
        <AccordionItem title="Loaders" count={p.selectedLoaders.length}>
          {p.loaders.map((l) => <FilterOption key={l} label={l} isSelected={p.selectedLoaders.includes(l)} onClick={() => p.onLoaderToggle(l)} accentColor={p.accentColor} />)}
        </AccordionItem>
      )}
      <AccordionItem title="Categories" count={p.selectedCategories.length}>
        {p.categories.map((c) => <FilterOption key={c} label={c.replace(/-/g, ' ')} isSelected={p.selectedCategories.includes(c)} onClick={() => p.onCategoryToggle(c)} accentColor={p.accentColor} />)}
      </AccordionItem>
      {p.onEnv && (
        <AccordionItem title="Environment" count={p.env ? 1 : 0}>
          {(['client', 'server'] as const).map((e) => <FilterOption key={e} label={e === 'client' ? 'Client' : 'Server'} isSelected={p.env === e} onClick={() => p.onEnv!(p.env === e ? null : e)} accentColor={p.accentColor} />)}
        </AccordionItem>
      )}
    </div>
  );
}
