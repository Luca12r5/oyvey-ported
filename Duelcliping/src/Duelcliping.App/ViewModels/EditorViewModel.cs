using System.IO;
using System.Windows.Input;
using System.Windows.Media;
using Duelcliping.App.Controls;
using Duelcliping.App.Services;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Library;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.App.ViewModels;

public enum ExportMode { Lossless, Exact }
public enum ExportResolution { Original, P1080, P720 }

/// <summary>
/// The video cutter: open any clip, recording or video file, split it at the playhead, remove the
/// parts you do not want and save the result as a new video.
/// </summary>
public sealed class EditorViewModel : ObservableObject
{
    private readonly AppController _app;
    private CutPlan? _plan;
    private MediaInfo? _info;
    private CancellationTokenSource? _exportCts;
    private string? _filePath;
    private TimeSpan _position;
    private int _selectedIndex = -1;
    private bool _isPlaying;
    private bool _previewCut = true;
    private ExportMode _mode = ExportMode.Lossless;
    private ExportResolution _resolution = ExportResolution.Original;
    private ContainerFormat _format = ContainerFormat.Mp4;
    private bool _replaceOriginal;
    private bool _isExporting;
    private double _progress;
    private string _status = "";
    private ImageSource? _filmStrip;
    private string? _lastOutput;

    public EditorViewModel(AppController app)
    {
        _app = app;
        SplitCommand = new RelayCommand(Split, () => _plan != null && !IsExporting);
        RemoveCommand = new RelayCommand(() => SetSelectedKeep(false), () => SelectedPiece is { Keep: true } && !IsExporting);
        KeepCommand = new RelayCommand(() => SetSelectedKeep(true), () => SelectedPiece is { Keep: false } && !IsExporting);
        MarkInCommand = new RelayCommand(() => Guard(() => _plan!.RemoveBefore(Position)), () => _plan != null && !IsExporting);
        MarkOutCommand = new RelayCommand(() => Guard(() => _plan!.RemoveAfter(Position)), () => _plan != null && !IsExporting);
        UndoCommand = new RelayCommand(() => _plan?.Undo(), () => _plan?.CanUndo == true && !IsExporting);
        RedoCommand = new RelayCommand(() => _plan?.Redo(), () => _plan?.CanRedo == true && !IsExporting);
        ResetCommand = new RelayCommand(() => _plan?.Reset(), () => _plan is { IsUnchanged: false } && !IsExporting);
        ExportCommand = new AsyncCommand(ExportAsync, () => _plan != null && !IsExporting);
        CancelExportCommand = new RelayCommand(() => _exportCts?.Cancel(), () => IsExporting);
        OpenOutputCommand = new RelayCommand(() => ShellHelpers.RevealInExplorer(_lastOutput!), () => _lastOutput != null);
        EditOutputCommand = new AsyncCommand(() => LoadAsync(_lastOutput!), () => _lastOutput != null && !IsExporting);
    }

    public ICommand SplitCommand { get; }
    public ICommand RemoveCommand { get; }
    public ICommand KeepCommand { get; }
    public ICommand MarkInCommand { get; }
    public ICommand MarkOutCommand { get; }
    public ICommand UndoCommand { get; }
    public ICommand RedoCommand { get; }
    public ICommand ResetCommand { get; }
    public ICommand ExportCommand { get; }
    public ICommand CancelExportCommand { get; }
    public ICommand OpenOutputCommand { get; }
    public ICommand EditOutputCommand { get; }

    /// <summary>The view seeks its player when this fires.</summary>
    public event Action<TimeSpan>? SeekRequested;
    /// <summary>The view must release the file (e.g. before replacing the original).</summary>
    public event Action? ReleaseMediaRequested;

    public string? FilePath { get => _filePath; private set { Set(ref _filePath, value); OnPropertiesChanged(nameof(HasVideo), nameof(Title), nameof(SourceUri)); } }
    public Uri? SourceUri => FilePath == null ? null : new Uri(FilePath);
    public bool HasVideo => FilePath != null && _plan != null;
    public string Title => FilePath == null ? "No video open" : Path.GetFileNameWithoutExtension(FilePath);
    public string SourceDetails => _info == null ? "" :
        $"{_info.Width}×{_info.Height} · {_info.FrameRate:0.#} FPS · {_info.VideoCodec.ToUpperInvariant()} · {_info.AudioTracks} audio track(s)";
    public TimeSpan Duration => _plan?.Duration ?? TimeSpan.Zero;
    public IReadOnlyList<CutPiece> Pieces => _plan?.Pieces ?? Array.Empty<CutPiece>();

