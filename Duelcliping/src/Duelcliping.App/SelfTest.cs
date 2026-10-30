using System.IO;
using System.Windows;
using System.Windows.Threading;
using Duelcliping.App.Services;
using Duelcliping.App.ViewModels;
using Duelcliping.App.Views;
using Duelcliping.Core.Library;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.App;

/// <summary>
/// "Duelcliping.exe --selftest [report.txt]": builds the real UI, renders every page in both themes,
/// opens a generated video in the cutter, and checks ffmpeg and encoder detection. Used by CI on a
/// Windows runner so XAML/resource errors fail the build instead of reaching users.
/// </summary>
internal static class SelfTest
{
    public static async Task<int> RunAsync(string[] args)
    {
        var report = args.SkipWhile(a => a != "--selftest").Skip(1).FirstOrDefault() ?? Path.Combine(Path.GetTempPath(), "duelcliping-selftest.txt");
        var lines = new List<string>();
        int failures = 0;
        void Ok(string m) => lines.Add("PASS " + m);
        void Fail(string m) { lines.Add("FAIL " + m); failures++; }

        var bindingErrors = new BindingErrorListener();
        System.Diagnostics.PresentationTraceSources.Refresh();
        System.Diagnostics.PresentationTraceSources.DataBindingSource.Listeners.Add(bindingErrors);
        System.Diagnostics.PresentationTraceSources.DataBindingSource.Switch.Level = System.Diagnostics.SourceLevels.Error;

        AppController? controller = null;
        try
        {
            controller = new AppController();
            Ok("controller created");
            foreach (var theme in new[] { AppTheme.Dark, AppTheme.Light })
            {
                App.ApplyTheme(theme);
                var vm = new MainViewModel(controller);
                var window = new MainWindow(vm) { Left = -4000, Top = -4000, ShowActivated = false };
                window.Show();
                foreach (Page page in Enum.GetValues(typeof(Page)))
                {
                    vm.Page = page;
                    await Dispatcher.Yield(DispatcherPriority.ApplicationIdle);
                    window.UpdateLayout();
                    Ok($"rendered {page} ({theme})");
                }
                window.Close();
            }

            var ffmpegVersion = await FfmpegRunner.RunFfmpegAsync(new[] { "-hide_banner", "-version" });
            if (ffmpegVersion.Success) Ok("ffmpeg: " + ffmpegVersion.StdOut.Split('\n')[0].Trim()); else Fail("ffmpeg not runnable: " + ffmpegVersion.ErrorSummary);

            await controller.Encoders.DetectAsync(forceRefresh: true);
            if (controller.Encoders.Available.Count > 0) Ok("encoders: " + string.Join(", ", controller.Encoders.Available.Select(e => e.FfmpegName)));
            else Fail("no encoder detected");

            // Cutter: generate a video, open it in the editor, cut the middle out, verify the result.
            var dir = Path.Combine(Path.GetTempPath(), "duelcliping-selftest");
            Directory.CreateDirectory(dir);
            var src = Path.Combine(dir, "source.mp4");
            var gen = await FfmpegRunner.RunFfmpegAsync(new[] { "-v", "error", "-y", "-f", "lavfi", "-i", "testsrc2=s=640x360:r=30:d=6",
                "-f", "lavfi", "-i", "sine=d=6", "-c:v", "libx264", "-pix_fmt", "yuv420p", "-c:a", "aac", src });
            if (!gen.Success) Fail("could not generate test video: " + gen.ErrorSummary);
            else
            {
                var editor = new EditorViewModel(controller);
                await editor.LoadAsync(src);
                if (!editor.HasVideo) Fail("editor could not open the test video");
                else
                {
                    editor.Position = TimeSpan.FromSeconds(2);
                    editor.SplitCommand.Execute(null);
                    editor.Position = TimeSpan.FromSeconds(4);
                    editor.SplitCommand.Execute(null);
                    editor.SelectedIndex = 1;
                    editor.RemoveCommand.Execute(null);
                    var segments = new CutPlanProbe(editor).Segments;
                    var output = Path.Combine(dir, "cut.mp4");
                    await ClipEditor.RunAsync(new EditRequest { Input = src, Output = output, Segments = segments }, null, CancellationToken.None);
                    var info = await MediaProbe.ProbeAsync(output);
                    if (info != null && info.DurationSeconds is > 3 and < 5.5) Ok($"cutter output {info.DurationSeconds:0.00}s from 6s source");
                    else Fail($"cutter output has wrong duration: {info?.DurationSeconds}");
                }
            }
        }
        catch (Exception ex)
        {
            Fail("exception: " + ex);
        }
        finally
        {
            if (controller != null) await controller.DisposeAsync();
        }

        foreach (var e in bindingErrors.Errors.Distinct()) Fail("binding: " + e);
        lines.Add(failures == 0 ? "SELFTEST OK" : $"SELFTEST FAILED ({failures})");
        await File.WriteAllLinesAsync(report, lines);
        return failures == 0 ? 0 : 1;
    }

    private sealed class BindingErrorListener : System.Diagnostics.TraceListener
    {
        public List<string> Errors { get; } = new();
        public override void Write(string? message) { }
        public override void WriteLine(string? message)
        {
            // Bindings to ContextMenu PlacementTarget resolve lazily; those are expected before a menu opens.
            if (message != null && !message.Contains("PlacementTarget")) Errors.Add(message);
        }
    }

    /// <summary>Reads the kept segments the editor would export (mirrors what the Save button sends).</summary>
    private sealed class CutPlanProbe
    {
        public CutPlanProbe(EditorViewModel editor) =>
            Segments = editor.Pieces.Where(p => p.Keep).Select(p => new CutSegment(p.Start, p.End)).ToList();
        public IReadOnlyList<CutSegment> Segments { get; }
    }
}
