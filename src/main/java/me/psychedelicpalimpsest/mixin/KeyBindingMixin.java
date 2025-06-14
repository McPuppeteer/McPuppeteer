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
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        KeyBinding binding = KeyBinding.KEY_TO_BINDINGS.get(key);

        System.err.println((binding == null ? "null" : binding.toString()) + "\t" + key.getTranslationKey());
        if (binding == null) return;

        if (PuppeteerInput.onKeyPressed(binding))
            ci.cancel();
    }

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    void setPressed(boolean pressed, CallbackInfo ci) {
        if (PuppeteerInput.setPressed((KeyBinding) (Object) this))
            ci.cancel();
    }


}
