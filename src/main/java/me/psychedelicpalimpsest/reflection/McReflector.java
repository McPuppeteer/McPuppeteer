/**
 * Copyright (C) 2025 - PsychedelicPalimpsest
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.psychedelicpalimpsest.reflection;

import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import me.psychedelicpalimpsest.BaseCommand;
import me.psychedelicpalimpsest.McPuppeteer;
import net.minecraft.block.Block;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.context.ContextParameterMap;
import net.minecraft.util.context.ContextType;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static me.psychedelicpalimpsest.reflection.YarnMapping.serializeUnknownEnum;

public class McReflector {
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }


    public static JsonArray listToJsonArray(List<JsonElement> element) {
        JsonArray jarr = new JsonArray(element.size());
        for (JsonElement e : element) jarr.add(e);
        return jarr;
    }


    private static final class CircularRefHandler {
        Stack<Object> stack = new Stack<>();
        Set<Object> set = Collections.newSetFromMap(new IdentityHashMap<>());

        public CircularRefHandler() {
        }

        boolean canPush(Object o) {
            return !set.contains(o);
        }

        int getCircularRefLevel(Object e) {
            for (int i = stack.size() - 1; i >= 0; i--) {
                if (stack.get(i) == e) {
                    return stack.size() - i;
                }
            }
            return -1;
        }

        void push(Object o) {
            stack.push(o);
            set.add(o);
        }

        Object pop() {
            set.remove(stack.peek());
            return stack.pop();
        }

        int size() {
            return stack.size();
        }

        void printAll() {
            stack.forEach(e -> System.out.println("ST:\t" + e.getClass().getName()));
        }
    }


    public static JsonElement serializeObject(Object o) {
        return serializeObject(o, new CircularRefHandler());
    }

    public static RegistryWrapper.WrapperLookup getSomethingAsARegistry() {
//         var networkHandler = MinecraftClient.getInstance().getNetworkHandler();
//         if (networkHandler != null)
//             return BuiltinRegistries.REGISTRY_BUILDER.createWrapperLookup(networkHandler.getRegistryManager());
//         else
        return BuiltinRegistries.createWrapperLookup();
    }

    private static final Gson gson = new Gson();

    @SuppressWarnings({"unchecked"})
    private static JsonElement serializeObject(Object obj, CircularRefHandler stack) {
        if (obj == null) return JsonNull.INSTANCE;


        if (!stack.canPush(obj)) {
            return BaseCommand.jsonOf(
                    "_TYPE", stringifyClassName(obj.getClass().getName()),
                    "circular reference", stack.getCircularRefLevel(obj)
            );
        }
        if (stack.size() > 50) {
            stack.printAll();
            throw new StackOverflowError();
        }

        stack.push(obj);

        try {
            if (obj instanceof JsonElement)
                return typeWrap(obj, (JsonElement) obj);




                /* Primitives */
            else if (obj instanceof String)
                return new JsonPrimitive((String) obj);
            else if (obj instanceof Number)
                return new JsonPrimitive((Number) obj);
            else if (obj instanceof Boolean)
                return new JsonPrimitive((Boolean) obj);
            else if (obj instanceof Character)
                return typeWrap(obj, new JsonPrimitive((Character) obj));

                /* Other generic-ish java types */
            else if (obj instanceof Instant)
                return typeWrap(obj, new JsonPrimitive(((Instant) obj).toEpochMilli()));
            else if (obj instanceof Optional<?>)
                return ((Optional<?>) obj).isEmpty()
                        ? JsonNull.INSTANCE
                        : serializeObject(((Optional<?>) obj).get(), stack);

            else if (obj instanceof OptionalLong)
                return typeWrap(obj, ((OptionalLong) obj).isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(((OptionalLong) obj).getAsLong()));
            else if (obj instanceof OptionalDouble)
                return typeWrap(obj, ((OptionalDouble) obj).isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(((OptionalDouble) obj).getAsDouble()));
            else if (obj instanceof OptionalInt)
                return typeWrap(obj, ((OptionalInt) obj).isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(((OptionalInt) obj).getAsInt()));


            else if (obj instanceof byte[])
                return typeWrap(obj, new JsonPrimitive(
                        Base64.getEncoder().encodeToString((byte[]) obj)
                ));

                /* Minecraft Specific types */
            else if (obj instanceof Identifier)
                return typeWrap(obj, new JsonPrimitive(((Identifier) obj).toString()));
            else if (obj instanceof Block)
                return typeWrap(obj, new JsonPrimitive(Registries.BLOCK.getId((Block) obj).toString()));
            else if (obj instanceof BlockItem)
                return typeWrap(obj, serializeObject(((BlockItem) obj).getBlock(), stack));
            else if (obj instanceof Item)
                return typeWrap(obj, new JsonPrimitive(Registries.ITEM.getId((Item) obj).toString()));
            else if (obj instanceof ItemStack itemStack)
                return typeWrap(obj,
                        itemStack.isEmpty()
                            ? new JsonPrimitive("empty stack")
                            :serializeObject((itemStack).toNbt(getSomethingAsARegistry())));
            else if (obj instanceof SingleStackRecipe)
                return BaseCommand.jsonOf(
                        "_TYPE", stringifyClassName(obj.getClass().getName()),
                        "group", ((SingleStackRecipe) obj).getGroup(),
                        "ingredient", serializeObject(((SingleStackRecipe) obj).ingredient().entries, stack),
                        "result", ((SingleStackRecipe) obj).result
                );
            else if (obj instanceof CuttingRecipeDisplay.GroupEntry<?> entry)
                return BaseCommand.jsonOf(
                        "_TYPE", stringifyClassName(obj.getClass().getName()),
                        "ingredient", serializeObject(entry.input().entries, stack),
                        "recipe display", serializeObject(entry.recipe().optionDisplay().getStacks(new ContextParameterMap.Builder().build(new ContextType.Builder().build())), stack)
                );

            else if (obj instanceof NbtElement)
                return typeWrap(obj, new JsonPrimitive(((NbtElement) obj).asString()));


            else if (obj instanceof RegistryKey<?>)
                return typeWrap(obj, new JsonPrimitive(((RegistryKey<?>) obj).toString()));
            else if (obj instanceof StringIdentifiable /* Covers most enums */)
                return typeWrap(obj, new JsonPrimitive(((StringIdentifiable) obj).asString()));
            else if (obj.getClass().isEnum())
                return new JsonPrimitive(serializeUnknownEnum(obj));

            else if (obj instanceof RegistryEntry<?>) {
                return typeWrap(obj, new JsonPrimitive(((RegistryEntry<?>) obj).getIdAsString()));
            }
            // This exists just to stop stack overflows
            else if (obj instanceof RegistryEntry.Reference<?>) {
                RegistryEntry.Reference<?> ref = (RegistryEntry.Reference<?>) obj;

                return BaseCommand.jsonOf(
                        "_TYPE", stringifyClassName(obj.getClass().getName()),
                        "tags", serializeObject(ref.tags, stack),
                        "referenceType", serializeObject(ref.referenceType, stack),
                        "registryKey", serializeObject(ref.registryKey, stack),
                        "value", serializeObject(ref.value, stack)
                );
            } else if (obj instanceof ArgumentSerializer.ArgumentTypeProperties properties) {
                JsonObject json = new JsonObject();
                properties.getSerializer().writeJson((ArgumentSerializer.ArgumentTypeProperties) obj, json);
                return typeWrap(obj, json);
            } else if (obj instanceof Collection<?>)
                return typeWrap(obj, listToJsonArray(
                        ((Collection<Object>) obj)
                                .stream()
                                .map((e) -> serializeObject(e, stack))
                                .collect(Collectors.toList())
                ));
            else if (obj instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) obj;
                JsonArray json = new JsonArray();
                map.forEach((k, v) -> {
                    json.add(BaseCommand.jsonOf(
                            "key", serializeObject(k, stack),
                            "value", serializeObject(v, stack)
                    ));
                });
                return typeWrap(obj, json);
            } else if (obj instanceof ByteBuf)
                return typeWrap(obj, new JsonPrimitive(Base64.getEncoder().encodeToString(obj.toString().getBytes())));
            else if (obj.getClass().getPackageName().startsWith("net.minecraft"))
                return serializeGenericObject(obj, stack);
            else {
                try {
                    return typeWrap(obj, gson.toJsonTree(obj));
                } catch (Exception e) {
                    System.err.println(obj.getClass());
                    e.printStackTrace();
                    throw e;
                }
            }
        } finally {
            stack.pop();
        }

    }


    private static JsonObject typeWrap(Object obj, JsonElement element) {
        return BaseCommand.jsonOf(
                "_TYPE", stringifyClassName(obj.getClass().getName()),
                "data", element
        );
    }


    public static String stringifyClassName(String name) {
        if (name.startsWith("net.minecraft.")) {
            return YarnMapping.getInstance().unmapClassName(YarnMapping.Namespace.NAMED, name);
        } else {
            return name;
        }
    }


    public static JsonObject serializeGenericObject(Object inputObj, CircularRefHandler stack) {
        JsonObject object = new JsonObject();
        object.addProperty("_TYPE", YarnMapping.getInstance().mapClassName(YarnMapping.Namespace.NAMED, inputObj.getClass().getName()));

        try {
            for (Field field : getAllFields(inputObj.getClass())) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                field.setAccessible(true);
                Object obj = field.get(inputObj);


                if (obj instanceof CuttingRecipeDisplay<?>) {
                    System.out.println("CuttingRecipeDisplay");
                }

                String trueName = YarnMapping.getInstance().unmapFieldName(
                        YarnMapping.Namespace.NAMED,

                        /* Handle inheritance */
                        field.getDeclaringClass().getName(),
                        field.getName(),
                        Type.getDescriptor(field.getType())
                );
                if (trueName == null) {
                    // trueName = field.getType().getName();

                    /* Likely an injected field */
                    continue;
                }

                JsonObject fieldObject = new JsonObject();
                fieldObject.addProperty("type", stringifyClassName(field.getType().getCanonicalName()));
                fieldObject.add("data", serializeObject(obj, stack));
                object.add(trueName, fieldObject);

            }
        } catch (IllegalAccessException e) {
            McPuppeteer.LOGGER.error("Cannot serialize packet", e);
        }


        return object;
    }

}
