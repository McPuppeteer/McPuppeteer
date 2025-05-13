/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.modInfo;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.*;


@PuppeteerCommand(
        cmd = "list commands",
        description = "Tells you about all the commands supported in this version of Puppeteer"
)
public class GetCommandsList implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        callback.resultCallback(BaseCommand.jsonOf(
                "commands", getCommands()
        ));
    }

    public static List<Object> getCommands() {
        List<Object> ret = new ArrayList<>(COMMANDS.size());
        COMMAND_DESC_MAP.forEach((commandName, commandDesc) -> {
            ret.add(BaseCommand.jsonOf(
                    "cmd", commandName,
                    "desc", commandDesc,
                    "requirements", Arrays.stream(COMMAND_REQUIREMENTS_MAP.get(commandName)).toList()
            ));
        });
        return ret;
    }
}
