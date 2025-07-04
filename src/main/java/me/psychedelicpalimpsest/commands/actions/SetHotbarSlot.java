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
import net.minecraft.util.math.MathHelper;


@PuppeteerCommand(
        cmd = "set hotbar slot",
        description = "Set the current hotbar slot. The parameter is 'slot' and is [1, 9]",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class SetHotbarSlot implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(() -> {

            MinecraftClient.getInstance().player.getInventory().setSelectedSlot(
                    MathHelper.clamp(request.get("slot").getAsInt() - 1, 0, 8)
            );

            callback.resultCallback(BaseCommand.jsonOf());
        });

    }
}
