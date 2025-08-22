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

package me.psychedelicpalimpsest.commands.integration;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.MesaConfigUtils.*;

public class TweakerooIntegration {

	/*
	    See: https://github.com/sakura-ryoko/tweakeroo/blob/601acef5eb34bbf44d9b51585d676358d91efb7c/src/main/java/fi/dy/masa/tweakeroo/config/Configs.java#L551
	    Not sure how well the categories reflect what you see in the gui.
	 */

	private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
	    "Fixes", Configs.Fixes.OPTIONS,
	    "Generic", Configs.Generic.OPTIONS,
	    "GenericHotkeys", Hotkeys.HOTKEY_LIST,
	    "Internal", Configs.Internal.OPTIONS,
	    "Lists", Configs.Lists.OPTIONS,
	    "DisableToggles", Configs.Disable.OPTIONS,
	    "TweakToggles", FeatureToggle.VALUES);

	@PuppeteerCommand(
	    cmd = "dump tweakeroo config", description = "Dumps tweakeroos config", mod_requirements = "tweakeroo")
	public static class DumpTweakeroo implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(dumpJsonForMasaConfig(config)));
		}
	}

	@PuppeteerCommand(
	    cmd = "get tweakeroo config item", description = "Gets specific tweakeroo config item", mod_requirements = "tweakeroo")
	public static class GetTweakerooItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleGetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(
	    cmd = "set tweakeroo config item", description = "Sets specific tweakeroo config item", mod_requirements = "tweakeroo")
	public static class SetTweakerooItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleSetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(
	    cmd = "exec tweakeroo config item", description = "Executes specific tweakeroo hotkey config item", mod_requirements = "tweakeroo")
	public static class ExecTweakerooItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleExecMesaConfigRequest(config, request)));
		}
	}
}
