package me.psychedelicpalimpsest.mixin;

import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.CallbackManager;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerServer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public class ChatHubMixin {
    @Shadow @Final private List<ChatHudLine> messages;

    @Inject(method="addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at=@At("HEAD"))
    void onAddMessage(ChatHudLine message, CallbackInfo ci) {
        PuppeteerServer.broadcastJsonPacket(CallbackManager.CallbackType.CHAT, BaseCommand.jsonOf(
           "message", McPuppeteer.textToString(message.content()),
           "message json",   McPuppeteer.createTextJsonSerializer().toJsonTree(message.content())
        ));
    }


}
