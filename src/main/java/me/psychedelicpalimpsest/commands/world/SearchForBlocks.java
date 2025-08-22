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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;

@PuppeteerCommand(
    cmd = "search for blocks", description = "Search the players render distance for a block by id",
    cmd_context = BaseCommand.CommandContext.PLAY)
public class SearchForBlocks implements BaseCommand {

	static int[] computelocation(int edge, int index) {
		// mask for one coordinate (edgebits low-order bits set to 1)
		int mask = (1 << edge) - 1;
		// extract x from lowest edgebits bits
		int x = index & mask;
		// extract z from next edgebits bits
		int z = (index >> edge) & mask;
		// extract y from following edgebits bits
		int y = (index >> (2 * edge)) & mask;
		return new int[] {x, y, z};
	}

	// Search for blocks by RAW BLOCK ID
	// NOTE: Although I have tried to optimize it, this can still be quite long-running
	static JsonArray blockSearcher(HashSet<Integer> target) {
		JsonArray ret = new JsonArray();
		var map = MinecraftClient.getInstance().world.getChunkManager().chunks.chunks;
		var world = MinecraftClient.getInstance().world;
		for (int i = 0; i < map.length(); i++) {
			var chunk = map.get(i);
			// Skip unloaded chunks
			if (chunk == null) continue;

			var cp = chunk.getPos();
			// Chunks are composed of many sections, with an implementation defined start idx
			int ySec = chunk.getBottomSectionCoord();

			for (var section : chunk.getSectionArray()) {
				// From section to cord
				final int offy = (ySec++) * 16;

				if (section.isEmpty()) continue;

				// Palette idx to value, but only target items
				var results = new HashMap<Integer, BlockState>();
				var blockStateContainer = section.getBlockStateContainer();
				PalettedContainer.Data<BlockState> data = section.getBlockStateContainer().data;

				// Search the palette
				var palette = data.palette();
				for (int idx = 0; idx < palette.getSize(); idx++) {
					BlockState item = palette.get(idx);
					/* Can this be further optimized? */
					if (target.contains(Registries.BLOCK.getRawId(item.getBlock())))
						results.put(idx, item);
				}

				// Skip if no results in the section are found
				if (results.isEmpty()) continue;
				// Index within a section
				final int[] secIdx = {0};
				data.storage().forEach((id) -> {
					// Skip blocks which are now our results
					if (results.containsKey(id)) {
						JsonObject obj = new JsonObject();

						var rel_loc = computelocation(blockStateContainer.paletteProvider.edgeBits, secIdx[0]);
						var global_loc = new BlockPos(
						    cp.getOffsetX(rel_loc[0]),
						    rel_loc[1] + offy,
						    cp.getOffsetZ(rel_loc[2]));
						var entity = world.getBlockEntity(global_loc);

						obj.add("state", McReflector.serializeObject(results.get(id)));
						if (entity != null) {
							obj.add("entity data", McReflector.serializeObject(
										   entity.createNbtWithIdentifyingData(
										       world.getRegistryManager())));
						}
						obj.addProperty("x", global_loc.getX());
						obj.addProperty("y", global_loc.getX());
						obj.addProperty("z", global_loc.getZ());

						ret.add(obj);
					}
					secIdx[0]++;
				});
			}
		}
		return ret;
	}

	@Override
	public void onRequest(JsonObject request, LaterCallback callback) {
		JsonArray array = request.getAsJsonArray("blocks");

		HashSet<Integer> targets = new HashSet<>(array.size());
		for (JsonElement idElem : array) {
			Identifier id = Identifier.of(idElem.getAsString());
			Block b = Registries.BLOCK.get(id);

			// Ensure valid block
			if (!Registries.BLOCK.getId(b).toString().equals(id.toString())) {
				callback.resultCallback(BaseCommand.jsonOf(
				    "status", "error",
				    "type", "search error",
				    "message", "Block id is not valid: " + id.toString()));
				return;
			}
			targets.add(Registries.BLOCK.getRawId(b));
		}

		new Thread(() -> {
			try {
				callback.resultCallback(BaseCommand.jsonOf(
				    "blocks", blockSearcher(targets)));
			} catch (Exception e) {
				LOGGER.error("Error in block searcher", e);
			}
		}).start();
	}
}
