package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.safefreecam.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow private boolean thirdPerson;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci){
        if (Freecam.isFreecamActive()){
            this.thirdPerson = true;
            ci.cancel();
        }

    }


}
