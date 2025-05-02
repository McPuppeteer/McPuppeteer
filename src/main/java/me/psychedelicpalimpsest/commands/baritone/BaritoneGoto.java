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
package me.psychedelicpalimpsest.commands.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.process.ICustomGoalProcess;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaritoneListener;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.PuppeteerCommand;
import net.minecraft.util.math.BlockPos;

@PuppeteerCommand(
        cmd="baritone goto", mod_requirements = {"baritone"},
        description = "Tell baritone to go somewhere"
)
public class BaritoneGoto implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {

        if (!request.has("x") || !request.get("x").isJsonPrimitive()
              || !request.has("y") || !request.get("y").isJsonPrimitive()
              || !request.has("z") || !request.get("z").isJsonPrimitive()){
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "message", "Must have three integer arguments, x, y, z"
            ));
            return;
        }
        int x = request.get("x").getAsInt();
        int y = request.get("y").getAsInt();
        int z = request.get("z").getAsInt();

        ICustomGoalProcess progress = BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess();


        boolean noCancel = request.has("no cancel") && request.get("no cancel").getAsBoolean();

        if (!noCancel)
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();



        BaritoneListener.getOrCreateInstance().addBaritoneEventCallback(callback);

        Goal goal = new GoalGetToBlock(new BlockPos(x, y, z));
        progress.setGoalAndPath(goal);




    }
}
