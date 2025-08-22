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

package me.psychedelicpalimpsest.mixin;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.PuppeteerServer;
import me.psychedelicpalimpsest.reflection.McReflector;
import me.psychedelicpalimpsest.reflection.PacketJsonEncoder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.psychedelicpalimpsest.CallbackManager.PacketCallbackMode.*;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        String id = packet.getPacketType().toString();


        PuppeteerServer.broadcastPacket(id, (type, attachment) -> {
            if (type == NOTIFY_NEXT || type == NETWORK_SERIALIZED_NEXT || type == OBJECT_SERIALIZED_NEXT) {
                attachment.packetCallbacks.remove(id);
            }
            System.out.println(id + " with " + type.name());

            switch (type){
                case NOTIFY_NEXT:
                case NOTIFY_ONLY:
                    return new JsonObject();
                case NETWORK_SERIALIZED:
                case NETWORK_SERIALIZED_NEXT:
                    return PacketJsonEncoder.encode(packet);
                case OBJECT_SERIALIZED:
                case OBJECT_SERIALIZED_NEXT:
                    /* Note: This is safe because we KNOW that packets will return an object */
                    return (JsonObject) McReflector.serializeObject(packet);


                default:
                    throw new AssertionError("Unreachable " + type.name());
            }
        });


    }
}
