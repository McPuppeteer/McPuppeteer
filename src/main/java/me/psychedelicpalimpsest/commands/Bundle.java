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


package me.psychedelicpalimpsest.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;


@PuppeteerCommand(
        cmd = "Bundle",
        description = "Bundle many packets together"
)
public class Bundle implements BaseCommand {





    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        switch (request.get("method").getAsString()){
            case "instant":
                for (JsonElement packet: request.getAsJsonArray("packets")) callback.simulatePuppeteerCommand(packet.getAsJsonObject(), null);


                break;




        }







      
      




  }
}
