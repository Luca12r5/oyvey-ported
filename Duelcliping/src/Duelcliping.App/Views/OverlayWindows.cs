using System.Windows;
using System.Windows.Controls;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.Windows.Threading;
using Duelcliping.App.Interop;
using Duelcliping.App.Services;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;

namespace Duelcliping.App.Views;

/// <summary>
/// The in-game recording indicator. A tiny layered window that is click-through
/// (WS_EX_TRANSPARENT), never activates (WS_EX_NOACTIVATE), is hidden from Alt+Tab and excluded from
/// screen capture. It redraws at most once per second and only when its text changes, so its GPU
/// cost is a single small composition update per second.
/// </summary>
public sealed class RecordingOverlay : Window
{
    private readonly AppController _app;
    private readonly DispatcherTimer _timer = new() { Interval = TimeSpan.FromSeconds(1) };
    private readonly TextBlock _label = new() { FontSize = 12, FontWeight = FontWeights.SemiBold, Foreground = Brushes.White, VerticalAlignment = VerticalAlignment.Center };
    private readonly TextBlock _detail = new() { FontSize = 11, Foreground = new SolidColorBrush(Color.FromArgb(0xCC, 0xFF, 0xFF, 0xFF)), VerticalAlignment = VerticalAlignment.Center, Margin = new Thickness(8, 0, 0, 0) };
    private readonly Ellipse _dot = new() { Width = 8, Height = 8, VerticalAlignment = VerticalAlignment.Center, Margin = new Thickness(0, 0, 8, 0) };
    private DateTime _flashUntil = DateTime.MinValue;
    private string _flashText = "";
    private IntPtr _hwnd;
    private int _topmostCounter;

    public RecordingOverlay(AppController app)
    {
        _app = app;
        WindowStyle = WindowStyle.None;
        AllowsTransparency = true;
        Background = Brushes.Transparent;
        ShowInTaskbar = false;
        ShowActivated = false;
        Topmost = true;
        Focusable = false;
        IsHitTestVisible = false;
        SizeToContent = SizeToContent.WidthAndHeight;
        ResizeMode = ResizeMode.NoResize;
        Left = -10000;
        Top = -10000;
        FontFamily = (FontFamily)Application.Current.FindResource("UiFont");

        var row = new StackPanel { Orientation = Orientation.Horizontal };
        row.Children.Add(_dot);
        row.Children.Add(_label);
        row.Children.Add(_detail);
        Content = new Border
        {
            Background = new SolidColorBrush(Color.FromArgb(0xD9, 0x16, 0x15, 0x14)),
            BorderBrush = new SolidColorBrush(Color.FromArgb(0x26, 0xFF, 0xFF, 0xFF)),
            BorderThickness = new Thickness(1),
            CornerRadius = new CornerRadius(14),
            Padding = new Thickness(12, 6, 14, 6),
            Margin = new Thickness(16),
            Child = row,
        };
        _timer.Tick += (_, _) => Refresh();
        SourceInitialized += (_, _) =>
        {
            _hwnd = new WindowInteropHelper(this).Handle;
            NativeMethods.MakeClickThroughOverlay(_hwnd);
        };
        SizeChanged += (_, _) => PlaceOnMonitor();
    }

    /// <summary>Shows or hides the overlay according to the settings and the engine state.</summary>
    public void Sync()
    {
        var s = _app.Settings.Current;
        var status = _app.Engine.GetStatus();
        bool active = status.State == EngineState.Running || status.IsManualRecording;
        bool show = s.OverlayEnabled && (active || DateTime.UtcNow < _flashUntil);
        Opacity = s.OverlayOpacity;
        if (show)
        {
            if (!IsVisible) Show();
            Refresh();
            if (!_timer.IsEnabled) _timer.Start();
        }
        else
        {
            _timer.Stop();
            if (IsVisible) Hide();
        }
    }

    public void Flash(string text)
    {
        _flashText = text;
        _flashUntil = DateTime.UtcNow.AddSeconds(2.5);
        Sync();
    }

    private void Refresh()
    {
        var status = _app.Engine.GetStatus();
        string label, detail;
        Brush dot;
        if (DateTime.UtcNow < _flashUntil)
        {
            label = _flashText;
            detail = "";
            dot = (Brush)Application.Current.FindResource("SuccessBrush");
        }
        else if (status.IsManualRecording)
        {
            label = "REC";
            detail = AppController.FormatDuration(status.ManualRecordingSeconds) + (_app.Settings.Current.ReplayBufferEnabled ? " · Replay on" : "");
            dot = (Brush)Application.Current.FindResource("RecordingBrush");
        }
        else if (status.State == EngineState.Running)
        {
            label = "REPLAY";
            detail = AppController.FormatDuration(status.BufferedSeconds);
            dot = (Brush)Application.Current.FindResource("AccentBrush");
        }
        else
        {
            Sync();
            return;
        }
        if (_label.Text != label) _label.Text = label;
        if (_detail.Text != detail) _detail.Text = detail;
        if (!ReferenceEquals(_dot.Fill, dot)) _dot.Fill = dot;
        // Fullscreen games may push other topmost windows down; re-assert every few seconds.
        if (_hwnd != IntPtr.Zero && ++_topmostCounter % 5 == 0) NativeMethods.KeepTopmost(_hwnd);
    }

