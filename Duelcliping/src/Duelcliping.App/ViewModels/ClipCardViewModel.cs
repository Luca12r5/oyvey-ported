using System.IO;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Duelcliping.App.Controls;
using Duelcliping.Core.Library;

namespace Duelcliping.App.ViewModels;

/// <summary>A clip as shown in the library grid and on the dashboard.</summary>
public sealed class ClipCardViewModel : ObservableObject
{
    private static readonly SemaphoreSlim ThumbnailGate = new(2, 2);
    private ImageSource? _thumbnail;
    private bool _thumbnailRequested;
    private Uri? _previewUri;
    private ClipItem _item;

    public ClipCardViewModel(ClipItem item) => _item = item;

    public ClipItem Item
    {
        get => _item;
        set
        {
            _item = value;
            OnPropertiesChanged(nameof(Title), nameof(FilePath), nameof(DurationText), nameof(DateText), nameof(Details), nameof(IsPinned), nameof(IsRecording));
        }
    }

    public string FilePath => Item.FilePath;
    public string Title => Item.Title;
    public bool IsRecording => Item.IsRecording;
    public bool IsPinned => Item.Meta.IsPinned;
    public string DurationText => Item.Meta.DurationSeconds > 0 ? CutTimeline.Format(TimeSpan.FromSeconds(Item.Meta.DurationSeconds)) : "—";
    public string DateText => Item.Meta.CreatedUtc.ToLocalTime().ToString("g");
    public string Details
    {
        get
        {
            var parts = new List<string>();
            if (Item.Meta.Height > 0) parts.Add($"{Item.Meta.Height}p");
            if (Item.Meta.FrameRate > 0) parts.Add($"{Item.Meta.FrameRate:0} FPS");
            parts.Add(Format.Bytes(Item.Meta.SizeBytes));
            if (Path.GetExtension(FilePath) is { } ext) parts.Add(ext.TrimStart('.').ToUpperInvariant());
            return string.Join(" · ", parts);
        }
    }

    public ImageSource? Thumbnail
    {
        get => _thumbnail;
        private set => Set(ref _thumbnail, value);
    }

    /// <summary>Muted hover preview; null when not hovered so no decoder runs.</summary>
    public Uri? PreviewUri
    {
        get => _previewUri;
        set => Set(ref _previewUri, value);
    }

    public async void EnsureThumbnail(ClipLibrary library)
    {
        if (_thumbnailRequested) return;
        _thumbnailRequested = true;
        await ThumbnailGate.WaitAsync();
        try
        {
            var path = await library.GetThumbnailAsync(Item);
            if (path == null) return;
            var image = new BitmapImage();
            image.BeginInit();
            image.CacheOption = BitmapCacheOption.OnLoad; // do not lock the file
            image.DecodePixelWidth = 480;
            image.UriSource = new Uri(path);
            image.EndInit();
            image.Freeze();
            Thumbnail = image;
        }
        catch (Exception ex)
        {
            Core.Logging.Log.Warn("Library", $"Thumbnail could not be loaded for {Item.FileName}: {ex.Message}");
        }
        finally { ThumbnailGate.Release(); }
    }
}
