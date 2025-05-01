/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.modInfo;


import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.Optional;

import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;

@PuppeteerCommand(
        cmd = "client info",
        description = "Returns information about the client itself"
)
public class GetClientInfo implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (mod.isEmpty()) {
            throw new RuntimeException("Could not find ModContainer for " + MOD_ID);
        }
        ModContainer modc = mod.get();


        callback.callback(Map.of(
                "puppeteer commands", GetCommandsList.getCommands(),
                "puppeteer info", Map.of(
                        "name", modc.getMetadata().getName(),
                        "mod id", MOD_ID,
                        "version", modc.getMetadata().getVersion().getFriendlyString(),
                        "description", modc.getMetadata().getDescription()
                ),
                "minecraft version", SharedConstants.getGameVersion().getName(),
                "minecraft protocol version", SharedConstants.getProtocolVersion(),

                "username", MinecraftClient.getInstance().getSession().getUsername(),
                "account type", MinecraftClient.getInstance().getSession().getAccountType().getName(),


                "uuid", MinecraftClient.getInstance().getSession().getXuid().orElse("UNKNOWN")
        ));


    }
}
