package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommandRegistry;
import me.psychedelicpalimpsest.PuppeteerSocketServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;



@Mixin(MinecraftClient.class)
public class ExampleMixin {
	@Shadow @Final private Window window;


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


	@Inject(at = @At("RETURN"), method="getWindowTitle", cancellable = true)
	private void onGetTitle(CallbackInfoReturnable<String> cir){
		String port_s = PuppeteerSocketServer.getInstance() == null ? "unknown" : "" + PuppeteerSocketServer.getInstancePort();

		cir.setReturnValue(cir.getReturnValue() + " - [" +  port_s + ", "+ PuppeteerCommandRegistry.COMMANDS.size() +"]");
	}
	@Inject(at = @At("HEAD"), method="close")
	private void onClose(CallbackInfo ci) {
		PuppeteerSocketServer.killServer();
	}
}