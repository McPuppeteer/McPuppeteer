/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest.commands.baritone;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;

@PuppeteerCommand(
    cmd = "test baritone",
    mod_requirements = {"baritone"},
    description =
        "A quick and simple way to test if baritone is installed. Gives an error if not installed")
public class TestBaritone implements BaseCommand {
  @Override
  public void onRequest(JsonObject request, LaterCallback callback) {
    callback.resultCallback(new JsonObject());
  }
}
