 /**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.utils;

 import me.psychedelicpalimpsest.PuppeteerConfig;
 import net.minecraft.client.Keyboard;
 import net.minecraft.client.MinecraftClient;
 import org.lwjgl.glfw.GLFW;

 public class KeyboardOverride extends Keyboard {


     public KeyboardOverride(MinecraftClient client) {
         super(client);
     }

     @Override
     public void onKey(long window, int key, int scancode, int action, int modifiers) {
         if (PuppeteerConfig.SWAP_CAPSLOCK_AND_ESCAPE.getBooleanValue()){
             if (key == GLFW.GLFW_KEY_ESCAPE) key = GLFW.GLFW_KEY_CAPS_LOCK;
             else if (key == GLFW.GLFW_KEY_CAPS_LOCK) key = GLFW.GLFW_KEY_ESCAPE;

         }

         super.onKey(window, key, scancode, action, modifiers);
     }
 }
