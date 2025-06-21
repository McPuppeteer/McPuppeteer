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
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.utils.EventBasedTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;


@PuppeteerCommand(
        cmd = "swap slots", description = "Attempts to swap slots with simplistic pickup actions. Does NOT handle accidental merges.",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class SlotSwap implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        int slot1 = request.getAsJsonPrimitive("slot1").getAsInt();
        int slot2 = request.getAsJsonPrimitive("slot2").getAsInt();
        boolean useOffhand = request.has("useOffhand") && request.getAsJsonPrimitive("useOffhand").getAsBoolean();

        var mng = MinecraftClient.getInstance().interactionManager;
        var p = MinecraftClient.getInstance().player;

        /* According to the wiki, 40 in swap mode swaps with the offhand */
        int button = useOffhand ? 40 : 0;
        var action = useOffhand ? SlotActionType.SWAP : SlotActionType.PICKUP;


        McPuppeteer.tasks.add(new EventBasedTask(List.of(
                (self, onCompletion) -> mng.clickSlot(
                        p.currentScreenHandler.syncId,
                        slot1,
                        button,
                        action,
                        p
                ),

                (self, onCompletion) -> mng.clickSlot(
                        p.currentScreenHandler.syncId,
                        slot2,
                        button,
                        action,
                        p
                ),

                (self, onCompletion) -> {
                    mng.clickSlot(
                            p.currentScreenHandler.syncId,
                            slot1,
                            button,
                            action,
                            p
                    );
                    callback.resultCallback(new JsonObject());
                }
        ), 1));
    }
}
