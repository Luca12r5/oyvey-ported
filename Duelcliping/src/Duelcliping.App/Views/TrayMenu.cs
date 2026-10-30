using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Shapes;
using Duelcliping.App.Interop;

namespace Duelcliping.App.Views;

public sealed record TrayMenuItem(string Glyph, string Text, Action OnClick, bool IsChecked = false, bool IsDanger = false);

/// <summary>
/// The tray icon's right-click menu in the Moments design (dark, rounded, icons) instead of the
/// white Windows menu. It opens at the cursor, closes when you click anywhere else and is only
/// created while it is shown.
/// </summary>
public sealed class TrayMenu : Window
{
    private readonly int _cursorX, _cursorY;
    private bool _closing;

    public TrayMenu(string header, string status, Brush statusBrush, IReadOnlyList<TrayMenuItem?> items, int cursorX, int cursorY)
    {
        _cursorX = cursorX;
        _cursorY = cursorY;
        WindowStyle = WindowStyle.None;
        AllowsTransparency = true;
        Background = Brushes.Transparent;
        ShowInTaskbar = false;
        Topmost = true;
        ResizeMode = ResizeMode.NoResize;
        SizeToContent = SizeToContent.WidthAndHeight;
        FontFamily = (FontFamily)Application.Current.FindResource("UiFont");
        Left = -10000;
        Top = -10000;

        var panel = new StackPanel { MinWidth = 236 };

        // Header: app name and live replay state
        var head = new DockPanel { Margin = new Thickness(12, 8, 12, 10) };
        var logo = new Image
        {
            Source = new System.Windows.Media.Imaging.BitmapImage(new Uri("pack://application:,,,/Assets/logo.png")),
            Width = 22, Height = 22, Margin = new Thickness(0, 0, 10, 0),
        };
        DockPanel.SetDock(logo, Dock.Left);
        head.Children.Add(logo);
        var titles = new StackPanel();
        titles.Children.Add(Text(header, 13, FontWeights.Bold, "TextPrimaryBrush"));
        var statusRow = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 2, 0, 0) };
        statusRow.Children.Add(new Ellipse { Width = 7, Height = 7, Fill = statusBrush, VerticalAlignment = VerticalAlignment.Center, Margin = new Thickness(0, 0, 6, 0) });
        statusRow.Children.Add(Text(status, 11, FontWeights.Normal, "TextSecondaryBrush"));
        titles.Children.Add(statusRow);
        head.Children.Add(titles);
        panel.Children.Add(head);
        panel.Children.Add(Separator());

        foreach (var item in items)
        {
            if (item == null) { panel.Children.Add(Separator()); continue; }
            panel.Children.Add(Row(item));
        }

        var card = new Border
        {
            CornerRadius = new CornerRadius(12),
            BorderThickness = new Thickness(1),
            Padding = new Thickness(5),
            Margin = new Thickness(10),
            Child = panel,
            Effect = (System.Windows.Media.Effects.Effect)Application.Current.FindResource("SoftShadow"),
        };
        card.SetResourceReference(Border.BackgroundProperty, "SurfaceElevatedBrush");
        card.SetResourceReference(Border.BorderBrushProperty, "BorderBrush");
        Content = card;

        Deactivated += (_, _) => SafeClose();
        KeyDown += (_, e) => { if (e.Key == Key.Escape) SafeClose(); };
        SourceInitialized += (_, _) => NativeMethods.ExcludeFromCapture(new WindowInteropHelper(this).Handle);
        ContentRendered += (_, _) => PlaceAtCursor();
    }

    private static TextBlock Text(string text, double size, FontWeight weight, string brushKey)
    {
        var t = new TextBlock { Text = text, FontSize = size, FontWeight = weight, VerticalAlignment = VerticalAlignment.Center };
        t.SetResourceReference(TextBlock.ForegroundProperty, brushKey);
        return t;
    }

    private static UIElement Separator()
    {
        var b = new Border { Height = 1, Margin = new Thickness(8, 4, 8, 4) };
        b.SetResourceReference(Border.BackgroundProperty, "BorderBrush");
        return b;
    }

    private UIElement Row(TrayMenuItem item)
    {
        var row = new Border { CornerRadius = new CornerRadius(8), Padding = new Thickness(10, 8, 12, 8), Background = Brushes.Transparent, Cursor = Cursors.Hand };
        var grid = new DockPanel();
        var icon = new TextBlock
        {
            Text = item.Glyph, FontSize = 14, Width = 22, VerticalAlignment = VerticalAlignment.Center,
            FontFamily = (FontFamily)Application.Current.FindResource("IconFont"),
        };
        icon.SetResourceReference(TextBlock.ForegroundProperty, item.IsDanger ? "DangerBrush" : item.IsChecked ? "AccentBrush" : "TextSecondaryBrush");
        DockPanel.SetDock(icon, Dock.Left);
        grid.Children.Add(icon);
        if (item.IsChecked)
        {
            var check = new TextBlock { Text = "", FontSize = 12, Margin = new Thickness(16, 0, 0, 0), VerticalAlignment = VerticalAlignment.Center,
                FontFamily = (FontFamily)Application.Current.FindResource("IconFont") };
            check.SetResourceReference(TextBlock.ForegroundProperty, "AccentBrush");
            DockPanel.SetDock(check, Dock.Right);
            grid.Children.Add(check);
        }
        grid.Children.Add(Text(item.Text, 13, FontWeights.SemiBold, item.IsDanger ? "DangerBrush" : "TextPrimaryBrush"));
        row.Child = grid;
        row.MouseEnter += (_, _) => row.SetResourceReference(Border.BackgroundProperty, "SurfaceHoverBrush");
        row.MouseLeave += (_, _) => row.Background = Brushes.Transparent;
        row.MouseLeftButtonUp += (_, _) =>
        {
            SafeClose();
            item.OnClick();
        };
        return row;
    }

    /// <summary>Opens above (or below) the cursor and always fully on the screen that holds the cursor.</summary>
    private void PlaceAtCursor()
    {
        var hwnd = new WindowInteropHelper(this).Handle;
        var dpi = VisualTreeHelper.GetDpi(this);
        int w = (int)(ActualWidth * dpi.DpiScaleX), h = (int)(ActualHeight * dpi.DpiScaleY);
        var screen = System.Windows.Forms.Screen.FromPoint(new System.Drawing.Point(_cursorX, _cursorY)).WorkingArea;
        int x = Math.Clamp(_cursorX - w + 12, screen.Left, screen.Right - w);
        int y = _cursorY - h + 12 >= screen.Top ? _cursorY - h + 12 : _cursorY - 12;
        y = Math.Clamp(y, screen.Top, screen.Bottom - h);
        NativeMethods.SetWindowPos(hwnd, NativeMethods.HWND_TOPMOST, x, y, 0, 0, NativeMethods.SWP_NOSIZE | NativeMethods.SWP_SHOWWINDOW);
        NativeMethods.SetForegroundWindow(hwnd);
        Activate();
    }

    private void SafeClose()
    {
        if (_closing) return;
        _closing = true;
        Close();
    }
}
