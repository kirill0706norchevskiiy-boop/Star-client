package com.example.star;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class StarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // ЕДИНСТВЕННАЯ точка рендера: HUD-слой поверх игры
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MISC_OVERLAYS,
            Identifier.of("star", "main"), StarClient::render);
        // Если HudElementRegistry не найден, замени на:
        // HudRenderCallback.EVENT.register(StarClient::render);

        ClientTickEvents.END_CLIENT_TICK.register(Fx::tick);

        AttackEntityCallback.EVENT.register((pl, world, hand, e, hit) -> {
            if (world.isClient() && e instanceof LivingEntity le) {
                TargetHud.set(le);
                Fx.hit(le);
            }
            return ActionResult.PASS;
        });
    }

    static void render(DrawContext c, RenderTickCounter t) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        float td = t.getTickProgress(false);
        int w = c.getScaledWindowWidth(), h = c.getScaledWindowHeight();
        Proj.begin(td, w, h);
        Esp.render(c, td);
        Fx.render(c);
        TargetHud.render(c, w, h);
        StatusHud.render(c, w, h);
    }
}
