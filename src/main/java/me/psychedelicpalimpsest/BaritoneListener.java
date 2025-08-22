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

package me.psychedelicpalimpsest;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;

import static me.psychedelicpalimpsest.PuppeteerTask.TaskType.BARITONE;

public class BaritoneListener implements AbstractGameEventListener {
	public static void baritoneInit() {
		/* This needs to be separate or things explode without baritone */
		BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(getOrCreateInstance());
	}

	public static void panic() {
		BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
	}

	private static BaritoneListener instance;

	public static BaritoneListener getOrCreateInstance() {
		if (instance == null) instance = new BaritoneListener();
		return instance;
	}

	private boolean hasStartedSinceCancel = false;

	@Override
	public void onPathEvent(PathEvent pathEvent) {
		/* Seems something somebody might want */
		PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.BARITONE, () -> BaseCommand.jsonOf("type", "baritone event", "path state", pathEvent.name()));
		if (PathEvent.CALC_FINISHED_NOW_EXECUTING == pathEvent)
			hasStartedSinceCancel = true;

		if (McPuppeteer.tasks.isEmpty()) return;

		PuppeteerTask task = McPuppeteer.tasks.peek();

		if (PathEvent.CANCELED == pathEvent && hasStartedSinceCancel) {
			hasStartedSinceCancel = false;

			if (task.getType() == BARITONE)
				task.onBaritoneCancel();
		}
		if (PathEvent.CALC_FAILED == pathEvent) {
			if (task.getType() == BARITONE)
				task.onBaritoneCalculationFailure();
		}
	}
}
