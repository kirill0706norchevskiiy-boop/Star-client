package com.example.star;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class TargetHud {
    static LivingEntity cur, last;
    static long hitMs;
    static float shown, hpA, trail;
    static final EquipmentSlot[] S = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static void set(LivingEntity e) { cur = e; hitMs = System.currentTimeMillis(); }

    public static void render(DrawContext c, int w, int h) {
        var mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        LivingEntity t = null;
        if (cur != null && cur.isAlive() && System.currentTimeMillis() - hitMs < 4000) t = cur;
        else if (mc.targetedEntity instanceof LivingEntity l) t = l;

        if (t != null && t != last) {
            last = t;
            float fr = MathHelper.clamp((t.getHealth() + t.getAbsorptionAmount()) / t.getMaxHealth(), 0, 1);
            hpA = trail = fr;
        }
        shown += ((t != null ? 1f : 0f) - shown) * 0.15f;
        if (last == null || shown < 0.03f) return;

        float hp = last.getHealth() + last.getAbsorptionAmount();
        float fr = MathHelper.clamp(hp / last.getMaxHealth(), 0, 1);
        hpA += (fr - hpA) * 0.12f;
        trail += (fr - trail) * 0.04f;

        int x = w / 2 + 30, y = h / 2 + 10, W = 150, H = 54;
        float k = 0.85f + 0.15f * shown;
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate(x + W / 2f, y + H / 2f);
        m.scale(k, k);
        m.translate(-(x + W / 2f), -(y + H / 2f));

        int bar = MathHelper.hsvToRgb(hpA * 0.33f, 0.75f, 1f);
        c.fill(x, y, x + W, y + H, Proj.col(0x101018, 0.78f * shown));
        c.fill(x, y, x + W, y + 1, Proj.col(bar, shown));

        c.drawText(tr, last.getName().getString(), x + 6, y + 5, Proj.col(0xFFFFFF, shown), true);
        float dist = mc.player.distanceTo(last);
        c.drawText(tr, String.format("%.1f/%.0f  |  %.1fм", hp, last.getMaxHealth(), dist),
            x + 6, y + 16, Proj.col(0xBBBBBB, shown), false);

        int bx = x + 6, by = y + 28, bw = W - 12;
        c.fill(bx, by, bx + bw, by + 5, Proj.col(0x000000, 0.6f * shown));
        c.fill(bx, by, bx + (int) (bw * trail), by + 5, Proj.col(0xFFFFFF, 0.5f * shown));
        c.fill(bx, by, bx + (int) (bw * hpA), by + 5, Proj.col(bar, shown));

        for (int i = 0; i < 4; i++) {
            ItemStack s = last.getEquippedStack(S[i]);
            int ix = x + 6 + i * 36;
            if (s.isEmpty()) { c.drawText(tr, "-", ix + 6, y + 38, Proj.col(0x777777, shown), false); continue; }
            c.drawItem(s, ix, y + 34);
            int pct = s.isDamageable() ? (s.getMaxDamage() - s.getDamage()) * 100 / s.getMaxDamage() : 100;
            int pc = MathHelper.hsvToRgb(pct / 100f * 0.33f, 0.7f, 1f);
            c.drawText(tr, pct + "", ix + 17, y + 38, Proj.col(pc, shown), true);
        }
        m.popMatrix();
    }
}
