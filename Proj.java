package com.example.star;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class Proj {
    public static int W, H;
    static double cx, cy, cz, fx, fy, fz, rx, rz, ux, uy, uz;
    static float f;

    /** Раз за кадр: считаем базис камеры по глазам игрока */
    public static void begin(float td, int w, int h) {
        var mc = MinecraftClient.getInstance();
        var p = mc.player;
        Vec3d c = p.getCameraPosVec(td);
        cx = c.x; cy = c.y; cz = c.z; W = w; H = h;
        double yaw = Math.toRadians(p.getYaw(td)), pit = Math.toRadians(p.getPitch(td));
        fx = -Math.sin(yaw) * Math.cos(pit);
        fy = -Math.sin(pit);
        fz = Math.cos(yaw) * Math.cos(pit);
        rx = -Math.cos(yaw); rz = -Math.sin(yaw);
        ux = -rz * fy; uy = rz * fx - rx * fz; uz = rx * fy;
        double fov = mc.options.getFov().getValue();
        f = (float) (h / 2.0 / Math.tan(Math.toRadians(fov) / 2));
    }

    /** Мир → экран. Возвращает {x, y, глубина} или null, если точка за спиной */
    public static float[] pr(double x, double y, double z) {
        double dx = x - cx, dy = y - cy, dz = z - cz;
        double d = dx * fx + dy * fy + dz * fz;
        if (d < 0.05) return null;
        double sx = dx * rx + dz * rz;
        double sy = dx * ux + dy * uy + dz * uz;
        return new float[]{(float) (W / 2.0 + sx / d * f), (float) (H / 2.0 - sy / d * f), (float) d};
    }

    /** RGB + прозрачность 0..1 → ARGB (альфа никогда не 0, иначе текст не рисуется) */
    public static int col(int rgb, float a) {
        int al = Math.max(5, Math.min(255, (int) (a * 255)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    /** Линия из 1x1 пикселей */
    public static void line(DrawContext c, float x1, float y1, float x2, float y2, int col) {
        float dx = x2 - x1, dy = y2 - y1;
        int n = (int) Math.min(Math.max(Math.abs(dx), Math.abs(dy)), 2500);
        if (n < 1) n = 1;
        for (int i = 0; i <= n; i++) {
            int x = (int) (x1 + dx * i / n), y = (int) (y1 + dy * i / n);
            if (x < 0 || y < 0 || x >= W || y >= H) continue;
            c.fill(x, y, x + 1, y + 1, col);
        }
    }

    /** Контур прямоугольника */
    public static void frame(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.fill(x1, y1, x2, y1 + 1, col);
        c.fill(x1, y2 - 1, x2, y2, col);
        c.fill(x1, y1, x1 + 1, y2, col);
        c.fill(x2 - 1, y1, x2, y2, col);
    }
}
