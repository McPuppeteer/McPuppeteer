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


package me.psychedelicpalimpsest.modules;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static me.psychedelicpalimpsest.PuppeteerEffects.noRotationEffect;


public class Freerot {

    private static boolean isFreerot = false;

    public static boolean toggleFreerot(KeyAction keyAction, IKeybind iKeybind) {
        isFreerot = !isFreerot;

        if (isFreerot) initializeFreerot();
        else deactivateFreerot();
        return true;
    }

    public static void initializeFreerot() {
        if (!isFreerot) return;
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Enabled freerot"), false);


        noRotationEffect.isActive = true;
    }

    public static void deactivateFreerot() {
        if (isFreerot) return;
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("Disabled freerot"), false);


        noRotationEffect.isActive = false;
    }


    public static Boolean isFreerotActive() {
        return isFreerot;
    }
}
