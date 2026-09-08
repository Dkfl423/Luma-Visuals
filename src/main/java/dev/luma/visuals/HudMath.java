package dev.luma.visuals;

/** Dependency-free geometry; shared by rendering, dragging and smoke tests. */
public final class HudMath {
    private HudMath() { }
    public static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
    public static float effectiveScale(int width, int height, float requested) {
        return (float) Math.min(clamp(requested, .75, 1.3),
                Math.min(Math.max(1, width) / 480.0, Math.max(1, height) / 300.0));
    }
    public static int position(double normalized, int canvas, int size) {
        return (int) Math.round(clamp(normalized, 0, 1) * Math.max(0, canvas - size));
    }
    public static double normalized(double position, int canvas, int size) {
        return canvas <= size ? 0 : clamp(position / (canvas - size), 0, 1);
    }
    public static double smooth(double from, double to, double seconds, boolean reduced) {
        return reduced ? to : from + (to - from) * (1 - Math.exp(-12 * clamp(seconds, 0, .1)));
    }
    public record Box(int x, int y, int width, int height) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
    }
}
