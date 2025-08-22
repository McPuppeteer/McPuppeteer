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

package me.psychedelicpalimpsest.commands.currentWorld;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

@PuppeteerCommand(
    cmd = "join world",
    description = "Joins a local world. This requires one parameter, 'load name', which needs to be the EXACT same as from 'load name' from 'get worlds'")
public class JoinWorld implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {

		if (request.get("load name") == null || !request.get("load name").isJsonPrimitive()) {
			callback.resultCallback(BaseCommand.jsonOf(
			    "status", "error",
			    "message", "Missing parameter 'load name' in joinWorld",
			    "type", "expected argument"));
			return;
		}
		MinecraftClient.getInstance().execute(() -> {
			Thread listenThread = new Thread(() -> {
				try {
					while (true) {
						if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
							callback.resultCallback(BaseCommand.jsonOf("message", "in game"));
							return;
						}

						Thread.sleep(100);
						Thread.yield();
					}
				} catch (InterruptedException e) {
				}
			});
			listenThread.start();

			MinecraftClient.getInstance().createIntegratedServerLoader().start(request.get("load name").getAsString(), () -> {
				callback.resultCallback(BaseCommand.jsonOf(
				    "status", "error",
				    "type", "cannot join world",
				    "message", "Unknown world join error, are you sure you sent the 'load name' parameter directly from the 'load name' value from 'get worlds'?"));
				listenThread.interrupt();
				MinecraftClient.getInstance().setScreen(new TitleScreen());
			});
		});
	}
}
