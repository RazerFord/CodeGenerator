package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.function.TriConsumer;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class GenericResolver {
    private final Map<HistoryType, TriConsumer<History<Executable>, HistoryNode<Executable>, Map<Type, Type>>> typeToConsumer = new EnumMap<>(HistoryType.class);
    private final Map<Object, TypeName> cachedTypes = new IdentityHashMap<>();
    private final History<Executable> history;

    public GenericResolver(History<Executable> history) {
        this.history = history;
        typeToConsumer.put(HistoryType.PRIMITIVE, this::processPrimitive);
        typeToConsumer.put(HistoryType.ARRAY, this::processArray);
        typeToConsumer.put(HistoryType.OBJECT, this::processObject);
    }

    public TypeName resolve(@NotNull HistoryNode<Executable> node) {
        return resolve(node.getObject());
    }

    public TypeName resolve(Object o) {
        HistoryNode<Executable> node = history.get(o);
        return cachedTypes.computeIfAbsent(node.getObject(), o1 -> {
            recursiveResolve(history, node, new HashMap<>());
            return cachedTypes.get(o1);
        });
    }

    private void recursiveResolve(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            Map<Type, Type> typeToType
    ) {
        Objects.requireNonNull(typeToConsumer.get(node.getType())).accept(history, node, typeToType);
    }

    private void processArray(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Type, Type> typeToType
    ) {
        Object o = node.getObject();
        Object array = node.getObject();
        Class<?> type = array.getClass();
        typeToType.put(type, type);
        for (int i = 0, length = Array.getLength(array); i < length; i++) {
            recursiveResolve(history, history.get(Array.get(array, i)), typeToType);
        }
        cachedTypes.put(o, TypeName.get(type));
    }

    private void processPrimitive(
            @NotNull History<Executable> ignored,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Type, Type> typeToType
    ) {
        Object o = node.getObject();
        Class<?> type = node.getObject().getClass();
        typeToType.put(type, type);
        cachedTypes.put(o, TypeName.get(type));
    }

    private void processObject(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Type, Type> typeToType
    ) {
        Class<?> clazz = node.getObject().getClass();
        TypeVariable<? extends GenericDeclaration>[] typeParameters = clazz.getTypeParameters();

        if (typeParameters.length == 0) {
            cachedTypes.put(node.getObject(), ClassName.get(clazz));
            return;
        }

        Arrays.asList(typeParameters).forEach(tp -> typeToType.put(tp, tp));

        for (HistoryCall<Executable> call : node.getHistoryCalls()) {
            Type[] types = call.getMethod().getGenericParameterTypes();
            Object[] args = call.getArgs();
            assert types.length == args.length;
            for (int i = 0; i < types.length; i++) {
                HistoryNode<Executable> next = call.getHistoryArg(i);
                recursiveResolve(history, next, typeToType);
                typeToType.put(types[i], typeToType.get(next.getObject().getClass()));
            }
        }

        ClassName rawType = ClassName.get(clazz);
        TypeName[] types = new TypeName[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            types[i] = TypeName.get(typeToType.get(typeParameters[i]));
        }
        TypeName parameterizedTypeName = ParameterizedTypeName.get(rawType, types);
        cachedTypes.put(node.getObject(), parameterizedTypeName);
    }
}
