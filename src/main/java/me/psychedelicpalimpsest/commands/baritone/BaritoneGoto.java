/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.commands.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.process.ICustomGoalProcess;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.util.math.BlockPos;

import static me.psychedelicpalimpsest.PuppeteerTask.baritoneTask;


@PuppeteerCommand(
        cmd = "baritone goto", mod_requirements = {"baritone"},
        description = "Tell baritone to go somewhere",
        cmd_context = BaseCommand.CommandContext.PLAY
)
public class BaritoneGoto implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        if (!request.has("x") || !request.get("x").isJsonPrimitive()
                || !request.has("y") || !request.get("y").isJsonPrimitive()
                || !request.has("z") || !request.get("z").isJsonPrimitive()) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "type", "expected argument",
                    "message", "Must have three integer arguments, x, y, z"
            ));
            return;
        }
        final int x = request.get("x").getAsInt();
        final int y = request.get("y").getAsInt();
        final int z = request.get("z").getAsInt();

        final boolean noCancel = request.has("no cancel") && request.get("no cancel").getAsBoolean();

        if (!noCancel)
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();


        McPuppeteer.tasks.add(baritoneTask((task, ignored) -> {
            ICustomGoalProcess progress = BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess();


            Goal goal = new GoalGetToBlock(new BlockPos(x, y, z));
            progress.setGoalAndPath(goal);
        }, (task, ignored) -> callback.resultCallback(BaseCommand.jsonOf(
                "message", "baritone completed the operation"
        )), (t, e) -> callback.resultCallback(e)));
    }
}
