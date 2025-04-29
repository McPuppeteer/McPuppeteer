package me.psychedelicpalimpsest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@PuppeteerCommand(cmd="player info", description = "Reports a bunch of info about the player (position, health, etc)")
public class GetPlayerInfo implements BaseCommand {

    @Override
    public void onRequest(JsonNode request, LaterCallback callback) {

    }
}
