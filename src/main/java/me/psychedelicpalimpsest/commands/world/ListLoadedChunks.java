/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.commands.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.atomic.AtomicReferenceArray;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.chunk.WorldChunk;

@PuppeteerCommand(
    cmd = "list loaded chunks",
    description =
        "Lists what chunks are loaded for the player. Multiply these by 16 to get the 'real'"
            + " coordinates",
    cmd_context = BaseCommand.CommandContext.PLAY)
public class ListLoadedChunks implements BaseCommand {
  @Override
  public void onRequest(JsonObject request, LaterCallback callback) {
    JsonArray chunks =
        new JsonArray(MinecraftClient.getInstance().worldRenderer.getBuiltChunks().size());

    AtomicReferenceArray<WorldChunk> map =
        MinecraftClient.getInstance().world.getChunkManager().chunks.chunks;
    for (int i = 0; i < map.length(); i++) {
      JsonArray array = new JsonArray(2);
      WorldChunk chunk = map.get(i);
      if (chunk == null) continue;

      array.add(chunk.getPos().x);
      array.add(chunk.getPos().z);
      chunks.add(array);
    }

    callback.resultCallback(BaseCommand.jsonOf("chunks", chunks));
  }
}
