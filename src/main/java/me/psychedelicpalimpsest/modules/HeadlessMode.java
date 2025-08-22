/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

public class HeadlessMode {

  private static boolean activated = false;

  public static boolean isHeadless() {
    return activated;
  }

  public static void setHeadless() {
    if (activated) return;

    activated = true;

    /* This is evil */
    Window win = MinecraftClient.getInstance().getWindow();
    GLFW.glfwHideWindow(win.getHandle());
  }

  public static void disableHeadless() {
    if (!activated) return;

    activated = false;
    Window win = MinecraftClient.getInstance().getWindow();
    GLFW.glfwShowWindow(win.getHandle());
  }
}
