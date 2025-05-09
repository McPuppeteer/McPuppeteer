package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.NoWalk;

@PuppeteerCommand(
        cmd = "is nowalk",
        description = "Gets the state of nowalk"
)
public class isNoWalk implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.resultCallback(BaseCommand.jsonOf(
                "is nowalk", NoWalk.isActive
        ));
    }
}
