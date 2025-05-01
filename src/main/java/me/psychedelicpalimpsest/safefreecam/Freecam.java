package me.psychedelicpalimpsest.safefreecam;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/*
    Again, I stole a lot from tweakeroo

 */

public class Freecam {
    private static boolean isFreecam = false;
    public static Input inputNop = new FreecamNoInput();
    public static Input oldKeyboardInput = null;
    public static FreecamMovementHandler movementHandler = null;


    public static boolean toggleFreecam(KeyAction keyAction, IKeybind iKeybind) {
        isFreecam = !isFreecam;

        if (isFreecam) initializeFreecam();
        else deactivateFreecam();
        return false;
    }

    public static boolean isFreecamActive(){
        return isFreecam;
    }


    private static void initializeFreecam() {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Enabled freecam"), false);
        movementHandler = new FreecamMovementHandler();

    }
    private static void deactivateFreecam() {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Disabled freecam"), false);
    }

    // https://github.com/sakura-ryoko/tweakeroo/blob/8e762332d29135d9634a86ebe5f325a8484dc3f6/src/main/java/fi/dy/masa/tweakeroo/util/CameraEntity.java
    public static final class FreecamMovementHandler {
        private Vec3d cameraMotion = new Vec3d(0.0, 0.0, 0.0);
        private boolean sprinting = false;

        public void movementTick(){
            GameOptions options = MinecraftClient.getInstance().options;
            if (options.sprintKey.isPressed())
                sprinting = true;
            else if (!options.forwardKey.isPressed() && !options.backKey.isPressed())
                sprinting = false;
            cameraMotion = calculatePlayerMotionWithDeceleration(cameraMotion, 0.15, 0.4);
            double forward = sprinting ? cameraMotion.x * 3 : cameraMotion.x;
            handleMotion(forward, cameraMotion.y, cameraMotion.z);
        }
        private static double getMoveSpeed(){
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
