package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci){
        KeyBinding binding = KeyBinding.KEY_TO_BINDINGS.get(key);

        if (binding == null) return;

        if (PuppeteerInput.onKeyPressed(binding))
            ci.cancel();
    }
    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    void setPressed(boolean pressed, CallbackInfo ci){
        if (PuppeteerInput.setPressed((KeyBinding) (Object)this))
            ci.cancel();
    }


}