    public void PlaceOnMonitor()
    {
        if (_hwnd == IntPtr.Zero) return;
        var s = _app.Settings.Current;
        var display = DisplayEnumerator.Find(_app.Displays, s.MonitorDeviceName);
        if (display == null) return;
        var dpi = VisualTreeHelper.GetDpi(this);
        int w = (int)(ActualWidth * dpi.DpiScaleX), h = (int)(ActualHeight * dpi.DpiScaleY);
        int x = s.OverlayPosition is OverlayCorner.TopLeft or OverlayCorner.BottomLeft ? display.Left : display.Left + display.Width - w;
        int y = s.OverlayPosition is OverlayCorner.TopLeft or OverlayCorner.TopRight ? display.Top : display.Top + display.Height - h;
        NativeMethods.SetWindowPos(_hwnd, NativeMethods.HWND_TOPMOST, x, y, 0, 0, NativeMethods.SWP_NOSIZE | NativeMethods.SWP_NOACTIVATE);
    }
}

/// <summary>Non-activating notification card in the bottom-right corner; it never steals focus from a game.</summary>
public sealed class ToastWindow : Window
{
    private static readonly List<ToastWindow> Open = new();
    private readonly DispatcherTimer _close = new() { Interval = TimeSpan.FromSeconds(4.5) };

    public ToastWindow(Toast toast, Action<string>? openInEditor)
    {
        WindowStyle = WindowStyle.None;
        AllowsTransparency = true;
        Background = Brushes.Transparent;
        ShowInTaskbar = false;
        ShowActivated = false;
        Topmost = true;
        SizeToContent = SizeToContent.Height;
        Width = 380;
        ResizeMode = ResizeMode.NoResize;
        FontFamily = (FontFamily)Application.Current.FindResource("UiFont");

        string accentKey = toast.Kind switch
        {
            ToastKind.Success => "SuccessBrush",
            ToastKind.Warning => "WarningBrush",
            ToastKind.Error => "DangerBrush",
            _ => "AccentBrush",
        };
        string glyph = toast.Kind switch { ToastKind.Success => "", ToastKind.Warning => "", ToastKind.Error => "", _ => "" };

        var icon = new TextBlock { Text = glyph, FontFamily = (FontFamily)Application.Current.FindResource("IconFont"), FontSize = 16, Margin = new Thickness(0, 2, 12, 0) };
        icon.SetResourceReference(TextBlock.ForegroundProperty, accentKey);
        var title = new TextBlock { Text = toast.Title, FontWeight = FontWeights.SemiBold, FontSize = 13, TextTrimming = TextTrimming.CharacterEllipsis };
        title.SetResourceReference(TextBlock.ForegroundProperty, "TextPrimaryBrush");
        var message = new TextBlock { Text = toast.Message, FontSize = 12, TextWrapping = TextWrapping.Wrap, Margin = new Thickness(0, 2, 0, 0) };
        message.SetResourceReference(TextBlock.ForegroundProperty, "TextSecondaryBrush");
        var text = new StackPanel();
        text.Children.Add(title);
        text.Children.Add(message);

        if (toast.FilePath != null)
        {
            var actions = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(-8, 6, 0, 0) };
            var ghost = (Style)Application.Current.FindResource("GhostButton");
            var path = toast.FilePath;
            if (openInEditor != null && System.IO.File.Exists(path))
            {
                var cut = new Button { Content = "Cut", Style = ghost };
                cut.Click += (_, _) => { openInEditor(path); Close(); };
                actions.Children.Add(cut);
            }
            var show = new Button { Content = "Show in folder", Style = ghost };
            show.Click += (_, _) => { ShellHelpers.RevealInExplorer(path); Close(); };
            actions.Children.Add(show);
            text.Children.Add(actions);
        }

        var row = new DockPanel();
        DockPanel.SetDock(icon, Dock.Left);
        row.Children.Add(icon);
        row.Children.Add(text);
        var card = new Border
        {
            CornerRadius = new CornerRadius(14),
            BorderThickness = new Thickness(1),
            Padding = new Thickness(16, 14, 16, 12),
            Margin = new Thickness(12),
            Child = row,
            Effect = (System.Windows.Media.Effects.Effect)Application.Current.FindResource("SoftShadow"),
        };
        card.SetResourceReference(Border.BackgroundProperty, "SurfaceElevatedBrush");
        card.SetResourceReference(Border.BorderBrushProperty, "BorderBrush");
        card.MouseEnter += (_, _) => _close.Stop();
        card.MouseLeave += (_, _) => _close.Start();
        Content = card;

        _close.Tick += (_, _) => Close();
        SourceInitialized += (_, _) => NativeMethods.MakeNonActivating(new WindowInteropHelper(this).Handle);
        Loaded += (_, _) =>
        {
            Open.Add(this);
            Reposition();
            BeginAnimation(OpacityProperty, new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(160)));
            _close.Start();
        };
        Closed += (_, _) =>
        {
            _close.Stop();
            Open.Remove(this);
            Reposition();
        };
    }

    private static void Reposition()
    {
        var area = SystemParameters.WorkArea;
        double bottom = area.Bottom;
        for (int i = Open.Count - 1; i >= 0; i--)
        {
            var t = Open[i];
            t.Left = area.Right - t.Width;
            t.Top = bottom - t.ActualHeight;
            bottom = t.Top + 4;
        }
        while (Open.Count > 4) Open[0].Close();
    }
}
