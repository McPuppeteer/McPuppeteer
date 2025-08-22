/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
  @Shadow private boolean thirdPerson;
  @Shadow private float lastCameraY;
  @Shadow private float cameraY;

  @Inject(method = "update", at = @At("HEAD"), cancellable = true)
  private void onUpdate(
      BlockView area,
      Entity focusedEntity,
      boolean thirdPerson,
      boolean inverseView,
      float tickDelta,
      CallbackInfo ci) {
    if (Freecam.isFreecamActive()) {
      this.thirdPerson = true;
      ci.cancel();
    } else if (Freerot.isFreerotActive()) {
      ci.cancel();

      MinecraftClient.getInstance()
          .gameRenderer
          .getCamera()
          .setPos(
              MathHelper.lerp(tickDelta, focusedEntity.getX(), focusedEntity.getX()),
              MathHelper.lerp(tickDelta, focusedEntity.getY(), focusedEntity.getY())
                  + MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY),
              MathHelper.lerp(tickDelta, focusedEntity.getZ(), focusedEntity.getZ()));
    }
  }
}
