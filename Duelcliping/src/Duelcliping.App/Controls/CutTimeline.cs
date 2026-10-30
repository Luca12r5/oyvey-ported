using System.Globalization;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Duelcliping.Core.Library;

namespace Duelcliping.App.Controls;

/// <summary>
/// Cutting timeline: film strip, kept (accent) and removed (dimmed, hatched) pieces, the selected
/// piece and the playhead. Click or drag to scrub; clicking a piece selects it. Rendered with a
/// single OnRender pass – nothing animates, so it costs nothing while idle.
/// </summary>
public sealed class CutTimeline : FrameworkElement
{
    private const double TrackTop = 22;
    private const double TrackHeight = 64;

    public static readonly DependencyProperty PiecesProperty = DependencyProperty.Register(nameof(Pieces), typeof(IReadOnlyList<CutPiece>),
        typeof(CutTimeline), new FrameworkPropertyMetadata(Array.Empty<CutPiece>(), FrameworkPropertyMetadataOptions.AffectsRender));
    public static readonly DependencyProperty DurationProperty = DependencyProperty.Register(nameof(Duration), typeof(TimeSpan),
        typeof(CutTimeline), new FrameworkPropertyMetadata(TimeSpan.Zero, FrameworkPropertyMetadataOptions.AffectsRender));
    public static readonly DependencyProperty PositionProperty = DependencyProperty.Register(nameof(Position), typeof(TimeSpan),
        typeof(CutTimeline), new FrameworkPropertyMetadata(TimeSpan.Zero, FrameworkPropertyMetadataOptions.AffectsRender | FrameworkPropertyMetadataOptions.BindsTwoWayByDefault));
    public static readonly DependencyProperty SelectedIndexProperty = DependencyProperty.Register(nameof(SelectedIndex), typeof(int),
        typeof(CutTimeline), new FrameworkPropertyMetadata(-1, FrameworkPropertyMetadataOptions.AffectsRender | FrameworkPropertyMetadataOptions.BindsTwoWayByDefault));
    public static readonly DependencyProperty FilmStripProperty = DependencyProperty.Register(nameof(FilmStrip), typeof(ImageSource),
        typeof(CutTimeline), new FrameworkPropertyMetadata(null, FrameworkPropertyMetadataOptions.AffectsRender));

    public IReadOnlyList<CutPiece> Pieces { get => (IReadOnlyList<CutPiece>)GetValue(PiecesProperty); set => SetValue(PiecesProperty, value); }
    public TimeSpan Duration { get => (TimeSpan)GetValue(DurationProperty); set => SetValue(DurationProperty, value); }
    public TimeSpan Position { get => (TimeSpan)GetValue(PositionProperty); set => SetValue(PositionProperty, value); }
    public int SelectedIndex { get => (int)GetValue(SelectedIndexProperty); set => SetValue(SelectedIndexProperty, value); }
    public ImageSource? FilmStrip { get => (ImageSource?)GetValue(FilmStripProperty); set => SetValue(FilmStripProperty, value); }

    /// <summary>Raised when the user scrubs; the view seeks the player.</summary>
    public event Action<TimeSpan>? SeekRequested;

    private bool _dragging;

    public CutTimeline()
    {
        Focusable = true;
        Cursor = Cursors.Hand;
        ClipToBounds = true;
    }

    protected override Size MeasureOverride(Size availableSize) =>
        new(double.IsInfinity(availableSize.Width) ? 600 : availableSize.Width, TrackTop + TrackHeight + 8);

    private Brush Res(string key, Brush fallback) => TryFindResource(key) as Brush ?? fallback;

    private double X(TimeSpan t) => Duration.TotalMilliseconds <= 0 ? 0 : t.TotalMilliseconds / Duration.TotalMilliseconds * ActualWidth;

    private TimeSpan TimeAt(double x) =>
        Duration <= TimeSpan.Zero ? TimeSpan.Zero : TimeSpan.FromMilliseconds(Math.Clamp(x / ActualWidth, 0, 1) * Duration.TotalMilliseconds);

