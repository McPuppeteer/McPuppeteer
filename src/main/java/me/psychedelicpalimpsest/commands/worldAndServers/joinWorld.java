package me.psychedelicpalimpsest.commands.worldAndServers;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

import java.util.Map;

@PuppeteerCommand(
        cmd="join world",
        description = "Joins a local world. This requires one parameter, 'load name', which needs to be the EXACT same as from 'load name' from 'get worlds'"
)
public class joinWorld implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {

        if (request.get("load name") == null || !request.get("load name").isTextual()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'load name' in joinWorld"
            ));
            return;
        }
        MinecraftClient.getInstance().execute(() -> {
            Thread listenThread = new Thread(() -> {
                try {
                    while (true) {
                        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                            callback.callback(Map.of("message", "in game"));
                            return;
                        }

                        Thread.sleep(100);
                        Thread.yield();
                    }
                } catch (InterruptedException e) {}
            });
            listenThread.start();

            MinecraftClient.getInstance().createIntegratedServerLoader().start(request.get("load name").asText(), () -> {
                callback.callback(Map.of(
                        "status", "error",
                        "message", "Unknown world join error, are you sure you sent the 'load name' parameter directly from the 'load name' value from 'get worlds'?"
                ));
                listenThread.interrupt();
                MinecraftClient.getInstance().setScreen(new TitleScreen());
            });
        });
    }

}
