package me.psychedelicpalimpsest.commands.modInfo;


import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.Freecam;

@PuppeteerCommand(
        cmd = "is freecam",
        description = "Gets the state of freecam"
)
public class IsFreecam implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.resultCallback(BaseCommand.jsonOf(
                "is freecam", Freecam.isFreecamActive()
        ));
    }
}