    protected override void OnRender(DrawingContext dc)
    {
        double w = ActualWidth;
        var text = Res("TextTertiaryBrush", Brushes.Gray);
        var accent = Res("TimelineKeptBrush", Brushes.Orange);
        var removed = Res("TimelineRemovedBrush", Brushes.DimGray);
        var surface = Res("SurfaceBrush", Brushes.Black);
        var playhead = Res("TextPrimaryBrush", Brushes.White);
        var track = new Rect(0, TrackTop, w, TrackHeight);

        dc.DrawRectangle(Brushes.Transparent, null, new Rect(0, 0, w, ActualHeight)); // hit-testable background
        DrawRuler(dc, text, w);

        dc.PushClip(new RectangleGeometry(track, 8, 8));
        dc.DrawRectangle(surface, null, track);
        if (FilmStrip != null) dc.DrawImage(FilmStrip, track);

        var pieces = Pieces ?? Array.Empty<CutPiece>();
        var hatch = HatchBrush(removed);
        for (int i = 0; i < pieces.Count; i++)
        {
            var p = pieces[i];
            var r = new Rect(X(p.Start), TrackTop, Math.Max(1, X(p.End) - X(p.Start)), TrackHeight);
            if (p.Keep)
            {
                var tint = accent.Clone();
                tint.Opacity = 0.18;
                dc.DrawRectangle(tint, null, r);
                dc.DrawRectangle(accent, null, new Rect(r.X, r.Bottom - 4, r.Width, 4));
            }
            else
            {
                dc.DrawRectangle(new SolidColorBrush(Color.FromArgb(0xB0, 0x10, 0x0F, 0x0E)), null, r);
                dc.DrawRectangle(hatch, null, r);
            }
            if (i > 0) dc.DrawRectangle(playhead, null, new Rect(r.X - 1, TrackTop, 2, TrackHeight)); // cut marker
        }
        dc.Pop();

        if (SelectedIndex >= 0 && SelectedIndex < pieces.Count)
        {
            var p = pieces[SelectedIndex];
            var r = new Rect(X(p.Start) + 1, TrackTop + 1, Math.Max(2, X(p.End) - X(p.Start) - 2), TrackHeight - 2);
            dc.DrawRoundedRectangle(null, new Pen(accent, 2), r, 7, 7);
        }

        // Playhead
        double px = Math.Clamp(X(Position), 1, w - 1);
        dc.DrawRectangle(playhead, null, new Rect(px - 1, TrackTop - 6, 2, TrackHeight + 10));
        var head = new StreamGeometry();
        using (var g = head.Open())
        {
            g.BeginFigure(new Point(px - 6, TrackTop - 12), true, true);
            g.LineTo(new Point(px + 6, TrackTop - 12), true, false);
            g.LineTo(new Point(px, TrackTop - 4), true, false);
        }
        head.Freeze();
        dc.DrawGeometry(playhead, null, head);
    }

    private void DrawRuler(DrawingContext dc, Brush brush, double width)
    {
        if (Duration <= TimeSpan.Zero) return;
        double seconds = Duration.TotalSeconds;
        double[] steps = { 1, 2, 5, 10, 15, 30, 60, 120, 300, 600, 900, 1800, 3600 };
        double step = steps.FirstOrDefault(s => seconds / s <= Math.Max(2, width / 90), 3600);
        var pen = new Pen(brush, 1);
        double dpi = VisualTreeHelper.GetDpi(this).PixelsPerDip;
        for (double t = 0; t <= seconds; t += step)
        {
            double x = X(TimeSpan.FromSeconds(t));
            dc.DrawLine(pen, new Point(x, TrackTop - 5), new Point(x, TrackTop - 1));
            var label = new FormattedText(Format(TimeSpan.FromSeconds(t)), CultureInfo.CurrentCulture, FlowDirection.LeftToRight,
                new Typeface("Segoe UI"), 10, brush, dpi);
            if (x + label.Width + 4 < width) dc.DrawText(label, new Point(x + 3, 2));
        }
    }

    private static Brush HatchBrush(Brush color)
    {
        var drawing = new GeometryDrawing(null, new Pen(color, 2), Geometry.Parse("M0,8 L8,0"));
        var brush = new DrawingBrush(drawing)
        {
            TileMode = TileMode.Tile, Viewport = new Rect(0, 0, 8, 8), ViewportUnits = BrushMappingMode.Absolute, Opacity = 0.6,
        };
        brush.Freeze();
        return brush;
    }

    public static string Format(TimeSpan t) =>
        t.TotalHours >= 1 ? t.ToString(@"h\:mm\:ss") : t.ToString(@"m\:ss");

    protected override void OnMouseLeftButtonDown(MouseButtonEventArgs e)
    {
        Focus();
        _dragging = true;
        CaptureMouse();
        Scrub(e.GetPosition(this).X, select: true);
        e.Handled = true;
    }

    protected override void OnMouseMove(MouseEventArgs e)
    {
        if (_dragging) Scrub(e.GetPosition(this).X, select: false);
    }

    protected override void OnMouseLeftButtonUp(MouseButtonEventArgs e)
    {
        _dragging = false;
        ReleaseMouseCapture();
    }

    private void Scrub(double x, bool select)
    {
        var t = TimeAt(x);
        Position = t;
        if (select)
        {
            var pieces = Pieces ?? Array.Empty<CutPiece>();
            for (int i = 0; i < pieces.Count; i++)
                if (pieces[i].Contains(t) || (i == pieces.Count - 1 && t >= pieces[i].End)) { SelectedIndex = i; break; }
        }
        SeekRequested?.Invoke(t);
    }

    /// <summary>Loads a film strip JPEG without locking the file.</summary>
    public static ImageSource? LoadImage(string? path)
    {
        if (path == null || !System.IO.File.Exists(path)) return null;
        var image = new BitmapImage();
        image.BeginInit();
        image.CacheOption = BitmapCacheOption.OnLoad;
        image.UriSource = new Uri(path);
        image.EndInit();
        image.Freeze();
        return image;
    }
}
