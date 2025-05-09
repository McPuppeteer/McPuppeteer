/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.MinihudUtils;
import me.psychedelicpalimpsest.PuppeteerEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
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

        /* A nice courtesy, don't draw over your shit */
        if (McPuppeteer.installedMods.contains("minihud"))
            y += MinihudUtils.getMinhudHeight() + 4;

        for (PuppeteerEffects.PuppeteerEffect effect : PuppeteerEffects.effects) {
            if (!effect.isActive) continue;


            context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.EFFECT_BACKGROUND_TEXTURE, x, y, 24, 24);
            context.drawTexture(RenderLayer::getGuiTextured, effect.texture, x + 2, y + 2, 0f, 0f, 20, 20, 20, 20);
            x += 24 + 2 + 2;
        }
    }
}
