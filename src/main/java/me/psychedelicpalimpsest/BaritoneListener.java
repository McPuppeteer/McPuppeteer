package me.psychedelicpalimpsest;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import net.minecraft.client.MinecraftClient;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class BaritoneListener implements AbstractGameEventListener {
    public static void baritoneInit(){
        /* This needs to be separate or things explode without baritone */
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(getOrCreateInstance());
    }

    public void addBaritoneEventCallback(BaseCommand.LaterCallback callback){
        this.baritoneCommandCallbacks.add(callback);
    }

    private Queue<BaseCommand.LaterCallback> baritoneCommandCallbacks = new LinkedBlockingQueue<>();

    private static BaritoneListener instance;
    public static BaritoneListener getOrCreateInstance(){
        if (instance == null) instance = new BaritoneListener();
        return instance;
    }
    private boolean hasStartedSinceCancel = false;


    @Override
    public void onPathEvent(PathEvent pathEvent) {
        if (PathEvent.CALC_FINISHED_NOW_EXECUTING == pathEvent)
            hasStartedSinceCancel = true;

        /* Seems something somebody might want */
        PuppeteerServer.getInstance().broadcastJsonPacket(BaseCommand.jsonOf(
                "type", "baritone event",
                "path state", pathEvent.name()
        ));

        if (baritoneCommandCallbacks.isEmpty()) return;



         if (PathEvent.CANCELED == pathEvent && hasStartedSinceCancel){
            baritoneCommandCallbacks.remove().resultCallback(BaseCommand.jsonOf(
                    "message", "baritone canceled the operation"
            ));


            hasStartedSinceCancel = false;
        } else if (PathEvent.CALC_FAILED == pathEvent){
            baritoneCommandCallbacks.remove().resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "message", "CALC_FAILED"
            ));

        }

    }
}
