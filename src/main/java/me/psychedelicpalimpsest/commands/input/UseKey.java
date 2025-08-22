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

package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(cmd = "use key click", description = "Simulate a single click of the use key",
		  cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT)
public class UseKey implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		MinecraftClient.getInstance().execute(() -> {
			MinecraftClient.getInstance().doItemUse();
			callback.resultCallback(new JsonObject());
		});
	}
}
