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

import me.psychedelicpalimpsest.McPuppeteer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.impl.LazyMappingResolver;
import net.fabricmc.loader.impl.lib.mappingio.MappingReader;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.MappingFormat;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.mappingio.tree.MemoryMappingTree;
import net.fabricmc.loader.impl.util.mappings.FilteringMappingVisitor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class YarnMapping {
    private MappingTree tree;

    private static YarnMapping instance = null;

    private final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
    private MappingTree fabricTree;

    private final Map<String, MappingTree.ClassMapping> intermediaryToClass;
    private final Map<String, MappingTree.ClassMapping> namedToClass;
    private final int namedIdx;
    private final int intermediaryIdx;

    public static YarnMapping getInstance() {
        return instance;
    }

    public static void createMapping() {
        instance = new YarnMapping();
    }


    private YarnMapping() {


        MappingResolver res = resolver;


        // Fabric hides all impl details, so we need to use java
        // reflections to get shit done
        try {

            // Get past any and all cache layers
            while (res instanceof LazyMappingResolver) {
                Method delegateMethod = LazyMappingResolver.class.getDeclaredMethod("getDelegate");
                delegateMethod.setAccessible(true);
                res = (MappingResolver) delegateMethod.invoke(res);
            }

            // Access our mapping tree
            Field mappingsTreeField = res.getClass().getDeclaredField("mappings");
            mappingsTreeField.setAccessible(true);
            fabricTree = (MappingTree) mappingsTreeField.get(res);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            McPuppeteer.LOGGER.error("Cannot load mapping tree from fabric", e);
            fabricTree = null;
        }

        tree = new MemoryMappingTree();
        try (InputStream is = YarnMapping.class.getResourceAsStream("/assets/mc-puppeteer/mappings.tiny")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            MappingFormat mf = MappingReader.detectFormat(reader);

            final FilteringMappingVisitor mappingFilter = new FilteringMappingVisitor((MappingVisitor) tree);


            switch (mf) {
                case TINY_FILE:
                    Tiny1FileReader.read(reader, mappingFilter);
                    break;
                case TINY_2_FILE:
                    Tiny2FileReader.read(reader, mappingFilter);
                    break;
                default:
                    throw new RuntimeException("Invalid mappings format");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!tree.getSrcNamespace().equals("official")) {
            throw new RuntimeException("Invalid mappings format. Puppeteer requires the mappings format to have the official namespace as the 'source'");
        }

        intermediaryToClass = new HashMap<>(tree.getClasses().size());
        namedToClass = new HashMap<>(tree.getClasses().size());

        intermediaryIdx = tree.getNamespaceId("intermediary");
        namedIdx = tree.getNamespaceId("named");

        for (MappingTree.ClassMapping mapping : tree.getClasses()) {
            intermediaryToClass.put(mapping.getName(intermediaryIdx), mapping);
            namedToClass.put(mapping.getName(namedIdx), mapping);
        }

    }

    public enum Namespace {
        INTERMEDIARY,
        NAMED,
    }

    private int nsToIdx(Namespace namespace) {
        return namespace == Namespace.INTERMEDIARY ? intermediaryIdx : namedIdx;
    }

    private Map<String, MappingTree.ClassMapping> nsToMap(Namespace namespace) {
        return namespace == Namespace.INTERMEDIARY ? intermediaryToClass : namedToClass;
    }


    public static String serializeUnknownEnum(Object obj) {
        assert obj.getClass().isEnum();
        Optional<Field> origin = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter((field -> {
                            field.setAccessible(true);

                            try {
                                return field.get(obj).equals(obj);
                            } catch (IllegalAccessException e) {
                                return false;
                            }
                        })
                ).findFirst();

        if (origin.isEmpty())
            return "unknown/invalid enum value";

        Field real_origin = origin.get();
        return YarnMapping.getInstance().unmapFieldName(
                YarnMapping.Namespace.NAMED,

                real_origin.getDeclaringClass().getName(),
                real_origin.getName(),
                Type.getDescriptor(real_origin.getType())
        );
    }

    @Nullable
    public MappingTree.ClassMapping mapClass(Namespace namespace, String className) {
        className = className.replace('.', '/');
        return nsToMap(namespace).get(className);
    }

    @Nullable
    public MappingTree.ClassMapping unmapClass(String className) {
        String inter = resolver.unmapClassName("intermediary", className.replace('/', '.')).replace('.', '/');
        return intermediaryToClass.get(inter);
    }

    @Nullable
    public MappingTree.ClassMapping unmapClass_builtin(String className) {
        String inter = resolver.unmapClassName("intermediary", className.replace('/', '.')).replace('.', '/');
        return fabricTree.getClass(inter, fabricTree.getNamespaceId("intermediary"));
    }


    /**
     * Map a class name to the mapping currently used at runtime.
     *
     *
     * @param namespace the namespace of the provided class name
     * @param className the provided binary class name
     * @return the mapped class name, or null if no such mapping is present
     */
    @Nullable
    public String mapClassName(Namespace namespace, String className) {
        var cls = mapClass(namespace, className);
        if (cls == null) return null;
        String inter = cls.getName(intermediaryIdx);
        return resolver.mapClassName("intermediary", inter.replace('/', '.'));
    }

    /**
     * Unmap a class name to the mapping currently used at runtime.
     *
     * @param targetNamespace The target namespace for unmapping.
     * @param className the provided binary class name of the mapping form currently used at runtime
     * @return the mapped class name, or {@code className} if no such mapping is present
     */
    public String unmapClassName(Namespace targetNamespace, String className) {
        var cls = unmapClass(className);
        if (cls == null) return null;

        String unmapped = cls.getName(nsToIdx(targetNamespace));
        if (unmapped == null) return null;
        return unmapped.replace('/', '.');
    }

    /**
     * Map a field name to the mapping currently used at runtime.
     *
     * @param namespace the namespace of the provided field name and descriptor
     * @param owner the binary name of the owner class of the field
     * @param name the name of the field
     * @param descriptor the descriptor of the field
     * @return the mapped field name, or {@code name} if no such mapping is present
     */
    public String mapFieldName(Namespace namespace, String owner, String name, String descriptor) {
        System.err.println(descriptor);
        var cls = mapClass(namespace, owner.replace('/', '.'));
        if (cls == null) return null;

        var field = cls.getField(name, descriptor, nsToIdx(namespace));
        if (field == null) return null;

        return fabricTree.getField(
                cls.getName(intermediaryIdx),
                field.getName(intermediaryIdx),
                descriptor,
                fabricTree.getNamespaceId("intermediary")
        ).getName(resolver.getCurrentRuntimeNamespace());
    }

    /**
     * Unmap a field name to the mapping currently used at runtime.
     *
     * @param targetNamespace the target namespace for unmapping
     * @param owner the binary name of the owner class of the field
     * @param name the name of the field
     * @param descriptor the descriptor of the field
     * @return the mapped field name, or {@code name} if no such mapping is present
     */
    public String unmapFieldName(Namespace targetNamespace, String owner, String name, String descriptor) {
        var cls = unmapClass(owner);
        var clsF = unmapClass_builtin(owner);
        if (cls == null || clsF == null) return null;

        var field = clsF.getField(name, descriptor, fabricTree.getNamespaceId(resolver.getCurrentRuntimeNamespace()));
        if (field == null) return null;

        String intr = field.getName("intermediary");

        // If we got this far, an intermediary is enough
        var yarnF = cls.getField(intr, null, intermediaryIdx);
        if (yarnF == null) return null;

        return yarnF.getName(nsToIdx(targetNamespace));
    }


    /**
     * Map a method name to the mapping currently used at runtime.
     *
     * @param namespace the namespace of the provided method name and descriptor
     * @param owner the binary name of the owner class of the method
     * @param name the name of the method
     * @param descriptor the descriptor of the method
     * @return the mapped method name, or {@code name} if no such mapping is present
     */
    String mapMethodName(Namespace namespace, String owner, String name, String descriptor) {
        return null;
    }

}
