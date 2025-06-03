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


package me.psychedelicpalimpsest.commands.integration;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.MaLiLibConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.MesaConfigUtils.*;


public class MalilibIntegration {

    /*
        See: https://github.com/sakura-ryoko/malilib/blob/1.21.5/src/main/java/fi/dy/masa/malilib/MaLiLibConfigs.java

        Note: Not adding experimental and testing options
    */
    private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
            "Generic", MaLiLibConfigs.Generic.OPTIONS,
            "Debug", MaLiLibConfigs.Debug.OPTIONS
    );


    @PuppeteerCommand(
            cmd = "dump malilib config", description = "Dumps malilibs config")
    public static class DumpMalilib implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(dumpJsonForMasaConfig(config)));
        }
    }

    @PuppeteerCommand(
            cmd = "get malilib config item", description = "Gets specific malilib config item")
    public static class GetMalilibItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleGetMalilibConfigRequest(config, request)));
        }
    }

    @PuppeteerCommand(
            cmd = "set malilib config item", description = "Sets specific malilib config item")
    public static class SetMalilibItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleSetMalilibConfigRequest(config, request)));
        }
    }

    @PuppeteerCommand(
            cmd = "exec malilib config item", description = "Executes specific malilib hotkey config item")
    public static class ExecMalilibItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleExecMesaConfigRequest(config, request)));
        }
    }

}
