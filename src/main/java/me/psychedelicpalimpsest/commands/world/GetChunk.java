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

package me.psychedelicpalimpsest.commands.world;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;

@PuppeteerCommand(cmd = "get chunk", description = "Gets a **binary** pelleted version of a chunk.",
		  cmd_context = BaseCommand.CommandContext.PLAY)
public class GetChunk implements BaseCommand {

	/*
	    Minecraft keeps its chunks serialized internally, to we simply serialize the palette, and send the
	    complete segment.
	 */
	public static void serializeSimple(ChunkSection section, PacketByteBuf buf) {
		var data = section.getBlockStateContainer().data;
		NbtList nbtList = new NbtList();
		for (int i = 0; i < data.palette().getSize(); i++) {
			nbtList.add(NbtHelper.fromBlockState(data.palette().get(i)));
		}
		long[] longs = data.storage().getData();
		buf.writeShort(data.storage().getElementBits());
		buf.writeShort(section.getBlockStateContainer().paletteProvider.edgeBits);

		buf.writeInt(longs.length);
		buf.writeNbt(nbtList);

		for (long l : longs) buf.writeLong(l);
	}

	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		ClientWorld world = MinecraftClient.getInstance().world;

		new Thread(() -> {
			WorldChunk c = MinecraftClient.getInstance().world.getChunk(request.get("cx").getAsInt(),
										    request.get("cz").getAsInt());
			if (c == null) {
				callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "unknown chunk",
									   "message", "Cannot find chunk"));
				return;
			}

			PacketByteBuf buf = PacketByteBufs.create();
			NbtList nbtList = new NbtList();
			c.getBlockEntities().forEach(((blockPos, blockEntity) -> {
				NbtCompound tag = new NbtCompound();
				tag.putInt("x", blockPos.getX());
				tag.putInt("y", blockPos.getY());
				tag.putInt("z", blockPos.getZ());

				tag.put("data", blockEntity.createNbt(world.getRegistryManager()));
				nbtList.add(tag);
			}));

			/* Block entities */
			buf.writeNbt(nbtList);

			/* Section info */
			buf.writeInt(c.getBottomSectionCoord());
			buf.writeInt(c.getTopSectionCoord());
			buf.writeShort(c.getSectionArray().length);
			for (ChunkSection section : c.getSectionArray()) { serializeSimple(section, buf); }

			callback.packetResultCallback(buf.array());
		}).start();
	}
}
