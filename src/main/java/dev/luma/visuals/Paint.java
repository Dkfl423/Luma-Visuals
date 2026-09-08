package dev.luma.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Pixel-aligned panels, intentional stepped corners, no private render APIs. */
public final class Paint {
    public static final int TEXT = 0xFFF1F7F5;
    public static final int MUTED = 0xFFABBDB9;
    public static final int LINE = 0xFF30413E;
    public static final int SURFACE = 0xFF131E1C;
    private Paint() { }
    public static int accent() { return LumaVisualsClient.config.accent(); }
    public static int alpha(int color, float opacity) {
        return ((int) Math.round(HudMath.clamp(opacity, 0, 1) * 255) << 24) | (color & 0xFFFFFF);
    }
    public static void box(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (w < 8 || h < 8) { g.fill(x, y, x+w, y+h, color); return; }
        g.fill(x+3, y, x+w-3, y+h, color);
        g.fill(x+1, y+1, x+3, y+h-1, color);
        g.fill(x+w-3, y+1, x+w-1, y+h-1, color);
        g.fill(x, y+3, x+1, y+h-3, color);
        g.fill(x+w-1, y+3, x+w, y+h-3, color);
    }
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        // A solid one-pixel border, with a configurable translucent inner surface.
        box(g, x, y, w, h, alpha(0x0C1513, LumaVisualsClient.config.opacity));
        int c=alpha(LINE,.9f);
        g.fill(x+3,y,x+w-3,y+1,c); g.fill(x+3,y+h-1,x+w-3,y+h,c);
        g.fill(x,y+3,x+1,y+h-3,c); g.fill(x+w-1,y+3,x+w,y+h-3,c);
        g.fill(x+1,y+1,x+3,y+2,c); g.fill(x+w-3,y+1,x+w-1,y+2,c);
        g.fill(x+1,y+h-2,x+3,y+h-1,c); g.fill(x+w-3,y+h-2,x+w-1,y+h-1,c);
    }
    public static void text(GuiGraphics g, String text, int x, int y, int color) {
        g.drawString(Minecraft.getInstance().font, text, x, y, color, false);
    }
    public static void clipped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        var font = Minecraft.getInstance().font;
        String safe = text == null ? "" : text;
        if (font.width(safe) > maxWidth) safe = font.plainSubstrByWidth(safe, Math.max(0, maxWidth-6)) + "…";
        text(g, safe, x, y, color);
    }
    public static void centered(GuiGraphics g, String text, int x, int y, int color) {
        text(g, text, x - Minecraft.getInstance().font.width(text)/2, y, color);
    }
    public static void heading(GuiGraphics g, String text, int x, int y, float scale, int color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y); g.pose().scale(scale, scale);
        text(g, text, 0, 0, color);
        g.pose().popMatrix();
    }
    public static void logo(GuiGraphics g, int x, int y, int color) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 12 - i*3; j++) {
                int px = x + i*5 + (11-j)/3;
                g.fill(px, y+j+i*3, px+3, y+j+i*3+1, color);
            }
        }
    }
    public static void bar(GuiGraphics g, int x, int y, int width, float progress, int color) {
        box(g, x, y, width, 4, 0xFF2A3B36);
        int fill = (int) Math.round(width * HudMath.clamp(progress, 0, 1));
        if (fill > 0) box(g, x, y, fill, 4, color);
    }
}
