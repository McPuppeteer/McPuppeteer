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
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.MesaConfigUtils.*;


public class TweakerooIntegration {
    private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
            "Fixes", Configs.Fixes.OPTIONS,
            "Generic", Configs.Generic.OPTIONS,
            "GenericHotkeys", Hotkeys.HOTKEY_LIST,
            "Internal", Configs.Internal.OPTIONS,
            "Lists", Configs.Lists.OPTIONS,
            "DisableToggles", Configs.Disable.OPTIONS,
            "TweakToggles", FeatureToggle.VALUES
    );


    @PuppeteerCommand(
            cmd = "dump tweakeroo config", description = "Dumps tweakeroos config")
    public static class DumpTweakeroo implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            callback.resultCallback(dumpJsonForMasaConfig(config));
        }
    }
    @PuppeteerCommand(
            cmd = "get tweakeroo config item", description = "Gets specific tweakeroo config item")
    public static class GetTweakerooItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            callback.resultCallback(handleGetMalilibConfigRequest(config, request));
        }
    }
    @PuppeteerCommand(
            cmd = "set tweakeroo config item", description = "Gets specific tweakeroo config item")
    public static class SetTweakerooItem implements BaseCommand {

        @Override
        public void onRequest(JsonObject request, LaterCallback callback) {
            callback.resultCallback(handleSetMalilibConfigRequest(config, request));
        }
    }



}
