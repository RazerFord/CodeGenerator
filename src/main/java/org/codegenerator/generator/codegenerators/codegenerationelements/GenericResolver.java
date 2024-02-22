package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import org.apache.commons.lang3.function.TriConsumer;
import org.codegenerator.Utils;
import org.codegenerator.generator.methodsequencefinders.internal.BuilderMethodSequenceFinder;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

public class GenericResolver {
    private final Map<HistoryType, TriConsumer<History<Executable>, HistoryNode<Executable>, Map<Object, Type>>> typeToConsumer = new EnumMap<>(HistoryType.class);
    private final Map<Object, TypeName> cachedTypeNames = new IdentityHashMap<>();
    private final History<Executable> history;

    public GenericResolver(History<Executable> history) {
        this.history = history;

        init();
    }

    public TypeName resolve(@NotNull HistoryNode<Executable> node) {
        return cachedTypeNames.computeIfAbsent(node.getObject(), o1 -> {
            recursiveResolve(history, node, new HashMap<>());
            return cachedTypeNames.get(o1);
        });
    }

    public TypeName resolve(Object o) {
        HistoryNode<Executable> node = history.get(o);
        return resolve(node);
    }

    public ResolvedTypeName getResolvedTypeName(@NotNull HistoryNode<Executable> node) {
        return new ResolvedTypeName(node.getObject().getClass(), resolve(node));
    }

    public ResolvedTypeName getResolvedTypeName(Object o) {
        return getResolvedTypeName(history.get(o));
    }

    public TypeName unCachedResolve(@NotNull HistoryNode<Executable> node) {
        cachedTypeNames.remove(node.getObject());
        return resolve(node);
    }

    public TypeName unCachedResolve(Object o) {
        cachedTypeNames.remove(o);
        return resolve(o);
    }

    private void recursiveResolve(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            Map<Object, Type> typeToType
    ) {
        Objects.requireNonNull(typeToConsumer.get(node.getType())).accept(history, node, typeToType);
    }

    private void processArray(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Object, Type> typeToType
    ) {
        Object array = node.getObject();
        Class<?> type = array.getClass();
        cachedTypeNames.put(array, TypeName.get(type));
        for (int i = 0, length = Array.getLength(array); i < length; i++) {
            recursiveResolve(history, history.get(Array.get(array, i)), typeToType);
        }
    }

    private void processPrimitive(
            @NotNull History<Executable> ignored,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Object, Type> typeToType
    ) {
        Object o = node.getObject();
        Class<?> type = node.getObject().getClass();
        cachedTypeNames.put(o, TypeName.get(type));
    }

    private void processObject(
            @NotNull History<Executable> history,
            @NotNull HistoryNode<Executable> node,
            @NotNull Map<Object, Type> typeToType
    ) {
        Object object = node.getObject();
        Class<?> type = object.getClass();
        TypeVariable<? extends GenericDeclaration>[] typeParameters = type.getTypeParameters();

        if (typeParameters.length == 0) {
            cachedTypeNames.put(object, ClassName.get(type));
            return;
        }
        Map<Type, Object> typeToObject = new HashMap<>();
        for (HistoryCall<Executable> call : node.getHistoryCalls()) {
            Type[] types = call.getMethod().getGenericParameterTypes();
            Object[] args = call.getArgs();
            assert types.length == args.length;
            for (int i = 0; i < types.length; i++) {
                recursiveResolve(history, call.getHistoryArg(i), typeToType);
                typeToObject.put(types[i], args[i]);
            }
        }

        cachedTypeNames.put(object, getType(typeToObject, node, typeParameters));
    }

    private @NotNull TypeName getType(
            @NotNull Map<Type, Object> typeToObject,
            @NotNull HistoryNode<Executable> node,
            TypeVariable<? extends GenericDeclaration> @NotNull [] typeParameters
    ) {
        Class<?> type = node.getObject().getClass();
        Class<?> creator = node.getCreatorType();
        if (creator == BuilderMethodSequenceFinder.class) {
            return getTypeBuilder(typeToObject, node, typeParameters);
        } else {
            return getType(typeToObject, type, typeParameters);
        }
    }

    private @NotNull TypeName getTypeBuilder(
            @NotNull Map<Type, Object> typeToObject,
            @NotNull HistoryNode<Executable> node,
            TypeVariable<? extends GenericDeclaration> @NotNull [] typeParameters
    ) {
        List<HistoryCall<Executable>> calls = node.getHistoryCalls();
        Method build = (Method) calls.get(calls.size() - 1).getMethod();

        ParameterizedType retType = (ParameterizedType) build.getGenericReturnType();
        Class<?> type = node.getObject().getClass();
        Supplier<IllegalArgumentException> exceptionSupplier = IllegalArgumentException::new;
        Utils.throwIf(type != retType.getRawType(), exceptionSupplier);

        Type[] types = retType.getActualTypeArguments();
        Map<Type, Object> newTypeToObject = new HashMap<>();
        for (int i = 0; i < types.length; i++) {
            Object o = typeToObject.get(types[i]);
            newTypeToObject.put(typeParameters[i], o);
        }
        return getType(newTypeToObject, type, typeParameters);
    }

    private @NotNull TypeName getType(
            @NotNull Map<Type, Object> typeToObject,
            @NotNull Class<?> type,
            TypeVariable<? extends GenericDeclaration> @NotNull [] typeParameters
    ) {
        ClassName rawType = ClassName.get(type);
        TypeName[] types = new TypeName[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<? extends GenericDeclaration> typeVariable = typeParameters[i];
            types[i] = cachedTypeNames.getOrDefault(typeToObject.get(typeVariable), TypeVariableName.get(typeVariable));
        }
        return ParameterizedTypeName.get(rawType, types);
    }

    private void init() {
        typeToConsumer.put(HistoryType.PRIMITIVE, this::processPrimitive);
        typeToConsumer.put(HistoryType.ARRAY, this::processArray);
        typeToConsumer.put(HistoryType.OBJECT, this::processObject);
    }

    public static class ResolvedTypeName {
        private final Class<?> clazz;
        private final List<Map.Entry<TypeVariable<? extends GenericDeclaration>, TypeName>> types = new ArrayList<>();

        public ResolvedTypeName(
                @NotNull Class<?> clazz,
                TypeName type
        ) {
            this.clazz = clazz;
            TypeVariable<? extends GenericDeclaration>[] parameters = clazz.getTypeParameters();
            if (type instanceof ParameterizedTypeName) {
                ParameterizedTypeName parameterizedType = (ParameterizedTypeName) type;
                for (int i = 0; i < parameters.length; i++) {
                    types.add(new AbstractMap.SimpleEntry<>(parameters[i], parameterizedType.typeArguments.get(i)));
                }
            }
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public int numberParameters() {
            return types.size();
        }

        public Map.Entry<TypeVariable<? extends GenericDeclaration>, TypeName> getTypeByIndex(int i) {
            return types.get(i);
        }
    }
}