    public TimeSpan Position
    {
        get => _position;
        set { if (Set(ref _position, value)) OnPropertyChanged(nameof(PositionText)); }
    }

    public string PositionText => $"{CutTimeline.Format(Position)} / {CutTimeline.Format(Duration)}";
    public string OutputText => _plan == null ? "" :
        _plan.IsUnchanged ? "Nothing cut yet" : $"New video: {CutTimeline.Format(_plan.OutputDuration)} · {_plan.ToSegments().Count} part(s)";

    public int SelectedIndex
    {
        get => _selectedIndex;
        set { if (Set(ref _selectedIndex, value)) OnPropertyChanged(nameof(SelectedPiece)); }
    }

    public CutPiece? SelectedPiece => _plan != null && SelectedIndex >= 0 && SelectedIndex < _plan.Pieces.Count ? _plan.Pieces[SelectedIndex] : null;

    public bool IsPlaying { get => _isPlaying; set => Set(ref _isPlaying, value); }
    public bool PreviewCut { get => _previewCut; set => Set(ref _previewCut, value); }

    public bool ModeLossless { get => _mode == ExportMode.Lossless; set { if (value) SetMode(ExportMode.Lossless); } }
    public bool ModeExact { get => _mode == ExportMode.Exact; set { if (value) SetMode(ExportMode.Exact); } }
    private void SetMode(ExportMode mode)
    {
        _mode = mode;
        if (mode == ExportMode.Lossless) _resolution = ExportResolution.Original;
        OnPropertiesChanged(nameof(ModeLossless), nameof(ModeExact), nameof(ModeHint), nameof(ResOriginal), nameof(Res1080), nameof(Res720));
    }
    public string ModeHint => _mode == ExportMode.Lossless
        ? "Instant and without quality loss. Cuts snap to the nearest keyframe (up to 1 s earlier)."
        : "Frame-exact cuts. Re-encodes with your " + (_app.ActiveEncoder?.Encoder.IsHardware == true ? "GPU" : "CPU") + " encoder; can also downscale.";

    public bool ResOriginal { get => _resolution == ExportResolution.Original; set { if (value) SetResolution(ExportResolution.Original); } }
    public bool Res1080 { get => _resolution == ExportResolution.P1080; set { if (value) SetResolution(ExportResolution.P1080); } }
    public bool Res720 { get => _resolution == ExportResolution.P720; set { if (value) SetResolution(ExportResolution.P720); } }
    private void SetResolution(ExportResolution r)
    {
        _resolution = r;
        if (r != ExportResolution.Original && _mode == ExportMode.Lossless) SetMode(ExportMode.Exact);
        OnPropertiesChanged(nameof(ResOriginal), nameof(Res1080), nameof(Res720));
    }

    public bool FormatMp4 { get => _format == ContainerFormat.Mp4; set { if (value) { _format = ContainerFormat.Mp4; OnPropertyChanged(nameof(FormatMkv)); } } }
    public bool FormatMkv { get => _format == ContainerFormat.Mkv; set { if (value) { _format = ContainerFormat.Mkv; OnPropertyChanged(nameof(FormatMp4)); } } }
    public bool ReplaceOriginal { get => _replaceOriginal; set => Set(ref _replaceOriginal, value); }

    public bool IsExporting { get => _isExporting; private set => Set(ref _isExporting, value); }
    public double Progress { get => _progress; private set => Set(ref _progress, value); }
    public string Status { get => _status; private set => Set(ref _status, value); }
    public ImageSource? FilmStrip { get => _filmStrip; private set => Set(ref _filmStrip, value); }
    public bool HasOutput => _lastOutput != null;

    // ------------------------------------------------------------------ effects
    private string? _musicPath;
    private double _musicVolume = 80, _originalVolume = 100, _transitionSeconds = 0.5;
    private TransitionStyle _transition = TransitionStyle.None;
    private ColorLook _look = ColorLook.None;
    private double _speed = 1.0;
    private bool _fadeIn, _fadeOut;
    private string _title = "";
    private TitlePlacement _titlePosition = TitlePlacement.Bottom;

