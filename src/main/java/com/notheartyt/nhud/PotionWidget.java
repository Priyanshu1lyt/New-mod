package com.notheartyt.nhud;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;

class PotionWidget extends HudWidget {
    private static final int PAD = 3;

    private HudConfig g() { return HudConfig.INSTANCE; }
    private int timeW() { return mc.textRenderer.getWidth("88:88"); }

    private int entryW() {
        if (g().potMini) return 12;
        return g().potHorizontal ? Math.max(18, timeW()) : 18 + 4 + timeW();
    }

    private int entryH() {
        if (g().potMini) return 15;
        return g().potHorizontal ? 18 + 1 + 9 : 18;
    }

    @Override boolean enabled() { return g().potEnabled; }

    @Override
    int width() {
        int n = HudData.effects().size();
        if (n == 0) return 0;
        int sp = g().potSpacing;
        return PAD * 2 + (g().potHorizontal ? n * entryW() + (n - 1) * sp : entryW());
    }

    @Override
    int height() {
        int n = HudData.effects().size();
        if (n == 0) return 0;
        int sp = g().potSpacing;
        return PAD * 2 + (g().potHorizontal ? entryH() : n * entryH() + (n - 1) * sp);
    }

    @Override int rawX() { return g().potX; }
    @Override int rawY() { return g().potY; }
    @Override void setRaw(int x, int y) { g().potX = x; g().potY = y; }
    @Override int defaultX(int sw, int sh) { return 4; }
    @Override int defaultY(int sw, int sh) { return 4; }

    private static Identifier icon(RegistryEntry<StatusEffect> e) {
        return e.getKey().map(k -> k.getValue().withPrefixedPath("mob_effect/"))
                .orElse(Identifier.of("minecraft", "mob_effect/speed"));
    }

    @Override
    void render(DrawContext c, int x, int y) {
        HudConfig g = g();
        List<HudData.Fx> fx = HudData.effects();
        HudUtil.bg(c, x, y, width(), height(), g.potAlpha);

        int ew = entryW(), eh = entryH(), sp = g.potSpacing;
        for (int i = 0; i < fx.size(); i++) {
            HudData.Fx f = fx.get(i);
            int ex = x + PAD + (g.potHorizontal ? i * (ew + sp) : 0);
            int ey = y + PAD + (g.potHorizontal ? 0 : i * (eh + sp));
            float frac = f.inf() ? 1f : Math.min(1f, f.dur() / (float) Math.max(1, f.max()));
            Identifier tex = icon(f.effect());

            if (g.potMini) {
                c.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, ex, ey, 12, 12);
                c.fill(ex, ey + 13, ex + 12, ey + 15, 0xFF000000);
                int fw = Math.round(12 * frac);
                if (fw > 0) c.fill(ex, ey + 13, ex + fw, ey + 15, HudUtil.durColor(frac * 0.9f));
            } else {
                String t = f.inf() ? "**" : HudUtil.time(f.dur());
                int col = (!f.inf() && f.dur() < 200) ? 0xFFFF5555 : 0xFFFFFFFF;
                int ix = g.potHorizontal ? ex + (ew - 18) / 2 : ex;
                c.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, ix, ey, 18, 18);
                if (f.amp() > 0) {
                    String a = String.valueOf(f.amp() + 1);
                    c.drawText(mc.textRenderer, a, ix + 18 - mc.textRenderer.getWidth(a), ey + 10, 0xFFFFFFFF, true);
                }
                if (g.potHorizontal) {
                    c.drawText(mc.textRenderer, t, ex + (ew - mc.textRenderer.getWidth(t)) / 2, ey + 19, col, true);
                } else {
                    c.drawText(mc.textRenderer, t, ex + 22, ey + 5, col, true);
                }
            }
        }
    }
}
