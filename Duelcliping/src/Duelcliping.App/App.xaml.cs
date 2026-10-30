using System.IO;
using System.Windows;
using System.Windows.Threading;
using Duelcliping.App.Services;
using Duelcliping.App.ViewModels;
using Duelcliping.App.Views;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;
using Forms = System.Windows.Forms;

namespace Duelcliping.App;

public partial class App : Application
{
    private const string InstanceName = "Duelcliping.SingleInstance.v1";
    private Mutex? _instanceMutex;
    private EventWaitHandle? _activateSignal;
    private AppController? _controller;
    private MainViewModel? _mainVm;
    private MainWindow? _window;
    private RecordingOverlay? _overlay;
    private Forms.NotifyIcon? _tray;
    private Forms.ToolStripMenuItem? _trayRecord;
    private Forms.ToolStripMenuItem? _trayReplay;
    private bool _exiting;

    protected override async void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        if (e.Args.Contains("--selftest"))
        {
            Shutdown(await SelfTest.RunAsync(e.Args));
            return;
        }
        _instanceMutex = new Mutex(true, InstanceName, out bool first);
        _activateSignal = new EventWaitHandle(false, EventResetMode.AutoReset, InstanceName + ".Activate");
        if (!first)
        {
            _activateSignal.Set(); // ask the running instance to show its window
            Shutdown();
            return;
        }

        DispatcherUnhandledException += OnDispatcherUnhandledException;
        AppDomain.CurrentDomain.UnhandledException += (_, args) => Log.Error("Crash", "Unhandled exception", args.ExceptionObject as Exception);
        TaskScheduler.UnobservedTaskException += (_, args) => { Log.Error("Task", "Unobserved task exception", args.Exception); args.SetObserved(); };

        try
        {
            _controller = new AppController();
        }
        catch (Exception ex)
        {
            MessageBox.Show("Duelcliping could not start: " + ex.Message, "Duelcliping", MessageBoxButton.OK, MessageBoxImage.Error);
            Shutdown(1);
            return;
        }

        ApplyTheme(_controller.Settings.Current.Theme);
        _mainVm = new MainViewModel(_controller);
        AsyncCommand.ErrorHandler = message => _controller.RaiseToast(new Toast(ToastKind.Error, "Something went wrong", message));
        _controller.ToastRaised += toast => Dispatcher.InvokeAsync(() => ShowToast(toast));
        _controller.OpenRequested += () => Dispatcher.InvokeAsync(ShowMainWindow);
        _controller.StatusChanged += () => Dispatcher.InvokeAsync(OnStatusChanged);
        _controller.Settings.Changed += (old, now) => Dispatcher.InvokeAsync(() => OnSettingsChanged(old, now));
        _controller.ClipCreated += path => Dispatcher.InvokeAsync(() => _overlay?.Flash("Saved"));

        _controller.GameDetector.Changed += active => Dispatcher.InvokeAsync(() =>
        {
            if (active) return;
            while (_deferredToasts.Count > 0) ShowToast(_deferredToasts.Dequeue());
        });
        CreateTray();
        _overlay = new RecordingOverlay(_controller);
        StartActivationListener();

        bool background = e.Args.Contains("--background") || e.Args.Contains("--minimized");
        if (!(background && _controller.Settings.Current.StartMinimized)) ShowMainWindow();

