using System.Diagnostics;
using System.Runtime.InteropServices;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Recording;

/// <summary>
/// Sets the Windows GPU scheduling priority of a process (the same kernel API the OS and capture
/// tools use). Below normal means the GPU scheduler always prefers the game's work over the
/// capture/encode work of ffmpeg, so recording yields instead of stealing frame time.
/// </summary>
public static class GpuPriority
{
    private const int BelowNormal = 1; // D3DKMT_SCHEDULINGPRIORITYCLASS_BELOW_NORMAL

    [DllImport("gdi32.dll", ExactSpelling = true)]
    private static extern int D3DKMTSetProcessSchedulingPriorityClass(IntPtr process, int priority);

    public static void TrySetBelowNormal(Process process)
    {
        if (!OperatingSystem.IsWindows()) return;
        try
        {
            int status = D3DKMTSetProcessSchedulingPriorityClass(process.Handle, BelowNormal);
            if (status != 0) Log.Warn("Pipeline", $"GPU priority could not be lowered (NTSTATUS 0x{status:X8})");
            else Log.Info("Pipeline", "Encoder GPU priority set to below normal");
        }
        catch (Exception ex) when (ex is EntryPointNotFoundException or DllNotFoundException or InvalidOperationException or System.ComponentModel.Win32Exception)
        {
            Log.Warn("Pipeline", "GPU priority not supported: " + ex.Message);
        }
    }
}
