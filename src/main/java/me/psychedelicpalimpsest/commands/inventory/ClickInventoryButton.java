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

package me.psychedelicpalimpsest.commands.inventory;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(cmd = "click inventory button", description = "Simulates clicking an inventory button",
		  cmd_context = BaseCommand.CommandContext.PLAY)
public class ClickInventoryButton implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		if (MinecraftClient.getInstance().currentScreen == null) {
			callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "unexpected screen",
								   "message", "No screen is open"));
			return;
		}

		MinecraftClient.getInstance().interactionManager.clickButton(
		    MinecraftClient.getInstance().player.currentScreenHandler.syncId, request.get("button").getAsInt());

		callback.resultCallback(new JsonObject());
	}
}
