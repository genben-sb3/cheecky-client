package dev.cheecky.gui;

/** Farben wie die Standardwerte von Wursts ClickGUI. */
public final class Theme {
	public static final int BACKGROUND = 0x404040;
	public static final int ACCENT = 0x101010;
	public static final int ENABLED = 0x00FF00;
	public static final int TEXT = 0xFFF0F0F0;
	public static final float OPACITY = 0.5F;

	private Theme() {}

	public static int withAlpha(int rgb, float opacity) {
		int alpha = Math.round(Math.clamp(opacity, 0, 1) * 255);
		return alpha << 24 | rgb & 0xFFFFFF;
	}

	public static int background(boolean hovering) {
		return withAlpha(BACKGROUND, OPACITY * (hovering ? 1.5F : 1));
	}

	public static int outline() {
		return withAlpha(ACCENT, 0.5F);
	}
}
