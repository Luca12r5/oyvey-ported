using System.Collections.ObjectModel;
using System.IO;
using System.Windows.Input;
using Duelcliping.App.Services;
using Duelcliping.App.Views;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Library;
using Duelcliping.Core.Settings;
using Duelcliping.Core.Storage;

namespace Duelcliping.App.ViewModels;

public sealed class LibraryViewModel : ObservableObject
{
    private readonly AppController _app;
    private readonly MainViewModel _main;
    private List<ClipItem> _all = new();
    private string _search = "";
    private ClipSort _sort = ClipSort.Newest;
    private bool _onlyRecordings;
    private bool _loading;
    private string _summary = "";
    private string? _busyText;

    public LibraryViewModel(AppController app, MainViewModel main)
    {
        _app = app;
        _main = main;
        RefreshCommand = new AsyncCommand(RefreshAsync);
        OpenCommand = new AsyncCommand(p => _main.OpenInEditorAsync(Card(p).FilePath));
        PlayExternalCommand = new RelayCommand(p => ShellHelpers.OpenWithDefaultApp(Card(p).FilePath));
        RenameCommand = new RelayCommand(p => Rename(Card(p)));
        DeleteCommand = new RelayCommand(p => Delete(Card(p)));
        RevealCommand = new RelayCommand(p => ShellHelpers.RevealInExplorer(Card(p).FilePath));
        OpenFolderCommand = new RelayCommand(() => ShellHelpers.RevealInExplorer(_app.Library.Directory));
        PinCommand = new RelayCommand(p => TogglePin(Card(p)));
        ExportCopyCommand = new AsyncCommand(p => ExportCopyAsync(Card(p)));
        Export1080Command = new AsyncCommand(p => ExportEncodedAsync(Card(p), 1080));
        Export720Command = new AsyncCommand(p => ExportEncodedAsync(Card(p), 720));
    }

    private static ClipCardViewModel Card(object? p) => (ClipCardViewModel)p!;

    public ObservableCollection<ClipCardViewModel> Clips { get; } = new();
    public ICommand RefreshCommand { get; }
    public ICommand OpenCommand { get; }
    public ICommand PlayExternalCommand { get; }
    public ICommand RenameCommand { get; }
    public ICommand DeleteCommand { get; }
    public ICommand RevealCommand { get; }
    public ICommand OpenFolderCommand { get; }
    public ICommand PinCommand { get; }
    public ICommand ExportCopyCommand { get; }
    public ICommand Export1080Command { get; }
    public ICommand Export720Command { get; }

    public string Search { get => _search; set { if (Set(ref _search, value)) ApplyFilter(); } }
    public bool OnlyRecordings { get => _onlyRecordings; set { if (Set(ref _onlyRecordings, value)) ApplyFilter(); } }
    public bool SortNewest { get => _sort == ClipSort.Newest; set { if (value) SetSort(ClipSort.Newest); } }
    public bool SortOldest { get => _sort == ClipSort.Oldest; set { if (value) SetSort(ClipSort.Oldest); } }
    public bool SortLongest { get => _sort == ClipSort.Longest; set { if (value) SetSort(ClipSort.Longest); } }
    public bool SortShortest { get => _sort == ClipSort.Shortest; set { if (value) SetSort(ClipSort.Shortest); } }
    public bool IsLoading { get => _loading; private set => Set(ref _loading, value); }
    public string Summary { get => _summary; private set => Set(ref _summary, value); }
    public string? BusyText { get => _busyText; private set => Set(ref _busyText, value); }
    public bool IsEmpty => !IsLoading && Clips.Count == 0;
    public string EmptyText => _all.Count == 0
        ? "No clips yet. Press " + _app.Settings.Current.HotkeySaveShort + " in a game to save the last " + _app.Settings.Current.ShortClipSeconds + " seconds."
        : "No clips match your search.";

    private void SetSort(ClipSort sort)
    {
        _sort = sort;
        OnPropertiesChanged(nameof(SortNewest), nameof(SortOldest), nameof(SortLongest), nameof(SortShortest));
        ApplyFilter();
    }

    public async Task RefreshAsync()
    {
        IsLoading = true;
        try
        {
            _all = (await _app.Library.ScanAsync()).ToList();
            var usage = StorageManager.Measure(_all, _app.Library.Directory, _app.Settings.Current.MaxStorageGb);
            Summary = $"{_all.Count} videos · {Controls.Format.Bytes(usage.ClipBytes)} · {Controls.Format.Bytes(usage.DriveFreeBytes)} free";
            ApplyFilter();
        }
        finally
        {
            IsLoading = false;
            OnPropertiesChanged(nameof(IsEmpty), nameof(EmptyText));
        }
    }

