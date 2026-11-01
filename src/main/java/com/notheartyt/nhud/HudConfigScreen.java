package com.notheartyt.nhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class HudConfigScreen extends Screen {
    private static final int PW = 214, PH = 236;
    private static int tab = 0;

    private final Screen parent;
    private HudWidget dragging;
    private int dragDX, dragDY;
    private boolean wasDown, first = true;

    public HudConfigScreen(Screen parent) {
        super(Text.literal("NotHeartYT HUD"));
        this.parent = parent;
    }

    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

    @Override
    protected void init() {
        HudData.preview = true;
        HudConfig g = HudConfig.INSTANCE;
        int x = px() + 7, y = py() + 22, fw = PW - 14, tw = (fw - 8) / 3;
        String[] names = {"Inventory", "Potion", "Armor"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            ButtonWidget b = ButtonWidget.builder(Text.literal(names[i]), btn -> { tab = idx; clearAndInit(); })
                    .dimensions(x + i * (tw + 4), y, tw, 20).build();
            b.active = tab != i;
            addDrawableChild(b);
        }
        y += 28;

        switch (tab) {
            case 0 -> {
                y = row(y, () -> "Inventory HUD: " + onOff(g.invEnabled), () -> g.invEnabled = !g.invEnabled);
                y = row(y, () -> "Mode: " + (g.invMini ? "Mini" : "Normal"), () -> g.invMini = !g.invMini);
                y = row(y, () -> "Layout: " + (g.invHorizontal ? "Horizontal" : "Vertical"), () -> g.invHorizontal = !g.invHorizontal);
                y = slider(y, "Background", 0, 100, g.invAlpha, v -> g.invAlpha = v);
                y = row(y, () -> "Animations: " + onOff(g.invAnimations), () -> g.invAnimations = !g.invAnimations);
            }
            case 1 -> {
                y = row(y, () -> "Potion HUD: " + onOff(g.potEnabled), () -> g.potEnabled = !g.potEnabled);
                y = row(y, () -> "Mode: " + (g.potMini ? "Mini (duration bar)" : "Normal"), () -> g.potMini = !g.potMini);
                y = row(y, () -> "Layout: " + (g.potHorizontal ? "Horizontal" : "Vertical"), () -> g.potHorizontal = !g.potHorizontal);
                y = slider(y, "Spacing", 0, 12, g.potSpacing, v -> g.potSpacing = v);
                y = slider(y, "Background", 0, 100, g.potAlpha, v -> g.potAlpha = v);
            }
            default -> {
                y = row(y, () -> "Armor HUD: " + onOff(g.armorEnabled), () -> g.armorEnabled = !g.armorEnabled);
                y = slider(y, "Scale %", 50, 200, g.armorScale, v -> g.armorScale = v);
                String[] st = {"Bar", "Percent", "Number"};
                y = row(y, () -> "Damage style: " + st[g.armorStyle % 3], () -> g.armorStyle = (g.armorStyle + 1) % 3);
                String[] eq = {"Helmet", "Chestplate", "Leggings", "Boots", "Main hand", "Off hand"};
                for (int i = 0; i < 6; i += 2) {
                    final int a = i, b = i + 1;
                    half(0, y, () -> eq[a] + ": " + onOff(g.armorShow[a]), () -> g.armorShow[a] = !g.armorShow[a]);
                    half(1, y, () -> eq[b] + ": " + onOff(g.armorShow[b]), () -> g.armorShow[b] = !g.armorShow[b]);
                    y += 22;
                }
                half(0, y, () -> "Free slots: " + onOff(g.armorSlots), () -> g.armorSlots = !g.armorSlots);
                half(1, y, () -> "Arrows: " + onOff(g.armorArrows), () -> g.armorArrows = !g.armorArrows);
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(px() + 7, py() + PH - 27, PW - 14, 20).build());
    }

    private int row(int y, Supplier<String> label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label.get()), b -> {
            action.run();
            HudConfig.save();
            clearAndInit();
        }).dimensions(px() + 7, y, PW - 14, 20).build());
        return y + 22;
    }

    private void half(int col, int y, Supplier<String> label, Runnable action) {
        int w = (PW - 14 - 4) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal(label.get()), b -> {
            action.run();
            HudConfig.save();
            clearAndInit();
        }).dimensions(px() + 7 + col * (w + 4), y, w, 20).build());
    }

    private int slider(int y, String prefix, int min, int max, int val, IntConsumer onChange) {
        addDrawableChild(new IntSlider(px() + 7, y, PW - 14, 20, prefix, min, max, val, onChange));
        return y + 22;
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        if (client.world == null) {
            super.renderBackground(c, mx, my, delta);
            for (HudWidget w : NHudClient.WIDGETS) if (w.enabled()) w.draw(c);
        }
        for (HudWidget w : NHudClient.WIDGETS) {
            if (w.enabled() && w.width() > 0) {
                int col = w == dragging ? 0xFFFFFF55 : 0x90FFFFFF;
                HudUtil.outline(c, w.x() - 1, w.y() - 1, w.width() + 2, w.height() + 2, col);
            }
        }
        c.fill(px(), py(), px() + PW, py() + PH, 0xD0101018);
        HudUtil.outline(c, px(), py(), PW, PH, 0xFF5A5A7A);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("NotHeartYT HUD Config"), width / 2, py() + 7, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag the outlined HUDs to move them"), width / 2, py() + PH + 5, 0xFFAAAAAA);
        handleDrag(mx, my);
    }

    private void handleDrag(int mx, int my) {
        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (first) {
            wasDown = down;
            first = false;
            return;
        }
        boolean inPanel = mx >= px() && mx <= px() + PW && my >= py() && my <= py() + PH;
        if (down && !wasDown && !inPanel) {
            for (int i = NHudClient.WIDGETS.size() - 1; i >= 0; i--) {
                HudWidget w = NHudClient.WIDGETS.get(i);
                if (!w.enabled() || w.width() <= 0) continue;
                if (mx >= w.x() && mx <= w.x() + w.width() && my >= w.y() && my <= w.y() + w.height()) {
                    dragging = w;
                    dragDX = mx - w.x();
                    dragDY = my - w.y();
                    break;
                }
            }
        }
        if (down && dragging != null) {
            int nx = MathHelper.clamp(mx - dragDX, 0, Math.max(0, width - dragging.width()));
            int ny = MathHelper.clamp(my - dragDY, 0, Math.max(0, height - dragging.height()));
            dragging.setRaw(nx, ny);
        }
        if (!down && wasDown && dragging != null) {
            dragging = null;
            HudConfig.save();
        }
        wasDown = down;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void removed() {
        HudData.preview = false;
        HudConfig.save();
    }

    private static class IntSlider extends SliderWidget {
        private final String prefix;
        private final int min, max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, int h, String prefix, int min, int max, int val, IntConsumer onChange) {
            super(x, y, w, h, Text.empty(), (val - min) / (double) (max - min));
            this.prefix = prefix;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        private int get() { return min + (int) Math.round(value * (max - min)); }

        @Override
        protected void updateMessage() {
            if (prefix == null) return;
            setMessage(Text.literal(prefix + ": " + get()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(get());
            HudConfig.save();
        }
    }
}
