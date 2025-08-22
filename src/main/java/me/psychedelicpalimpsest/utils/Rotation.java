/*
 * I stole this code from baritone
 * From: https://github.com/cabaletta/baritone/blob/89b0fd74ddf8a9e3c2450fafd28267769e31d6a2/src/api/java/baritone/api/utils/Rotation.java
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

public class Rotation {
    private final float yaw;
    private final float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        if (Float.isInfinite(yaw) || Float.isNaN(yaw) || Float.isInfinite(pitch) || Float.isNaN(pitch)) {
            throw new IllegalStateException("" + yaw + " " + pitch);
        }
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Rotation add(Rotation other) {
        return new Rotation(this.yaw + other.yaw, this.pitch + other.pitch);
    }

    public Rotation subtract(Rotation other) {
        return new Rotation(this.yaw - other.yaw, this.pitch - other.pitch);
    }

    public Rotation clamp() {
        return new Rotation(this.yaw, clampPitch(this.pitch));
    }

    public Rotation normalize() {
        return new Rotation(normalizeYaw(this.yaw), this.pitch);
    }

    public Rotation normalizeAndClamp() {
        return new Rotation(normalizeYaw(this.yaw), clampPitch(this.pitch));
    }

    public boolean isReallyCloseTo(Rotation other) {
        return this.yawIsReallyClose(other) && (double) Math.abs(this.pitch - other.pitch) < 0.01;
    }

    public boolean yawIsReallyClose(Rotation other) {
        float yawDiff = Math.abs(normalizeYaw(this.yaw) - normalizeYaw(other.yaw));
        return (double) yawDiff < 0.01 || (double) yawDiff > 359.99;
    }

    public static float clampPitch(float pitch) {
        return Math.max(-90.0F, Math.min(90.0F, pitch));
    }

    public static float normalizeYaw(float yaw) {
        float newYaw = yaw % 360.0F;
        if (newYaw < -180.0F) {
            newYaw += 360.0F;
        }

        if (newYaw > 180.0F) {
            newYaw -= 360.0F;
        }

        return newYaw;
    }

    public String toString() {
        return "Yaw: " + this.yaw + ", Pitch: " + this.pitch;
    }
}

