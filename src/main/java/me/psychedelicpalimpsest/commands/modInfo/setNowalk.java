package me.psychedelicpalimpsest.commands.modInfo;


import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.NoWalk;

@PuppeteerCommand(
        cmd="set nowalk",
        description = "Enable/disable nowalk"
)
public class setNowalk implements BaseCommand {
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
        if (request.get("enabled").getAsBoolean() != NoWalk.isActive){
            NoWalk.toggle(null, null);
        }
        callback.resultCallback(BaseCommand.jsonOf());
    }
}
