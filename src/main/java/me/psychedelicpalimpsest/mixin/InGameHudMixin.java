package me.psychedelicpalimpsest.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.render.RenderUtils;
import me.psychedelicpalimpsest.McPuppeteer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method="renderStatusEffectOverlay", at=@At("HEAD"))
    void onRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
        int x = 1;
        int y = 1;
        for (McPuppeteer.PuppeteerEffect effect : McPuppeteer.effects) {
            if (!effect.isActive) continue;


            context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.EFFECT_BACKGROUND_TEXTURE, x, y, 24, 24);
            context.drawTexture(RenderLayer::getGuiTextured, effect.texture, x + 2, y + 2, 0f, 0f, 20, 20, 20, 20);
        }
    }
}
