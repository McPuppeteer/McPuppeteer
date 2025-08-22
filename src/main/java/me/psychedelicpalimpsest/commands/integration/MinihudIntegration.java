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
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.InfoToggle;
import fi.dy.masa.minihud.config.RendererToggle;
import fi.dy.masa.minihud.config.StructureToggle;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.MesaConfigUtils.*;

public class MinihudIntegration {

	/* https://github.com/sakura-ryoko/minihud/blob/1.21.5/src/main/java/fi/dy/masa/minihud/config/Configs.java#L360
	 */
	private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
	    "Colors", Configs.Colors.OPTIONS, "Generic", Configs.Generic.OPTIONS, "InfoTypeToggles", InfoToggle.VALUES,
	    "RendererToggles", RendererToggle.VALUES, "StructureColors", StructureToggle.COLOR_CONFIGS,
	    "StructureHotkeys", StructureToggle.HOTKEY_CONFIGS, "StructureToggles", StructureToggle.TOGGLE_CONFIGS);

	@PuppeteerCommand(cmd = "dump minihud config", description = "Dumps minihuds config",
			  mod_requirements = "minihud")
	public static class DumpMinihud implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(
			    () -> callback.resultCallback(dumpJsonForMasaConfig(config)));
		}
	}

	@PuppeteerCommand(cmd = "get minihud config item", description = "Gets specific minihud config item",
			  mod_requirements = "minihud")
	public static class GetMinihudItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(
			    () -> callback.resultCallback(handleGetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(cmd = "set minihud config item", description = "Sets specific minihud config item",
			  mod_requirements = "minihud")
	public static class SetMinihudItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(
			    () -> callback.resultCallback(handleSetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(cmd = "exec minihud config item",
			  description = "Executes specific minihud hotkey config item", mod_requirements = "minihud")
	public static class ExecMinihudItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(
			    () -> callback.resultCallback(handleExecMesaConfigRequest(config, request)));
		}
	}
}
