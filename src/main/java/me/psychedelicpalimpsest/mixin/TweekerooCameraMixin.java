/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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


    /* Scream at the user for using tweekeroo freecam */

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
