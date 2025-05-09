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

package me.psychedelicpalimpsest.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.psychedelicpalimpsest.McPuppeteer;

import net.minecraft.client.ClientWatchdog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import org.lwjgl.glfw.GLFW;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class HeadlessMode {

    private static boolean activated = false;

    private static boolean hasCapturedSettings = false;
    private static WindowSettings capturedSettings = null;
    private static String capturedMode = null;

    public static void onCreateWindow(WindowSettings settings, String videoMode, String ignored){
        hasCapturedSettings = true;
        capturedSettings = settings;
        capturedMode = videoMode;
    }





    public static boolean isHeadless(){
        return activated;
    }

    @SuppressWarnings({"sunapi", "restriction", "deprecated", "removal"})
    private static void setWindow(MinecraftClient client, Window window) throws NoSuchFieldException, IllegalAccessException {
        Field field = client.getClass().getDeclaredField("window");

        // Get Unsafe instance
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        // Get field offset
        long offset = unsafe.objectFieldOffset(field);

        // Set field value directly in memory
        unsafe.putObject(client, offset, window);
    }


    public static void setHeadless(){
        if (!hasCapturedSettings){
            McPuppeteer.LOGGER.error("No window was ever opened?");
            return;
        }
        if (activated) return;

        activated = true;

        /* This is evil */
        Window win = MinecraftClient.getInstance().getWindow();
        GLFW.glfwDestroyWindow(win.getHandle());

    }

    public static void disableHeadless(){
        if (!hasCapturedSettings){
            McPuppeteer.LOGGER.error("No window was ever opened?");
            return;
        }
        if (!activated) return;

        activated = false;

        MinecraftClient mc = MinecraftClient.getInstance();
        WindowProvider wp = mc.windowProvider;

        Window w = wp.createWindow(capturedSettings, capturedMode, mc.getWindowTitle());

        try {
            setWindow(MinecraftClient.getInstance(), w);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        w.setCloseCallback(new Runnable() {
            private boolean closed;

            public void run() {
                if (!this.closed) {
                    this.closed = true;
                    ClientWatchdog.shutdownClient(mc.runDirectory, mc.thread.threadId());
                }
            }
        });


// Reset rendering systems
        RenderSystem.setupDefaultState(0, 0, w.getFramebufferWidth(), w.getFramebufferHeight());
        RenderSystem.initRenderer(mc.options.glDebugVerbosity, false);

        // Recreate framebuffers
        mc.getFramebuffer().resize(w.getFramebufferWidth(), w.getFramebufferHeight());

        // Reset game renderer
        mc.gameRenderer.onResized(w.getFramebufferWidth(), w.getFramebufferHeight());

        // Reload shaders
        mc.gameRenderer.preloadPrograms(mc.getDefaultResourcePack().getFactory());

        // Reset viewport
        RenderSystem.viewport(0, 0, w.getFramebufferWidth(), w.getFramebufferHeight());

        // Reset matrices
        RenderSystem.clear(16640);

        w.setVsync(mc.options.getEnableVsync().getValue());
        w.setRawMouseMotion(mc.options.getRawMouseInput().getValue());
        w.logOnGlError();

        mc.mouse.setup(w.getHandle());

        // Force a resource reload if needed
        mc.reloadResources();

        mc.onWindowFocusChanged(true);
    }



}
