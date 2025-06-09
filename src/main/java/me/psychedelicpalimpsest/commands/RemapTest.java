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

package me.psychedelicpalimpsest.commands;

 import com.google.gson.JsonObject;
 import me.psychedelicpalimpsest.BaseCommand;
 import me.psychedelicpalimpsest.PuppeteerCommand;
 import net.fabricmc.loader.api.FabricLoader;
 import net.fabricmc.loader.api.MappingResolver;
 import net.minecraft.client.MinecraftClient;

 @PuppeteerCommand(
         cmd="remap", description = ""
 )
 public class RemapTest implements BaseCommand {
     @Override
     public void onRequest(JsonObject request, LaterCallback callback) {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getClass().getCanonicalName();
        System.out.println(mc.getClass().getCanonicalName());
     }
 }
