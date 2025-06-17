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

 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import me.psychedelicpalimpsest.BaseCommand;
 import me.psychedelicpalimpsest.PuppeteerCommand;
 import net.minecraft.client.MinecraftClient;
 import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.registry.RegistryKeys;
 import net.minecraft.registry.entry.RegistryEntry;
 import net.minecraft.screen.EnchantmentScreenHandler;

 import java.util.Optional;

 @PuppeteerCommand(
         cmd = "get enchantments", description = "Gets the enchantment options available",
         cmd_context = BaseCommand.CommandContext.PLAY
 )
 public class GetEnchants implements BaseCommand {
     @Override
     public void onRequest(JsonObject request, LaterCallback callback) {

         if (!(MinecraftClient.getInstance().player.currentScreenHandler instanceof EnchantmentScreenHandler enchantmentScreen)){
             callback.resultCallback(BaseCommand.jsonOf(
                     "status", "error",
                     "type", "unexpected screen",
                     "message", "No beacon screen is open"
             ));
             return;
         }
         var reg = MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

         JsonArray arr = new JsonArray();
         for (int i = 0; i < 3; i++){
             int id = enchantmentScreen.enchantmentId[i];
             int level = enchantmentScreen.enchantmentLevel[i];
             int cost = enchantmentScreen.enchantmentPower[i];
             Optional<RegistryEntry.Reference<Enchantment>> entry =  reg.getEntry(id);

             if (entry.isEmpty()) continue;
             arr.add(BaseCommand.jsonOf(
                "id", entry.get().getIdAsString(),
                     "level", level,
                     "cost", cost
             ));
         }

         callback.resultCallback(BaseCommand.jsonOf(
            "enchantments", arr
         ));
     }
 }
