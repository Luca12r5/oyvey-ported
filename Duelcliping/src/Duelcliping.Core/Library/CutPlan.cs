namespace Duelcliping.Core.Library;

/// <summary>One piece of the editor timeline: a time range that is either kept or removed.</summary>
public sealed record CutPiece(TimeSpan Start, TimeSpan End, bool Keep)
{
    public TimeSpan Duration => End - Start;
    public bool Contains(TimeSpan t) => t >= Start && t < End;
}

/// <summary>
/// The editing model behind the cutter: the video is a sequence of contiguous pieces covering the
/// whole duration. Splitting at the playhead creates pieces; each piece is kept or removed. Every
/// change is undoable. <see cref="ToSegments"/> produces the input for <see cref="ClipEditor"/>.
/// </summary>
public sealed class CutPlan
{
    public static readonly TimeSpan MinPiece = TimeSpan.FromMilliseconds(100);
    private readonly Stack<List<CutPiece>> _undo = new();
    private readonly Stack<List<CutPiece>> _redo = new();
    private List<CutPiece> _pieces;

    public CutPlan(TimeSpan duration)
    {
        if (duration <= TimeSpan.Zero) throw new ArgumentException("The video has no duration.");
        Duration = duration;
        _pieces = new List<CutPiece> { new(TimeSpan.Zero, duration, true) };
    }

    public TimeSpan Duration { get; }
    public IReadOnlyList<CutPiece> Pieces => _pieces;
    public bool CanUndo => _undo.Count > 0;
    public bool CanRedo => _redo.Count > 0;
    public bool IsUnchanged => _pieces.Count == 1 && _pieces[0].Keep;
    public TimeSpan OutputDuration => TimeSpan.FromTicks(_pieces.Where(p => p.Keep).Sum(p => p.Duration.Ticks));
    public event Action? Changed;

    public int IndexAt(TimeSpan t)
    {
        for (int i = 0; i < _pieces.Count; i++) if (_pieces[i].Contains(t)) return i;
        return _pieces.Count - 1;
    }

    /// <summary>Splits the piece under <paramref name="t"/>. Returns false if too close to an existing cut.</summary>
    public bool Split(TimeSpan t)
    {
        int i = IndexAt(t);
        var p = _pieces[i];
        if (t - p.Start < MinPiece || p.End - t < MinPiece) return false;
        Mutate(list =>
        {
            list[i] = p with { End = t };
            list.Insert(i + 1, p with { Start = t });
        });
        return true;
    }

    public void SetKeep(int index, bool keep)
    {
        if (index < 0 || index >= _pieces.Count || _pieces[index].Keep == keep) return;
        if (!keep && _pieces.Count(p => p.Keep) == 1) throw new InvalidOperationException("At least one part of the video has to stay.");
        Mutate(list => list[index] = list[index] with { Keep = keep });
    }

    public void Toggle(int index) => SetKeep(index, !_pieces[index].Keep);

    /// <summary>Mark in: everything before <paramref name="t"/> is removed.</summary>
    public void RemoveBefore(TimeSpan t) => RemoveRange(TimeSpan.Zero, t);

    /// <summary>Mark out: everything after <paramref name="t"/> is removed.</summary>
    public void RemoveAfter(TimeSpan t) => RemoveRange(t, Duration);

    private void RemoveRange(TimeSpan from, TimeSpan to)
    {
        if (to - from < MinPiece) return;
        var keptAfter = _pieces.Where(p => p.Keep).Sum(p => Overlap(p, from, to) < p.Duration ? 1 : 0);
        if (keptAfter == 0) throw new InvalidOperationException("That would remove the whole video.");
        Mutate(list =>
        {
            var result = new List<CutPiece>();
            foreach (var p in list)
            {
                if (p.End <= from || p.Start >= to) { result.Add(p); continue; }
                if (p.Start < from) result.Add(p with { End = from });
                result.Add(p with { Start = p.Start > from ? p.Start : from, End = p.End < to ? p.End : to, Keep = false });
                if (p.End > to) result.Add(p with { Start = to });
            }
            list.Clear();
            list.AddRange(result.Where(p => p.Duration > TimeSpan.Zero));
        });
    }

    private static TimeSpan Overlap(CutPiece p, TimeSpan from, TimeSpan to)
    {
        var s = p.Start > from ? p.Start : from;
        var e = p.End < to ? p.End : to;
        return e > s ? e - s : TimeSpan.Zero;
    }

    public void Reset()
    {
        if (IsUnchanged) return;
        Mutate(list => { list.Clear(); list.Add(new CutPiece(TimeSpan.Zero, Duration, true)); });
    }

    public void Undo()
    {
        if (_undo.Count == 0) return;
        _redo.Push(_pieces);
        _pieces = _undo.Pop();
        Changed?.Invoke();
    }

    public void Redo()
    {
        if (_redo.Count == 0) return;
        _undo.Push(_pieces);
        _pieces = _redo.Pop();
        Changed?.Invoke();
    }

    /// <summary>During preview playback: where to continue if <paramref name="t"/> is inside a removed piece (null = end).</summary>
    public TimeSpan? SkipRemoved(TimeSpan t)
    {
        int i = IndexAt(t);
        if (_pieces[i].Keep) return t;
        for (int j = i + 1; j < _pieces.Count; j++) if (_pieces[j].Keep) return _pieces[j].Start;
        return null;
    }

    /// <summary>Kept ranges, adjacent kept pieces merged (a split without removal costs nothing).</summary>
    public IReadOnlyList<CutSegment> ToSegments()
    {
        var segments = new List<CutSegment>();
        foreach (var p in _pieces.Where(p => p.Keep))
        {
            if (segments.Count > 0 && segments[^1].End == p.Start) segments[^1] = segments[^1] with { End = p.End };
            else segments.Add(new CutSegment(p.Start, p.End));
        }
        return segments;
    }

    private void Mutate(Action<List<CutPiece>> change)
    {
        var copy = new List<CutPiece>(_pieces);
        change(copy);
        // Merge neighbours with the same state so the timeline stays clean.
        var merged = new List<CutPiece>();
        foreach (var p in copy)
        {
            if (merged.Count > 0 && merged[^1].Keep == p.Keep && merged[^1].End == p.Start && !p.Keep)
                merged[^1] = merged[^1] with { End = p.End };
            else merged.Add(p);
        }
        _undo.Push(_pieces);
        _redo.Clear();
        _pieces = merged;
        Changed?.Invoke();
    }
}
