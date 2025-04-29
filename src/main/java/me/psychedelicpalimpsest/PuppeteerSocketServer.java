package me.psychedelicpalimpsest;


/*
    NOTE:  ChatGPT wrote this, therefore it is public domain.


    This is a full-duplex json server for interprocess communication.
 */



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.COMMAND_BARITONE_MAP;
import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.COMMAND_MAP;

public class PuppeteerSocketServer implements Runnable {
    private static int S_PORT = 0;
    private static PuppeteerSocketServer instance = null;
    private static Thread serverThread = null;

    public static void createServer() throws IOException {
        instance = new PuppeteerSocketServer(0);
        S_PORT = instance.getPort();

        serverThread = new Thread(instance);
        serverThread.start();
    }



    public static int getInstancePort(){
        return S_PORT;
    }
    public static PuppeteerSocketServer getInstance(){
        return instance;
    }
    public static void killServer(){
        instance.shutdown();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private final int port;
    public int getPort() {
        return port;
    }

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<SocketChannel, ClientSession> clients = new HashMap<>();
    private volatile boolean running = true;

    private PuppeteerSocketServer(int port) throws IOException {
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.port = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
    }


    @Override
    public void run() {
        try {
            while (running) {
                selector.select();
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isWritable()) {
                        handleWrite(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                for (ClientSession session : clients.values()) {
                    session.close();
                }
                serverChannel.close();
                selector.close();
            } catch (IOException ignored) {}
        }
    }
    public void shutdown() {
        running = false;
        selector.wakeup(); // Unblock select()
    }

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
            session.readRequests();
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

    // Example: send a callback to a client from other code
    public void sendCallback(SocketChannel client, Map<String, Object> callback) {
        ClientSession session = clients.get(client);
        if (session != null) {
            session.queueResponse(callback);
            session.key.interestOpsOr(SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    private void sendPacket(ClientSession session, Map<String, Object> response){
        session.queueResponse(response);
        session.key.interestOpsOr(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    // For demonstration: handle requests (replace with your logic)
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

        if (!COMMAND_MAP.containsKey(cmd)){
            response.put("status", "error");
            response.put("message", "unknown command");
            response.put("id", id);
            sendPacket(session, response);
            return;
        }
        if (COMMAND_BARITONE_MAP.get(cmd) && !McPuppeteer.hasBaritoneInstalled){
            response.put("status", "error");
            response.put("message", "baritone not installed");
            response.put("id", id);
            sendPacket(session, response);
            return;
        }

        /* The command has the option send something at ANY TIME. */
        COMMAND_MAP.get(cmd).onRequest(request, (result -> {
            if (!result.containsKey("status"))
                result.put("status", "ok");
            result.put("id", id);
            sendPacket(session, result);
        }));
    }

    // Inner class for per-client state
    class ClientSession {
        final SocketChannel channel;
        final SelectionKey key;
        final ObjectMapper mapper;
        final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
        final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
        final StringBuilder incoming = new StringBuilder();

        ClientSession(SocketChannel channel, SelectionKey key, ObjectMapper mapper) {
            this.channel = channel;
            this.key = key;
            this.mapper = mapper;
        }

        void readRequests() throws IOException {
            int n = channel.read(readBuffer);
            if (n == -1) throw new IOException("Client closed");
            readBuffer.flip();
            while (readBuffer.hasRemaining()) {
                char c = (char) readBuffer.get();
                incoming.append(c);
                if (c == '\n') {
                    String line = incoming.toString().trim();
                    incoming.setLength(0);
                    JsonNode req = mapper.readTree(line);
                    handleRequest(this, req);
                }
            }
            readBuffer.clear();
        }

        void queueResponse(Map<String, Object> response) {
            try {
                String json = mapper.writeValueAsString(response) + "\n";
                writeQueue.add(ByteBuffer.wrap(json.getBytes()));
            } catch (IOException e) {
                // ignore
            }
        }

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
