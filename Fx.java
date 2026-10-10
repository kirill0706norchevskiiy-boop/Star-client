package com.example.star;

import java.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Fx {
    static class P { double x, y, z, vx, vy, vz; long t; int col; boolean star; String s; }
    static final List<P> L = new ArrayList<>();
    static final Map<Integer, Float> hp = new HashMap<>();
    static final Map<Integer, Long> hit = new HashMap<>();
    static final int[] COL = {0xFFD84A, 0xFF5C5C, 0x5CE1FF, 0xFF7BE5};
    static final Random R = new Random();

    public static void hit(Entity e) { hit.put(e.getId(), System.currentTimeMillis()); }

    public static void tick(MinecraftClient mc) {
        if (mc.world == null) { L.clear(); hp.clear(); return; }
        long now = System.currentTimeMillis();
        Map<Integer, Float> cur = new HashMap<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == mc.player) continue;
            float v = le.getHealth() + le.getAbsorptionAmount();
            Float old = hp.get(le.getId());
            Long t = hit.get(le.getId());
            cur.put(le.getId(), v);
            if (old != null && v < old - 0.01f && t != null && now - t < 1000) burst(le, old - v);
        }
        hp.clear(); hp.putAll(cur);
        hit.values().removeIf(t -> now - t > 5000);
    }

    static void burst(LivingEntity e, float dmg) {
        double y = e.getY() + e.getHeight() * 0.6;
        P n = new P();
        n.x = e.getX(); n.y = y + 0.3; n.z = e.getZ(); n.t = System.currentTimeMillis();
        n.s = String.format("%.1f", dmg);
        n.col = dmg >= 6 ? 0xFF5555 : 0xFFFF55;
        L.add(n);
        for (int i = 0; i < 14; i++) {
            P p = new P();
            p.x = e.getX(); p.y = y; p.z = e.getZ(); p.t = n.t;
            p.vx = (R.nextDouble() - .5) * 3; p.vy = R.nextDouble() * 2 + .5; p.vz = (R.nextDouble() - .5) * 3;
            p.col = COL[R.nextInt(COL.length)]; p.star = R.nextBoolean();
            L.add(p);
        }
    }

    public static void render(DrawContext c) {
        long now = System.currentTimeMillis();
        var tr = MinecraftClient.getInstance().textRenderer;
        L.removeIf(p -> now - p.t > (p.s != null ? 1100 : 800));
        for (P p : L) {
            double t = (now - p.t) / 1000.0;
            if (p.s != null) {
                float[] s = Proj.pr(p.x, p.y + t * 1.2, p.z);
                if (s == null) continue;
                float a = (float) (1 - t / 1.1);
                float sc = (float) (Math.max(0.8, Math.min(2.2, 14 / s[2])) * (1 + Math.max(0, .25 - t) * 2));
                var m = c.getMatrices();
                m.pushMatrix(); m.translate(s[0], s[1]); m.scale(sc, sc);
                c.drawText(tr, p.s, -tr.getWidth(p.s) / 2, -4, Proj.col(p.col, a), true);
                m.popMatrix();
            } else {
                float[] s = Proj.pr(p.x + p.vx * t, p.y + p.vy * t - 4 * t * t, p.z + p.vz * t);
                if (s == null) continue;
                int col = Proj.col(p.col, (float) (1 - t / 0.8));
                int r = Math.max(1, (int) (10 / s[2])), x = (int) s[0], y = (int) s[1];
                if (p.star) {
                    c.fill(x - r, y, x + r + 1, y + 1, col);
                    c.fill(x, y - r, x + 1, y + r + 1, col);
                    c.fill(x - 1, y - 1, x + 2, y + 2, Proj.col(0xFFFFFF, (float) (1 - t / 0.8)));
                } else {
                    for (int dy = -r; dy <= r; dy++) {
                        int hw = (int) Math.sqrt(r * r - dy * dy);
                        c.fill(x - hw, y + dy, x + hw + 1, y + dy + 1, col);
                    }
                }
            }
        }
    }
}
