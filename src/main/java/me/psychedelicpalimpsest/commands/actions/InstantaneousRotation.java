 /**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.psychedelicpalimpsest.commands.actions;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "instantaneous rotation",
        description = "Immediately set the players rotation, no interpolation, just speed!",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class InstantaneousRotation implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (!request.has("pitch") || !request.has("yaw")) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Must have two float arguments, pitch and yaw"
            ));
            return;
        }
        float pitch = request.get("pitch").getAsFloat();
        float yaw = request.get("yaw").getAsFloat();


        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().player.setPitch(pitch);
            MinecraftClient.getInstance().player.setYaw(yaw);

            callback.resultCallback(BaseCommand.jsonOf());
        });

    }
}
