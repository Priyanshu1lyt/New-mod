package com.notheartyt.nhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

class ArmorWidget extends HudWidget {
    private static final int LW = 3 + 18 + 4 + 40 + 3;

    private HudConfig g() { return HudConfig.INSTANCE; }
    private float scale() { return g().armorScale / 100f; }

    private int lines() {
        ItemStack[] eq = HudData.equipment();
        int n = 0;
        for (int i = 0; i < 6; i++) if (g().armorShow[i] && !eq[i].isEmpty()) n++;
        if (g().armorSlots) n++;
        if (g().armorArrows) n++;
        return n;
    }

    @Override boolean enabled() { return g().armorEnabled; }
    @Override int width() { return lines() == 0 ? 0 : (int) Math.ceil(LW * scale()); }
    @Override int height() { return lines() == 0 ? 0 : (int) Math.ceil((6 + lines() * 18) * scale()); }
    @Override int rawX() { return g().armorX; }
    @Override int rawY() { return g().armorY; }
    @Override void setRaw(int x, int y) { g().armorX = x; g().armorY = y; }
    @Override int defaultX(int sw, int sh) { return sw - width() - 4; }
    @Override int defaultY(int sw, int sh) { return sh / 2 - height() / 2; }

    @Override
    void render(DrawContext c, int x, int y) {
        HudConfig g = g();
        float s = scale();
        ItemStack[] eq = HudData.equipment();

        HudUtil.bg(c, x, y, width(), height(), 40);
        c.getMatrices().pushMatrix();
        c.getMatrices().translate((float) x, (float) y);
        c.getMatrices().scale(s, s);

        int row = 0;
        for (int i = 0; i < 6; i++) {
            if (!g.armorShow[i] || eq[i].isEmpty()) continue;
            ItemStack st = eq[i];
            int ry = 3 + row * 18;
            c.drawItem(st, 3, ry + 1);
            drawIndicator(c, st, 3 + 18 + 4, ry);
            row++;
        }
        if (g.armorSlots) {
            int ry = 3 + row * 18;
            c.drawItem(new ItemStack(Items.CHEST), 3, ry + 1);
            c.drawText(mc.textRenderer, HudData.freeSlots() + " free", 25, ry + 5, 0xFFFFFFFF, true);
            row++;
        }
        if (g.armorArrows) {
            int ry = 3 + row * 18;
            c.drawItem(new ItemStack(Items.ARROW), 3, ry + 1);
            c.drawText(mc.textRenderer, "x" + HudData.arrows(), 25, ry + 5, 0xFFFFFFFF, true);
        }
        c.getMatrices().popMatrix();
    }

    private void drawIndicator(DrawContext c, ItemStack st, int x, int ry) {
        if (!st.isDamageable()) {
            if (st.getCount() > 1) c.drawText(mc.textRenderer, "x" + st.getCount(), x, ry + 5, 0xFFFFFFFF, true);
            return;
        }
        int max = st.getMaxDamage();
        int left = Math.max(0, max - st.getDamage());
        float frac = max <= 0 ? 0f : left / (float) max;
        int col = HudUtil.durColor(frac);
        switch (g().armorStyle) {
            case 1 -> c.drawText(mc.textRenderer, Math.round(frac * 100) + "%", x, ry + 5, col, true);
            case 2 -> c.drawText(mc.textRenderer, String.valueOf(left), x, ry + 5, col, true);
            default -> {
                c.fill(x, ry + 7, x + 40, ry + 11, 0xFF000000);
                int fw = Math.round(38 * frac);
                if (fw > 0) c.fill(x + 1, ry + 8, x + 1 + fw, ry + 10, col);
            }
        }
    }
}