    public IReadOnlyList<Option<TransitionStyle>> Transitions { get; } = new[]
    {
        new Option<TransitionStyle>(TransitionStyle.None, "None (hard cut)"), new Option<TransitionStyle>(TransitionStyle.Fade, "Fade"),
        new Option<TransitionStyle>(TransitionStyle.Dissolve, "Dissolve"), new Option<TransitionStyle>(TransitionStyle.Wipe, "Wipe"),
        new Option<TransitionStyle>(TransitionStyle.Slide, "Slide"), new Option<TransitionStyle>(TransitionStyle.Circle, "Circle"),
        new Option<TransitionStyle>(TransitionStyle.Flash, "Flash"),
    };
    public IReadOnlyList<Option<ColorLook>> Looks { get; } = new[]
    {
        new Option<ColorLook>(ColorLook.None, "Original"), new Option<ColorLook>(ColorLook.Vivid, "Vivid"),
        new Option<ColorLook>(ColorLook.Cinematic, "Cinematic"), new Option<ColorLook>(ColorLook.BlackWhite, "Black & white"),
        new Option<ColorLook>(ColorLook.Warm, "Warm"), new Option<ColorLook>(ColorLook.Cool, "Cool"),
        new Option<ColorLook>(ColorLook.Retro, "Retro"), new Option<ColorLook>(ColorLook.Vignette, "Vignette"),
    };
    public IReadOnlyList<Option<double>> Speeds { get; } = new[]
    {
        new Option<double>(0.5, "0.5× slow motion"), new Option<double>(0.75, "0.75×"), new Option<double>(1.0, "1× normal"),
        new Option<double>(1.25, "1.25×"), new Option<double>(1.5, "1.5×"), new Option<double>(2.0, "2× fast"),
    };
    public IReadOnlyList<Option<TitlePlacement>> TitlePositions { get; } = new[]
    {
        new Option<TitlePlacement>(TitlePlacement.Top, "Top"), new Option<TitlePlacement>(TitlePlacement.Center, "Center"),
        new Option<TitlePlacement>(TitlePlacement.Bottom, "Bottom"),
    };

    public string MusicName => _musicPath == null ? "No music" : Path.GetFileName(_musicPath);
    public bool HasMusic => _musicPath != null;
    public double MusicVolume { get => _musicVolume; set { if (Set(ref _musicVolume, value)) EffectsChanged(); } }
    public double OriginalVolume { get => _originalVolume; set { if (Set(ref _originalVolume, value)) EffectsChanged(); } }
    public TransitionStyle Transition { get => _transition; set { if (Set(ref _transition, value)) EffectsChanged(); } }
    public double TransitionSeconds { get => _transitionSeconds; set { if (Set(ref _transitionSeconds, value)) EffectsChanged(); } }
    public ColorLook Look { get => _look; set { if (Set(ref _look, value)) EffectsChanged(); } }
    public double Speed { get => _speed; set { if (Set(ref _speed, value)) EffectsChanged(); } }
    public bool FadeIn { get => _fadeIn; set { if (Set(ref _fadeIn, value)) EffectsChanged(); } }
    public bool FadeOut { get => _fadeOut; set { if (Set(ref _fadeOut, value)) EffectsChanged(); } }
    public string Title2 { get => _title; set { if (Set(ref _title, value ?? "")) EffectsChanged(); } }
    public TitlePlacement TitlePosition { get => _titlePosition; set { if (Set(ref _titlePosition, value)) EffectsChanged(); } }
    public bool HasEffects => BuildEffects() is { HasAny: true };
    public string EffectsHint => HasEffects
        ? "Effects are on: the video is rendered frame-exact with your " + (_app.ActiveEncoder?.Encoder.IsHardware == true ? "GPU" : "CPU") + " encoder."
        : "Add music, transitions, a look or a title – everything is rendered when you save.";

    public ICommand PickMusicCommand => _pickMusic ??= new RelayCommand(PickMusic);
    public ICommand ClearMusicCommand => _clearMusic ??= new RelayCommand(() => { _musicPath = null; EffectsChanged(); }, () => _musicPath != null);
    public ICommand ResetEffectsCommand => _resetFx ??= new RelayCommand(ResetEffects);
    private ICommand? _pickMusic, _clearMusic, _resetFx;

