/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * Copyright (C) 2025 - Tweekeroo developers (Mostly mesa)
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


package me.psychedelicpalimpsest.modules;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import me.psychedelicpalimpsest.PuppeteerEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/*
    Again, I stole a lot from tweakeroo. But the difference is I am updating
    the camera itself, while not doing anything with the player entity, meaning
    baritone will still work, and Puppeteer can still function, while still in
    freecam.

    The free cam tool used the following classes to function:
        * CameraMixin.java - Prevent camera updates and render player
        * ClientPlayerEntityMixin.java - Change player input, tick movement
        * EntityMixin.java - Calculate rotation

 */

public class Freecam {
    private static boolean isFreecam = false;
    public static FreecamMovementHandler movementHandler = null;


    public static boolean toggleFreecam(KeyAction keyAction, IKeybind iKeybind) {
        isFreecam = !isFreecam;

        if (isFreecam) initializeFreecam();
        else deactivateFreecam();
        return true;
    }

    public static boolean isFreecamActive() {
        return isFreecam;
    }

    private static boolean oldViewBobSetting = false;

    private static void initializeFreecam() {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Enabled freecam"), false);
        movementHandler = new FreecamMovementHandler();


        SimpleOption<Boolean> bob = MinecraftClient.getInstance().options.getBobView();
        oldViewBobSetting = bob.getValue();
        bob.setValue(false);

        PuppeteerEffects.freecamEffect.isActive = true;

    }

    private static void deactivateFreecam() {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Disabled freecam"), false);

        SimpleOption<Boolean> bob = MinecraftClient.getInstance().options.getBobView();
        bob.setValue(oldViewBobSetting);

        PuppeteerEffects.freecamEffect.isActive = false;
    }

    // https://github.com/sakura-ryoko/tweakeroo/blob/8e762332d29135d9634a86ebe5f325a8484dc3f6/src/main/java/fi/dy/masa/tweakeroo/util/CameraEntity.java
    public static final class FreecamMovementHandler {
        private Vec3d cameraMotion = new Vec3d(0.0, 0.0, 0.0);
        private boolean sprinting = false;

        public void movementTick() {
            GameOptions options = MinecraftClient.getInstance().options;
            if (options.sprintKey.isPressed())
                sprinting = true;
            else if (!options.forwardKey.isPressed() && !options.backKey.isPressed())
                sprinting = false;
            cameraMotion = calculatePlayerMotionWithDeceleration(cameraMotion, 0.15, 0.4);
            double forward = sprinting ? cameraMotion.x * 3 : cameraMotion.x;
            handleMotion(forward, cameraMotion.y, cameraMotion.z);
        }

        private static double getMoveSpeed() {
            return 0.7d;
        }


        private void handleMotion(double forward, double up, double strafe) {
            Camera c = MinecraftClient.getInstance().gameRenderer.getCamera();
            float yaw = c.getYaw();
            double scale = getMoveSpeed();
            double xFactor = Math.sin(yaw * Math.PI / 180.0);
            double zFactor = Math.cos(yaw * Math.PI / 180.0);

            double x = (strafe * zFactor - forward * xFactor) * scale;
            double y = up * scale;
            double z = (forward * zFactor + strafe * xFactor) * scale;

            c.setPos(
                    c.getPos().add(new Vec3d(x, y, z))
            );
        }

        // Credit: https://github.com/sakura-ryoko/tweakeroo/blob/1.21.5/src/main/java/fi/dy/masa/tweakeroo/util/MiscUtils.java#L77
        public static Vec3d calculatePlayerMotionWithDeceleration(Vec3d lastMotion,
                                                                  double rampAmount,
                                                                  double decelerationFactor) {
            GameOptions options = MinecraftClient.getInstance().options;
            int forward = 0;
            int vertical = 0;
            int strafe = 0;

            if (options.forwardKey.isPressed())
                forward += 1;
            if (options.backKey.isPressed())
                forward -= 1;
            if (options.leftKey.isPressed())
                strafe += 1;
            if (options.rightKey.isPressed())
                strafe -= 1;
            if (options.jumpKey.isPressed())
                vertical += 1;
            if (options.sneakKey.isPressed())
                vertical -= 1;

            double speed = (forward != 0 && strafe != 0) ? 1.2 : 1.0;
            double forwardRamped = getRampedMotion(lastMotion.x, forward, rampAmount, decelerationFactor) / speed;
            double verticalRamped = getRampedMotion(lastMotion.y, vertical, rampAmount, decelerationFactor);
            double strafeRamped = getRampedMotion(lastMotion.z, strafe, rampAmount, decelerationFactor) / speed;

            return new Vec3d(forwardRamped, verticalRamped, strafeRamped);
        }

        public static double getRampedMotion(double current, int input, double rampAmount, double decelerationFactor) {
            if (input != 0) {
                if (input < 0)
                    rampAmount *= -1.0;


                // Immediately kill the motion when changing direction to the opposite
                if ((input < 0) != (current < 0.0))
                    current = 0.0;

                current = Math.clamp(current + rampAmount, -1.0, 1.0);
            } else {
                current *= decelerationFactor;
            }

            return current;
        }


    }

}
