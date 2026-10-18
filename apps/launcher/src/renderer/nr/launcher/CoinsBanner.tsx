// Top-left card on the play screen (where the NoRisk launcher shows its referral
// banner): LEGO coins, the daily reward and the current quest.
import { useEffect, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { useThemeStore } from '../store/theme.ts';
import { lego, errorToast, toast, fmtNum } from '../../ui.ts';
import { pixel } from '../../icons.ts';

interface Progress { daily: { claimedToday: boolean; streak: number; nextReward: number }; quests: { id: string; title: { de: string; en?: string }; period: string; progress: number; target: number; credits: number; claimed: boolean }[] }

export function CoinsBanner({ connected, onChanged }: { connected: boolean; onChanged: () => void }) {
  const accent = useThemeStore((s) => s.accentColor);
  const [data, setData] = useState<{ p: Progress; balance: number; today: number } | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!connected) return;
    let off = false;
    Promise.all([lego<Progress>('GET', '/api/progress'), lego<{ balance: number; history: { delta: number; created_at: number }[] }>('GET', '/api/me/credits')])
      .then(([p, c]) => {
        const dayStart = new Date().setHours(0, 0, 0, 0);
        const today = c.history.filter((h) => h.created_at >= dayStart && h.delta > 0).reduce((n, h) => n + h.delta, 0);
        if (!off) setData({ p, balance: c.balance, today });
      })
      .catch(() => { if (!off) setFailed(true); });
    return () => { off = true; };
  }, [connected]);

  const q = data?.p.quests.find((x) => x.period === 'daily' && !x.claimed) ?? data?.p.quests.find((x) => !x.claimed);
  return (
    <div id="progressCard" className="lx-card px-3 py-2.5 min-w-[230px] max-w-[270px] text-white">
      <div className="flex items-center gap-2">
        <span className="nr-px" dangerouslySetInnerHTML={{ __html: pixel('coin', 'md').__raw }} />
        <div className="flex-1 min-w-0">
          {data ? <><div className="text-lg leading-none">{fmtNum(data.balance)}</div><div className="text-xs text-white/60">LEGO Coins · heute +{fmtNum(data.today)}</div></>
            : <><div className="text-sm leading-tight">LEGO Coins</div><div className="text-xs text-white/60">{connected && !failed ? 'Lädt …' : 'Mit Konto: tägliche Belohnung und Quests'}</div></>}
        </div>
        {data && (
          <Button size="xs" variant={data.p.daily.claimedToday ? 'flat' : '3d'} id="daily" disabled={data.p.daily.claimedToday} className="h-8 px-3"
            icon={<Icon icon="solar:gift-bold" className="w-4 h-4" />}
            onClick={async () => { try { await lego('POST', '/api/daily/claim'); toast('Tägliche Belohnung abgeholt', 'ok'); onChanged(); } catch (e) { errorToast(e); } }}>
            {data.p.daily.claimedToday ? `${data.p.daily.streak}×` : `+${data.p.daily.nextReward}`}
          </Button>
        )}
      </div>
      {q && (
        <div className="mt-2">
          <div className="flex justify-between text-xs text-white/80"><span className="truncate">{q.title.de}</span><span className="text-white/60">{q.progress}/{q.target}</span></div>
          <div className="h-1 mt-1 rounded-full bg-white/15 overflow-hidden"><div className="h-full rounded-full" style={{ width: `${Math.min(100, (q.progress / q.target) * 100)}%`, backgroundColor: accent.value }} /></div>
        </div>
      )}
    </div>
  );
}
