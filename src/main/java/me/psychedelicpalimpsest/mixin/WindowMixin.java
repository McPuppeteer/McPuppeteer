/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
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

package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.modules.HeadlessMode;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(at=@At("HEAD"), method="onFramebufferSizeChanged", cancellable=true)
    void onFramebufferSizeChanged(long window, int width, int height, CallbackInfo ci){
        if (HeadlessMode.isHeadless()) ci.cancel();
    }
    @Inject(at=@At("HEAD"), method="updateFramebufferSize", cancellable=true)
    void updateFramebufferSize(CallbackInfo ci){
        if (HeadlessMode.isHeadless()) ci.cancel();
    }

    @Inject(at=@At("HEAD"), method="swapBuffers", cancellable=true)
    void swapBuffers(CallbackInfo ci){
        if (HeadlessMode.isHeadless()) ci.cancel();
    }

    @Inject(at=@At("HEAD"), method="updateWindowRegion", cancellable=true)
    void updateWindowRegion(CallbackInfo ci){
        if (HeadlessMode.isHeadless()) ci.cancel();
    }

    @Inject(at=@At("HEAD"), method="setTitle", cancellable=true)
    void setTitle(CallbackInfo ci){
        if (HeadlessMode.isHeadless()) ci.cancel();
    }
}
