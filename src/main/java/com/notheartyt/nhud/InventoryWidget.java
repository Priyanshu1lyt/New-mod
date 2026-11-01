package com.notheartyt.nhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

class InventoryWidget extends HudWidget {
    private final ItemStack[] last = new ItemStack[27];
    private final long[] changed = new long[27];

    private HudConfig g() { return HudConfig.INSTANCE; }
    private float scale() { return g().invMini ? 0.67f : 1f; }
    private int cols() { return g().invHorizontal ? 9 : 3; }
    private int rows() { return g().invHorizontal ? 3 : 9; }

    @Override boolean enabled() { return g().invEnabled; }
    @Override int width() { return (int) Math.ceil((cols() * 18 + 6) * scale()); }
    @Override int height() { return (int) Math.ceil((rows() * 18 + 6) * scale()); }
    @Override int rawX() { return g().invX; }
    @Override int rawY() { return g().invY; }
    @Override void setRaw(int x, int y) { g().invX = x; g().invY = y; }
    @Override int defaultX(int sw, int sh) { return 4; }
    @Override int defaultY(int sw, int sh) { return sh / 2 - height() / 2; }

    @Override
    void render(DrawContext c, int x, int y) {
        HudConfig g = g();
        float s = scale();
        boolean mini = g.invMini;
        ItemStack[] st = HudData.inv();
        long now = Util.getMeasuringTimeMs();

        HudUtil.bg(c, x, y, width(), height(), g.invAlpha);
        c.getMatrices().pushMatrix();
        c.getMatrices().translate((float) x, (float) y);
        c.getMatrices().scale(s, s);

        for (int i = 0; i < 27; i++) {
            int r = i / 9, col = i % 9;
            int cx = g.invHorizontal ? col : r;
            int cy = g.invHorizontal ? r : col;
            int sx = 3 + cx * 18, sy = 3 + cy * 18;
            ItemStack cur = st[i];

            c.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0x28FFFFFF);

            if (g.invAnimations) {
                if (last[i] == null || !ItemStack.areEqual(last[i], cur)) {
                    if (last[i] != null) changed[i] = now;
                    last[i] = cur.copy();
                }
                float a = 1f - (now - changed[i]) / 450f;
                if (a > 0f) c.fill(sx + 1, sy + 1, sx + 17, sy + 17, (((int) (a * 120)) << 24) | 0xFFFFFF);
            } else {
                last[i] = null;
            }

            if (!cur.isEmpty()) {
                c.drawItem(cur, sx + 1, sy + 1);
                if (!mini) c.drawStackOverlay(mc.textRenderer, cur, sx + 1, sy + 1);
            }
        }
        c.getMatrices().popMatrix();
    }
}
