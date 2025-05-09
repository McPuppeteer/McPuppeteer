package me.psychedelicpalimpsest.mixin;


import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.psychedelicpalimpsest.PuppeteerConfig.WARN_ON_TWEAKEROO_FREECAM;

@Mixin(value = CameraEntity.class, remap = false)
@Pseudo /* Ignore if not installed */
public class TweekerooCameraMixin {
    @Inject(at = @At("HEAD"), method = "setCameraState")
    private static void setCameraState(boolean enabled, CallbackInfo ci) {
        if (!WARN_ON_TWEAKEROO_FREECAM.getBooleanValue()) return;
        if (!enabled) return;

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                Text.of("§c§lWARNING:§r Tweakeroo's freecam is not compatible with §6McPuppeteer§r or §bBaritone§r. "
                        + "Please use McPuppeteer's freecam module instead.")
        );
    }
}
