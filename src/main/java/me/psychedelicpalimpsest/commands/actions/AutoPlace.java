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

package me.psychedelicpalimpsest.commands.actions;

 import com.google.gson.JsonObject;
 import me.psychedelicpalimpsest.BaseCommand;
 import me.psychedelicpalimpsest.PuppeteerCommand;
 import net.minecraft.client.MinecraftClient;
 import net.minecraft.client.network.ClientPlayerEntity;
 import net.minecraft.client.world.ClientWorld;
 import net.minecraft.util.math.BlockPos;
 import net.minecraft.util.math.Direction;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;

 import static me.psychedelicpalimpsest.commands.actions.AutoUse.AutomaticallyUse;
 import static me.psychedelicpalimpsest.commands.actions.AutoUse.fromDirectionMap;

 @PuppeteerCommand(
         cmd = "auto place", description = "",
         cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT
 )
 public class AutoPlace implements BaseCommand {
     @Override
     public void onRequest(JsonObject request, LaterCallback callback) {
         ClientWorld w = MinecraftClient.getInstance().world;
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         String sdirection = request.has("direction") ? request.get("direction").getAsString() : null;
         BlockPos bp = new BlockPos(request.get("x").getAsInt(), request.get("y").getAsInt(), request.get("z").getAsInt());
         Direction direction;

         if (sdirection != null) {
             direction = Direction.valueOf(sdirection.toUpperCase());
         }else {
             List<Direction> candidates = Arrays.stream(Direction.values())
                     .filter(d -> !w.getBlockState(bp.offset(d)).isAir())
                     .filter(d -> bp.offset(d).toCenterPos().isInRange(player.getPos(), player.getBlockInteractionRange()))
                     .sorted((a, b) -> (int) bp.offset(a).getSquaredDistance(bp.offset(b)))
                     .toList();
             if (candidates.isEmpty()){
                 callback.resultCallback(BaseCommand.jsonOf(
                         "status", "error", "type", "cannot place", "message", "Either the block is out of range, or has no block to place against"
                 ));
                 return;
             }
             direction = candidates.getFirst();
         }

         BlockPos placeFrom = bp.offset(direction);
         direction = direction.getOpposite();
         AutomaticallyUse(
                 placeFrom.getX(), placeFrom.getY(), placeFrom.getZ(),
                 request.has("degrees per tick")
                         ? request.get("degrees per tick").getAsFloat()
                         : 4.0f,
                 request.has("method") ? request.get("method").getAsString() : "linear",
                 fromDirectionMap.get(direction),
                 callback::resultCallback,
                 () -> callback.resultCallback(BaseCommand.jsonOf())
         );
     }
 }
