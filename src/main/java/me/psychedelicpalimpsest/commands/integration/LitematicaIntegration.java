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

package me.psychedelicpalimpsest.commands.integration;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.MesaConfigUtils.*;

public class LitematicaIntegration {
    // TODO: Add more litematica specific integration (Ex: editing schematics)

    /* https://github.com/sakura-ryoko/litematica/blob/1.21.5/src/main/java/fi/dy/masa/litematica/config/Configs.java#L389 */
    private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
            "Colors", Configs.Colors.OPTIONS,
            "Generic", Configs.Generic.OPTIONS,
            "Hotkeys", Hotkeys.HOTKEY_LIST,
            "InfoOverlays", Configs.InfoOverlays.OPTIONS,
            "Visuals", Configs.Visuals.OPTIONS
    );


    @PuppeteerCommand(
            cmd = "dump litematica config", description = "Dumps litematicas config", mod_requirements = "litematica")
    public static class DumpLitematica implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(dumpJsonForMasaConfig(config)));
        }
    }

    @PuppeteerCommand(
            cmd = "get litematica config item", description = "Gets specific litematica config item", mod_requirements = "litematica")
    public static class GetLitematicaItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleGetMalilibConfigRequest(config, request)));
        }
    }

    @PuppeteerCommand(
            cmd = "set litematica config item", description = "Sets specific litematica config item", mod_requirements = "litematica")
    public static class SetLitematicaItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleSetMalilibConfigRequest(config, request)));
        }
    }

    @PuppeteerCommand(
            cmd = "exec litematica config item", description = "Executes specific litematica hotkey config item", mod_requirements = "litematica")
    public static class ExecLitematicaItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleExecMalilibConfigRequest(config, request)));
        }
    }

}
