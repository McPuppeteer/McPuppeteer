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

package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.NoWalk;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(cmd = "set nowalk", description = "Enable/disable nowalk",
		  cmd_context = BaseCommand.CommandContext.PLAY)
public class SetNowalk implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		if (!request.has("enabled") || !request.get("enabled").isJsonPrimitive()) {
			callback.resultCallback(BaseCommand.jsonOf("status", "error", "message",
								   "Must have 'enabled' as a boolean property", "type",
								   "expected argument"));
			return;
		}

		MinecraftClient.getInstance().execute(() -> {
			if (request.get("enabled").getAsBoolean() != NoWalk.isActive) { NoWalk.toggle(null, null); }
			callback.resultCallback(BaseCommand.jsonOf());
		});
	}
}
