/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.actions;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.util.game.BlockUtils;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.PuppeteerTask;
import me.psychedelicpalimpsest.utils.EventBasedTask;
import me.psychedelicpalimpsest.utils.Rotation;
import me.psychedelicpalimpsest.utils.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.RotationUtils.calcRotationFromVec3d;


@PuppeteerCommand(
        cmd = "instant use", description = ""
)
public class InstantaneousUse implements BaseCommand {
    final static Map<String, Direction> directionMap = ImmutableMap.of(
            "north", Direction.NORTH,
            "south", Direction.SOUTH,

            "east", Direction.EAST,
            "west", Direction.WEST,

            "up", Direction.UP,
            "down", Direction.DOWN
    );

    private static Vec3d getCenter(VoxelShape shape) {
        double minX = shape.getMin(Direction.Axis.X);
        double minY = shape.getMin(Direction.Axis.Y);
        double minZ = shape.getMin(Direction.Axis.Z);

        double maxX = shape.getMax(Direction.Axis.X);
        double maxY = shape.getMax(Direction.Axis.Y);
        double maxZ = shape.getMax(Direction.Axis.Z);



        return new Vec3d(
                minX + (maxX - minX) / 2,
                minY + (maxY - minY) / 2,
                minZ + (maxZ - minZ) / 2
        );
    }


    @Nullable
    public static Vec3d handleAndGetPositionForRequest(JsonObject request, LaterCallback callback) {
        ClientWorld world = MinecraftClient.getInstance().world;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        BlockPos bp = new BlockPos(
                request.get("x").getAsInt(), request.get("y").getAsInt(), request.get("z").getAsInt()
        );


        BlockState bs =  world.getBlockState(bp);

        VoxelShape shape = bs.getOutlineShape(world, bp).offset(bp.getX(), bp.getY(), bp.getZ());
        if (shape.isEmpty()){
            callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "block surface"));
            return null;
        }

        Vec3d point;


        if (request.has("direction")) {
            Direction direction = directionMap.get(request.get("direction").getAsString());
            if (direction == null){
                callback.resultCallback(BaseCommand.jsonOf("status", "error", "type", "expected argument", "message", "Invalid direction"));
                return null;
            }

            /*
                This a dumb way of doing things...

                Go two block in that direction, and get the closest face.
                But it does seem to work.
             */

            point = getCenter(shape).add(Vec3d.of(direction.getVector()).multiply(2));
            point = RotationUtils.getCenterOfClosestFace(shape, point).get();
        }else{
            point = RotationUtils.getCenterOfClosestFace(shape, p.getEyePos()).get();
        }

        return point;
    }



    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {

        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        BlockPos bp = new BlockPos(
                request.get("x").getAsInt(), request.get("y").getAsInt(), request.get("z").getAsInt()
        );


        GameRenderer

        final Vec3d point = handleAndGetPositionForRequest(request, callback);
        if (point == null) return;



        if (p.squaredDistanceTo(point) > 1){}



        McPuppeteer.tasks.add(new EventBasedTask(List.of(
                (self, onCompletion) -> {
                    Rotation rot = calcRotationFromVec3d(
                            p.getEyePos(),
                            point,
                            new Rotation(p.getYaw(), p.getPitch())
                    );
                    p.setYaw(rot.getYaw());
                    p.setPitch(rot.getPitch());
                },
                (self, onCompletion) -> {
                    MinecraftClient.getInstance().interactionManager.interactBlock(
                            p,
                            Hand.MAIN_HAND,
                            new BlockHitResult(
                                    point,
                                    request.has("direction")
                                            ? directionMap.get(request.get("direction").getAsString())
                                            : Direction.getFacing(point.subtract(Vec3d.of(bp))),
                                    bp,
                                    false

                            )
                    );
                }
        )));





//         Vec3d closet = shape.getClosestPointTo(p.getEyePos()).get();













    }
}
