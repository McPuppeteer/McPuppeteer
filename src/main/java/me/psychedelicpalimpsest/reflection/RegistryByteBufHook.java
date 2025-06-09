 /**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.reflection;

 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.mojang.serialization.Codec;
 import com.mojang.serialization.JsonOps;
 import io.netty.buffer.ByteBuf;
 import it.unimi.dsi.fastutil.ints.IntList;
 import me.psychedelicpalimpsest.BaseCommand;
 import net.minecraft.network.PacketByteBuf;
 import net.minecraft.network.RegistryByteBuf;
 import net.minecraft.network.codec.PacketEncoder;
 import net.minecraft.registry.DynamicRegistryManager;

 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Stack;

 import static me.psychedelicpalimpsest.BaseCommand.jsonOf;

 public class RegistryByteBufHook extends RegistryByteBuf {
     private boolean doSerialize = true;

     private JsonArray output = new JsonArray();

     private interface JsonAppender {
         void append(JsonElement element);
     }

     Stack<JsonAppender> appenderStack = new Stack<>();


     public RegistryByteBufHook(ByteBuf buf, DynamicRegistryManager registryManager) {
         super(buf, registryManager);

         appenderStack.push(element -> output.add(element));
     }
     public void append(JsonElement element) {
         appenderStack.peek().append(element);
     }
     public void append_rec(JsonElement element) {
         appenderStack.get(appenderStack.size() - 2).append(element);
     }
     public void wrap_stack(String type){

         appenderStack.push((element -> append_rec(jsonOf(
                 "TYPE", type,
                 "data", element
         ))));
     }

     @Override
     public <T> void encodeAsJson(Codec<T> codec, T value) {
         wrap_stack("json");
         super.encodeAsJson(codec, value);

         appenderStack.pop();
     }

     @Override
     public PacketByteBuf writeString(String string, int maxLength) {
         appenderStack.peek().append(new JsonPrimitive(string));
         return super.writeString(string, maxLength);
     }

     @Override
     public <T> void writeCollection(Collection<T> collection, PacketEncoder<? super PacketByteBuf, T> writer) {
         wrap_stack("collection");
         super.writeCollection(collection, writer);
         appenderStack.pop();
     }

     @Override
     public void writeIntList(IntList list) {
         append(McReflector.serializeObject(list));
         super.writeIntList(list);
     }

     @Override
     public <K, V> void writeMap(Map<K, V> map, PacketEncoder<? super PacketByteBuf, K> keyWriter, PacketEncoder<? super PacketByteBuf, V> valueWriter) {
         JsonArray jarray = new JsonArray();
         map.forEach((key, value) -> {
             keyWriter.encode(this, (K)key);
             valueWriter.encode(this, (V)value);
         });

     }
 }
