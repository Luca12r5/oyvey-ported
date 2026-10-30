namespace Duelcliping.Core.Media;

/// <summary>Low level helpers for MPEG transport stream packets (ISO/IEC 13818-1).</summary>
public static class TsPacket
{
    public const int Size = 188;
    public const byte SyncByte = 0x47;
    public const int PatPid = 0x0000;
    public const int NullPid = 0x1FFF;

    public static int Pid(ReadOnlySpan<byte> p) => ((p[1] & 0x1F) << 8) | p[2];
    public static bool PayloadUnitStart(ReadOnlySpan<byte> p) => (p[1] & 0x40) != 0;
    public static bool HasAdaptation(ReadOnlySpan<byte> p) => (p[3] & 0x20) != 0;
    public static bool HasPayload(ReadOnlySpan<byte> p) => (p[3] & 0x10) != 0;

    public static bool RandomAccessIndicator(ReadOnlySpan<byte> p)
    {
        if (!HasAdaptation(p)) return false;
        int afLength = p[4];
        return afLength > 0 && (p[5] & 0x40) != 0;
    }

    /// <summary>Offset of the payload inside the packet, or -1 when there is none.</summary>
    public static int PayloadOffset(ReadOnlySpan<byte> p)
    {
        if (!HasPayload(p)) return -1;
        int offset = 4;
        if (HasAdaptation(p)) offset += 1 + p[4];
        return offset < Size ? offset : -1;
    }

    /// <summary>Reads the 33-bit PTS of a PES header at the start of <paramref name="payload"/>.</summary>
    public static bool TryReadPesPts(ReadOnlySpan<byte> payload, out long pts)
    {
        pts = 0;
        if (payload.Length < 14) return false;
        if (payload[0] != 0 || payload[1] != 0 || payload[2] != 1) return false;
        int ptsDtsFlags = (payload[7] >> 6) & 0x3;
        if ((ptsDtsFlags & 0x2) == 0) return false;
        pts = ((long)((payload[9] >> 1) & 0x07) << 30)
              | ((long)payload[10] << 22)
              | ((long)(payload[11] >> 1) << 15)
              | ((long)payload[12] << 7)
              | ((long)payload[13] >> 1);
        return true;
    }

    /// <summary>Returns the first program's PMT PID from a PAT packet, or -1.</summary>
    public static int ParsePatForPmtPid(ReadOnlySpan<byte> packet)
    {
        var section = SectionStart(packet);
        if (section.Length < 12 || section[0] != 0x00) return -1;
        int sectionLength = ((section[1] & 0x0F) << 8) | section[2];
        int end = Math.Min(3 + sectionLength - 4, section.Length);
        for (int i = 8; i + 4 <= end; i += 4)
        {
            int program = (section[i] << 8) | section[i + 1];
            int pid = ((section[i + 2] & 0x1F) << 8) | section[i + 3];
            if (program != 0) return pid;
        }
        return -1;
    }

    /// <summary>Returns the PID of the video elementary stream described by a PMT packet, or -1.</summary>
    public static int ParsePmtForVideoPid(ReadOnlySpan<byte> packet)
    {
        var section = SectionStart(packet);
        if (section.Length < 16 || section[0] != 0x02) return -1;
        int sectionLength = ((section[1] & 0x0F) << 8) | section[2];
        int end = Math.Min(3 + sectionLength - 4, section.Length);
        int programInfoLength = ((section[10] & 0x0F) << 8) | section[11];
        int i = 12 + programInfoLength;
        int firstPid = -1, privateAv1Pid = -1;
        while (i + 5 <= end)
        {
            int streamType = section[i];
            int pid = ((section[i + 1] & 0x1F) << 8) | section[i + 2];
            int esInfoLength = ((section[i + 3] & 0x0F) << 8) | section[i + 4];
            if (firstPid < 0) firstPid = pid;
            if (IsVideoStreamType(streamType)) return pid;
            if (streamType == 0x06 && privateAv1Pid < 0 && HasRegistration(section.Slice(i + 5, Math.Min(esInfoLength, end - i - 5)), "AV01"))
                privateAv1Pid = pid;
            i += 5 + esInfoLength;
        }
        return privateAv1Pid >= 0 ? privateAv1Pid : firstPid;
    }

    private static bool IsVideoStreamType(int type) => type is 0x01 or 0x02 or 0x10 or 0x1B or 0x24 or 0x33;

    private static bool HasRegistration(ReadOnlySpan<byte> descriptors, string format)
    {
        int i = 0;
        while (i + 2 <= descriptors.Length)
        {
            int tag = descriptors[i], len = descriptors[i + 1];
            if (tag == 0x05 && len >= 4 && i + 6 <= descriptors.Length &&
                descriptors[i + 2] == format[0] && descriptors[i + 3] == format[1] &&
                descriptors[i + 4] == format[2] && descriptors[i + 5] == format[3])
                return true;
            i += 2 + len;
        }
        return false;
    }

    private static ReadOnlySpan<byte> SectionStart(ReadOnlySpan<byte> packet)
    {
        if (!PayloadUnitStart(packet)) return ReadOnlySpan<byte>.Empty;
        int offset = PayloadOffset(packet);
        if (offset < 0) return ReadOnlySpan<byte>.Empty;
        int pointer = packet[offset];
        int start = offset + 1 + pointer;
        return start < Size ? packet.Slice(start) : ReadOnlySpan<byte>.Empty;
    }
}
