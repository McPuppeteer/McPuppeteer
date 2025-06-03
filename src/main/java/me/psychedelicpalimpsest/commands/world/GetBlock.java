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


package me.psychedelicpalimpsest.commands.world;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

@PuppeteerCommand(
        cmd = "get block",
        description = "Get the **nbt** data for a block",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class GetBlock implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        ClientWorld world = MinecraftClient.getInstance().world;
        BlockState bs = world.getBlockState(new BlockPos(request.get("x").getAsInt(), request.get("y").getAsInt(), request.get("z").getAsInt()));


        callback.nbtResultCallback(
                NbtHelper.fromBlockState(bs)
        );
    }
}
