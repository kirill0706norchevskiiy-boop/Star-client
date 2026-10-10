package com.example.star;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Esp {
    public static boolean BOX = true, BOX3D = true, TRACERS = true, TAGS = true;
    static final int COL = 0xFFFF4D4D;

    public static void render(DrawContext c, float td) {
        var mc = MinecraftClient.getInstance();
        var tr = mc.textRenderer;
        for (var e : mc.world.getPlayers()) {
            if (e == mc.player) continue;
            Vec3d p = e.getLerpedPos(td);
            double hw = e.getWidth() / 2, h = e.getHeight();

            if (TRACERS) {
                float[] f = Proj.pr(p.x, p.y + h / 2, p.z);
                if (f != null) Proj.line(c, Proj.W / 2f, Proj.H, f[0], f[1], COL);
            }

            if (BOX) {
                float[][] k = new float[8][];
                boolean ok = true;
                for (int i = 0; i < 8 && ok; i++) {
                    k[i] = Proj.pr(p.x + ((i & 1) == 0 ? -hw : hw),
                                   p.y + ((i & 2) == 0 ? 0 : h),
                                   p.z + ((i & 4) == 0 ? -hw : hw));
                    ok = k[i] != null;
                }
                if (ok) {
                    if (BOX3D) {
                        for (int i = 0; i < 8; i++)
                            for (int b = 1; b <= 4; b <<= 1)
                                if ((i & b) == 0) Proj.line(c, k[i][0], k[i][1], k[i | b][0], k[i | b][1], COL);
                    } else {
                        float x1 = 1e9f, y1 = 1e9f, x2 = -1e9f, y2 = -1e9f;
                        for (float[] q : k) {
                            x1 = Math.min(x1, q[0]); x2 = Math.max(x2, q[0]);
                            y1 = Math.min(y1, q[1]); y2 = Math.max(y2, q[1]);
                        }
                        Proj.frame(c, (int) x1, (int) y1, (int) x2, (int) y2, COL);
                    }
                }
            }

            if (TAGS) {
                float[] t = Proj.pr(p.x, p.y + h + 0.35, p.z);
                if (t == null) continue;
                float hp = e.getHealth() + e.getAbsorptionAmount();
                float fr = MathHelper.clamp(hp / e.getMaxHealth(), 0, 1);
                String s = e.getName().getString() + " " + (int) hp;
                float sc = MathHelper.clamp(12f / t[2], 0.6f, 1.4f);
                var m = c.getMatrices();
                m.pushMatrix(); m.translate(t[0], t[1]); m.scale(sc, sc);
                int w = tr.getWidth(s);
                c.fill(-w / 2 - 2, -12, w / 2 + 2, 0, 0x88000000);
                c.drawText(tr, s, -w / 2, -10, 0xFFFFFFFF, true);
                c.fill(-w / 2 - 2, 1, w / 2 + 2, 3, 0x88000000);
                c.fill(-w / 2 - 2, 1, -w / 2 - 2 + (int) ((w + 4) * fr), 3,
                       0xFF000000 | MathHelper.hsvToRgb(fr * 0.33f, 0.75f, 1f));
                m.popMatrix();
            }
        }
    }
}
