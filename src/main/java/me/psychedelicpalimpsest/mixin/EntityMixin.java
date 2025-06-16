/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.PuppeteerServer;
import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract boolean isPlayer();

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract float getPitch();

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!this.isPlayer()) return;

        if (Freecam.isFreecamActive() || Freerot.isFreerotActive()) {
            ci.cancel();

            Camera c = MinecraftClient.getInstance().gameRenderer.getCamera();
            float f = (float) cursorDeltaY * 0.15F;
            float g = (float) cursorDeltaX * 0.15F;

            c.setRotation(
                    c.getYaw() + g,
                    MathHelper.clamp(c.getPitch() + f, -90.0F, 90.0F)
            );


        }
    }

    @Inject(method = "setPos", at = @At("HEAD"))
    void onSetPosition(double x, double y, double z, CallbackInfo ci) {
        if (!this.isPlayer()) return;
        /* Since this method sets the position, this check tells us if the player actually moved */
        if (getPos().equals(new Vec3d(x, y, z))) return;

        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_POSITION, ()->BaseCommand.jsonOf(
                "x", x,
                "y", y,
                "z", z
        ));
    }

    @Inject(method = "setYaw", at = @At("HEAD"))
    void onSetYaw(float yaw, CallbackInfo ci) {
        if (!this.isPlayer()) return;
        if (getYaw() == yaw) return;
        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_YAW, ()->BaseCommand.jsonOf(
                "yaw", yaw
        ));
    }

    @Inject(method = "setPitch", at = @At("HEAD"))
    void onSetPitch(float pitch, CallbackInfo ci) {
        if (!this.isPlayer()) return;
        if (getPitch() == pitch) return;
        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.PLAYER_PITCH, ()->BaseCommand.jsonOf(
                "pitch", pitch
        ));
    }


}
