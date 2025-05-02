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

package me.psychedelicpalimpsest.commands.worldAndServers;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.mixin.HiddenServerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import java.util.ArrayList;
import java.util.List;

@PuppeteerCommand(
        cmd = "get server list",
        description = "Gets all the multiplayer servers in your server list, along with the \"hidden\" ones (your direct connect history). "
)
public class GetServers implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        new Thread(() -> {
            ServerList serverList = new ServerList(MinecraftClient.getInstance());
            serverList.loadFile();

            HiddenServerAccessor hiddenServerAccessor = (HiddenServerAccessor) serverList;
            List<ServerInfo> hiddenServers =  hiddenServerAccessor.getHiddenServers();


            List<JsonObject> jsonHiddenServers = new ArrayList<>(hiddenServers.size());
            for (ServerInfo info : hiddenServers) {
                jsonHiddenServers.add(BaseCommand.jsonOf(
                        "address", info.address,
                        "name", info.name
                ));
            }

            List<JsonObject> jsonServerList = new ArrayList<>(serverList.size());
            for (int i = 0; i < serverList.size(); i++){
                ServerInfo info = serverList.get(i);

                jsonServerList.add(BaseCommand.jsonOf(
                        "address", info.address,
                        "name", info.name
                ));
            }

            callback.resultCallback(BaseCommand.jsonOf(
                    "server list", jsonServerList,
                    "hidden servers", jsonHiddenServers
            ));
        }).start();
    }
}
