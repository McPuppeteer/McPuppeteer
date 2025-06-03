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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.nio.ByteBuffer;
import java.util.*;


@PuppeteerCommand(
        cmd="get chunk",
        description = "Gets a **binary** pelleted version of a chunk.",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class GetChunk implements BaseCommand {

    /* I feel in my heart this is a good estimation */
    final static int SET_SIZE_EST = MathHelper.ceilLog2(16 * 16 * 16);


    /*
        Minecraft (for some reason) changes with almost ever version.
        So it is simply to use my own format! This way we do not need to change every version
     */
    public static void serializeSimple(ChunkSection section, PacketByteBuf buf) {
        HashMap<Integer, Integer> bsMap = new HashMap<>(SET_SIZE_EST);
        int id = 0;

        NbtList nbtList = new NbtList();

        /* Build the palette */
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 16; y++) {
            BlockState bs = section.getBlockState(x, y, z);
            int rawId = Block.getRawIdFromState(bs);
            if (!bsMap.containsKey(rawId)) {
                bsMap.put(rawId, id++);
                nbtList.add(NbtHelper.fromBlockState(bs));
            }
        }




        int bitSize = Math.max(1, MathHelper.ceilLog2(bsMap.size()));

        /* Overestimate the amount of data (as longs) */
        int dataSize = ((bitSize * 16 * 16 * 16) + 63) / 64;

        int bit = 0;
        int clong = 0;

        long[] blocks = new long[dataSize];
        blocks[0] = 0;

        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 16; y++) {
            BlockState bs = section.getBlockState(x, y, z);
            long rawId = (long) bsMap.get(Block.getRawIdFromState(bs));

            blocks[clong] |= rawId << bit;

            bit += bitSize;
            if (bit > 64){
                bit -= 64;
                clong++;

                blocks[clong] = rawId >> bit;
            }
        }

        buf.writeShort(bitSize);
        buf.writeInt(dataSize);
        buf.writeNbt(nbtList);

        for (long l : blocks) {
            buf.writeLong(l);
        }
    }


    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        ClientWorld world = MinecraftClient.getInstance().world;

        WorldChunk c = MinecraftClient.getInstance().world.getChunk(request.get("cx").getAsInt(), request.get("cz").getAsInt());
        if (c == null) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "unknown chunk",
                    "message", "Cannot find chunk"
            ));
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
        for (ChunkSection section : c.getSectionArray()) {
            serializeSimple(section, buf);
        }

        callback.packetResultCallback(buf.array());
    }
}
