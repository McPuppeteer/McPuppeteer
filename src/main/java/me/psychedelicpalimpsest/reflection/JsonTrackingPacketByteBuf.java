package me.psychedelicpalimpsest.reflection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import net.minecraft.registry.Registries;

public class JsonTrackingPacketByteBuf extends RegistryByteBuf {
    private final JsonArray rootJsonArray;
    private final Stack<JsonArray> jsonStack;
    private boolean isWriting;



    /* Sometimes you need a registry, no matter what */
    public static DynamicRegistryManager getSomethingAsARegistry(){
         var networkHandler = MinecraftClient.getInstance().getNetworkHandler();
         if (networkHandler != null)
             return networkHandler.getRegistryManager();
         else
            return DynamicRegistryManager.of(Registries.REGISTRIES);
    }




    public JsonTrackingPacketByteBuf(ByteBuf parent, DynamicRegistryManager drm) {
        super(parent, drm);
        this.rootJsonArray = new JsonArray();
        this.jsonStack = new Stack<>();
        this.jsonStack.push(rootJsonArray);
        this.isWriting = false;
    }
    public JsonTrackingPacketByteBuf(ByteBuf parent) {
        this(parent, getSomethingAsARegistry());
    }

    private void withWriting(Runnable action) {
        boolean prev = isWriting;
        isWriting = true;
        try {
            action.run();
        } finally {
            isWriting = prev;
        }
    }

    public JsonArray getJsonArray() {
        return rootJsonArray;
    }

    private void appendToJson(JsonElement value) {
        if (!isWriting) {
            jsonStack.peek().add(value);
        }
    }

