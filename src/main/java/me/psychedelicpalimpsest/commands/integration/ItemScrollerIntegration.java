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
import fi.dy.masa.itemscroller.config.Configs;
import fi.dy.masa.itemscroller.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.MesaConfigUtils.*;

public class ItemScrollerIntegration {

	/*
	    See: https://github.com/sakura-ryoko/itemscroller/blob/1.21.5/src/main/java/fi/dy/masa/itemscroller/config/Configs.java#L144
	 */
	private final static Map<String, List<? extends IConfigBase>> config = ImmutableMap.of(
	    "Generic", Configs.Generic.OPTIONS,
	    "Hotkeys", Hotkeys.HOTKEY_LIST,
	    "Toggles", Configs.Toggles.OPTIONS);

	@PuppeteerCommand(
	    cmd = "dump itemscroller config", description = "Dumps itemscrollers' config", mod_requirements = "itemscroller")
	public static class DumpItemscroller implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(dumpJsonForMasaConfig(config)));
		}
	}

	@PuppeteerCommand(
	    cmd = "get itemscroller config item", description = "Gets specific itemscroller config item", mod_requirements = "itemscroller")
	public static class GetItemscrollerItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleGetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(
	    cmd = "set itemscroller config item", description = "Sets specific itemscroller config item", mod_requirements = "itemscroller")
	public static class SetItemscrollerItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleSetMalilibConfigRequest(config, request)));
		}
	}

	@PuppeteerCommand(
	    cmd = "exec itemscroller config item", description = "Executes specific itemscroller hotkey config item", mod_requirements = "itemscroller")
	public static class ExecItemscrollerItem implements BaseCommand {

		@Override
		public void onRequest(JsonObject request, LaterCallback callback) {
			MinecraftClient.getInstance().execute(() -> callback.resultCallback(handleExecMesaConfigRequest(config, request)));
		}
	}
}
