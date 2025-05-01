package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.safefreecam.Freecam;
import me.psychedelicpalimpsest.safefreecam.FreecamNoInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.psychedelicpalimpsest.safefreecam.Freecam.inputNop;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Shadow public Input input;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void tickMovement(CallbackInfo ci){

        if (Freecam.isFreecamActive() && this.input.getClass() == KeyboardInput.class) {
            this.input = inputNop;
            Freecam.oldKeyboardInput = this.input;
        }
        if (!Freecam.isFreecamActive() && this.input.getClass() == FreecamNoInput.class) {
            this.input = new KeyboardInput(MinecraftClient.getInstance().options);
        }


        if (Freecam.isFreecamActive()){
            this.input.tick(false, 0f);

            Freecam.movementHandler.movementTick();
        }
    }
}
