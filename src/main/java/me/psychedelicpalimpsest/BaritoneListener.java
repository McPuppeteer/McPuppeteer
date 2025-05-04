package me.psychedelicpalimpsest;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import me.psychedelicpalimpsest.Tasks.PuppeteerTask;

import static me.psychedelicpalimpsest.Tasks.PuppeteerTask.TaskType.BARITONE;

public class BaritoneListener implements AbstractGameEventListener {
    public static void baritoneInit(){
        /* This needs to be separate or things explode without baritone */
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(getOrCreateInstance());
    }

    public static void panic() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }


    private static BaritoneListener instance;
    public static BaritoneListener getOrCreateInstance(){
        if (instance == null) instance = new BaritoneListener();
        return instance;
    }
    private boolean hasStartedSinceCancel = false;


    @Override
    public void onPathEvent(PathEvent pathEvent) {
        /* Seems something somebody might want */
        PuppeteerServer.broadcastJsonPacket(BaseCommand.jsonOf(
                "type", "baritone event",
                "path state", pathEvent.name()
        ));
        if (PathEvent.CALC_FINISHED_NOW_EXECUTING == pathEvent)
            hasStartedSinceCancel = true;


        if (McPuppeteer.tasks.isEmpty()) return;

        PuppeteerTask task = McPuppeteer.tasks.peek();

        if (PathEvent.CANCELED == pathEvent && hasStartedSinceCancel){
            hasStartedSinceCancel = false;

            if (task.getType() == BARITONE)
                task.onBaritoneCancel();
        }
        if (PathEvent.CALC_FAILED == pathEvent){
            if (task.getType() == BARITONE)
                task.onBaritoneCalculationFailure();
        }







    }
}
