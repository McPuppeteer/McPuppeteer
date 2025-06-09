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

package me.psychedelicpalimpsest.mixin;

 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.ByteBufAllocator;
 import io.netty.buffer.Unpooled;
 import me.psychedelicpalimpsest.McPuppeteer;
 import net.minecraft.client.MinecraftClient;
 import net.minecraft.network.ClientConnection;
 import net.minecraft.network.RegistryByteBuf;
 import net.minecraft.network.codec.PacketCodec;
 import net.minecraft.network.codec.PacketCodecs;
 import net.minecraft.network.listener.PacketListener;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
 import net.minecraft.registry.Registries;
 import org.spongepowered.asm.mixin.Mixin;
 import org.spongepowered.asm.mixin.injection.At;
 import org.spongepowered.asm.mixin.injection.Inject;
 import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

 import java.lang.reflect.Field;


 @Mixin(ClientConnection.class)
public class ClientConnectionMixin {
   @Inject(method="handlePacket", at=@At("HEAD"))
   private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {

   /* TODO: REFLECTION */
//        String id = packet.getPacketId().toString();
//
//        if (packet instanceof ChunkDataS2CPacket) return;
//        try {
//            Field f = packet.getClass().getDeclaredField("CODEC");
//            f.setAccessible(true);
//
//            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
//            RegistryByteBuf regBuf = new RegistryByteBuf(buf, MinecraftClient.getInstance().getNetworkHandler().getRegistryManager());
//
//            PacketCodec<RegistryByteBuf, Packet<?>> codec = (PacketCodec<RegistryByteBuf, Packet<?>>) f.get(packet);
//            regBuf.encodeAsJson(codec, packet);
//
//
////            McPuppeteer.serializationTester.enqueue(packet);
//        } catch (StackOverflowError e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException | NoSuchFieldException e) {
//            /* Ignored */
        }
}
