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

package me.psychedelicpalimpsest.mixin;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;
import static me.psychedelicpalimpsest.PuppeteerServer.broadcastState;
import static me.psychedelicpalimpsest.PuppeteerTask.TaskType.TICKLY;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerConfig;
import me.psychedelicpalimpsest.PuppeteerServer;
import me.psychedelicpalimpsest.modules.HeadlessMode;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.thread.ThreadExecutor;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow private int itemUseCooldown;

	@Shadow @Nullable public ClientPlayerEntity player;

	@Shadow public abstract void scheduleStop();

	@Shadow @Final private Queue<Runnable> renderTaskQueue;

	@Shadow private @Nullable CompletableFuture<Void> resourceReloadFuture;

	@Shadow private @Nullable Overlay overlay;

	@Shadow public abstract CompletableFuture<Void> reloadResources();

	@Shadow @Final private RenderTickCounter.Dynamic renderTickCounter;

	@Shadow public abstract void tick();

	@Shadow public abstract Window getWindow();

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		McPuppeteer.init();
	}

	/*
	    Send UDP broadcats, run tickly tasks, and enforce forced player input when screens are open.
	*/
	@Inject(at = @At("TAIL"), method = "tick")
	private void tick(CallbackInfo info) {
		if (PuppeteerConfig.SEND_BROADCASTS.getBooleanValue()) {
			long interval_millis = (long) (PuppeteerConfig.UDP_BROADCAST_INTERVAL.getFloatValue() * 1000f);

			long time = System.currentTimeMillis();
			if (time - McPuppeteer.lastBroadcast > interval_millis) {
				McPuppeteer.lastBroadcast = time;
				new Thread(() -> {
					try {
						broadcastState();
					} catch (IOException e) { LOGGER.error("Error trying to broadcast state", e); }
				}).start();
			}
		}

		var queueIt =  McPuppeteer.tasks.iterator();
		boolean keepGoing = true;
		while (queueIt.hasNext() && keepGoing) {
			var task = queueIt.next();
			switch (task.getState()) {
				case NOT_STARTED:
					task.start();
					keepGoing = task.isTransparent();
					break;
				case ENDED: queueIt.remove(); break;
				case RUNNING:
					if (task.getType() == TICKLY) { task.tick(); }

					keepGoing = task.isTransparent();
					break;
			}
		}

		/* Minecraft skips this when a screen is open */
		if (MinecraftClient.getInstance().currentScreen != null &&
		    PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.ATTACK)) {
			MinecraftClient.getInstance().handleBlockBreaking(false /* Ignored param */);
		}

		if (PuppeteerInput.isForcePressed.getOrDefault(PuppeteerInput.USE, false) &&
		    this.itemUseCooldown == 0 && !this.player.isUsingItem())
			MinecraftClient.getInstance().doItemUse();
	}

	@Inject(at = @At("RETURN"), method = "getWindowTitle", cancellable = true)
	private void onGetTitle(CallbackInfoReturnable<String> cir) {
		String port_s =
		    PuppeteerServer.getInstance() == null ? "unknown" : "" + PuppeteerServer.getInstance().getPort();

		cir.setReturnValue(cir.getReturnValue() + " - [" + port_s + "]");
	}

	@Inject(at = @At("HEAD"), method = "close")
	private void onClose(CallbackInfo ci) {
		PuppeteerServer.killServer();
		if (!McPuppeteer.tasks.isEmpty()) McPuppeteer.tasks.peek().kill();
	}

	/* Enforce forced attack state */
	@ModifyVariable(at = @At("HEAD"), method = "handleBlockBreaking", ordinal = 0, argsOnly = true)
	boolean handleBlockBreaking(boolean breaking) {
		if (!PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.ATTACK)) return breaking;

		MinecraftClient.getInstance().attackCooldown = 0;
		return PuppeteerInput.isForcePressed.get(PuppeteerInput.ATTACK);
	}

	/* Enforce forced use state */
	@Inject(at = @At("HEAD"), method = "doItemUse", cancellable = true)
	void doItemUse(CallbackInfo ci) {
		if (!PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.USE)) return;

		if (!PuppeteerInput.isForcePressed.get(PuppeteerInput.USE)) ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "onResolutionChanged", cancellable = true)
	void onResolutionChanged(CallbackInfo ci) {
		if (HeadlessMode.isHeadless()) ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	void onRender(boolean tick, CallbackInfo ci) {
		if (!HeadlessMode.isHeadless()) return;
		ci.cancel();

		/* This is all the stuff that 'looked' necessary */

		if (!HeadlessMode.isHeadless() && this.getWindow().shouldClose()) { this.scheduleStop(); }
		if (this.resourceReloadFuture != null && !(this.overlay instanceof SplashOverlay)) {
			CompletableFuture<Void> completableFuture = this.resourceReloadFuture;
			this.resourceReloadFuture = null;
			this.reloadResources().thenRun(() -> completableFuture.complete(null));
		}

		Runnable runnable;
		while ((runnable = this.renderTaskQueue.poll()) != null) { runnable.run(); }

		int i = this.renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), tick);
		Profiler profiler = Profilers.get();
		if (tick) {
			profiler.push("scheduledExecutables");

			((ThreadExecutor<Runnable>) (Object) this).runTask();
			profiler.pop();
			profiler.push("tick");

			for (int j = 0; j < Math.min(10, i); j++) {
				profiler.visit("clientTick");
				this.tick();
			}

			profiler.pop();
		}
	}
}
