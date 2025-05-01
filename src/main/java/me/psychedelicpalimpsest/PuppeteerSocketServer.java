package me.psychedelicpalimpsest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.shaded.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtHelper;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;
import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.*;

public class PuppeteerSocketServer implements Runnable {


    private static final int BROADCAST_PORT = 43842;
    private static final String SERVER_HOST = "0.0.0.0";

    // --- Static API ---
    private static int S_PORT = 0;
    private static PuppeteerSocketServer instance = null;
    private static Thread serverThread = null;

    public static void createServer() throws IOException {
        instance = new PuppeteerSocketServer(0);
        S_PORT = instance.getPort();
        serverThread = new Thread(instance);
        serverThread.start();
    }

    public static int getInstancePort() {
        return S_PORT;
    }

    public static PuppeteerSocketServer getInstance() {
        return instance;
    }

    public static void killServer() {
        instance.shutdown();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static void broadcastState() throws IOException {
        UUID uid = MinecraftClient.getInstance().getSession().getUuidOrNull();

        JsonObject state = new JsonObject();
        state.addProperty("mod", MOD_ID);
        state.addProperty("port", getInstancePort());
        state.addProperty("server uuid", instance.uuid.toString());
        state.addProperty("player username", MinecraftClient.getInstance().getSession().getUsername());
        if (uid != null)
            state.addProperty("player uuid", uid.toString());
        state.addProperty("is in world", MinecraftClient.getInstance().player != null);
        if (MinecraftClient.getInstance().player != null){
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




    // --- Instance Fields ---
    private final int port;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<SocketChannel, ClientSession> clients = new HashMap<>();
    private final UUID uuid;
    private volatile boolean running = true;

    // --- Constructor ---
    private PuppeteerSocketServer(int port) throws IOException {
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(SERVER_HOST, port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.port = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();

        this.uuid = UUID.randomUUID();
    }

    public int getPort() {
        return port;
    }

    // --- Main Loop ---
    @Override
    public void run() {
        try {
            while (running) {
                try {
                    selector.select();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();

                        if (key.isAcceptable()) handleAccept();
                        if (key.isReadable()) handleRead(key);
                        if (key.isWritable()) handleWrite(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                for (ClientSession session : clients.values()) session.close();
                serverChannel.close();
                selector.close();
                System.out.println("Closing server");
            } catch (IOException ignored) {}
        }
    }

    public void shutdown() {
        running = false;
        selector.wakeup();
    }

    // --- Accept/Read/Write Handlers ---
    private void handleAccept() throws IOException {
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        SelectionKey key = client.register(selector, SelectionKey.OP_READ);
        ClientSession session = new ClientSession(client, key, mapper);
        clients.put(client, session);
        key.attach(session);
    }

    private void handleRead(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        try {
            session.readPackets();
        } catch (IOException e) {
            session.close();
            clients.remove(session.channel);
        }
    }

    private void handleWrite(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        try {
            session.writeResponses();
        } catch (IOException e) {
            session.close();
            clients.remove(session.channel);
        }
    }

    // --- Packet Send/Receive ---
    public void sendCallback(SocketChannel client, Map<String, Object> callback) {
        ClientSession session = clients.get(client);
        if (session != null) {
            session.queueJsonResponse(callback);
            session.key.interestOpsOr(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    private void sendPacket(ClientSession session, Map<String, Object> response) {
        session.queueJsonResponse(response);
        session.key.interestOpsOr(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    // --- Request Handler ---
    private void handleRequest(ClientSession session, JsonNode request) {
        Map<String, Object> response = new HashMap<>();
        if (!request.has("cmd") || !request.has("id")) {
            response.put("status", "error");
            response.put("message", "invalid request");
            if (request.has("id"))
                response.put("id", request.get("id"));
            sendPacket(session, response);
            return;
        }

        String cmd = request.path("cmd").asText();
        String id = request.path("id").asText();

        if (!COMMAND_MAP.containsKey(cmd)) {
            response.put("status", "error");
            response.put("message", "unknown command");
            response.put("id", id);
            sendPacket(session, response);
            return;
        }


        for (String r : COMMAND_REQUIREMENTS_MAP.get(cmd)){
            if (McPuppeteer.installedMods.contains(r)) continue;

            response.put("status", "error");
            response.put("message", r + " not installed");
            response.put("id", id);
            sendPacket(session, response);
            return;
        }


        COMMAND_MAP.get(cmd).onRequest(request, (result -> {
            Map<String, Object> responseMap = new HashMap<>(result);
            if (!responseMap.containsKey("status"))
                responseMap.put("status", "ok");
            responseMap.put("id", id);
            sendPacket(session, responseMap);
        }));
    }

    // --- Inner Class: ClientSession ---
    class ClientSession {
        final SocketChannel channel;
        final SelectionKey key;
        final ObjectMapper mapper;
        final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
        final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
        final ByteBuffer headerBuffer = ByteBuffer.allocate(5); // 1 byte type + 4 bytes length
        ByteBuffer payloadBuffer = null;

        ClientSession(SocketChannel channel, SelectionKey key, ObjectMapper mapper) {
            this.channel = channel;
            this.key = key;
            this.mapper = mapper;
            headerBuffer.clear();
        }

        // Read packets with framing: [type][len][payload]
        void readPackets() throws IOException {
            while (true) {
                // Read header if needed
                if (headerBuffer.hasRemaining()) {
                    int n = channel.read(headerBuffer);
                    if (n == -1) throw new IOException("Client closed");
                    if (headerBuffer.hasRemaining()) return;
                    headerBuffer.flip();
                }

                // Parse header
                if (payloadBuffer == null && headerBuffer.remaining() == 5) {
                    byte type = headerBuffer.get();
                    int len = ((headerBuffer.get() & 0xFF) << 24) |
                            ((headerBuffer.get() & 0xFF) << 16) |
                            ((headerBuffer.get() & 0xFF) << 8) |
                            (headerBuffer.get() & 0xFF);
                    if (len < 0 || len > 10_000_000) throw new IOException("Invalid packet length");
                    payloadBuffer = ByteBuffer.allocate(len);
                    headerBuffer.clear();
                }

                // Read payload
                int n = channel.read(payloadBuffer);
                if (n == -1) throw new IOException("Client closed");
                if (payloadBuffer.hasRemaining()) return;

                // Process packet
                payloadBuffer.flip();
                byte type = headerBuffer.get(0); // type from last header
                if (type == 'j') {
                    byte[] data = new byte[payloadBuffer.remaining()];
                    payloadBuffer.get(data);
                    String json = new String(data);
                    JsonNode req = mapper.readTree(json);
                    handleRequest(this, req);
                } else if (type == 'n') {
                    // NBT not implemented
                }
                payloadBuffer = null;
            }
        }

        // Queue a JSON response packet
        void queueJsonResponse(Map<String, Object> response) {
            try {
                String json = mapper.writeValueAsString(response);
                byte[] data = json.getBytes();
                ByteBuffer buf = ByteBuffer.allocate(1 + 4 + data.length);
                buf.put((byte) 'j');
                buf.putInt(data.length);
                buf.put(data);
                buf.flip();
                writeQueue.add(buf);
            } catch (IOException e) {
                // ignore
            }
        }

        // Write all queued responses
        void writeResponses() throws IOException {
            while (!writeQueue.isEmpty()) {
                ByteBuffer buf = writeQueue.peek();
                channel.write(buf);
                if (buf.hasRemaining()) break;
                writeQueue.poll();
            }
            if (writeQueue.isEmpty()) {
                key.interestOpsAnd(~SelectionKey.OP_WRITE);
            }
        }

        void close() {
            try {
                channel.close();
            } catch (IOException ignored) {}
        }
    }
}
