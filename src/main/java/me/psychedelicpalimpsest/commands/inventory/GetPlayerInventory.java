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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.reflection.McReflector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;

@PuppeteerCommand(
    cmd = "get player inventory", description = "Gets the players inventory",
    cmd_context = BaseCommand.CommandContext.PLAY)
public class GetPlayerInventory implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		boolean force = request.has("force") && request.getAsJsonPrimitive("force").getAsBoolean();
		var mc = MinecraftClient.getInstance();

		if (!force && mc.currentScreen instanceof HandledScreen<?> handledScreen && !(handledScreen instanceof InventoryScreen || handledScreen instanceof CreativeInventoryScreen)) {
			callback.resultCallback(BaseCommand.jsonOf(
			    "status", "error",
			    "type", "incorrect inventory",
			    "message", "The player has a screen open that is not the inventory, any inventory features will not work correctly. "
					   + "This message can be disabled by setting force=true, but be warned not to use any slot commands."));
			return;
		}
		callback.resultCallback(getJson());
	}

	public static JsonObject getJson() {
		var mc = MinecraftClient.getInstance();
		var inventory = mc.player.getInventory();

		/* Use a temporary PlayerScreenHandler to convert the slot indexes for us (From storage indexes to network indexes) */
		PlayerScreenHandler psh = new PlayerScreenHandler(inventory, false, mc.player);

		JsonArray array = new JsonArray();
		for (Slot slot : psh.slots) {
			array.add(McReflector.serializeObject(slot.getStack()));
		}
		return BaseCommand.jsonOf(
		    "slots", array,
		    "name", inventory.getName().getString(),
		    "type", "inventory");
	}
}
