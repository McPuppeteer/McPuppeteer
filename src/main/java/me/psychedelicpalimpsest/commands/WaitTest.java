package me.psychedelicpalimpsest.commands;

import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;

import java.util.concurrent.atomic.AtomicLong;

import static me.psychedelicpalimpsest.Tasks.PuppeteerTask.ticklyTask;

@PuppeteerCommand(
        cmd="wait",description = ""
)
public class WaitTest implements BaseCommand {
    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        AtomicLong t = new AtomicLong();
        McPuppeteer.tasks.add(ticklyTask(
                (task, ignored) -> {
                    t.set(System.currentTimeMillis());
                },
                (task, onComplete)->{
                    System.out.println("Tick");
                    if (t.get() + 5000 < System.currentTimeMillis()) {
                        onComplete.invoke();
                        callback.resultCallback(BaseCommand.jsonOf());
                    }
                }
        ));
    }
}
