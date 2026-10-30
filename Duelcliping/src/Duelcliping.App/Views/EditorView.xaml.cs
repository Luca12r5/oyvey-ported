using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Threading;
using Duelcliping.App.ViewModels;
using Duelcliping.Core.Library;

namespace Duelcliping.App.Views;

public partial class EditorView : UserControl
{
    private readonly DispatcherTimer _timer;
    private EditorViewModel? _vm;
    private bool _playing;

    public EditorView()
    {
        InitializeComponent();
        // Only ticks while playing; the editor costs nothing when it is idle or not visible.
        _timer = new DispatcherTimer(DispatcherPriority.Render) { Interval = TimeSpan.FromMilliseconds(33) };
        _timer.Tick += OnTick;
        DataContextChanged += OnDataContextChanged;
        Timeline.SeekRequested += t => SeekPlayer(t);
        PreviewKeyDown += OnKeyDown;
        Drop += OnDrop;
        Unloaded += (_, _) => Pause();
        IsVisibleChanged += (_, e) => { if (!(bool)e.NewValue) Pause(); };
    }

    private void OnDataContextChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        if (_vm != null)
        {
            _vm.SeekRequested -= SeekPlayer;
            _vm.ReleaseMediaRequested -= ReleaseMedia;
            _vm.PropertyChanged -= OnVmPropertyChanged;
        }
        _vm = DataContext as EditorViewModel;
        if (_vm == null) return;
        _vm.SeekRequested += SeekPlayer;
        _vm.ReleaseMediaRequested += ReleaseMedia;
        _vm.PropertyChanged += OnVmPropertyChanged;
        if (_vm.SourceUri != null) Player.Source = _vm.SourceUri;
    }

    private void OnVmPropertyChanged(object? sender, System.ComponentModel.PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(EditorViewModel.SourceUri) && _vm?.SourceUri != null)
        {
            Pause();
            Player.Source = _vm.SourceUri;
            Player.Play();   // opening requires Play/Pause in Manual mode to render the first frame
            Player.Pause();
            Focus();
        }
    }

    private void ReleaseMedia()
    {
        Pause();
        Player.Close();
        Player.Source = null;
    }

    private void OnMediaOpened(object sender, RoutedEventArgs e) => SeekPlayer(_vm?.Position ?? TimeSpan.Zero);

    private void OnMediaEnded(object sender, RoutedEventArgs e) => Pause();

    private void OnMediaFailed(object sender, ExceptionRoutedEventArgs e)
    {
        Pause();
        if (_vm != null)
            System.Windows.MessageBox.Show(Window.GetWindow(this)!,
                "Windows cannot preview this video (" + e.ErrorException.Message + "). Cutting still works; for HEVC/AV1 install the matching Windows video extension.",
                "Preview unavailable", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    private void OnTick(object? sender, EventArgs e)
    {
        if (_vm == null || !_playing) return;
        var jump = _vm.PlaybackTick(Player.Position);
        if (jump is { } target)
        {
            if (target >= _vm.Duration) { Pause(); return; }
            Player.Position = target;
        }
    }

    private void SeekPlayer(TimeSpan t)
    {
        if (Player.Source == null) return;
        Player.Position = t;
    }

    private void Play()
    {
        if (_vm is not { HasVideo: true }) return;
        if (_vm.Position >= _vm.Duration - TimeSpan.FromMilliseconds(100)) _vm.Seek(TimeSpan.Zero);
        if (_vm.PreviewCut && _vm.PlaybackTick(_vm.Position) is { } start && start < _vm.Duration) _vm.Seek(start);
        Player.Play();
        _playing = true;
        _vm.IsPlaying = true;
        PlayGlyph.Text = "";
        _timer.Start();
    }

    private void Pause()
    {
        if (!_playing) return;
        Player.Pause();
        _playing = false;
        if (_vm != null) _vm.IsPlaying = false;
        PlayGlyph.Text = "";
        _timer.Stop();
    }

    private void OnPlayPause(object sender, RoutedEventArgs e)
    {
        if (_playing) Pause(); else Play();
        Focus();
    }

    private void OnPlayerClick(object sender, MouseButtonEventArgs e) => OnPlayPause(sender, e);
    private void OnBack(object sender, RoutedEventArgs e) => _vm?.Nudge(-5);
    private void OnForward(object sender, RoutedEventArgs e) => _vm?.Nudge(5);

    private async void OnOpenFile(object sender, RoutedEventArgs e)
    {
        var dialog = new Microsoft.Win32.OpenFileDialog
        {
            Title = "Open a video to cut",
            Filter = "Videos|*.mp4;*.mkv;*.mov;*.ts;*.webm;*.avi|All files|*.*",
        };
        if (dialog.ShowDialog(Window.GetWindow(this)) == true && _vm != null) await _vm.LoadAsync(dialog.FileName);
    }

    private async void OnDrop(object sender, DragEventArgs e)
    {
        if (_vm == null || !e.Data.GetDataPresent(DataFormats.FileDrop)) return;
        var files = (string[])e.Data.GetData(DataFormats.FileDrop);
        var video = files.FirstOrDefault(f => ClipLibrary.VideoExtensions.Contains(System.IO.Path.GetExtension(f), StringComparer.OrdinalIgnoreCase)
                                              || f.EndsWith(".webm", StringComparison.OrdinalIgnoreCase));
        if (video != null) await _vm.LoadAsync(video);
    }

    private void OnKeyDown(object sender, KeyEventArgs e)
    {
        if (_vm is not { HasVideo: true } || e.OriginalSource is TextBox) return;
        bool ctrl = Keyboard.Modifiers.HasFlag(ModifierKeys.Control);
        switch (e.Key)
        {
            case Key.Space: OnPlayPause(this, e); break;
            case Key.S when !ctrl: Execute(_vm.SplitCommand); break;
            case Key.Delete or Key.Back: Execute(_vm.RemoveCommand); break;
            case Key.K: Execute(_vm.KeepCommand); break;
            case Key.I: Execute(_vm.MarkInCommand); break;
            case Key.O: Execute(_vm.MarkOutCommand); break;
            case Key.Z when ctrl: Execute(_vm.UndoCommand); break;
            case Key.Y when ctrl: Execute(_vm.RedoCommand); break;
            case Key.Left: _vm.Nudge(Keyboard.Modifiers.HasFlag(ModifierKeys.Shift) ? -1 / 30.0 : -5); break;
            case Key.Right: _vm.Nudge(Keyboard.Modifiers.HasFlag(ModifierKeys.Shift) ? 1 / 30.0 : 5); break;
            case Key.Home: _vm.Seek(TimeSpan.Zero); break;
            case Key.End: _vm.Seek(_vm.Duration); break;
            default: return;
        }
        e.Handled = true;
    }

    private static void Execute(ICommand command)
    {
        if (command.CanExecute(null)) command.Execute(null);
    }
}