    private void PickMusic()
    {
        var dialog = new Microsoft.Win32.OpenFileDialog
        {
            Title = "Choose background music",
            Filter = "Audio|*.mp3;*.wav;*.m4a;*.aac;*.flac;*.ogg;*.opus;*.wma|All files|*.*",
        };
        if (dialog.ShowDialog() != true) return;
        _musicPath = dialog.FileName;
        EffectsChanged();
    }

    private void ResetEffects()
    {
        _musicPath = null; _musicVolume = 80; _originalVolume = 100; _transition = TransitionStyle.None; _transitionSeconds = 0.5;
        _look = ColorLook.None; _speed = 1.0; _fadeIn = _fadeOut = false; _title = ""; _titlePosition = TitlePlacement.Bottom;
        OnPropertiesChanged(nameof(MusicVolume), nameof(OriginalVolume), nameof(Transition), nameof(TransitionSeconds), nameof(Look),
            nameof(Speed), nameof(FadeIn), nameof(FadeOut), nameof(Title2), nameof(TitlePosition));
        EffectsChanged();
    }

    private void EffectsChanged()
    {
        OnPropertiesChanged(nameof(MusicName), nameof(HasMusic), nameof(HasEffects), nameof(EffectsHint), nameof(ModeHint));
        CommandManager.InvalidateRequerySuggested();
    }

    private EditEffects BuildEffects() => new()
    {
        MusicPath = _musicPath,
        MusicVolume = _musicVolume / 100,
        OriginalVolume = _originalVolume / 100,
        Transition = _transition,
        TransitionSeconds = _transitionSeconds,
        Look = _look,
        Speed = _speed,
        FadeIn = _fadeIn,
        FadeOut = _fadeOut,
        Title = string.IsNullOrWhiteSpace(_title) ? null : _title,
        TitlePosition = _titlePosition,
    };

    public async Task LoadAsync(string path)
    {
        ReleaseMediaRequested?.Invoke();
        Status = "Opening…";
        var info = await MediaProbe.ProbeAsync(path);
        if (info == null || info.DurationSeconds <= 0)
        {
            Status = "This file could not be read as a video.";
            return;
        }
        if (_plan != null) _plan.Changed -= OnPlanChanged;
        _info = info;
        _plan = new CutPlan(TimeSpan.FromSeconds(info.DurationSeconds));
        _plan.Changed += OnPlanChanged;
        _format = Path.GetExtension(path).Equals(".mkv", StringComparison.OrdinalIgnoreCase) ? ContainerFormat.Mkv : ContainerFormat.Mp4;
        _lastOutput = null;
        Position = TimeSpan.Zero;
        SelectedIndex = 0;
        FilmStrip = null;
        FilePath = path;
        Status = Path.GetExtension(path).Equals(".ts", StringComparison.OrdinalIgnoreCase)
            ? "Preview of .ts files is not supported by Windows; cutting still works."
            : "Space play/pause · S split · Del remove part · I / O trim start / end · Ctrl+Z undo";
        OnPropertiesChanged(nameof(Duration), nameof(Pieces), nameof(SourceDetails), nameof(OutputText), nameof(FormatMp4), nameof(FormatMkv),
            nameof(PositionText), nameof(HasOutput), nameof(ModeHint));
        CommandManager.InvalidateRequerySuggested();

        var strip = Path.Combine(Path.GetTempPath(), "moments-strip-" + Guid.NewGuid().ToString("N") + ".jpg");
        var created = await Duelcliping.Core.Library.FilmStrip.CreateAsync(path, info.DurationSeconds, strip);
        if (created != null && FilePath == path)
        {
            FilmStrip = CutTimeline.LoadImage(created);
            try { File.Delete(created); } catch { /* temp */ }
        }
    }

    private void OnPlanChanged()
    {
        OnPropertiesChanged(nameof(Pieces), nameof(OutputText), nameof(SelectedPiece));
        if (_plan != null) SelectedIndex = Math.Min(_plan.IndexAt(Position), _plan.Pieces.Count - 1);
        CommandManager.InvalidateRequerySuggested();
    }

    private void Guard(Action action)
    {
        try { action(); }
        catch (InvalidOperationException ex) { Status = ex.Message; }
    }

    private void Split()
    {
        if (_plan == null) return;
        if (!_plan.Split(Position)) Status = "Move the playhead a bit further from the existing cut.";
        else SelectedIndex = _plan.IndexAt(Position);
    }

