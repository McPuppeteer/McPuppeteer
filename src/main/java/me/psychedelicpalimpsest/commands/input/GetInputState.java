package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;


@PuppeteerCommand(
        cmd = "get forced input",
        description = "Reports the state of if certain input methods are forced. An key not being present indicates that no input is being forced. If a key is set to false, it is being forced up. And if a key is set to true, it is forced down."
)
public class GetInputState implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.resultCallback(BaseCommand.jsonOf(
                "inputs", PuppeteerInput.isForcePressed
        ));

    }
}
