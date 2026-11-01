package com.notheartyt.nhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

abstract class HudWidget {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    abstract boolean enabled();
    abstract int width();
    abstract int height();
    abstract int rawX();
    abstract int rawY();
    abstract void setRaw(int x, int y);
    abstract int defaultX(int sw, int sh);
    abstract int defaultY(int sw, int sh);
    abstract void render(DrawContext c, int x, int y);

    int sw() { return mc.getWindow().getScaledWidth(); }
    int sh() { return mc.getWindow().getScaledHeight(); }

    int x() {
        int v = rawX() < 0 ? defaultX(sw(), sh()) : rawX();
        return MathHelper.clamp(v, 0, Math.max(0, sw() - width()));
    }

    int y() {
        int v = rawY() < 0 ? defaultY(sw(), sh()) : rawY();
        return MathHelper.clamp(v, 0, Math.max(0, sh() - height()));
    }

    void draw(DrawContext c) {
        if (width() <= 0 || height() <= 0) return;
        render(c, x(), y());
    }
}