    private void SetSelectedKeep(bool keep)
    {
        if (_plan == null || SelectedIndex < 0) return;
        Guard(() => _plan.SetKeep(SelectedIndex, keep));
    }

    /// <summary>Called by the view while playing: jumps over removed parts when previewing the cut.</summary>
    public TimeSpan? PlaybackTick(TimeSpan mediaPosition)
    {
        Position = mediaPosition;
        if (!PreviewCut || _plan == null) return null;
        var target = _plan.SkipRemoved(mediaPosition);
        if (target == null) return Duration; // end reached
        return target.Value - mediaPosition > TimeSpan.FromMilliseconds(80) ? target : null;
    }

    public void Seek(TimeSpan t)
    {
        Position = t;
        SeekRequested?.Invoke(t);
    }

    public void Nudge(double seconds) =>
        Seek(TimeSpan.FromSeconds(Math.Clamp(Position.TotalSeconds + seconds, 0, Duration.TotalSeconds)));

    private async Task ExportAsync()
    {
        if (_plan == null || FilePath == null || _info == null) return;
        var source = FilePath;
        var ext = _format == ContainerFormat.Mkv ? ".mkv" : ".mp4";
        var output = ReplaceOriginal
            ? Path.Combine(Path.GetDirectoryName(source)!, Path.GetFileNameWithoutExtension(source) + ".editing" + ext)
            : Path.ChangeExtension(ClipEditor.SuggestOutputPath(source), ext);
        bool exact = _mode == ExportMode.Exact;
        int? maxHeight = _resolution switch { ExportResolution.P1080 => 1080, ExportResolution.P720 => 720, _ => null };
        int outHeight = Math.Min(maxHeight ?? _info.Height, _info.Height);
        int outWidth = _info.Height > 0 ? _info.Width * outHeight / _info.Height : 1920;
        int fps = (int)Math.Round(_info.FrameRate > 0 ? _info.FrameRate : 60);
        EncoderInfo? encoder = null;
        if (exact && _app.Encoders.Available.Count > 0)
            encoder = _app.Encoders.Select(VideoCodec.H264, _app.Settings.Current.Encoder).Encoder;

        var request = new EditRequest
        {
            Input = source,
            Output = output,
            Segments = _plan.ToSegments(),
            ReEncode = exact,
            Encoder = encoder,
            MaxHeight = maxHeight,
            BitrateKbps = Duelcliping.Core.Encoders.PipelineConfig.ComputeBitrateKbps(QualityPreset.High, 0, outWidth, outHeight, Math.Min(fps, 144), VideoCodec.H264),
            Effects = BuildEffects(),
        };
        if (request.Effects is { HasAny: true } && request.Encoder == null && _app.Encoders.Available.Count > 0)
            request = request with { Encoder = _app.Encoders.Select(VideoCodec.H264, _app.Settings.Current.Encoder).Encoder };

        IsExporting = true;
        Progress = 0;
        Status = exact ? "Encoding…" : "Cutting…";
        _exportCts = new CancellationTokenSource();
        try
        {
            var result = await ClipEditor.RunAsync(request, new Progress<double>(p => Progress = p), _exportCts.Token);
            if (ReplaceOriginal)
            {
                ReleaseMediaRequested?.Invoke();
                await Task.Delay(300); // let the media player release its file handle
                var final = Path.ChangeExtension(source, ext);
                ShellHelpers.DeleteToRecycleBin(source);
                File.Move(output, final, overwrite: true);
                output = final;
            }
            _lastOutput = output;
            Status = $"Saved {Path.GetFileName(output)}" + (result.Notice != null ? " · " + result.Notice : "");
            _app.NotifyClipCreated(output);
            _app.RaiseToast(new Toast(ToastKind.Success, "Video saved", Path.GetFileName(output), output));
            if (ReplaceOriginal) await LoadAsync(output);
        }
        catch (OperationCanceledException)
        {
            Status = "Export cancelled.";
        }
        catch (Exception ex)
        {
            Status = ex.Message;
            _app.RaiseToast(new Toast(ToastKind.Error, "Cutting failed", ex.Message));
        }
        finally
        {
            IsExporting = false;
            _exportCts.Dispose();
            _exportCts = null;
            OnPropertyChanged(nameof(HasOutput));
            CommandManager.InvalidateRequerySuggested();
        }
    }
}