        await _controller.StartAsync();
        OnStatusChanged();
    }

    private void StartActivationListener()
    {
        var thread = new Thread(() =>
        {
            while (!_exiting)
            {
                if (_activateSignal!.WaitOne(1000) && !_exiting) Dispatcher.InvokeAsync(ShowMainWindow);
            }
        }) { IsBackground = true, Name = "Duelcliping.Activation", Priority = ThreadPriority.Lowest };
        thread.Start();
    }

    public static void ApplyTheme(AppTheme theme)
    {
        var dictionaries = Current.Resources.MergedDictionaries;
        var palette = new ResourceDictionary
        {
            Source = new Uri(theme == AppTheme.Light ? "Theme/Colors.Light.xaml" : "Theme/Colors.Dark.xaml", UriKind.Relative),
        };
        dictionaries[0] = palette;
    }

    private void ShowMainWindow()
    {
        if (_mainVm == null) return;
        if (_window == null)
        {
            _window = new MainWindow(_mainVm);
            _window.Closing += OnWindowClosing;
            _window.Closed += (_, _) =>
            {
                _window = null;
                // Give back the memory of the visual tree, thumbnails and the media player.
                GC.Collect(2, GCCollectionMode.Optimized, false);
            };
            _window.Show();
        }
        if (_window.WindowState == WindowState.Minimized) _window.WindowState = WindowState.Normal;
        _window.Activate();
        _window.Topmost = true;  // reliably bring to front when triggered by a hotkey
        _window.Topmost = false;
        _window.Focus();
    }

    private void OnWindowClosing(object? sender, System.ComponentModel.CancelEventArgs e)
    {
        if (_exiting || _controller == null) return;
        if (!_controller.Settings.Current.CloseToTray)
        {
            e.Cancel = true;
            _ = ExitAsync();
            return;
        }
        if (_controller.Engine.IsManualRecording || _controller.Settings.Current.ReplayBufferEnabled)
            _tray?.ShowBalloonTip(2500, "Duelcliping keeps running", "Clips can still be saved with your hotkeys. Right-click the tray icon to exit.", Forms.ToolTipIcon.None);
    }

    private void OnStatusChanged()
    {
        if (_controller == null) return;
        _overlay?.Sync();
        var status = _controller.Engine.GetStatus();
        if (_trayRecord != null) _trayRecord.Text = status.IsManualRecording ? "Stop recording" : "Start recording";
        if (_trayReplay != null) _trayReplay.Checked = _controller.Settings.Current.ReplayBufferEnabled;
        if (_tray != null)
        {
            var text = "Duelcliping · " + (status.IsManualRecording ? "Recording" : status.State switch
            {
                EngineState.Running => "Replay buffer on",
                EngineState.Faulted => "Problem – open for details",
                EngineState.Starting or EngineState.Restarting => "Starting",
                _ => "Idle",
            });
            _tray.Text = text.Length > 63 ? text[..63] : text;
        }
    }

    private void OnSettingsChanged(AppSettings old, AppSettings now)
    {
        if (old.Theme != now.Theme)
        {
            ApplyTheme(now.Theme);
            if (_window != null)
            {
                // DWM title bar colors are applied once per window; recreate it to switch cleanly.
                var page = _mainVm!.Page;
                _window.Closing -= OnWindowClosing;
                _window.Close();
                _window = null;
                ShowMainWindow();
                _mainVm.Page = page;
            }
        }
        if (old.OverlayPosition != now.OverlayPosition || old.MonitorDeviceName != now.MonitorDeviceName) _overlay?.PlaceOnMonitor();
        OnStatusChanged();
    }

    private readonly Queue<Toast> _deferredToasts = new();

    private void ShowToast(Toast toast)
    {
        // A popup over a fullscreen game forces desktop composition and costs FPS: stay silent
        // while playing and show only problems once the game is no longer in focus.
        if (_controller!.Settings.Current.QuietWhileGaming && _controller.GameDetector.IsGameActive)
        {
            if (toast.Kind is ToastKind.Error or ToastKind.Warning && _deferredToasts.Count < 5) _deferredToasts.Enqueue(toast);
            return;
        }
        try
        {
            new ToastWindow(toast, path =>
            {
                ShowMainWindow();
                _ = _mainVm!.OpenInEditorAsync(path);
            }).Show();
        }
        catch (Exception ex)
        {
            Log.Warn("UI", "Toast failed: " + ex.Message);
        }
    }

    private void CreateTray()
    {
        var menu = new Forms.ContextMenuStrip();
        menu.Items.Add("Open Duelcliping", null, (_, _) => ShowMainWindow());
        menu.Items.Add(new Forms.ToolStripSeparator());
        menu.Items.Add("Save last 30 seconds", null, (_, _) => _ = _controller!.SaveClipAsync(30));
        menu.Items.Add("Save last 60 seconds", null, (_, _) => _ = _controller!.SaveClipAsync(60));
        _trayRecord = new Forms.ToolStripMenuItem("Start recording", null, (_, _) => _ = _controller!.ToggleManualRecordingAsync());
        menu.Items.Add(_trayRecord);
        _trayReplay = new Forms.ToolStripMenuItem("Replay buffer", null, (_, _) => _controller!.SetReplayEnabled(!_controller.Settings.Current.ReplayBufferEnabled));
        menu.Items.Add(_trayReplay);
        menu.Items.Add(new Forms.ToolStripSeparator());
        menu.Items.Add("Open clips folder", null, (_, _) => ShellHelpers.RevealInExplorer(_controller!.Settings.Current.ClipsDirectory));
        menu.Items.Add("Exit", null, (_, _) => _ = ExitAsync());

        System.Drawing.Icon icon;
        try
        {
            var stream = GetResourceStream(new Uri("pack://application:,,,/Assets/duelcliping.ico"))!.Stream;
            icon = new System.Drawing.Icon(stream);
        }
        catch
        {
            icon = System.Drawing.SystemIcons.Application;
        }
        _tray = new Forms.NotifyIcon { Icon = icon, Text = "Duelcliping", Visible = true, ContextMenuStrip = menu };
        _tray.MouseClick += (_, args) => { if (args.Button == Forms.MouseButtons.Left) ShowMainWindow(); };
    }

    private async Task ExitAsync()
    {
        if (_exiting) return;
        _exiting = true;
        _window?.Close();
        _overlay?.Close();
        if (_tray != null)
        {
            _tray.Visible = false;
            _tray.Dispose();
        }
        if (_controller != null) await _controller.DisposeAsync();
        Shutdown();
    }

    private void OnDispatcherUnhandledException(object sender, DispatcherUnhandledExceptionEventArgs e)
    {
        Log.Error("UI", "Unhandled UI exception", e.Exception);
        e.Handled = true; // keep recording alive; the error is logged and shown
        _controller?.RaiseToast(new Toast(ToastKind.Error, "Unexpected error", e.Exception.Message + " (details in the log)"));
    }

    protected override void OnExit(ExitEventArgs e)
    {
        _exiting = true;
        Log.Flush(TimeSpan.FromSeconds(1));
        _instanceMutex?.Dispose();
        base.OnExit(e);
    }
}
