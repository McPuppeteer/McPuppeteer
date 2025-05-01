package me.psychedelicpalimpsest.commands.modInfo;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.*;


@PuppeteerCommand(
        cmd = "list commands",
        description = "Tells you about all the commands supported in this version of Puppeteer"
)
public class GetCommandsList implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        callback.callback(Map.of(
                "commands", getCommands()
        ));
    }

    public static List<Object> getCommands(){
        List<Object> ret = new ArrayList<>(COMMANDS.size());
        COMMAND_DESC_MAP.forEach((commandName, commandDesc) -> {
            ret.add(Map.of(
               "cmd", commandName,
               "desc", commandDesc,
                "requirements", Arrays.stream(COMMAND_REQUIREMENTS_MAP.get(commandName)).toList()
            ));
        });
        return ret;
    }
}
