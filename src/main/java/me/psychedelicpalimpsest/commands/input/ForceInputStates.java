package me.psychedelicpalimpsest.commands.input;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.modules.PuppeteerInput;

@PuppeteerCommand(
        cmd = "force inputs",
        description = "Takes an 'inputs' parameter identical to 'get forced input's parameter. Also takes an array of strings 'remove' which will remove forced values"
)
public class ForceInputStates implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (request.has("inputs") && request.get("inputs").isJsonObject()) {
            JsonObject job = request.getAsJsonObject("inputs");

            job.asMap().forEach((key, value) -> {
                PuppeteerInput.isForcePressed.put(key, value.getAsBoolean());
            });


        }



        if (request.has("remove") && request.isJsonArray()) {
            request.getAsJsonArray().forEach((obj)->{
                String remove = obj.getAsString();
                if (!PuppeteerInput.validOptions.contains(remove)){
                    callback.resultCallback(BaseCommand.jsonOf(
                            "state", "error",
                            "type", "expected argument",
                            "message", "Item '" + remove + "' is not a valid option"
                    ));
                }



                PuppeteerInput.isForcePressed.remove(remove);
            });




        }
    }
}
