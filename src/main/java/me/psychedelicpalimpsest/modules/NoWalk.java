/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.modules;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import me.psychedelicpalimpsest.PuppeteerEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class NoWalk {
    public static boolean isActive = false;
    public static boolean toggle(KeyAction keyAction, IKeybind iKeybind) {
        isActive = !isActive;


        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of(isActive ? "Enabled NoWalk" : "Disabled NoWalk"), false);


        PuppeteerEffects.noWalkEffect.isActive = isActive;
        PuppeteerInput.allowUserInput = !isActive;

        return true;
    }

}
