/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;
import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;
import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.COMMAND_MAP;
import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.COMMAND_REQUIREMENTS_MAP;



/*
    Complete communication system for the Puppeteer protocol

    * The MC instance frequently sends UDP broadcasts:
        - Broadcasts are sent on port 43842.
        - Each broadcast contains the magic number 'PUPPETEER'.
        - The payload is JSON. See broadcastState() for details.
            * The JSON includes the server port.

    * The Puppeteer protocol is "full duplex":
        - Both the MC instance and the Python client can initiate communication.
        - This enables instantaneous callbacks and commands.

    * All packets must follow this format:
        [1 byte for data type: 'j' (JSON) or 'n' (NBT)]
        [32-bit network-endian length of data]
        [data]

    * The Python client can only send JSON.
    * When the client sends a JSON command, it must use this format:
        {"cmd": "SOME_VALID_PUPPETEER_COMMAND",
         "id": "some_unique_string",
         ... additional command-specific fields}

    * When the server sends data, it can be either a 'callback' or a 'response':
        - 'Callbacks' are unsolicited updates about the MC instance state.
            Format:
            {"callback": true,
             "status": "ok" or "error",
             "message": "Usually only present on error",
             "type": "Callback type",
             ...additional type-specific fields}
        - 'Responses' are replies to client requests.
            Format:
            {"id": "same_unique_string_for_request",
             "status": "ok" or "error",
             "message": "Usually only present on error",
             ...additional command-specific fields}

    * For some commands, the server may respond with NBT data. In this case,
      the packet format is:
        ['n']
        [32-bit network-endian length of data]
        [data]
        [16-bit network-endian length of id]
        [id]

        - If a command results in an error, the response will always be in JSON,
          regardless of the command.
*/


public class PuppeteerServer implements Runnable {

    private static final int BROADCAST_PORT = 43842;
    private static final String SERVER_HOST = "0.0.0.0";

    public PuppeteerServer() {
        this.uuid = UUID.randomUUID();
    }


    public static void broadcastState() throws IOException {
        UUID uid = MinecraftClient.getInstance().getSession().getUuidOrNull();

        JsonObject state = new JsonObject();
        state.addProperty("mod", MOD_ID);
        state.addProperty("port", instance.getPort());
        state.addProperty("server uuid", instance.uuid.toString());
        state.addProperty("player username", MinecraftClient.getInstance().getSession().getUsername());
        if (uid != null)
            state.addProperty("player uuid", uid.toString());
        state.addProperty("is in world", MinecraftClient.getInstance().player != null);
        if (MinecraftClient.getInstance().player != null) {
            state.addProperty("x", MinecraftClient.getInstance().player.getX());
            state.addProperty("y", MinecraftClient.getInstance().player.getY());
            state.addProperty("z", MinecraftClient.getInstance().player.getZ());
        }


        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = ("PUPPETEER" + state).getBytes();
        InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, broadcastAddr, BROADCAST_PORT
        );

        socket.send(packet);

