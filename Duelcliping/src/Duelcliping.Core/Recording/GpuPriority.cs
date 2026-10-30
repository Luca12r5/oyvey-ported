using System.Diagnostics;
using System.Runtime.InteropServices;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Recording;

/// <summary>
/// Sets the Windows GPU scheduling priority of the capture process (the kernel API capture tools
/// use). The duplication frame copy is tiny, but it must not wait behind the game's GPU queue,
/// otherwise the desktop compositor stalls while it waits for the frame to be released.
/// </summary>
public static class GpuPriority
{
    private const int AboveNormal = 3; // D3DKMT_SCHEDULINGPRIORITYCLASS_ABOVE_NORMAL

    [DllImport("gdi32.dll", ExactSpelling = true)]
    private static extern int D3DKMTSetProcessSchedulingPriorityClass(IntPtr process, int priority);

    public static void TrySetHigh(Process process)
    {
        if (!OperatingSystem.IsWindows()) return;
        try
        {
            int status = D3DKMTSetProcessSchedulingPriorityClass(process.Handle, AboveNormal);
            if (status != 0) Log.Warn("Pipeline", $"GPU priority could not be set (NTSTATUS 0x{status:X8})");
            else Log.Info("Pipeline", "Capture GPU priority set to above normal (prevents compositor stalls)");
        }
        catch (Exception ex) when (ex is EntryPointNotFoundException or DllNotFoundException or InvalidOperationException or System.ComponentModel.Win32Exception)
        {
            Log.Warn("Pipeline", "GPU priority not supported: " + ex.Message);
        }
    }
}
