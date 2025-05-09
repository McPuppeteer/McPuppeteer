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
