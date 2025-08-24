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

package me.psychedelicpalimpsest.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.PuppeteerTask;
import me.psychedelicpalimpsest.PuppeteerServer.PacketOnCompletion;

@PuppeteerCommand(cmd = "bundle", description = "Bundle many packets together")
public class Bundle implements BaseCommand {

	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		if (!request.has("method")) {
			callback.resultCallback(
			    BaseCommand.jsonOf("status", "error", "message", "Method must not be undefined"));
			return;
		}

		JsonArray packets = request.getAsJsonArray("packets");
		switch (request.get("method").getAsString()) {
			case "INSTANT": {
				for (JsonElement packet : packets)
					callback.simulatePuppeteerCommand(packet.getAsJsonObject(), null);
				callback.resultCallback(new JsonObject());

				break;
			}
			case "TICKLY": {
				final int interval = request.has("interval") ? request.get("interval").getAsInt() : 0;
				int[] idx = {0};
				McPuppeteer.tasks.add(
				    PuppeteerTask.ticklyTask((a, b) -> {/*No startup*/}, (task, onCompletion) -> {
					    if (idx[0] % interval == 0) {
						    callback.simulatePuppeteerCommand(
							packets.get(idx[0] / interval).getAsJsonObject(), null);
						    if (idx[0] / interval + 1 > packets.size()) {
							    onCompletion.invoke();
							    callback.resultCallback(new JsonObject());
						    }
					    }

					    idx[0]++;
				    }, true /* Allow other task to run */));

				break;
			}
			case "SEQUENTIAL": {
				int[] idx = {0};

				PacketOnCompletion[] next = {null};
				next[0] = (ignored) -> {
					idx[0]++;
					if (idx[0] < packets.size())
						callback.simulatePuppeteerCommand(packets.get(idx[0]).getAsJsonObject(),
										  next[0]);
					else
						callback.resultCallback(new JsonObject());
				};

				next[0].onCompletion(null);
				break;
			}
		}
	}
}
