using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;

namespace Duelcliping.App.Views;

/// <summary>Small themed modal dialogs built in code (rename prompt, confirmation).</summary>
internal static class DialogFactory
{
    public static Window Create(Window? owner, string title, UIElement content)
    {
        var window = new Window
        {
            Title = title,
            Owner = owner,
            WindowStartupLocation = owner != null ? WindowStartupLocation.CenterOwner : WindowStartupLocation.CenterScreen,
            SizeToContent = SizeToContent.WidthAndHeight,
            ResizeMode = ResizeMode.NoResize,
            WindowStyle = WindowStyle.None,
            AllowsTransparency = true,
            Background = Brushes.Transparent,
            ShowInTaskbar = false,
        };
        var card = new Border
        {
            Padding = new Thickness(24),
            Margin = new Thickness(16),
            CornerRadius = new CornerRadius(16),
            BorderThickness = new Thickness(1),
            Child = content,
            MinWidth = 380,
            MaxWidth = 460,
            Effect = (System.Windows.Media.Effects.Effect)Application.Current.FindResource("SoftShadow"),
        };
        card.SetResourceReference(Border.BackgroundProperty, "SurfaceElevatedBrush");
        card.SetResourceReference(Border.BorderBrushProperty, "BorderBrush");
        card.MouseLeftButtonDown += (_, e) => { if (e.ButtonState == MouseButtonState.Pressed) window.DragMove(); };
        window.Content = card;
        window.KeyDown += (_, e) => { if (e.Key == Key.Escape) window.DialogResult = false; };
        return window;
    }

    public static TextBlock Title(string text) => new() { Text = text, Style = (Style)Application.Current.FindResource("SectionText"), Margin = new Thickness(0, 0, 0, 8) };

    public static TextBlock Body(string text) => new() { Text = text, Style = (Style)Application.Current.FindResource("SecondaryText"), Margin = new Thickness(0, 0, 0, 16) };

    public static StackPanel Buttons(params Button[] buttons)
    {
        var panel = new StackPanel { Orientation = Orientation.Horizontal, HorizontalAlignment = HorizontalAlignment.Right, Margin = new Thickness(0, 18, 0, 0) };
        foreach (var b in buttons) { b.Margin = new Thickness(8, 0, 0, 0); panel.Children.Add(b); }
        return panel;
    }
}

public static class TextPromptWindow
{
    /// <summary>Returns the entered text, or null if cancelled. <paramref name="validate"/> may throw ArgumentException.</summary>
    public static string? Ask(Window? owner, string title, string label, string initial, Func<string, string> validate)
    {
        var box = new TextBox { Text = initial, MinWidth = 340 };
        var error = new TextBlock { Margin = new Thickness(0, 8, 0, 0), TextWrapping = TextWrapping.Wrap, Visibility = Visibility.Collapsed };
        error.SetResourceReference(TextBlock.ForegroundProperty, "DangerBrush");
        var ok = new Button { Content = "Save", IsDefault = true, Style = (Style)Application.Current.FindResource("PrimaryButton") };
        var cancel = new Button { Content = "Cancel", IsCancel = true };
        var panel = new StackPanel();
        panel.Children.Add(DialogFactory.Title(title));
        panel.Children.Add(DialogFactory.Body(label));
        panel.Children.Add(box);
        panel.Children.Add(error);
        panel.Children.Add(DialogFactory.Buttons(cancel, ok));
        var window = DialogFactory.Create(owner, title, panel);
        string? result = null;
        ok.Click += (_, _) =>
        {
            try
            {
                result = validate(box.Text);
                window.DialogResult = true;
            }
            catch (ArgumentException ex)
            {
                error.Text = ex.Message;
                error.Visibility = Visibility.Visible;
            }
        };
        window.Loaded += (_, _) => { box.Focus(); box.SelectAll(); };
        return window.ShowDialog() == true ? result : null;
    }
}

public static class ConfirmWindow
{
    public static bool Ask(Window? owner, string title, string message, string confirmText)
    {
        var ok = new Button { Content = confirmText, IsDefault = true, Style = (Style)Application.Current.FindResource("DangerButton") };
        var cancel = new Button { Content = "Cancel", IsCancel = true };
        var panel = new StackPanel();
        panel.Children.Add(DialogFactory.Title(title));
        panel.Children.Add(DialogFactory.Body(message));
        panel.Children.Add(DialogFactory.Buttons(cancel, ok));
        var window = DialogFactory.Create(owner, title, panel);
        ok.Click += (_, _) => window.DialogResult = true;
        return window.ShowDialog() == true;
    }
}
