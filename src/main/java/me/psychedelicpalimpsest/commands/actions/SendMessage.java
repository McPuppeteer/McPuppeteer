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


package me.psychedelicpalimpsest.commands.actions;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "send chat message",
        description = "Sends a PUBLIC chat message.",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class SendMessage implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        String message = request.get("message").getAsString();

        if (message.startsWith("/")) {
            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand(message.substring(1))
            );
        } else {
            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().player.networkHandler.sendChatMessage(message)
            );
        }

        callback.resultCallback(new JsonObject());
    }
}
