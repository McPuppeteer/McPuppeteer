package me.psychedelicpalimpsest.commands.modInfo;


import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.Freecam;
import me.psychedelicpalimpsest.modules.Freerot;

@PuppeteerCommand(
        cmd = "is freerot",
        description = "Gets the state of freerot"
)
public class IsFreerot implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.resultCallback(BaseCommand.jsonOf(
                "is freerot", Freerot.isFreerotActive()
        ));
    }
}
