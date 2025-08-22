/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
    cmd = "set directional movement degree",
    description =
        "Make the player move a certain direction, in degrees, globally independent of the players"
            + " rotation.",
    cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT)
public class SetDirectionalMovement implements BaseCommand {
  @Override
  public void onRequest(JsonObject request, LaterCallback callback) {
    MinecraftClient.getInstance()
        .execute(
            () -> {
              PuppeteerInput.direction = request.get("direction").getAsFloat();
              PuppeteerInput.directionalSpeed =
                  request.has("speed") ? request.get("speed").getAsFloat() : 1f;

              if (!(request.has("force") && request.get("force").getAsBoolean())) {
                PuppeteerInput.directionalSpeed =
                    Math.clamp(PuppeteerInput.directionalSpeed, 0f, 1f);
              }

              PuppeteerInput.isDirectionalMovement = true;
              callback.resultCallback(new JsonObject());
            });
  }
}
