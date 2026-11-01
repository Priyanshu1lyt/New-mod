package com.notheartyt.nhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class HudConfig {
    public static HudConfig INSTANCE = new HudConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Inventory HUD
    public boolean invEnabled = true;
    public boolean invMini = false;
    public boolean invHorizontal = true;
    public boolean invAnimations = true;
    public int invAlpha = 50;
    public int invX = -1, invY = -1;

    // Potion HUD
    public boolean potEnabled = true;
    public boolean potMini = false;
    public boolean potHorizontal = false;
    public int potSpacing = 3;
    public int potAlpha = 50;
    public int potX = -1, potY = -1;

    // Armor HUD
    public boolean armorEnabled = true;
    public int armorScale = 100;      // percent
    public int armorStyle = 0;        // 0 = bar, 1 = percent, 2 = number
    public boolean[] armorShow = {true, true, true, true, true, true}; // head, chest, legs, feet, main hand, off hand
    public boolean armorSlots = true;
    public boolean armorArrows = true;
    public int armorX = -1, armorY = -1;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("nhud.json");
    }

    public static void load() {
        try {
            if (Files.exists(path())) {
                HudConfig c = GSON.fromJson(Files.readString(path()), HudConfig.class);
                if (c != null) {
                    if (c.armorShow == null || c.armorShow.length != 6) c.armorShow = new boolean[]{true, true, true, true, true, true};
                    INSTANCE = c;
                }
            }
        } catch (Exception e) {
            INSTANCE = new HudConfig();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (Exception ignored) {
        }
    }
}
