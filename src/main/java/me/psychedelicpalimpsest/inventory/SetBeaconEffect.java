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

package me.psychedelicpalimpsest.inventory;

 import com.google.gson.JsonObject;
 import me.psychedelicpalimpsest.BaseCommand;
 import me.psychedelicpalimpsest.PuppeteerCommand;
 import net.minecraft.block.BeaconBlock;
 import net.minecraft.client.MinecraftClient;
 import net.minecraft.client.gui.screen.ingame.BeaconScreen;
 import net.minecraft.entity.effect.StatusEffect;
 import net.minecraft.network.packet.PlayPackets;
 import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
 import net.minecraft.registry.Registries;
 import net.minecraft.registry.RegistryKey;
 import net.minecraft.registry.entry.RegistryEntry;
 import net.minecraft.screen.BeaconScreenHandler;
 import net.minecraft.util.Identifier;

 import java.util.Optional;

 @PuppeteerCommand(
         cmd = "set beacon effect", description = "Sets the effect of the beacon",
         cmd_context = BaseCommand.CommandContext.PLAY
 )
public class SetBeaconEffect  implements BaseCommand {
     @Override
     public void onRequest(JsonObject request, LaterCallback callback) {
        MinecraftClient.getInstance().execute(()->{
            if (!(MinecraftClient.getInstance().currentScreen instanceof BeaconScreen beacon)){
                callback.resultCallback(BaseCommand.jsonOf(
                        "status", "error",
                        "type", "unexpected screen",
                        "message", "No beacon screen is open"
                ));
                return;
            }
            if (! ((BeaconScreenHandler)MinecraftClient.getInstance().player.currentScreenHandler).hasPayment() ){
                callback.resultCallback(BaseCommand.jsonOf(
                        "status", "error",
                        "type", "beacon payment required",
                        "message", "Please pay your beacon toll."
                ));
                return;
            }

            MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                    new UpdateBeaconC2SPacket(
                            request.has("primary") ? parseEffect(request.get("primary").getAsString()) : Optional.empty(),
                            request.has("secondary") ? parseEffect(request.get("secondary").getAsString()) : Optional.empty()
                    )
            );
            MinecraftClient.getInstance().player.closeHandledScreen();

        });
     }
     public static Optional<RegistryEntry<StatusEffect>> parseEffect(String effect) {
         return Registries.STATUS_EFFECT.getEntry(Identifier.ofVanilla(effect)).map((ref) -> ref);
     }

 }
