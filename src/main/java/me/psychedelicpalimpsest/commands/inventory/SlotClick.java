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

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.reflection.YarnMapping;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;



 /*
    See: https://minecraft.wiki/w/Java_Edition_protocol/Packets#Click_Container

  */


@PuppeteerCommand(
        cmd = "click slot", description = "simulates a single inventory slot click with an arbitrary button and action",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class SlotClick implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        Optional<SlotActionType> actionOpt =
                YarnMapping.deserializeEnum(SlotActionType.class, request.get("action").getAsString());
        if (actionOpt.isEmpty()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "enum error",
                    "message", "Please chose from: " + String.join(", ", YarnMapping.serializedValues(SlotActionType.class))
            ));
            return;
        }
        SlotActionType action = actionOpt.get();
        int button = request.get("button").getAsInt();
        int slotId = request.get("slot").getAsInt();


        MinecraftClient.getInstance().interactionManager.clickSlot(
                MinecraftClient.getInstance().player.currentScreenHandler.syncId,
                slotId,
                button,
                action,
                MinecraftClient.getInstance().player
        );


        callback.resultCallback(BaseCommand.jsonOf());
    }
}
