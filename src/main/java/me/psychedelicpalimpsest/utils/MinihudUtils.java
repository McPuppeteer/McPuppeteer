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

package me.psychedelicpalimpsest.utils;

import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.event.RenderHandler;
import me.psychedelicpalimpsest.McPuppeteer;

import java.lang.reflect.Field;
import java.util.List;

public class MinihudUtils {
    private static boolean hasWarned = false;

    /* Adapted from https://github.com/sakura-ryoko/minihud/blob/1.21.5/src/main/java/fi/dy/masa/minihud/event/RenderHandler.java#L98 */
    public static int getMinhudHeight() {
        /* Only the top left is our turf! */
        if (!Configs.Generic.MAIN_RENDERING_TOGGLE.getBooleanValue() || Configs.Generic.HUD_ALIGNMENT.getOptionListValue() != HudAlignment.TOP_LEFT)
            return 0;


        RenderHandler handler = RenderHandler.getInstance();
        try {
            /* Evil reflection hack */
            Field f = handler.getClass().getDeclaredField("lineWrappers");
            f.setAccessible(true);

            /* Evil casting */
            @SuppressWarnings("unchecked cast")
            List<Object> lines = (List<Object>) f.get(handler);

            /* Stolen math */
            return (int) (lines.size() * (StringUtils.getFontHeight() + 2) * Configs.Generic.FONT_SCALE.getDoubleValue());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            /* This is called onrender, so only print ONCE */
            if (!hasWarned) {
                McPuppeteer.LOGGER.error("Minihud changed is some HORRIBLE way!", e);
                hasWarned = true;
            }
            return 0;
        }
    }


}
