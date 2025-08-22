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

package me.psychedelicpalimpsest.reflection;

import java.util.Queue;
import java.util.concurrent.*;

public class SerializationTester {
	final Thread thread;
	final Queue<Object> queue = new ConcurrentLinkedQueue<>();

	private Runnable runner = () -> {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		while (true) {
			final Object o = queue.poll();
			if (o == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) { return; }

				continue;
			}

			Future<?> fut = executor.submit(() -> {
				try {
					System.out.println(McReflector.serializeObject(o).toString());
				} catch (Exception e) { e.printStackTrace(); }
			});

			try {
				fut.get(3, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				System.err.println("Evil doer at " + o.getClass().getName());
				e.printStackTrace();
				fut.cancel(true);
				break;
			}
		}
	};

	public SerializationTester() {
		thread = new Thread(runner);
		thread.start();
	}

	public void enqueue(Object o) { queue.add(o); }
}
