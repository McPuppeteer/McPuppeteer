package me.psychedelicpalimpsest.commands.actions;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import me.psychedelicpalimpsest.PuppeteerCommand;
import me.psychedelicpalimpsest.Tasks.PuppeteerTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@PuppeteerCommand(
        cmd = "algorithmic rotation",
        description = ""
)
public class AlgorithmicRotation implements BaseCommand {
    // percentage of time -> percentage toward target
    // The only limit is that 1f should return 1f (or quite close to 1f)
    public interface InterpolationMethod {
        float interpolate(float percentage);
    }



    private static final InterpolationMethod linearInterpolation =
            (percentage -> percentage);



    public static final Map<String, InterpolationMethod> interpolationMethods;
    static {
        // Yes, this is a lot, but i like variety >:}

        Map<String, InterpolationMethod> map = new HashMap<>();
        map.put("linear", linearInterpolation);
        map.put("sine", percentage -> (float) Math.sin(percentage * (Math.PI / 2f)));
        map.put("quadraticIn", percentage -> percentage * percentage);
        map.put("cubicIn", percentage -> percentage * percentage * percentage);
        map.put("quarticIn", percentage -> percentage * percentage * percentage * percentage);
        map.put("quinticIn", percentage -> (float) Math.pow(percentage, 5));
        map.put("sexticIn", percentage -> (float) Math.pow(percentage, 6));

        // Ease-out
        map.put("quadraticOut", t -> 1 - (1 - t) * (1 - t));
        map.put("cubicOut", t -> 1 - (float) Math.pow(1 - t, 3));
        map.put("quarticOut", t -> 1 - (float) Math.pow(1 - t, 4));
        map.put("quinticOut", t -> 1 - (float) Math.pow(1 - t, 5));

        // Ease-in-out
        map.put("sineInOut", t -> (float) (-0.5 * (Math.cos(Math.PI * t) - 1)));
        map.put("quadraticInOut", t -> t < 0.5 ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2);
        map.put("cubicInOut", t -> t < 0.5 ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2);
        map.put("quarticInOut", t -> t < 0.5 ? 8 * (float) Math.pow(t, 4) : 1 - (float) Math.pow(-2 * t + 2, 4) / 2);
        map.put("quinticInOut", t -> t < 0.5 ? 16 * (float) Math.pow(t, 5) : 1 - (float) Math.pow(-2 * t + 2, 5) / 2);

        // Exponential
        map.put("exponentialIn", t -> t == 0 ? 0 : (float) Math.pow(2, 10 * (t - 1)));
        map.put("exponentialOut", t -> t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t));
        map.put("exponentialInOut", t -> {
            if (t == 0) return 0f;
            if (t == 1) return 1f;
            return t < 0.5
                    ? (float) Math.pow(2, 20 * t - 10) / 2
                    : (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
        });

        // Elastic
        map.put("elasticOut", t -> (float) (Math.sin(-13 * Math.PI / 2 * (t + 1)) * Math.pow(2, -10 * t) + 1));

        interpolationMethods = Collections.unmodifiableMap(map);
    }




    public static float normalize(float angle) {
        float n = angle % 360f;
        return n < 0 ? n + 360f : n;
    }

    @Override
    public void onRequest(JsonObject request, LaterCallback callback) {
        ClientPlayerEntity mc = MinecraftClient.getInstance().player;
        float current_pitch = mc.getPitch();
        float current_yaw = mc.getYaw();

        float target_pitch = request.get("pitch").getAsFloat();
        float target_yaw = request.get("yaw").getAsFloat();
        float degrees_per_tick = request.has("degrees per tick")
                ? request.get("degrees per tick").getAsFloat()
                : 1.0f; // default speed

        // Pitch difference
        float pitch_total_diff = target_pitch - current_pitch;

        // Yaw difference (shortest path)
        float yaw_diff = normalize(target_yaw - current_yaw);
        if (yaw_diff > 180f) yaw_diff -= 360f;

        // Number of ticks needed for each axis
        int pitch_ticks = (int) Math.ceil(Math.abs(pitch_total_diff) / degrees_per_tick);
        int yaw_ticks = (int) Math.ceil(Math.abs(yaw_diff) / degrees_per_tick);
        int total_ticks = Math.max(1, Math.max(pitch_ticks, yaw_ticks));

        final int[] count = {0};
        InterpolationMethod method = interpolationMethods.get(
                request.has("interpolation") ? request.get("interpolation").getAsString() : "linear"
        );


        if (method == null) {
            callback.resultCallback(BaseCommand.jsonOf(
                    "status", "error",
                    "unexpected argument",
                    "message", "Unknown interpolation method"
            ));
            return;
        }


        final float[] last_yaw = {current_yaw};
        final float[] last_pitch = {current_pitch};

        final float fyaw_diff = yaw_diff;


        McPuppeteer.tasks.add(PuppeteerTask.ticklyTask(
                (a, b) -> {/* No setup */},
                (task, onFinish) -> {
                    float t = count[0] + 1;
                    float mult = method.interpolate(t / total_ticks);


                    /*
                        This may seem strange, but without working in diffs like this, if the user
                        attempted to look around while using their mouse, bad things would happen.
                     */


                    float theoretical_pitch = current_pitch + pitch_total_diff * mult;
                    float theoretical_yaw = current_yaw + fyaw_diff * mult;

                    float tick_pitch_diff = theoretical_pitch - last_pitch[0];
                    float tick_yaw_diff = theoretical_yaw - last_yaw[0];


                    last_yaw[0] = theoretical_yaw;
                    last_pitch[0] = theoretical_pitch;

                    mc.setPitch(mc.getPitch() + tick_pitch_diff);
                    mc.setYaw(mc.getYaw() + tick_yaw_diff);


                    count[0]++;
                    if (count[0] >= total_ticks) {
                        onFinish.invoke();

                        // Verify the results
                        float end_pitch_diff = target_pitch - mc.getPitch();
                        float end_yaw_diff = normalize(target_yaw - mc.getYaw());
                        if (end_yaw_diff > 180f) end_yaw_diff -= 360f;

                        // Two degrees of error are accepted
                        if (Math.abs(end_pitch_diff) > 2f || Math.abs(end_yaw_diff) > 2f) {
                            callback.resultCallback(BaseCommand.jsonOf(
                                    "status", "error",
                                    "type", "rotation error",
                                    "message", "An issue has occurred during algorithmic rotation. "
                                            +"It is likely that the user, or the server has caused a rotation "
                                            +"that we didn't expect! Pitch diff: " + end_pitch_diff + " Yaw diff: " + end_yaw_diff
                            ));
                            return;
                        }

                        // If within that error, snap to the target to avoid float errors
                        mc.setPitch(target_pitch);
                        mc.setYaw(target_yaw);


                        callback.resultCallback(BaseCommand.jsonOf("message", "rotation complete"));
                    }
                }
        ));
    }
}
