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

package me.psychedelicpalimpsest.commands.malilib;

import com.google.gson.JsonObject;
import fi.dy.masa.malilib.util.FileUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;

@PuppeteerCommand(cmd = "replace config", description = "Replaces a json file in the config folder")
public class ReplaceConfigForMod implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		if (request.get("file name") == null || !request.get("file name").isJsonPrimitive()) {
			callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "expected argument",
								   "message", "Missing parameter 'file name'"));
			return;
		}
		if (request.get("json") == null) {
			callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "expected argument",
								   "message", "Missing parameter 'json'"));
			return;
		}
		File config =
		    FileUtils.getConfigDirectoryAsPath().resolve(request.get("file name").getAsString()).toFile();

		try {
			FileWriter writer = new FileWriter(config);

			writer.write(request.get("json").toString());

			writer.close();

		} catch (IOException e) {
			callback.resultCallback(
			    BaseCommand.jsonOf("status", "error", "type", "exception", "message", e.getMessage()));
			LOGGER.error("IO exception in ReplaceConfigForMod", e);
		}
	}
}