        socket.close();
    }

    private static final int BUFFER_SIZE = 4096;

    private static PuppeteerServer instance = null;
    private static Thread listenThread = null;
    private final UUID uuid;

    public static PuppeteerServer getInstance() {
        return instance;
    }


    public static void createServer() throws IOException {
        instance = new PuppeteerServer();
        listenThread = new Thread(instance);
        listenThread.start();

    }

    public static void killServer() {
        instance.running = false;
        try {
            listenThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        try {
            return ((InetSocketAddress) serverSocket.getLocalAddress()).getPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private final Queue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();

    private void scheduleSelectorTask(Runnable task) {
        pendingTasks.add(task);
        selector.wakeup();
    }

    private final Set<SocketChannel> connectedClients = ConcurrentHashMap.newKeySet();
    private ServerSocketChannel serverSocket;
    private Selector selector;
    private boolean running = true;

    @Override
    public void run() {
        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(SERVER_HOST, 0));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            LOGGER.error("Error in creating puppeteer server", e);
            return;
        }
        while (running) {
            try {
                selector.select(20);

                Runnable task;
                while ((task = pendingTasks.poll()) != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        LOGGER.error("Error in creating puppeteer server task", e);
                    }
                }


                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ, new ClientAttachment());
                        connectedClients.add(client);
                    } else if (key.isReadable()) {
                        readData(key);
                    } else if (key.isWritable()) {
                        writeData(key);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error in creating puppeteer server iteration", e);
            }
        }
    }

    private void readData(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientAttachment attachment = (ClientAttachment) key.attachment();
        ByteBuffer buffer = attachment.readBuffer;


        try {
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                client.close();
                connectedClients.remove(client);

                return;
            }
        } catch (SocketException | ClosedChannelException e) {
            client.close();
            connectedClients.remove(client);

            return;
        }

        buffer.flip();

        while (buffer.remaining() >= 4 + 1 && attachment.expectedLength == -1) {
            attachment.dataType = buffer.get();
            attachment.expectedLength = buffer.getInt();
        }

        if (attachment.expectedLength != -1 &&
                buffer.remaining() >= attachment.expectedLength) {
            byte[] data = new byte[attachment.expectedLength];
            buffer.get(data);
            processData(client, attachment.dataType, data);

            // Reset for next message
            attachment.expectedLength = -1;
            attachment.dataType = 0;
        }

        buffer.compact();
    }

    private void processData(SocketChannel client, byte type, byte[] data) {
        try {

            if (type == 'j') {
                JsonElement elem = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));
                JsonObject request = elem.getAsJsonObject();

                if (!request.has("cmd") || !request.has("id")) {
                    JsonObject response = new JsonObject();

                    response.addProperty("status", "error");
                    response.addProperty("type", "format");
                    response.addProperty("message", "invalid request");
                    if (request.has("id"))
                        response.add("id", request.get("id"));

                    writeJsonPacket(client, response, true);
                    return;
                }

                String cmd = request.get("cmd").getAsString();
                String id = request.get("id").getAsString();

                if (!COMMAND_MAP.containsKey(cmd)) {
                    JsonObject response = new JsonObject();
                    response.addProperty("status", "error");
                    response.addProperty("type", "format");
                    response.addProperty("message", "unknown command");
                    response.addProperty("id", id);
                    writeJsonPacket(client, response, true);
                    return;
                }


                for (String r : COMMAND_REQUIREMENTS_MAP.get(cmd)) {
                    if (McPuppeteer.installedMods.contains(r)) continue;
                    JsonObject response = new JsonObject();
                    response.addProperty("status", "error");
                    response.addProperty("type", "mod requirement");
                    response.addProperty("message", r + " not installed");
                    response.addProperty("id", id);
                    writeJsonPacket(client, response, true);
                    return;
                }


                COMMAND_MAP.get(cmd).onRequest(request, new BaseCommand.LaterCallback() {
                    @Override
                    public void callbacksModView(BaseCommand.CallbackModView callback) {
                        scheduleSelectorTask(() -> {
                            ClientAttachment attachment = (ClientAttachment) client.keyFor(instance.selector).attachment();
                            callback.invoke(attachment.allowedCallbacks);
                        });
                    }

                    @Override
                    public void resultCallback(JsonObject result) {

                        if (!result.has("status"))
                            result.addProperty("status", "ok");
                        result.addProperty("id", id);
                        try {

                            writeJsonPacket(client, result, false);
                        } catch (IOException e) {
                            LOGGER.error("Error in resultCallback()", e);
                        }
                    }

                    @Override
                    public void generalCallback(JsonObject result) {

                        result.addProperty("callback", true);

                        try {
                            writeJsonPacket(client, result, false);
                        } catch (IOException e) {
                            LOGGER.error("Error in generalCallback()", e);
                        }
                    }
                });

            } else if (type == 'n') {
                /* TODO: THIS */
            } else {

                LOGGER.warn("Unknown data type: " + (char) type);
            }


        } catch (Exception e) {
            LOGGER.error("Error in processData()", e);
        }
    }


    private void writeJsonPacket(SocketChannel client, JsonObject packet, boolean isOnServerThread) throws IOException {
        String jsond = packet.toString();
        byte[] byteData = jsond.getBytes();

        ByteBuffer respBuffer = ByteBuffer.allocate(1 + 4 + byteData.length);
        respBuffer.put((byte) 'j');
        respBuffer.putInt(byteData.length);
        respBuffer.put(byteData);
        respBuffer.flip();

        Runnable run = (() -> {
            ClientAttachment attachment = (ClientAttachment)
                    client.keyFor(selector).attachment();
            attachment.writeQueue.add(respBuffer);

            SelectionKey key = client.keyFor(selector);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

        });

        if (isOnServerThread)
            run.run();
        else
            scheduleSelectorTask(run);
    }

    public static void broadcastJsonPacket(CallbackManager.CallbackType type, JsonObject packet) {
        String type_name = CallbackManager.CALLBACK_TYPE_STRING_MAP.getOrDefault(type, "UNKNOWN");

        if (!packet.has("callback"))
            packet.addProperty("callback", true);
        if (!packet.has("status"))
            packet.addProperty("status", "ok");
        packet.addProperty("type", type_name);

        /* No broadcasts :< */
        if (getInstance() == null) return;

        getInstance().scheduleSelectorTask(() -> {
            Selector s = instance.selector;
            for (SocketChannel client : instance.connectedClients) {
                if (client.isOpen()) {
                    ClientAttachment attachment = (ClientAttachment)
                            client.keyFor(s).attachment();

                    if (type != CallbackManager.CallbackType.FORCED && !attachment.allowedCallbacks.getOrDefault(type, false))
                        continue;


                    try {
                        instance.writeJsonPacket(client, packet, true);
                    } catch (IOException e) {
                        // Optionally remove client on error
                        instance.connectedClients.remove(client);
                        try {
                            client.close();
                        } catch (IOException ignored) {
                        }
                    }
                } else {
                    instance.connectedClients.remove(client);
                }
            }
        });
    }

    private static void writeData(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientAttachment attachment = (ClientAttachment) key.attachment();

        /* Just write all the shit */
        while (!attachment.writeQueue.isEmpty()) {
            ByteBuffer buffer = attachment.writeQueue.peek();
            client.write(buffer);
            if (buffer.remaining() == 0) attachment.writeQueue.poll();
        }

        key.interestOps(SelectionKey.OP_READ);

    }


    static class ClientAttachment {
        ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
        int expectedLength = -1;
        byte dataType = 0;

        Map<CallbackManager.CallbackType, Boolean> allowedCallbacks = new HashMap<>();
    }


}
