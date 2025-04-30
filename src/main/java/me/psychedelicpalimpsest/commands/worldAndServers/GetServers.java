package me.psychedelicpalimpsest.commands.worldAndServers;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.mixin.HiddenServerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@PuppeteerCommand(
        cmd = "get server list",
        description = "Gets all the multiplayer servers in your server list, along with the \"hidden\" ones (your direct connect history). "
)
public class GetServers implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        new Thread(() -> {
            ServerList serverList = new ServerList(MinecraftClient.getInstance());
            serverList.loadFile();

            HiddenServerAccessor hiddenServerAccessor = (HiddenServerAccessor) serverList;
            List<ServerInfo> hiddenServers =  hiddenServerAccessor.getHiddenServers();


            List<Map<String, Object>> jsonHiddenServers = new ArrayList<>(hiddenServers.size());
            for (ServerInfo info : hiddenServers) {
                jsonHiddenServers.add(Map.of(
                        "address", info.address,
                        "name", info.name
                ));
            }

            List<Map<String, Object>> jsonServerList = new ArrayList<>(serverList.size());
            for (int i = 0; i < serverList.size(); i++){
                ServerInfo info = serverList.get(i);

                jsonServerList.add(Map.of(
                        "address", info.address,
                        "name", info.name
                ));
            }

            callback.callback(Map.of(
                    "server list", jsonServerList,
                    "hidden servers", jsonHiddenServers
            ));
        }).start();
    }
}
