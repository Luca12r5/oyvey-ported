using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Threading;
using Duelcliping.App.ViewModels;

namespace Duelcliping.App.Views;

public partial class LibraryView : UserControl
{
    private readonly DispatcherTimer _hoverDelay = new() { Interval = TimeSpan.FromMilliseconds(450) };
    private ClipCardViewModel? _hovered;
    private ClipCardViewModel? _previewing;

    public LibraryView()
    {
        InitializeComponent();
        _hoverDelay.Tick += (_, _) =>
        {
            _hoverDelay.Stop();
            StopPreview();
            if (_hovered == null) return;
            // Only one muted preview plays at a time; it starts after a short hover so scrolling never decodes video.
            _previewing = _hovered;
            _previewing.PreviewUri = new Uri(_previewing.FilePath);
        };
        IsVisibleChanged += (_, e) => { if (!(bool)e.NewValue) StopPreview(); };
    }

    private void StopPreview()
    {
        if (_previewing != null) _previewing.PreviewUri = null;
        _previewing = null;
    }

    private void OnCardEnter(object sender, MouseEventArgs e)
    {
        _hovered = (sender as FrameworkElement)?.DataContext as ClipCardViewModel;
        _hoverDelay.Stop();
        _hoverDelay.Start();
    }

    private void OnCardLeave(object sender, MouseEventArgs e)
    {
        _hovered = null;
        _hoverDelay.Stop();
        StopPreview();
    }

    private void OnCardClick(object sender, MouseButtonEventArgs e)
    {
        if (e.OriginalSource is DependencyObject d && FindParent<ButtonBase>(d) != null) return;
        if ((sender as FrameworkElement)?.DataContext is ClipCardViewModel card && DataContext is LibraryViewModel vm && vm.OpenCommand.CanExecute(card))
        {
            StopPreview();
            vm.OpenCommand.Execute(card);
        }
    }

    private void OnMoreClick(object sender, RoutedEventArgs e)
    {
        var card = FindParent<Border>((DependencyObject)sender, b => b.ContextMenu != null);
        if (card?.ContextMenu == null) return;
        card.ContextMenu.PlacementTarget = card;
        card.ContextMenu.Placement = PlacementMode.MousePoint;
        card.ContextMenu.IsOpen = true;
    }

    private static T? FindParent<T>(DependencyObject child, Func<T, bool>? predicate = null) where T : DependencyObject
    {
        var current = child;
        while (current != null)
        {
            if (current is T match && (predicate == null || predicate(match))) return match;
            current = current is System.Windows.Media.Visual or System.Windows.Media.Media3D.Visual3D
                ? System.Windows.Media.VisualTreeHelper.GetParent(current)
                : LogicalTreeHelper.GetParent(current);
        }
        return null;
    }
}
