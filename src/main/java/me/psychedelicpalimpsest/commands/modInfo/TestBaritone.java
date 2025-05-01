package me.psychedelicpalimpsest.commands.modInfo;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.Map;

@PuppeteerCommand(
        cmd="test baritone", mod_requirements = {"baritone"},
        description = "A quick and simple way to test if baritone is installed. Gives an error if not installed")
public class TestBaritone implements BaseCommand {
    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {
        callback.callback(Map.of());
    }
}
