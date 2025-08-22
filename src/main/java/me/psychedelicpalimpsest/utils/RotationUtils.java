/*
 * I stole this code from baritone
 * From: https://github.com/cabaletta/baritone/blob/89b0fd74ddf8a9e3c2450fafd28267769e31d6a2/src/api/java/baritone/api/utils/RotationUtils.java
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.utils;

import java.util.Optional;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public final class RotationUtils {
  public static final double DEG_TO_RAD = 0.017453292519943295;
  public static final double RAD_TO_DEG = 57.29577951308232;
  public static final Vec3d[] BLOCK_SIDE_MULTIPLIERS =
      new Vec3d[] {
        new Vec3d(0.5, 0.0, 0.5),
        new Vec3d(0.5, 1.0, 0.5),
        new Vec3d(0.5, 0.5, 0.0),
        new Vec3d(0.5, 0.5, 1.0),
        new Vec3d(0.0, 0.5, 0.5),
        new Vec3d(1.0, 0.5, 0.5)
      };

  public RotationUtils() {}

  /**
   * Returns the center of the face (of any box in the shape) closest to the target point.
   *
   * @param shape The VoxelShape to search.
   * @param target The target point.
   * @return Optional containing the center of the closest face, or empty if shape is empty.
   *     <p>
   *     <p>Chatgpt made this, therefore public domain
   */
  public static Optional<Vec3d> getCenterOfClosestFace(VoxelShape shape, Vec3d target) {
    if (shape.isEmpty()) {
      return Optional.empty();
    }

    final Vec3d[] closestFaceCenter = {null};
    final double[] closestDistanceSq = {Double.POSITIVE_INFINITY};

    shape.forEachBox(
        (minX, minY, minZ, maxX, maxY, maxZ) -> {
          // All 6 face centers
          Vec3d[] faceCenters =
              new Vec3d[] {
                // minX face
                new Vec3d(minX, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0),
                // maxX face
                new Vec3d(maxX, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0),
                // minY face
                new Vec3d((minX + maxX) / 2.0, minY, (minZ + maxZ) / 2.0),
                // maxY face
                new Vec3d((minX + maxX) / 2.0, maxY, (minZ + maxZ) / 2.0),
                // minZ face
                new Vec3d((minX + maxX) / 2.0, (minY + maxY) / 2.0, minZ),
                // maxZ face
                new Vec3d((minX + maxX) / 2.0, (minY + maxY) / 2.0, maxZ)
              };

          for (Vec3d faceCenter : faceCenters) {
            double distanceSq = target.squaredDistanceTo(faceCenter);
            if (distanceSq < closestDistanceSq[0]) {
              closestDistanceSq[0] = distanceSq;
              closestFaceCenter[0] = faceCenter;
            }
          }
        });

    return Optional.ofNullable(closestFaceCenter[0]);
  }

  public static Rotation calcRotationFromCoords(BlockPos orig, BlockPos dest) {
    return calcRotationFromVec3d(
        new Vec3d(orig.getX(), orig.getY(), orig.getZ()),
        new Vec3d(dest.getX(), dest.getY(), dest.getZ()));
  }

  public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
    return current.yawIsReallyClose(target)
        ? new Rotation(current.getYaw(), target.getPitch())
        : target.subtract(current).normalize().add(current);
  }

  public static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest, Rotation current) {
    return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
  }

  public static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
    double[] delta = new double[] {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
    double yaw = MathHelper.atan2(delta[0], -delta[2]);
    double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
    double pitch = MathHelper.atan2(delta[1], dist);
    return new Rotation((float) (yaw * 57.29577951308232), (float) (pitch * 57.29577951308232));
  }

  public static Vec3d calcVector3dFromRotation(Rotation rotation) {
    float f = MathHelper.cos(-rotation.getYaw() * 0.017453292F - 3.1415927F);
    float f1 = MathHelper.sin(-rotation.getYaw() * 0.017453292F - 3.1415927F);
    float f2 = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
    float f3 = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
    return new Vec3d(f1 * f2, f3, f * f2);
  }

  public static Vec3d calculateBlockCenter(World world, BlockPos pos) {
    BlockState b = world.getBlockState(pos);
    VoxelShape shape = b.getCollisionShape(world, pos);
    if (shape.isEmpty()) {
      return getBlockPosCenter(pos);
    } else {
      double xDiff = (shape.getMin(Direction.Axis.X) + shape.getMax(Direction.Axis.X)) / 2.0;
      double yDiff = (shape.getMin(Direction.Axis.Y) + shape.getMax(Direction.Axis.Y)) / 2.0;
      double zDiff = (shape.getMin(Direction.Axis.Z) + shape.getMax(Direction.Axis.Z)) / 2.0;
      if (!Double.isNaN(xDiff) && !Double.isNaN(yDiff) && !Double.isNaN(zDiff)) {
        if (b.getBlock() instanceof AbstractFireBlock) {
          yDiff = 0.0;
        }

        return new Vec3d(
            (double) pos.getX() + xDiff, (double) pos.getY() + yDiff, (double) pos.getZ() + zDiff);
      } else {
        throw new IllegalStateException("" + b + " " + pos + " " + shape);
      }
    }
  }

  public static Vec3d getBlockPosCenter(BlockPos pos) {
    return new Vec3d(
        (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5);
  }
}
