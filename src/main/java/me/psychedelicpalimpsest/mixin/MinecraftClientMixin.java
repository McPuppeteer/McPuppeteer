package me.psychedelicpalimpsest.mixin;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import me.psychedelicpalimpsest.GuiConfigs;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerConfig;
import me.psychedelicpalimpsest.PuppeteerSocketServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.stream.Collectors;

import static me.psychedelicpalimpsest.PuppeteerSocketServer.broadcastState;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
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