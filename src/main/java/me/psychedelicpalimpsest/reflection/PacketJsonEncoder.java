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

package me.psychedelicpalimpsest.reflection;

import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import me.psychedelicpalimpsest.BaseCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;

import java.lang.reflect.Field;

public class PacketJsonEncoder {
    @SuppressWarnings({"unchecked"})
    public static JsonObject encode(Packet<?> packet) {
        var nh = MinecraftClient.getInstance().getNetworkHandler();
        JsonTrackingPacketByteBuf buf = new JsonTrackingPacketByteBuf(Unpooled.buffer());
        String name = YarnMapping.getInstance().unmapClassName(YarnMapping.Namespace.NAMED, packet.getClass().getName());
        try {
            Field f = packet.getClass().getDeclaredField("CODEC");
            f.setAccessible(true);
            PacketCodec<Object, Object> codec = (PacketCodec<Object, Object>) f.get(packet);
            codec.encode(buf, packet);
            return BaseCommand.jsonOf(
                    "packet type", name,
                    "data", buf.getJsonArray()
            );
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("UNKNOWN CODEC: " + name);
        } catch (Exception e) {
            System.err.println(name);
            e.printStackTrace();
        }
        return null;
    }
}
