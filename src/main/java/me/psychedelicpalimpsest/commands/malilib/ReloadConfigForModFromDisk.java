package me.psychedelicpalimpsest.commands.malilib;

import com.fasterxml.jackson.databind.JsonNode;
import fi.dy.masa.malilib.config.IConfigHandler;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.Map;

@PuppeteerCommand(
        cmd = "reload config",
        description = "Force a Malilib mod to reload a config file from disk (Warning: Does not call callbacks associated with settings changes)"
)
public class ReloadConfigForModFromDisk implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        Map<String, IConfigHandler> configs = ListConfigs.getConfigHandlers();
        if (request.get("mod id") == null || !request.get("mod id").isTextual()) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Missing parameter 'mod id'"
            ));
            return;
        }
        String modId = request.get("mod id").asText();

        if (!configs.containsKey(modId)) {
            callback.callback(Map.of(
                    "status", "error",
                    "message", "Unknown mod id"
            ));
            return;
        }


        configs.get(modId).load();
    }
}
