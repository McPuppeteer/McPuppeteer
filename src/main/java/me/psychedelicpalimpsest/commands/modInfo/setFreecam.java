package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.Freecam;

@PuppeteerCommand(
        cmd="set freecam",
        description = "Enable/disable freecam"
)
public class setFreecam implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (!request.has("enabled") || !request.get("enabled").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "message", "Must have 'enabled' as a boolean property",
                    "type", "expected argument"
            ));
            return;
        }
        if (request.get("enabled").getAsBoolean() && !Freecam.isFreecamActive()) {
            Freecam.toggleFreecam(null, null);
        }
        if (!request.get("enabled").getAsBoolean() && Freecam.isFreecamActive()) {
            Freecam.toggleFreecam(null, null);
        }

        callback.resultCallback(BaseCommand.jsonOf());
    }
}