    private JsonObject wrapWithType(String type, JsonElement value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type);
        jsonObject.add("value", value);
        return jsonObject;
    }

    @Override
    public PacketByteBuf writeBoolean(boolean value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeBoolean(value));
        return this;
    }

    @Override
    public PacketByteBuf writeByte(int value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeByte(value));
        return this;
    }

    @Override
    public PacketByteBuf writeShort(int value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeShort(value));
        return this;
    }

    @Override
    public PacketByteBuf writeShortLE(int value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeShortLE(value));
        return this;
    }

    @Override
    public PacketByteBuf writeInt(int value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeInt(value));
        return this;
    }

    @Override
    public PacketByteBuf writeIntLE(int value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeIntLE(value));
        return this;
    }

    @Override
    public PacketByteBuf writeMedium(int i) {
        appendToJson(new JsonPrimitive(i));
        withWriting(() -> super.writeMedium(i));
        return this;
    }

    @Override
    public PacketByteBuf writeMediumLE(int i) {
        appendToJson(new JsonPrimitive(i));
        withWriting(() -> super.writeMediumLE(i));
        return this;
    }

    @Override
    public PacketByteBuf writeVarInt(int value) {
        appendToJson(wrapWithType("varInt", new JsonPrimitive(value)));
        withWriting(() -> super.writeVarInt(value));
        return this;
    }

    @Override
    public PacketByteBuf writeVarLong(long value) {
        appendToJson(wrapWithType("varLong", new JsonPrimitive(value)));
        withWriting(() -> super.writeVarLong(value));
        return this;
    }

    @Override
    public PacketByteBuf writeLong(long value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeLong(value));
        return this;
    }

    @Override
    public PacketByteBuf writeFloat(float value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeFloat(value));
        return this;
    }

    @Override
    public PacketByteBuf writeDouble(double value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeDouble(value));
        return this;
    }

    @Override
    public PacketByteBuf writeChar(int value) {
        appendToJson(new JsonPrimitive((char) value));
        withWriting(() -> super.writeChar(value));
        return this;
    }

    @Override
    public PacketByteBuf writeString(String value) {
        appendToJson(new JsonPrimitive(value));
        withWriting(() -> super.writeString(value));
        return this;
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        appendToJson(wrapWithType("char sequence", new JsonPrimitive(sequence.toString())));
        final int[] result = new int[1];
        withWriting(() -> result[0] = super.writeCharSequence(sequence, charset));
        return result[0];
    }

    @Override
    public PacketByteBuf writeUuid(UUID uuid) {
        JsonObject uuidJson = new JsonObject();
        uuidJson.addProperty("mostSignificantBits", uuid.getMostSignificantBits());
        uuidJson.addProperty("leastSignificantBits", uuid.getLeastSignificantBits());
        appendToJson(wrapWithType("UUID", uuidJson));
        withWriting(() -> super.writeUuid(uuid));
        return this;
    }

    @Override
    public PacketByteBuf writeByteArray(byte[] array) {
        appendToJson(wrapWithType("byte[]", new JsonPrimitive(Base64.encodeBase64String(array))));
        withWriting(() -> super.writeByteArray(array));
        return this;
    }

    @Override
    public PacketByteBuf writeIntArray(int[] array) {
        JsonArray jsonArray = new JsonArray();
        for (int i : array) {
            jsonArray.add(new JsonPrimitive(i));
        }
        appendToJson(wrapWithType("int[]", jsonArray));
        withWriting(() -> super.writeIntArray(array));
        return this;
    }

    @Override
    public PacketByteBuf writeLongArray(long[] array) {
        JsonArray jsonArray = new JsonArray();
        for (long l : array) {
            jsonArray.add(new JsonPrimitive(l));
        }
        appendToJson(wrapWithType("long[]", jsonArray));
        withWriting(() -> super.writeLongArray(array));
        return this;
    }

    @Override
    public PacketByteBuf writeNbt(@Nullable NbtElement nbt) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        withWriting(() -> writeNbt(byteBuf, nbt));
        appendToJson(wrapWithType("NBT", new JsonPrimitive(Base64.encodeBase64String(byteBuf.array()))));
        withWriting(() -> super.writeNbt(nbt));
        return this;
    }

    @Override
    public <T> void writeCollection(Collection<T> collection, PacketEncoder<? super PacketByteBuf, T> writer) {
        JsonArray jsonArray = new JsonArray();
        jsonStack.push(jsonArray);
        withWriting(() -> {
            for (T item : collection) {
                withWriting(() -> writer.encode(this, item));
            }
        });
        jsonStack.pop();
        appendToJson(wrapWithType("Collection", jsonArray));
        withWriting(() -> super.writeCollection(collection, writer));
    }

    @Override
    public <K, V> void writeMap(Map<K, V> map, PacketEncoder<? super PacketByteBuf, K> keyWriter, PacketEncoder<? super PacketByteBuf, V> valueWriter) {
        JsonArray jsonArray = new JsonArray();
        jsonStack.push(jsonArray);
        withWriting(() -> {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                JsonArray entryArray = new JsonArray();
                jsonStack.push(entryArray);
                withWriting(() -> keyWriter.encode(this, entry.getKey()));
                withWriting(() -> valueWriter.encode(this, entry.getValue()));
                jsonStack.pop();
                jsonArray.add(entryArray);
            }
        });
        jsonStack.pop();
        appendToJson(wrapWithType("Map", jsonArray));
        withWriting(() -> super.writeMap(map, keyWriter, valueWriter));
    }

    @Override
    public <T> void writeOptional(Optional<T> value, PacketEncoder<? super PacketByteBuf, T> writer) {
        JsonArray jsonArray = new JsonArray();
        jsonStack.push(jsonArray);
        withWriting(() -> super.writeOptional(value, writer));
        appendToJson(wrapWithType("Optional", jsonStack.pop()));
    }

    @Override
    public <T> void writeNullable(@Nullable T value, PacketEncoder<? super PacketByteBuf, T> writer) {
        JsonArray jsonArray = new JsonArray();
        jsonStack.push(jsonArray);
        withWriting(() -> super.writeNullable(value, writer));
        appendToJson(wrapWithType("Nullable", jsonStack.pop()));
    }

    @Override
    public PacketByteBuf writeBlockPos(BlockPos pos) {
        JsonObject blockPosJson = new JsonObject();
        blockPosJson.addProperty("x", pos.getX());
        blockPosJson.addProperty("y", pos.getY());
        blockPosJson.addProperty("z", pos.getZ());
        appendToJson(wrapWithType("BlockPos", blockPosJson));
        withWriting(() -> super.writeBlockPos(pos));
        return this;
    }

    @Override
    public PacketByteBuf writeChunkPos(ChunkPos pos) {
        JsonObject chunkPosJson = new JsonObject();
        chunkPosJson.addProperty("x", pos.x);
        chunkPosJson.addProperty("z", pos.z);
        appendToJson(wrapWithType("ChunkPos", chunkPosJson));
        withWriting(() -> super.writeChunkPos(pos));
        return this;
    }

    @Override
    public void writeGlobalPos(GlobalPos pos) {
        JsonObject globalPosJson = new JsonObject();
        globalPosJson.addProperty("dimension", pos.dimension().getValue().toString());
        JsonObject blockPosJson = new JsonObject();
        blockPosJson.addProperty("x", pos.pos().getX());
        blockPosJson.addProperty("y", pos.pos().getY());
        blockPosJson.addProperty("z", pos.pos().getZ());
        globalPosJson.add("position", blockPosJson);
        appendToJson(wrapWithType("GlobalPos", globalPosJson));
        withWriting(() -> super.writeGlobalPos(pos));
    }

    @Override
    public void writeInstant(Instant instant) {
        appendToJson(wrapWithType("Instant", new JsonPrimitive(instant.toString())));
        withWriting(() -> super.writeInstant(instant));
    }

    @Override
    public PacketByteBuf writeDate(Date date) {
        appendToJson(wrapWithType("Date", new JsonPrimitive(date.toString())));
        withWriting(() -> super.writeDate(date));
        return this;
    }

    public static JsonObject jsonOfEnum(Enum<?> instance) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("enum type", YarnMapping.getInstance().mapClassName(
                YarnMapping.Namespace.NAMED,
                instance.getClass().getName()
        ));
        jsonObject.addProperty("value", YarnMapping.serializeUnknownEnum(instance));
        return jsonObject;
    }

    @Override
    public PacketByteBuf writeEnumConstant(Enum<?> instance) {
        appendToJson(wrapWithType("enum constant", jsonOfEnum(instance)));
        withWriting(() -> super.writeEnumConstant(instance));
        return this;
    }

    @Override
    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> type) {
        JsonArray jsonArray = new JsonArray();
        for (Enum<E> e : enumSet) {
            jsonArray.add(jsonOfEnum(e));
        }
        appendToJson(wrapWithType("EnumSet", jsonArray));
        withWriting(() -> super.writeEnumSet(enumSet, type));
    }

    @Override
    public void writeBitSet(BitSet bitSet) {
        JsonArray jsonArray = new JsonArray();
        for (long l : bitSet.toLongArray()) {
            jsonArray.add(new JsonPrimitive(l));
        }
        appendToJson(wrapWithType("BitSet", jsonArray));
        withWriting(() -> super.writeBitSet(bitSet));
    }

    @Override
    public PacketByteBuf writeIdentifier(Identifier id) {
        appendToJson(wrapWithType("Identifier", new JsonPrimitive(id.toString())));
        withWriting(() -> super.writeIdentifier(id));
        return this;
    }

    @Override
    public void writeSyncId(int syncId) {
        appendToJson(wrapWithType("SyncId", new JsonPrimitive(syncId)));
        withWriting(() -> super.writeSyncId(syncId));
    }

    @Override
    public void writeVector3f(Vector3f vector3f) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", vector3f.x);
        jsonObject.addProperty("y", vector3f.y);
        jsonObject.addProperty("z", vector3f.z);
        appendToJson(wrapWithType("Vector3f", jsonObject));
        withWriting(()->super.writeVector3f(vector3f));
    }

    @Override
    public void writeVec3d(Vec3d vec) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", vec.x);
        jsonObject.addProperty("y", vec.y);
        jsonObject.addProperty("z", vec.z);
        withWriting(()->{
            appendToJson(wrapWithType("Vec3d", jsonObject));
            super.writeVec3d(vec);
        });
    }

    @Override
    public void writeQuaternionf(Quaternionf quaternionf) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", quaternionf.x);
        jsonObject.addProperty("y", quaternionf.y);
        jsonObject.addProperty("z", quaternionf.z);
        jsonObject.addProperty("w", quaternionf.w);
        appendToJson(wrapWithType("Quaternionf", jsonObject));
        withWriting(()-> super.writeQuaternionf(quaternionf));
    }

    @Override
    public PacketByteBuf writePublicKey(PublicKey publicKey) {
        appendToJson(wrapWithType("PublicKey", new JsonPrimitive(Base64.encodeBase64String(publicKey.getEncoded()))));
        withWriting(()->super.writePublicKey(publicKey));
        return this;
    }

    @Override
    public PacketByteBuf writeChunkSectionPos(ChunkSectionPos pos) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", pos.getX());
        jsonObject.addProperty("y", pos.getY());
        jsonObject.addProperty("z", pos.getZ());
        appendToJson(wrapWithType("ChunkSectionPos", jsonObject));
        withWriting(()->super.writeChunkSectionPos(pos));

        return this;
    }
}