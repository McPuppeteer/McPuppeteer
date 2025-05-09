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

package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerConfig;
import me.psychedelicpalimpsest.PuppeteerServer;
import me.psychedelicpalimpsest.PuppeteerTask;
import me.psychedelicpalimpsest.modules.PuppeteerInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;
import static me.psychedelicpalimpsest.PuppeteerServer.broadcastState;
import static me.psychedelicpalimpsest.PuppeteerTask.TaskType.TICKLY;


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow private int itemUseCooldown;

	@Shadow @Nullable public ClientPlayerEntity player;

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		McPuppeteer.init();
	}

	@Inject(at = @At("TAIL"), method = "tick")
	private void tick(CallbackInfo info) {
		if (!PuppeteerConfig.SEND_BROADCASTS.getBooleanValue()) return;
		long interval_millis = (long)(PuppeteerConfig.UDP_BROADCAST_INTERVAL.getFloatValue() * 1000f);

		long time = System.currentTimeMillis();
		if (time - McPuppeteer.lastBroadcast > interval_millis) {
			McPuppeteer.lastBroadcast = time;
			new Thread(()->{
				try {
					broadcastState();
				} catch (IOException e) {
					LOGGER.error("Error trying to broadcast state", e);
				}
			}).start();
		}




		if (!McPuppeteer.tasks.isEmpty()){
			PuppeteerTask task = McPuppeteer.tasks.peek();
			switch (task.getState()){
				case NOT_STARTED:
					task.start();
					break;
				case ENDED:
					McPuppeteer.tasks.remove();
					break;
				case RUNNING:
					if (task.getType() == TICKLY)
						task.tick();
					break;
				default:
					break;
			}
		}

		/* Minecraft skips this when a screen is open */
		if (MinecraftClient.getInstance().currentScreen != null && PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.ATTACK)) {
			MinecraftClient.getInstance().handleBlockBreaking(false /* Ignored param */);
		}

		if (PuppeteerInput.isForcePressed.getOrDefault(PuppeteerInput.USE, false) && this.itemUseCooldown == 0 && !this.player.isUsingItem())
			MinecraftClient.getInstance().doItemUse();


	}


	@Inject(at = @At("RETURN"), method="getWindowTitle", cancellable = true)
	private void onGetTitle(CallbackInfoReturnable<String> cir){
		String port_s = PuppeteerServer.getInstance() == null ? "unknown" : "" + PuppeteerServer.getInstance().getPort();

		cir.setReturnValue(cir.getReturnValue() + " - [" +  port_s + "]");
	}
	@Inject(at = @At("HEAD"), method="close")
	private void onClose(CallbackInfo ci) {
		PuppeteerServer.killServer();
		if (!McPuppeteer.tasks.isEmpty())
			McPuppeteer.tasks.peek().kill();
	}



	@ModifyVariable(at = @At("HEAD"), method = "handleBlockBreaking", ordinal = 0, argsOnly = true)
	boolean handleBlockBreaking(boolean breaking){
		if  (!PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.ATTACK))
			return breaking;

		MinecraftClient.getInstance().attackCooldown = 0;
		return PuppeteerInput.isForcePressed.get(PuppeteerInput.ATTACK);
	}

	@Inject(at = @At("HEAD"), method = "doItemUse", cancellable = true)
	void doItemUse(CallbackInfo ci){
		if (!PuppeteerInput.isForcePressed.containsKey(PuppeteerInput.USE)) return;

		if (!PuppeteerInput.isForcePressed.get(PuppeteerInput.USE))
			ci.cancel();

	}


}