package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerSocketServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

import static me.psychedelicpalimpsest.PuppeteerSocketServer.broadcastState;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		/* Yes, this makes init slower, but i do not care */
        try {
            PuppeteerSocketServer.createServer();
        } catch (IOException e) {
			McPuppeteer.LOGGER.error("Failed to create server", e);
        }
		FabricLoader.getInstance().getAllMods().forEach((modContainer) -> {
			if (modContainer.toString().contains("baritone")) {
				McPuppeteer.hasBaritoneInstalled = true;
			}
		});
    }

	@Inject(at = @At("TAIL"), method = "tick")
	private void tick(CallbackInfo info) {
		long time = System.currentTimeMillis();
		if (time - McPuppeteer.lastBroadcast > 3_000) {
			McPuppeteer.lastBroadcast = time;
			new Thread(()->{
				try {
					broadcastState();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}

	}


	@Inject(at = @At("RETURN"), method="getWindowTitle", cancellable = true)
	private void onGetTitle(CallbackInfoReturnable<String> cir){
		String port_s = PuppeteerSocketServer.getInstance() == null ? "unknown" : "" + PuppeteerSocketServer.getInstancePort();

		cir.setReturnValue(cir.getReturnValue() + " - [" +  port_s + "]");
	}
	@Inject(at = @At("HEAD"), method="close")
	private void onClose(CallbackInfo ci) {
		PuppeteerSocketServer.killServer();
	}
}