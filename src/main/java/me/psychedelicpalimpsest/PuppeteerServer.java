/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.psychedelicpalimpsest;

import static me.psychedelicpalimpsest.McPuppeteer.LOGGER;
import static me.psychedelicpalimpsest.McPuppeteer.MOD_ID;
import static me.psychedelicpalimpsest.PuppeteerCommandRegistry.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;

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
	[1 byte for data type: 'j' (JSON), 'b' (Binary) or 'n' (NBT)]
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

    * For some commands, the server may respond with Binary or NBT data. In this case,
      the packet format is:
	['n' or 'b']
	[32-bit network-endian length of data]
	[data]
	[16-bit network-endian length of id]
	[id]

	- If a command results in an error, the response will always be in JSON,
	  regardless of the command.
*/

public class PuppeteerServer implements Runnable {
	/* I just asked google for a random number */
	private static final int BROADCAST_PORT = 43842;
	private static final String SERVER_HOST = "0.0.0.0";

	public PuppeteerServer() {
		this.uuid = UUID.randomUUID();
	}

	public static boolean validateContext(BaseCommand.CommandContext context) {
		return switch (context) {
			case ANY -> true;
			case PLAY -> MinecraftClient.getInstance().player != null;
			case PLAY_WITH_MOVEMENT -> {
				if (MinecraftClient.getInstance().player == null) yield false;
				yield !MinecraftClient.getInstance().player.isSleeping();
			}
			case PRE_PLAY -> MinecraftClient.getInstance().player == null;
			default -> {
				LOGGER.warn("Invalid command context: " + context);
				yield false;
			}
		};
	}

