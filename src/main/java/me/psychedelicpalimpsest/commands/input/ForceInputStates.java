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
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;

@PuppeteerCommand(
        cmd = "force inputs",
        description = "Takes an 'inputs' parameter identical to 'get forced input's parameter. Also takes an array of strings 'remove' which will remove forced values",
        cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT
)
public class ForceInputStates implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (request.has("inputs") && request.get("inputs").isJsonObject()) {
            JsonObject job = request.getAsJsonObject("inputs");

            job.asMap().forEach((key, value) -> {
                PuppeteerInput.isForcePressed.put(key, value.getAsBoolean());
            });


        }


        if (request.has("remove") && request.isJsonArray()) {
            request.getAsJsonArray().forEach((obj) -> {
                String remove = obj.getAsString();
                if (!PuppeteerInput.validOptions.contains(remove)) {
                    callback.resultCallback(BaseCommand.jsonOf(
                            "state", "error",
                            "type", "expected argument",
                            "message", "Item '" + remove + "' is not a valid option"
                    ));
                }


                MinecraftClient.getInstance().execute(() -> PuppeteerInput.isForcePressed.remove(remove));
            });
        }

        callback.resultCallback(new JsonObject());
    }
}
