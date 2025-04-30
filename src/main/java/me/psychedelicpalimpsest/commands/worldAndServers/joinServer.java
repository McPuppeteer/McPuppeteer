package me.psychedelicpalimpsest.commands.worldAndServers;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.resource.language.I18n;

import java.util.Map;

@PuppeteerCommand(
        cmd = "join server",
        description = ""
)
public class joinServer implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        if (request.get("address") == null || !request.get("address").isTextual()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'address' in join server"
            ));
            return;
        }
        if (!ServerAddress.isValid(request.get("address").asText())) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Invalid server address"
            ));
            return;
        }


        MinecraftClient.getInstance().execute(() -> {
            String addr = request.get("address").asText();

            ServerInfo info = new ServerInfo(I18n.translate("selectServer.defaultName"), addr, ServerInfo.ServerType.OTHER);
            ConnectScreen.connect(
                    MinecraftClient.getInstance().currentScreen,
                    MinecraftClient.getInstance(),
                    ServerAddress.parse(addr),
                    info,
                    false,
                    null
            );

            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(100);

                        if (MinecraftClient.getInstance().currentScreen instanceof DisconnectedScreen){
                            callback.callback(Map.of(
                                    "status", "error",
                                    "message", "Disconnect during connect"
                            ));
                            return;
                        }
                        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                            callback.callback(Map.of("message", "in game"));
                            return;
                        }
                        if (!(MinecraftClient.getInstance().currentScreen instanceof ConnectScreen)) {
                            callback.callback(Map.of(
                                    "status", "error",
                                    "message", "Unexpected screen: "
                                            + (MinecraftClient.getInstance().currentScreen.getTitle() != null
                                            ? MinecraftClient.getInstance().currentScreen.getTitle()
                                            : MinecraftClient.getInstance().currentScreen.toString())
                            ));
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }).start();

        });


    }
}
