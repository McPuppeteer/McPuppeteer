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


package me.psychedelicpalimpsest.utils;

import me.psychedelicpalimpsest.PuppeteerTask;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EventBasedTask extends PuppeteerTask implements PuppeteerTask.TaskEvent {

    private final Queue<PuppeteerTask.TaskEvent> taskEvents;

    public EventBasedTask(List<PuppeteerTask.TaskEvent> tasks) {
        super(
                TaskType.TICKLY,
                null,
                null,
                null,
                null
        );
        this.onTick = this;

        taskEvents = new LinkedList<>(tasks);
    }


    @Override
    public void invoke(PuppeteerTask self, @Nullable PuppeteerTask.ThreadStyleCompletion onCompletion) {
        PuppeteerTask.TaskEvent event = taskEvents.poll();

        if (event != null)
            event.invoke(self, onCompletion);

        if (taskEvents.isEmpty())
            onCompletion.invoke();
    }
}