	public static void broadcastState() throws IOException {
		UUID uid = MinecraftClient.getInstance().getSession().getUuidOrNull();

		JsonObject state = new JsonObject();
		state.addProperty("mod", MOD_ID);
		state.addProperty("port", instance.getPort());
		state.addProperty("server uuid", instance.uuid.toString());
		state.addProperty("player username", MinecraftClient.getInstance().getSession().getUsername());
		if (uid != null) state.addProperty("player uuid", uid.toString());
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

		DatagramPacket packet =
		    new DatagramPacket(buffer, buffer.length, broadcastAddr, BROADCAST_PORT);

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

	public void serverTask(Runnable task) {
		if (Thread.currentThread().threadId() != PuppeteerServer.listenThread.threadId()) {
			pendingTasks.add(task);
			selector.wakeup();
		} else {
			task.run();
		}
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
				selector.select();

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

		if (attachment.expectedLength != -1 && buffer.remaining() >= attachment.expectedLength) {
			byte[] data = new byte[attachment.expectedLength];
			buffer.get(data);
			processData(client, attachment.dataType, data);

			// Reset for next message
			attachment.expectedLength = -1;
			attachment.dataType = 0;
		}

		buffer.compact();
	}

	public interface PacketOnCompletion {
		void onCompletion(JsonObject result);
	}

	private void processPacket(
	    SocketChannel client, JsonObject request, PacketOnCompletion onCompletion) {
		if (!request.has("cmd") || !request.has("id")) {

			JsonObject response =
			    BaseCommand.jsonOf(
				"status", "error",
				"type", "format",
				"message", "invalid request");
			if (request.has("id")) response.add("id", request.get("id"));

			writeJsonPacket(client, response);
			return;
		}

		final String cmd = request.get("cmd").getAsString();
		final String id = request.get("id").getAsString();

		if (!COMMAND_MAP.containsKey(cmd)) {
			writeJsonPacket(
			    client,
			    BaseCommand.jsonOf(
				"status", "error",
				"type", "format",
				"message", "unknown command",
				"id", id));
			return;
		}

		for (String r : COMMAND_REQUIREMENTS_MAP.get(cmd)) {
			if (McPuppeteer.installedMods.contains(r)) continue;
			writeJsonPacket(
			    client,
			    BaseCommand.jsonOf(
				"status",
				"error",
				"type",
				"mod requirement",
				"message",
				r + " not installed",
				"id",
				id));
			return;
		}
		BaseCommand.CommandContext ctx = COMMAND_CONTEXT_MAP.get(cmd);
		if (!validateContext(ctx)) {
			writeJsonPacket(
			    client,
			    BaseCommand.jsonOf(
				"status",
				"error",
				"type",
				"context error",
				"message",
				"The player is not in the expected context, meaning they cannot complete the"
				    + " requested action. Expected context: " + ctx,
				"id",
				id));
			return;
		}

		try {
			COMMAND_MAP
			    .get(cmd)
			    .onRequest(
				request,
				new BaseCommand.LaterCallback() {
					@Override
					public void callbacksModView(BaseCommand.CallbackModView callback) {
						serverTask(
						    () -> {
							    ClientAttachment attachment =
								(ClientAttachment) client.keyFor(instance.selector).attachment();
							    callback.invoke(attachment.allowedCallbacks, attachment.packetCallbacks);
						    });
					}

					@Override
					public void resultCallback(JsonObject result) {

						if (!result.has("status")) result.addProperty("status", "ok");
						result.addProperty("id", id);

						if (onCompletion != null) onCompletion(result);

						writeJsonPacket(client, result);
					}

					@Override
					public void generalCallback(JsonObject result) {

						result.addProperty("callback", true);

						writeJsonPacket(client, result);
					}

					@Override
					public void packetResultCallback(byte[] result) {
						writeBinaryStyleData(client, (byte) 'b', result, id);
					}

					@Override
					public void nbtResultCallback(NbtElement result) {
						PacketByteBuf pb = PacketByteBufs.create();
						pb.writeNbt(result);
						writeBinaryStyleData(client, (byte) 'n', pb.array(), id);
					}

					@Override
					public void simulatePuppeteerCommand(
					    JsonObject request, PacketOnCompletion onCompletion) {
						PuppeteerServer.this.processPacket(client, request, onCompletion);
					}
				});
		} catch (Exception e) {
			LOGGER.error("Error in processData()", e);
			writeJsonPacket(
			    client,
			    BaseCommand.jsonOf(
				"status",
				"error",
				"type",
				"exception",
				"message",
				"An exception occurred while processing this request: " + e.toString(),
				"id",
				id));
		}
	}

	private void processData(SocketChannel client, byte type, byte[] data) {
		if (type == 'j') {
			JsonElement elem = JsonParser.parseString(new String(data, StandardCharsets.UTF_8));
			JsonObject request = elem.getAsJsonObject();
			processPacket(client, request);
		} else if (type == 'n') {
			/* TODO: THIS */
		} else {

			LOGGER.warn("Unknown data type: " + (char) type);
		}
	}

	private void writeByteBufferRaw(SocketChannel client, ByteBuffer respBuffer) {
		ClientAttachment attachment = (ClientAttachment) client.keyFor(selector).attachment();
		attachment.writeQueue.add(respBuffer);

		SelectionKey key = client.keyFor(selector);
		key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
	}

	private void writeBinaryStyleData(
	    SocketChannel client, byte format, byte[] data, @Nullable String id) {
		byte[] idBytes = id == null ? new byte[0] : id.getBytes(StandardCharsets.UTF_8);

		ByteBuffer respBuffer = ByteBuffer.allocate(1 + 4 + 2 + data.length + idBytes.length);
		respBuffer.put((byte) format);

		respBuffer.putInt(data.length);
		respBuffer.put(data);

		respBuffer.putShort((short) idBytes.length);
		respBuffer.put(idBytes);
		respBuffer.flip();

		writeByteBufferRaw(client, respBuffer);
	}

	private void writeJsonPacket(SocketChannel client, JsonObject packet) {
		String jsond = packet.toString();
		byte[] byteData = jsond.getBytes();

		ByteBuffer respBuffer = ByteBuffer.allocate(1 + 4 + byteData.length);
		respBuffer.put((byte) 'j');
		respBuffer.putInt(byteData.length);
		respBuffer.put(byteData);
		respBuffer.flip();

		writeByteBufferRaw(client, respBuffer);
	}

	public interface ServerToClientPacketCallback {
		JsonObject invoke(CallbackManager.PacketCallbackMode mode, ClientAttachment attachment);
	}

	public interface ServerToClientCallback {
		JsonObject invoke();
	}

	public static void broadcastPacket(String packetId, ServerToClientPacketCallback callback) {
		if (getInstance() == null) return;

		getInstance()
		    .serverTask(
			() -> {
				HashMap<CallbackManager.PacketCallbackMode, JsonObject> cache = null;

				Selector s = instance.selector;
				for (SocketChannel client : instance.connectedClients) {
					if (!client.isOpen()) {
						instance.connectedClients.remove(client);
						continue;
					}
					ClientAttachment attachment = (ClientAttachment) client.keyFor(s).attachment();

					var type =
					    attachment.packetCallbacks.getOrDefault(
						packetId, CallbackManager.PacketCallbackMode.DISABLED);
					if (type == CallbackManager.PacketCallbackMode.DISABLED) continue;

					if (cache == null) cache = new HashMap<>();
					if (!cache.containsKey(type)) {
						JsonObject packet = callback.invoke(type, attachment);
						if (!packet.has("callback")) packet.addProperty("callback", true);
						if (!packet.has("status")) packet.addProperty("status", "ok");
						packet.addProperty("type", packetId);
						cache.put(type, packet);
					}
					instance.writeJsonPacket(client, cache.get(type));
				}
			});
	}

	public static void broadcastJsonPacket(
	    CallbackManager.CallbackType type, ServerToClientCallback callback) {
		String type_name = type.name();

		/* No broadcasts :< */
		if (getInstance() == null) return;

		getInstance()
		    .serverTask(
			() -> {
				JsonObject packet = null;

				Selector s = instance.selector;
				for (SocketChannel client : instance.connectedClients) {
					if (!client.isOpen()) {
						instance.connectedClients.remove(client);
						continue;
					}
					ClientAttachment attachment = (ClientAttachment) client.keyFor(s).attachment();

					if (type != CallbackManager.CallbackType.FORCED && !attachment.allowedCallbacks.getOrDefault(type, false)) continue;

					if (packet == null) {
						packet = callback.invoke();
						if (!packet.has("callback")) packet.addProperty("callback", true);
						if (!packet.has("status")) packet.addProperty("status", "ok");
						packet.addProperty("type", type_name);
					}
					instance.writeJsonPacket(client, packet);
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

	public static class ClientAttachment {
		ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
		int expectedLength = -1;
		byte dataType = 0;

		public Map<CallbackManager.CallbackType, Boolean> allowedCallbacks = new HashMap<>();
		public Map<String, CallbackManager.PacketCallbackMode> packetCallbacks = new HashMap<>();
	}
}
