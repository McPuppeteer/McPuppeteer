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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.PuppeteerTask;
import me.psychedelicpalimpsest.utils.EventBasedTask;
import me.psychedelicpalimpsest.utils.Rotation;
import me.psychedelicpalimpsest.utils.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.utils.RotationUtils.calcRotationFromVec3d;


@PuppeteerCommand(
        cmd = "auto use", description = "",
        cmd_context = BaseCommand.CommandContext.PLAY_WITH_MOVEMENT
)
public class AutoUse implements BaseCommand {
    public interface UseOnError {
        void invoke(JsonObject error);
    }

    public interface UseOnSuccess {
        void invoke();
    }


    public final static Map<String, Direction> directionMap = ImmutableMap.of(
            "north", Direction.NORTH,
            "south", Direction.SOUTH,

            "east", Direction.EAST,
            "west", Direction.WEST,

            "up", Direction.UP,
            "down", Direction.DOWN
    );
    public final static Map<Direction, String> fromDirectionMap = ImmutableMap.of(
            Direction.NORTH, "north",
            Direction.SOUTH, "south",

            Direction.EAST, "east",
            Direction.WEST, "west",

            Direction.UP, "up",
            Direction.DOWN, "down"
    );

    private static Vec3d getCenter(VoxelShape shape) {
        double minX = shape.getMin(Direction.Axis.X);
        double minY = shape.getMin(Direction.Axis.Y);
        double minZ = shape.getMin(Direction.Axis.Z);

        double maxX = shape.getMax(Direction.Axis.X);
        double maxY = shape.getMax(Direction.Axis.Y);
        double maxZ = shape.getMax(Direction.Axis.Z);


        return new Vec3d(
                (maxX + minX) / 2,
                (maxY + minY) / 2,
                (maxZ + minZ) / 2
        );
    }


    @Nullable
    public static Vec3d handleAndGetPositionForRequest(int x, int y, int z, @Nullable String direction, UseOnError onError) {
        ClientWorld world = MinecraftClient.getInstance().world;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        BlockPos bp = new BlockPos(
                x, y, z
        );


        BlockState bs = world.getBlockState(bp);

        VoxelShape shape = bs.getOutlineShape(world, bp).offset(bp.getX(), bp.getY(), bp.getZ());
        if (shape.isEmpty()) {
            onError.invoke(BaseCommand.jsonOf("status", "error", "type", "block surface"));
            return null;
        }

        Vec3d point;


        if (direction != null) {
            Direction dirr = directionMap.get(direction);
            if (dirr == null) {
                onError.invoke(BaseCommand.jsonOf("status", "error", "type", "expected argument", "message", "Invalid direction"));
                return null;
            }

            /*
                This a dumb way of doing things...

                Go two block in that direction, and get the closest face.
                But it does seem to work.
             */

            point = getCenter(shape).add(Vec3d.of(dirr.getVector()).multiply(2));
            point = RotationUtils.getCenterOfClosestFace(shape, point).get();
        } else {
            point = RotationUtils.getCenterOfClosestFace(shape, p.getEyePos()).get();
        }

        return point;
    }

    public static void AutomaticallyUse(
            int x, int y, int z, float degreesPerTick, String method, @Nullable String direction, UseOnError onError, UseOnSuccess onSuccess) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;

        BlockPos bp = new BlockPos(
                x, y, z
        );


        final Vec3d point = handleAndGetPositionForRequest(x, y, z, direction, onError);
        if (point == null) return;


        double range = p.getBlockInteractionRange();
        if (p.squaredDistanceTo(point) > range * range) {
            onError.invoke(
                    BaseCommand.jsonOf("status", "error", "type", "block range")
            );
            return;
        }

        PuppeteerTask.TaskEvent useEvent = (self, onCompletion) -> {
            if (p.squaredDistanceTo(point) > range * range) {
                onError.invoke(
                        BaseCommand.jsonOf("status", "error", "type", "block range")
                );
                return;
            }


            MinecraftClient.getInstance().interactionManager.interactBlock(
                    p,
                    Hand.MAIN_HAND,
                    new BlockHitResult(
                            point,
                            direction != null
                                    ? directionMap.get(direction)
                                    : Direction.getFacing(point.subtract(Vec3d.of(bp))),
                            bp,
                            false

                    )
            );
            onSuccess.invoke();
        };
        final Rotation rot = calcRotationFromVec3d(
                p.getEyePos(),
                point,
                new Rotation(p.getYaw(), p.getPitch())
        );

        if (method.equals("instant")) {
            McPuppeteer.tasks.add(new EventBasedTask(List.of(
                    (self, onCompletion) -> {
                        p.setYaw(rot.getYaw());
                        p.setPitch(rot.getPitch());
                    },
                    useEvent
            )));
        } else {
            AlgorithmicRotation.AlgorithmiclyRotate(
                    rot.getPitch(), rot.getYaw(), degreesPerTick, method,
                    onError::invoke,
                    () -> MinecraftClient.getInstance().execute(() -> useEvent.invoke(null, null))
            );

        }
    }


    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        AutomaticallyUse(
                request.get("x").getAsInt(), request.get("y").getAsInt(), request.get("z").getAsInt(),
                request.has("degrees per tick")
                        ? request.get("degrees per tick").getAsFloat()
                        : 4.0f,
                request.has("method") ? request.get("method").getAsString() : "linear",
                request.has("direction") ? request.get("direction").getAsString() : null,
                callback::resultCallback,
                () -> callback.resultCallback(BaseCommand.jsonOf())
        );


    }
}
