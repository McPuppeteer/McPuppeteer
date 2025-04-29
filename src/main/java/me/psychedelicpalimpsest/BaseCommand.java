package me.psychedelicpalimpsest;


/*
    if you are making a command, you need to use the @RegisterPuppeteerCommand annotation.

 */

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface BaseCommand {
    interface LaterCallback {
        void callback(Map<String, Object> result);
    }


    /* You can request a packet be sent immediately */
    void onRequest(JsonNode request, LaterCallback callback);


}
