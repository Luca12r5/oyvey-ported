using System.Runtime.InteropServices;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Logging;

namespace Duelcliping.App.Interop;

public sealed record DisplayInfo(int AdapterIndex, int OutputIndex, string DeviceName, string AdapterName, int Left, int Top, int Width, int Height, bool IsPrimary)
{
    public string Label => $"{(IsPrimary ? "Primary · " : "")}{Width}×{Height} · {AdapterName.Trim()} ({DeviceName.Replace(@"\\.\", "")})";
    public MonitorTarget ToTarget() => new(AdapterIndex, OutputIndex, Left, Top, Width, Height, DeviceName);
}

/// <summary>
/// Enumerates monitors the way ffmpeg's ddagrab sees them: DXGI adapter index + output index on that
/// adapter. Using the same enumeration guarantees the selected monitor is the one that is captured.
/// </summary>
public static class DisplayEnumerator
{
    public static IReadOnlyList<DisplayInfo> Enumerate()
    {
        var result = new List<DisplayInfo>();
        IDXGIFactory1? factory = null;
        try
        {
            var iid = typeof(IDXGIFactory1).GUID;
            int hr = CreateDXGIFactory1(ref iid, out var obj);
            if (hr < 0) throw new COMException("CreateDXGIFactory1 failed", hr);
            factory = (IDXGIFactory1)obj;
            for (uint a = 0; factory.EnumAdapters1(a, out var adapter) >= 0; a++)
            {
                try
                {
                    adapter.GetDesc1(out var adesc);
                    if ((adesc.Flags & 2) != 0) continue; // software adapter
                    for (uint o = 0; adapter.EnumOutputs(o, out var output) >= 0; o++)
                    {
                        try
                        {
                            if (output.GetDesc(out var desc) < 0 || desc.AttachedToDesktop == 0) continue;
                            var r = desc.DesktopCoordinates;
                            result.Add(new DisplayInfo((int)a, (int)o, desc.DeviceName, adesc.Description, r.Left, r.Top, r.Width, r.Height,
                                r.Left == 0 && r.Top == 0));
                        }
                        finally { Marshal.ReleaseComObject(output); }
                    }
                }
                finally { Marshal.ReleaseComObject(adapter); }
            }
        }
        catch (Exception ex)
        {
            Log.Error("Displays", "DXGI enumeration failed; falling back to the primary screen", ex);
        }
        finally
        {
            if (factory != null) Marshal.ReleaseComObject(factory);
        }

        if (result.Count == 0)
        {
            var primary = System.Windows.Forms.Screen.PrimaryScreen;
            if (primary != null)
                result.Add(new DisplayInfo(0, 0, primary.DeviceName, "Display", primary.Bounds.Left, primary.Bounds.Top,
                    primary.Bounds.Width, primary.Bounds.Height, true));
        }
        return result;
    }

    public static DisplayInfo? Find(IReadOnlyList<DisplayInfo> displays, string deviceName) =>
        displays.FirstOrDefault(d => !string.IsNullOrEmpty(deviceName) && d.DeviceName == deviceName)
        ?? displays.FirstOrDefault(d => d.IsPrimary)
        ?? displays.FirstOrDefault();

    [DllImport("dxgi.dll")]
    private static extern int CreateDXGIFactory1(ref Guid riid, [MarshalAs(UnmanagedType.IUnknown)] out object factory);

    [StructLayout(LayoutKind.Sequential)]
    private struct Luid { public uint Low; public int High; }

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    private struct DxgiAdapterDesc1
    {
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 128)] public string Description;
        public uint VendorId, DeviceId, SubSysId, Revision;
        public nuint DedicatedVideoMemory, DedicatedSystemMemory, SharedSystemMemory;
        public Luid AdapterLuid;
        public uint Flags;
    }

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    private struct DxgiOutputDesc
    {
        [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 32)] public string DeviceName;
        public NativeMethods.RECT DesktopCoordinates;
        public int AttachedToDesktop;
        public int Rotation;
        public IntPtr Monitor;
    }

    // Vtable order matters; methods that are never called are declared as placeholders.
    [ComImport, Guid("770aae78-f26f-4dba-a829-253c83d1b387"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    private interface IDXGIFactory1
    {
        void SetPrivateData(); void SetPrivateDataInterface(); void GetPrivateData(); void GetParent();
        void EnumAdapters(); void MakeWindowAssociation(); void GetWindowAssociation(); void CreateSwapChain(); void CreateSoftwareAdapter();
        [PreserveSig] int EnumAdapters1(uint index, out IDXGIAdapter1 adapter);
        [PreserveSig] int IsCurrent();
    }

    [ComImport, Guid("29038f61-3839-4626-91fd-086879011a05"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    private interface IDXGIAdapter1
    {
        void SetPrivateData(); void SetPrivateDataInterface(); void GetPrivateData(); void GetParent();
        [PreserveSig] int EnumOutputs(uint index, out IDXGIOutput output);
        void GetDesc(); void CheckInterfaceSupport();
        [PreserveSig] int GetDesc1(out DxgiAdapterDesc1 desc);
    }

    [ComImport, Guid("ae02eedb-c735-4690-8d52-5a8dc20213aa"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    private interface IDXGIOutput
    {
        void SetPrivateData(); void SetPrivateDataInterface(); void GetPrivateData(); void GetParent();
        [PreserveSig] int GetDesc(out DxgiOutputDesc desc);
    }
}
