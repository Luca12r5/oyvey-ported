using System.Windows;
using Duelcliping.App.Services;

namespace Duelcliping.App.ViewModels;

public enum Page { Dashboard, Library, Editor, Performance, Settings }

public sealed class MainViewModel : ObservableObject
{
    private readonly AppController _app;
    private Page _page = Page.Dashboard;

    public MainViewModel(AppController app)
    {
        _app = app;
        Dashboard = new DashboardViewModel(app, this);
        Library = new LibraryViewModel(app, this);
        Editor = new EditorViewModel(app);
        Settings = new SettingsViewModel(app);
        Performance = new PerformanceViewModel(app);
    }

    public AppController App => _app;
    public DashboardViewModel Dashboard { get; }
    public LibraryViewModel Library { get; }
    public EditorViewModel Editor { get; }
    public SettingsViewModel Settings { get; }
    public PerformanceViewModel Performance { get; }
    public Window? OwnerWindow { get; set; }

    public Page Page
    {
        get => _page;
        set
        {
            if (!Set(ref _page, value)) return;
            OnPropertiesChanged(nameof(IsDashboard), nameof(IsLibrary), nameof(IsEditor), nameof(IsPerformance), nameof(IsSettings));
            PageChanged?.Invoke(value);
        }
    }

    public event Action<Page>? PageChanged;

    public bool IsDashboard { get => Page == Page.Dashboard; set { if (value) Page = Page.Dashboard; } }
    public bool IsLibrary { get => Page == Page.Library; set { if (value) Page = Page.Library; } }
    public bool IsEditor { get => Page == Page.Editor; set { if (value) Page = Page.Editor; } }
    public bool IsPerformance { get => Page == Page.Performance; set { if (value) Page = Page.Performance; } }
    public bool IsSettings { get => Page == Page.Settings; set { if (value) Page = Page.Settings; } }

    public void Navigate(Page page) => Page = page;

    public async Task OpenInEditorAsync(string path)
    {
        Page = Page.Editor;
        await Editor.LoadAsync(path);
    }
}
