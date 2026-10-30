using System.ComponentModel;
using System.Windows;
using System.Windows.Interop;
using System.Windows.Threading;
using Duelcliping.App.Interop;
using Duelcliping.App.ViewModels;
using Duelcliping.Core.Settings;

namespace Duelcliping.App.Views;

/// <summary>
/// The main window. It is created only when the user opens Duelcliping and destroyed when closed to
/// the tray, so no visual tree, bitmaps or render work exist while you are playing.
/// </summary>
public partial class MainWindow : Window
{
    private readonly MainViewModel _vm;
    private readonly DispatcherTimer _refresh;
    private IDisposable? _perfLease;

    public MainWindow(MainViewModel vm)
    {
        _vm = vm;
        vm.OwnerWindow = this;
        DataContext = vm;
        InitializeComponent();
        _refresh = new DispatcherTimer { Interval = TimeSpan.FromSeconds(1) };
        _refresh.Tick += (_, _) => RefreshVisiblePage();
        StateChanged += OnWindowStateChanged;
        vm.PageChanged += OnPageChanged;
        vm.App.StatusChanged += OnStatusChanged;
        vm.App.ClipCreated += OnClipCreated;
        vm.App.Performance.Sampled += OnPerfSampled;
        Loaded += async (_, _) =>
        {
            Activate();
            ResumeLiveUpdates();
            await vm.Dashboard.RefreshClipsAsync();
        };
        Closed += (_, _) =>
        {
            PauseLiveUpdates();
            vm.PageChanged -= OnPageChanged;
            vm.App.StatusChanged -= OnStatusChanged;
            vm.App.ClipCreated -= OnClipCreated;
            vm.App.Performance.Sampled -= OnPerfSampled;
            vm.OwnerWindow = null;
        };
    }

    protected override void OnSourceInitialized(EventArgs e)
    {
        base.OnSourceInitialized(e);
        var hwnd = new WindowInteropHelper(this).Handle;
        int dark = _vm.App.Settings.Current.Theme == AppTheme.Dark ? 1 : 0;
        NativeMethods.DwmSetWindowAttribute(hwnd, 20, ref dark, sizeof(int)); // DWMWA_USE_IMMERSIVE_DARK_MODE
        int round = 2;
        NativeMethods.DwmSetWindowAttribute(hwnd, 33, ref round, sizeof(int)); // DWMWA_WINDOW_CORNER_PREFERENCE: round (Win11)
        if (Environment.OSVersion.Version.Build >= 22621)
        {
            // Windows 11: subtle Mica material behind the translucent sidebar and cards.
            int mica = 2;
            if (NativeMethods.DwmSetWindowAttribute(hwnd, 38, ref mica, sizeof(int)) == 0)
            {
                var margins = new NativeMethods.MARGINS { Left = -1, Right = -1, Top = -1, Bottom = -1 };
                NativeMethods.DwmExtendFrameIntoClientArea(hwnd, ref margins);
                HwndSource.FromHwnd(hwnd)!.CompositionTarget!.BackgroundColor = System.Windows.Media.Colors.Transparent;
                SetResourceReference(BackgroundProperty, "WindowTintBrush");
            }
        }
    }

    private void OnWindowStateChanged(object? sender, EventArgs e)
    {
        MaxButton.Content = WindowState == WindowState.Maximized ? "" : "";
        Root.Margin = WindowState == WindowState.Maximized ? new Thickness(7) : new Thickness(0);
        if (WindowState == WindowState.Minimized) PauseLiveUpdates(); else ResumeLiveUpdates();
    }

    private void ResumeLiveUpdates()
    {
        _refresh.Start();
        _perfLease ??= _vm.App.Performance.Acquire();
        RefreshVisiblePage();
    }

    private void PauseLiveUpdates()
    {
        _refresh.Stop();
        _perfLease?.Dispose();
        _perfLease = null;
    }

    private void RefreshVisiblePage()
    {
        _vm.Dashboard.Refresh();
    }

    private async void OnPageChanged(Page page)
    {
        if (page == Page.Library) await _vm.Library.RefreshAsync();
        if (page == Page.Dashboard) await _vm.Dashboard.RefreshClipsAsync();
        if (page == Page.Settings) _vm.Settings.RefreshAll();
    }

    private void OnStatusChanged() => Dispatcher.InvokeAsync(RefreshVisiblePage);

    private void OnPerfSampled(Core.Performance.PerformanceSnapshot s) =>
        Dispatcher.InvokeAsync(() => { if (_vm.Page == Page.Performance) _vm.Performance.Update(s); });

    private void OnClipCreated(string path) => Dispatcher.InvokeAsync(async () =>
    {
        if (_vm.Page == Page.Library) await _vm.Library.RefreshAsync();
        else if (_vm.Page == Page.Dashboard) await _vm.Dashboard.RefreshClipsAsync();
    });

    private void OnMinimize(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void OnMaximize(object sender, RoutedEventArgs e) =>
        WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
    private void OnClose(object sender, RoutedEventArgs e) => Close();
}
