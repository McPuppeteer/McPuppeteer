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

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.*;
import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;
import me.psychedelicpalimpsest.modules.NoWalk;
import me.psychedelicpalimpsest.modules.PuppeteerInput;

@PuppeteerCommand(cmd = "panic", description = "Same as pressing the panic button")
public class Panic implements BaseCommand {
	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		panic();
	}

	public static void panic() {

		if (McPuppeteer.installedMods.contains("baritone")) { BaritoneListener.panic(); }

		if (!McPuppeteer.tasks.isEmpty()) {
			McPuppeteer.tasks.peek().kill();
			McPuppeteer.tasks.clear();
		}

		if (NoWalk.isActive) NoWalk.toggle(null, null);
		if (Freerot.isFreerotActive()) Freerot.toggleFreerot(null, null);
		if (Freecam.isFreecamActive()) Freecam.toggleFreecam(null, null);

		PuppeteerInput.isForcePressed.clear();
		PuppeteerInput.allowUserInput = true;
		PuppeteerInput.isDirectionalMovement = false;

		PuppeteerServer.broadcastJsonPacket(
		    CallbackManager.CallbackType.FORCED,
		    ()
			-> BaseCommand.jsonOf("status", "error", "type", "panic", "message",
					      "The user has pressed that panic button",

					      /* Specifically force the client to interpret as error */
					      "callback", false));
	}
}
