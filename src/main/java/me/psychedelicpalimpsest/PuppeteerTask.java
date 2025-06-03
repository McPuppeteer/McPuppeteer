 /**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package me.psychedelicpalimpsest;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single task managed by the Puppeteer system.
 */
public class PuppeteerTask {

    /**
     * The type of task.
     */
    public enum TaskType {
        /** Runs in a dedicated thread. */
        THREAD,
        /** Called each tick, ends via callback. */
        TICKLY,
        /** Runs in Baritone; see BaritoneListener for completion. */
        BARITONE
    }

    /**
     * The current state of a task.
     */
    public enum TaskState {
        NOT_STARTED,
        RUNNING,
        ENDED
    }

    private final TaskType type;
    protected TaskEvent onStart;
    protected TaskEvent onBaritoneFinish;
    protected TaskEvent onTick;
    protected TaskFailureEvent onFailure;

    private volatile Thread thread = null;
    private volatile TaskState state = TaskState.NOT_STARTED;

    /**
     * Callback for task events.
     */
    public interface TaskEvent {
        /**
         * Invoked for a task event.
         * @param self The current task
         * @param onCompletion Only used for TICKLY tasks; call to end the task
         */
        void invoke(PuppeteerTask self, @Nullable ThreadStyleCompletion onCompletion);
    }

    /**
     * Callback for task failure events.
     */
    public interface TaskFailureEvent {
        void invoke(PuppeteerTask self, JsonObject info);
    }

    /**
     * Callback to signal thread-style task completion.
     */
    public interface ThreadStyleCompletion {
        void invoke();
    }

    protected PuppeteerTask(
            TaskType type,
            TaskEvent onStart,
            @Nullable TaskEvent onBaritoneFinish,
            @Nullable TaskFailureEvent onFailure,
            @Nullable TaskEvent onTick
    ) {
        this.type = type;
        this.onStart = onStart;
        this.onBaritoneFinish = onBaritoneFinish;
        this.onFailure = onFailure;
        this.onTick = onTick;
    }

    public TaskType getType() {
        return type;
    }

    /**
     * Creates a Baritone task.
     */
    public static PuppeteerTask baritoneTask(
            TaskEvent onStart,
            TaskEvent onFinish,
            TaskFailureEvent onFailure
    ) {
        return new PuppeteerTask(
                TaskType.BARITONE,
                onStart,
                onFinish,
                onFailure,
                null
        );
    }

    /**
     * Creates a Tickly task.
     */
    public static PuppeteerTask ticklyTask(
            TaskEvent onStart,
            TaskEvent onTick
    ) {
        return new PuppeteerTask(
                TaskType.TICKLY,
                onStart,
                null,
                null,
                onTick
        );
    }

    public TaskState getState() {
        return state;
    }

    /**
     * Starts the task.
     * @throws IllegalStateException if already started or ended
     */
    public void start() {
        if (state != TaskState.NOT_STARTED) {
            throw new IllegalStateException("Task already started or ended");
        }
        state = TaskState.RUNNING;

        if (type == TaskType.THREAD) {
            thread = new Thread(() -> {
                try {
                    onStart.invoke(this, this::end);
                } catch (Exception e) {
                    handleFailure(e);
                } finally {
                    end();
                }
            });
            thread.start();
        } else {
            try {
                if (onStart != null)
                    onStart.invoke(this, this::end);
            } catch (Exception e) {
                handleFailure(e);
                end();
            }
        }
    }

    private void handleFailure(Exception e) {
        if (onFailure != null) {
            onFailure.invoke(this, BaseCommand.jsonOf(
                    "status", "error",
                    "type", "exception",
                    "message", "Exception thrown during task",
                    "exception", e.toString()
            ));
        }
        McPuppeteer.LOGGER.error("Exception in PuppeteerTask", e);
    }

    private void end() {
        state = TaskState.ENDED;
        McPuppeteer.LOGGER.info("Task ended: {}", this);
    }

    /**
     * Cancels a Baritone task.
     */
    public void onBaritoneCancel() {
        assert this.getType() == TaskType.BARITONE;
        assert this.onBaritoneFinish != null;

        this.state = TaskState.ENDED;
        this.onBaritoneFinish.invoke(this, null);
    }

    /**
     * Handles Baritone calculation failure.
     */
    public void onBaritoneCalculationFailure() {
        assert this.getType() == TaskType.BARITONE;

        this.state = TaskState.ENDED;

        if (this.onFailure != null) {
            this.onFailure.invoke(this, BaseCommand.jsonOf(
                    "status", "error",
                    "type", "baritone calculation",
                    "message", "Baritone could not path correctly"
            ));
        }
    }

    /**
     * Kills the task if running in a thread.
     */
    public void kill() {
        if (thread != null && thread.isAlive())
            thread.interrupt();
        this.state = TaskState.ENDED;
    }

    /**
     * Ticks a Tickly task.
     */
    public void tick() {
        assert this.getType() == TaskType.TICKLY;
        assert this.onTick != null;

        this.onTick.invoke(this, () -> this.state = TaskState.ENDED);
    }
}