    private void ApplyFilter()
    {
        var items = ClipLibrary.Sort(ClipLibrary.Filter(_all, Search), _sort);
        if (OnlyRecordings) items = items.Where(c => c.IsRecording);
        var existing = Clips.ToDictionary(c => c.FilePath, StringComparer.OrdinalIgnoreCase);
        Clips.Clear();
        foreach (var item in items)
        {
            if (existing.TryGetValue(item.FilePath, out var card)) card.Item = item;
            else
            {
                card = new ClipCardViewModel(item);
                card.EnsureThumbnail(_app.Library);
            }
            Clips.Add(card);
        }
        OnPropertiesChanged(nameof(IsEmpty), nameof(EmptyText));
    }

    private void Rename(ClipCardViewModel card)
    {
        var name = TextPromptWindow.Ask(_main.OwnerWindow, "Rename clip", "New name", card.Title, ClipLibrary.ValidateName);
        if (name == null) return;
        try
        {
            card.Item = _app.Library.Rename(card.Item, name);
            _ = RefreshAsync();
        }
        catch (Exception ex) when (ex is IOException or ArgumentException or UnauthorizedAccessException)
        {
            _app.RaiseToast(new Toast(ToastKind.Error, "Rename failed", ex.Message));
        }
    }

    private void Delete(ClipCardViewModel card)
    {
        if (!ConfirmWindow.Ask(_main.OwnerWindow, "Delete clip?", $"“{card.Title}” will be moved to the Recycle Bin.", "Delete")) return;
        try
        {
            _app.Library.Delete(card.Item);
            _all.RemoveAll(c => c.FilePath == card.FilePath);
            Clips.Remove(card);
            OnPropertiesChanged(nameof(IsEmpty), nameof(EmptyText));
        }
        catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or OperationCanceledException)
        {
            _app.RaiseToast(new Toast(ToastKind.Error, "Delete failed", ex.Message));
        }
    }

    private void TogglePin(ClipCardViewModel card)
    {
        _app.Library.SetPinned(card.Item, !card.IsPinned);
        card.Item = card.Item;
    }

    private string? AskExportPath(ClipCardViewModel card, string suffix, string ext)
    {
        var dialog = new Microsoft.Win32.SaveFileDialog
        {
            Title = "Export clip",
            FileName = card.Title + suffix + ext,
            Filter = ext == ".mkv" ? "MKV video|*.mkv" : "MP4 video|*.mp4",
            InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.Desktop),
        };
        return dialog.ShowDialog(_main.OwnerWindow) == true ? dialog.FileName : null;
    }

    private async Task ExportCopyAsync(ClipCardViewModel card)
    {
        var target = AskExportPath(card, "", Path.GetExtension(card.FilePath) == ".mkv" ? ".mkv" : Path.GetExtension(card.FilePath));
        if (target == null) return;
        BusyText = "Copying…";
        try
        {
            await Task.Run(() => File.Copy(card.FilePath, target, overwrite: true));
            _app.RaiseToast(new Toast(ToastKind.Success, "Exported", Path.GetFileName(target), target));
        }
        finally { BusyText = null; }
    }

    private async Task ExportEncodedAsync(ClipCardViewModel card, int height)
    {
        var target = AskExportPath(card, $" {height}p", ".mp4");
        if (target == null) return;
        var encoder = _app.Encoders.Available.Count > 0 ? _app.Encoders.Select(VideoCodec.H264, _app.Settings.Current.Encoder).Encoder : null;
        int fps = (int)Math.Clamp(Math.Round(card.Item.Meta.FrameRate), 24, 144);
        int width = card.Item.Meta.Height > 0 ? card.Item.Meta.Width * height / card.Item.Meta.Height : height * 16 / 9;
        BusyText = $"Exporting {height}p… 0%";
        try
        {
            await ClipEditor.RunAsync(new EditRequest
            {
                Input = card.FilePath,
                Output = target,
                ReEncode = true,
                Encoder = encoder,
                MaxHeight = height,
                BitrateKbps = PipelineConfig.ComputeBitrateKbps(QualityPreset.High, 0, width, height, fps, VideoCodec.H264),
            }, new Progress<double>(p => BusyText = $"Exporting {height}p… {p:P0}"), CancellationToken.None);
            _app.RaiseToast(new Toast(ToastKind.Success, "Exported", Path.GetFileName(target), target));
        }
        finally { BusyText = null; }
    }
}
