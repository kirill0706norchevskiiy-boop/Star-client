package com.example.star;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class StatusHud {
    static final EquipmentSlot[] S = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static void render(DrawContext c, int w, int h) {
        var mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        var p = mc.player;

        // Saturation HUD
        if (mc.interactionManager != null && mc.interactionManager.hasStatusBars()) {
            float s = p.getHungerManager().getSaturationLevel();
            int x = w / 2 + 10, y = h - 30;
            c.fill(x, y, x + 81, y + 2, 0x66000000);
            c.fill(x, y, x + (int) (81 * Math.min(s, 20) / 20), y + 2, 0xFFFFD34D);
            String t = String.format("%.1f", s);
            c.drawText(tr, t, w / 2 + 91 - tr.getWidth(t), h - 49, 0xFFFFD34D, true);
        }

        // Armor Notifier: прочность < 10%
        int line = 0;
        float a = 0.65f + 0.35f * (float) Math.sin(System.currentTimeMillis() / 150.0);
        for (EquipmentSlot sl : S) {
            ItemStack s = p.getEquippedStack(sl);
            if (s.isEmpty() || !s.isDamageable()) continue;
            int left = s.getMaxDamage() - s.getDamage();
            if (left * 10 >= s.getMaxDamage()) continue;
            String t = "! " + s.getName().getString() + ": " + (left * 100 / s.getMaxDamage()) + "%";
            c.drawText(tr, t, (w - tr.getWidth(t)) / 2, h / 3 + line * 11, Proj.col(0xFF4444, a), true);
            line++;
        }
    }
}
