package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;


@PuppeteerCommand(
        cmd = "clear force input",
        description = "No longer forces an input state"
)
public class ClearForcedInputs implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        PuppeteerInput.isForcePressed.clear();
        callback.resultCallback(new JsonObject());
    }
}
