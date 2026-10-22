package dev.visor.vr;

/**
 * How a video wraps around the player in VR. 3D videos carry a picture per eye; the headset shows
 * one eye (the top or left half), at the full resolution of the stream.
 */
public enum VrFormat {
	SPHERE("360°", "360", "full", 0),
	SPHERE_3D("360° 3D", "360tb", "top", 0),
	HALF("180°", "180", "full", 1),
	HALF_3D("180° 3D", "180sbs", "left", 1),
	FLAT("Flat", "", "full", 2);

	public static final int PROJECTION_SPHERE = 0;
	public static final int PROJECTION_HALF = 1;
	public static final int PROJECTION_FLAT = 2;

	/** Shown in the VR controls. */
	public final String label;
	/** The layout name used by the page script ({@code MediaState.format()}). */
	public final String code;
	/** Part of the video the stream carries: "full", "top" or "left". */
	public final String region;
	public final int projection;

	VrFormat(String label, String code, String region, int projection) {
		this.label = label;
		this.code = code;
		this.region = region;
		this.projection = projection;
	}

	public static VrFormat of(String code) {
		for (VrFormat format : values()) {
			if (format.code.equals(code)) {
				return format;
			}
		}
		return FLAT;
	}

	/**
	 * Pixel size of the stream: 2:1 for a full sphere and square for half of one (the same detail
	 * per degree), the video's own shape when flat.
	 */
	public int[] captureSize(boolean high, int videoWidth, int videoHeight) {
		return switch (this.projection) {
			case PROJECTION_SPHERE -> high ? new int[]{3840, 1920} : new int[]{2560, 1280};
			case PROJECTION_HALF -> high ? new int[]{2560, 2560} : new int[]{1920, 1920};
			default -> {
				float aspect = videoWidth > 0 && videoHeight > 0 ? (float) videoWidth / videoHeight : 16.0F / 9.0F;
				int maxWidth = high ? 2560 : 1920;
				int maxHeight = high ? 1440 : 1080;
				int width = Math.min(maxWidth, Math.round(maxHeight * aspect));
				int height = Math.min(maxHeight, Math.round(width / aspect));
				// Whole CSS pixels at the stream's device scale of 1.25.
				yield new int[]{Math.max(5, width / 5 * 5), Math.max(5, height / 5 * 5)};
			}
		};
	}
}
