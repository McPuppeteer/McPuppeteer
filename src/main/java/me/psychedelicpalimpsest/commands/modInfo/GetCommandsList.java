/**
 *     Copyright (C) 2025 - PsychedelicPalimpsest
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
