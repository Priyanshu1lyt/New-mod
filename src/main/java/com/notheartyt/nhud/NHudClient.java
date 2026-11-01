package com.notheartyt.nhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NHudClient implements ClientModInitializer {
    public static final List<HudWidget> WIDGETS = new ArrayList<>();
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        HudConfig.load();
        WIDGETS.add(new InventoryWidget());
        WIDGETS.add(new PotionWidget());
        WIDGETS.add(new ArmorWidget());

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nhud.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                KeyBinding.Category.create(Identifier.of("nhud", "main"))));

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.of("nhud", "hud"),
                (ctx, tick) -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null || mc.options.hudHidden) return;
                    for (HudWidget w : WIDGETS) if (w.enabled()) w.draw(ctx);
                });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new HudConfigScreen(null));
            }
        });

        // "Mod" button on the main menu -> opens Config
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Mod"),
                        b -> mc.setScreen(new HudConfigScreen(screen))).dimensions(6, 6, 60, 20).build());
            }
        });
    }
                                               }
