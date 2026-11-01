package com.notheartyt.nhud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

/** Reads live game data, or fake sample data while the config screen is open. */
final class HudData {
    private HudData() {}

    /** true while the config screen is open (shows sample data when there is nothing real to show) */
    static boolean preview = false;

    record Fx(RegistryEntry<StatusEffect> effect, int dur, int max, int amp, boolean inf) {}

    private static final Map<RegistryEntry<StatusEffect>, int[]> TRACK = new HashMap<>();
    static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    static PlayerEntity player() {
        return MinecraftClient.getInstance().player;
    }

    // ---------- inventory ----------
    static ItemStack[] inv() {
        ItemStack[] r = new ItemStack[27];
        PlayerEntity p = player();
        Item[] s = {Items.DIAMOND, Items.COBBLESTONE, Items.OAK_LOG, Items.GOLDEN_APPLE, Items.IRON_INGOT, Items.TORCH};
        for (int i = 0; i < 27; i++) {
            if (p != null) r[i] = p.getInventory().getStack(9 + i);
            else r[i] = (i % 2 == 0) ? new ItemStack(s[(i / 2) % s.length], 1 + (i * 7) % 60) : ItemStack.EMPTY;
        }
        return r;
    }

    // ---------- potions ----------
    static List<Fx> effects() {
        List<Fx> list = new ArrayList<>();
        PlayerEntity p = player();
        if (p != null) {
            Set<RegistryEntry<StatusEffect>> seen = new HashSet<>();
            for (StatusEffectInstance i : p.getStatusEffects()) {
                RegistryEntry<StatusEffect> e = i.getEffectType();
                seen.add(e);
                int[] t = TRACK.computeIfAbsent(e, k -> new int[]{i.getDuration(), i.getDuration()});
                if (i.getDuration() > t[1]) t[0] = i.getDuration(); // re-applied
                t[0] = Math.max(t[0], i.getDuration());
                t[1] = i.getDuration();
                list.add(new Fx(e, i.getDuration(), t[0], i.getAmplifier(), i.isInfinite()));
            }
            TRACK.keySet().retainAll(seen);
        }
        if (list.isEmpty() && (p == null || preview)) {
            list.add(new Fx(StatusEffects.SPEED, 2400, 3600, 1, false));
            list.add(new Fx(StatusEffects.STRENGTH, 600, 1800, 0, false));
            list.add(new Fx(StatusEffects.NIGHT_VISION, 1, 1, 0, true));
        }
        return list;
    }

    // ---------- armor ----------
    static ItemStack[] equipment() {
        ItemStack[] r = new ItemStack[6];
        PlayerEntity p = player();
        boolean any = false;
        for (int i = 0; i < 6; i++) {
            r[i] = p != null ? p.getEquippedStack(SLOTS[i]) : ItemStack.EMPTY;
            if (!r[i].isEmpty()) any = true;
        }
        if (!any && (p == null || preview)) {
            Item[] it = {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS,
                    Items.DIAMOND_BOOTS, Items.DIAMOND_SWORD, Items.SHIELD};
            int[] dmg = {40, 120, 90, 20, 300, 100};
            for (int i = 0; i < 6; i++) {
                r[i] = new ItemStack(it[i]);
                r[i].setDamage(dmg[i]);
            }
        }
        return r;
    }

    static int freeSlots() {
        PlayerEntity p = player();
        if (p == null) return 12;
        int n = 0;
        for (int i = 0; i < 36; i++) if (p.getInventory().getStack(i).isEmpty()) n++;
        return n;
    }

    static int arrows() {
        PlayerEntity p = player();
        if (p == null) return 64;
        int n = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(Items.ARROW) || s.isOf(Items.TIPPED_ARROW) || s.isOf(Items.SPECTRAL_ARROW)) n += s.getCount();
        }
        return n;
    }
}
