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

@PuppeteerCommand(
    cmd = "attack key click",
    description = "Simulate a single click of the attack key",
    cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT)
public class AttackKey implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		MinecraftClient.getInstance().execute(() -> {
			/* Wtf does this do? I just reset attackCooldown because of the check, but what does it actually do??????*/
			MinecraftClient.getInstance().attackCooldown = 0;
			MinecraftClient.getInstance().doAttack();
			callback.resultCallback(new JsonObject());
		});
	}
}
