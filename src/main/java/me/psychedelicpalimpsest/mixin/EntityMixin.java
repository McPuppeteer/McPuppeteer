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

import me.psychedelicpalimpsest.safefreecam.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method="changeLookDirection", at=@At("HEAD"), cancellable = true)
    void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci){
        if (Freecam.isFreecamActive()) {
            ci.cancel();

            Camera c = MinecraftClient.getInstance().gameRenderer.getCamera();
            float f = (float)cursorDeltaY * 0.15F;
            float g = (float)cursorDeltaX * 0.15F;

            c.setRotation(
                    c.getYaw() + g,
                    MathHelper.clamp(c.getPitch() + f, -90.0F, 90.0F)
            );


        }

    }

}
