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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.reflection.McReflector;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;

@PuppeteerCommand(
        cmd = "search for block", description = "Search the players render distance for a block by id",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class SearchForBlock implements BaseCommand {
    static <T> void extractPaletteItems(Palette<T> palette, Consumer<Pair<Integer, T>> consumer) {
        for (int i = 0; i < palette.getSize(); i++) {
            T item = palette.get(i);
            consumer.accept(new Pair<>(i, item));
        }
    }

    static int[] computelocation(int edge, int index) {
        // mask for one coordinate (edgebits low-order bits set to 1)
        int mask = (1 << edge) - 1;
        // extract x from lowest edgebits bits
        int x = index & mask;
        // extract z from next edgebits bits
        int z = (index >> edge) & mask;
        // extract y from following edgebits bits
        int y = (index >> (2 * edge)) & mask;
        return new int[]{x, y, z};
    }

    static JsonArray blockSearcher(HashSet<String> target) {
        JsonArray ret = new JsonArray();
        var map = MinecraftClient.getInstance().world.getChunkManager().chunks.chunks;
        for (int i = 0; i < map.length(); i++) {
            var chunk = map.get(i);
            if (chunk == null) continue;

            final int offx = chunk.getPos().x * 16;
            final int offz = chunk.getPos().z * 16;
            int ySec = 0;
            for (var section : chunk.getSectionArray()) {
                final int offy = (ySec++) * 16;
                var results = new HashMap<Integer, BlockState>();
                var cont = section.getBlockStateContainer();
                PalettedContainer.Data<BlockState> data = section.getBlockStateContainer().data;

                extractPaletteItems(
                        data.palette(), (item) -> {
                            if (target.contains(Registries.BLOCK.getId(item.getRight().getBlock()).toString()))
                                results.put(item.getLeft(), item.getRight());
                        }
                );
                if (results.isEmpty()) continue;
                final int[] secIdx = {0};
                data.storage().forEach((id) -> {
                    if (!results.containsKey(id)) return;
                    JsonObject obj = new JsonObject();
                    int edge = cont.paletteProvider.edgeBits;
                    var location = computelocation(edge, secIdx[0]);


                    obj.add("state", McReflector.serializeObject(results.get(id)));
                    obj.addProperty("x", location[0] + offx);
                    obj.addProperty("y", location[1] + offy);
                    obj.addProperty("z", location[2] + offz);

                    ret.add(obj);
                    secIdx[0]++;
                });
            }
        }
        return ret;
    }

    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        JsonArray array = request.getAsJsonArray("blocks");

        HashSet<String> targets = new HashSet<>(array.size());
        for (JsonElement idElem : array) {
            String id = idElem.getAsString();
            targets.add(id);
        }

        new Thread(() -> {
            try {
                callback.resultCallback(BaseCommand.jsonOf(
                        "blocks", blockSearcher(targets)
                ));
            } catch (Exception e) {
                LOGGER.error("Error in block searcher", e);
            }
        }).start();
    }
}
