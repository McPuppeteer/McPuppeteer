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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.reflection.McReflector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.registry.Registries;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;


@PuppeteerCommand(
        cmd = "get open inventory", description = "Gets what ever inventory/container/entity is open",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class GetOpenInventory implements BaseCommand {


    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        var screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof InventoryScreen
                || screen instanceof CreativeInventoryScreen
                || !(screen instanceof HandledScreen<?>)) {
            callback.resultCallback(GetPlayerInventory.getJson());
            return;
        }
        HandledScreen<ScreenHandler> handledScreen = (HandledScreen<ScreenHandler>) screen;
        ScreenHandler handler = handledScreen.getScreenHandler();

        JsonArray slots = new JsonArray();
        for (Slot slot : handler.slots) {
            slots.add(McReflector.serializeObject(slot.getStack()));
        }
        JsonObject ret = BaseCommand.jsonOf(
                "slots", slots,
                "name", handledScreen.getTitle().getString(),
                "type", handler instanceof HorseScreenHandler
                        ? "horse"
                        : Registries.SCREEN_HANDLER.getId(handler.getType()).toString()
        );
        if (handler instanceof HorseScreenHandler horseScreenHandler) {
            ret.add("horse data", BaseCommand.jsonOf(
                    "entity", Registries.ENTITY_TYPE.getId(horseScreenHandler.entity.getType()).toString(),
                    "inventory cols", horseScreenHandler.entity.getInventoryColumns()

            ));
        }

        callback.resultCallback(ret);
    }
}
