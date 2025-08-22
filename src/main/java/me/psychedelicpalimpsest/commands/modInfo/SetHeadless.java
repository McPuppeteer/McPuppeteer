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
import me.psychedelicpalimpsest.modules.HeadlessMode;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
    cmd = "set headless",
    description = "Disabled game rendering, and hides the game window. During this time it will be like Minecraft is not even open")
public class SetHeadless implements BaseCommand {

	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {

		MinecraftClient.getInstance().execute(() -> {
			if (request.getAsJsonPrimitive("enabled").getAsBoolean()) {
				HeadlessMode.setHeadless();
			} else {
				HeadlessMode.disableHeadless();
			}
			callback.resultCallback(BaseCommand.jsonOf());
		});
	}
}
