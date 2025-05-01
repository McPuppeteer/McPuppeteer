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
