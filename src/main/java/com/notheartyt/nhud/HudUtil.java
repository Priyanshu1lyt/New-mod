package com.notheartyt.nhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

final class HudUtil {
    private HudUtil() {}

    static int alpha(int pct) {
        return (Math.round(MathHelper.clamp(pct, 0, 100) * 2.55f) & 0xFF) << 24;
    }

    static void bg(DrawContext c, int x, int y, int w, int h, int pct) {
        if (pct > 0) c.fill(x, y, x + w, y + h, alpha(pct));
    }

    static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }

    static int durColor(float f) {
        return 0xFF000000 | MathHelper.hsvToRgb(Math.max(0f, f) / 3f, 1f, 1f);
    }

    static String time(int ticks) {
        int s = Math.max(0, ticks / 20);
        return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
    }
}
