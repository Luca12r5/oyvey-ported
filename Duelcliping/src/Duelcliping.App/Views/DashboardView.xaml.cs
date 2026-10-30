using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using Duelcliping.App.ViewModels;

namespace Duelcliping.App.Views;

public partial class DashboardView : UserControl
{
    public DashboardView() => InitializeComponent();

    private void OnTileClick(object sender, MouseButtonEventArgs e)
    {
        if ((sender as FrameworkElement)?.DataContext is ClipCardViewModel card && DataContext is DashboardViewModel vm && vm.OpenClipCommand.CanExecute(card))
            vm.OpenClipCommand.Execute(card);
    }
}
