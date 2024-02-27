package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import org.apache.commons.lang3.function.TriConsumer;
import org.codegenerator.Utils;
import org.codegenerator.generator.methodsequencefinders.concrete.BuilderMethodSequenceFinder;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.ItemType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

public class GenericResolver {
    private final Map<ItemType, TriConsumer<History<Executable>, HistoryNode<Executable>, Map<Object, Type>>> typeToConsumer = new EnumMap<>(ItemType.class);
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
        @NotNull Map<Type, Bounds> bounds = getBounds(typeParameters, typeToObject);
        ClassName rawType = ClassName.get(type);
        TypeName[] types = new TypeName[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<? extends GenericDeclaration> typeVariable = typeParameters[i];
            Bounds bound = bounds.get(typeVariable);
            if (bound != null) {
                if (bound.concreteType == null) {
                    TypeName[] types1 = bound.lower
                            .stream()
                            .filter(bounds::containsKey)
                            .map(t -> bounds.get(t).getType())
                            .map(t -> cachedTypeNames.getOrDefault(typeToObject.get(t), TypeName.get(t)))
                            .toArray(TypeName[]::new);
                    types[i] = TypeVariableName.get(bound.type.getTypeName(), types1);
                } else {
                    Object object = typeToObject.get(typeVariable);
                    TypeName typeName;
                    if (object != null) {
                        type = Utils.findClosestCommonSuperOrInterface(object.getClass(), bound.concreteType);
                        typeName = object.getClass() != type || !cachedTypeNames.containsKey(object) ?
                                TypeName.get(bound.concreteType) :
                                cachedTypeNames.get(object);
                    } else {
                        typeName = cachedTypeNames.getOrDefault(typeToObject.get(typeVariable), TypeName.get(bound.concreteType));
                    }
                    types[i] = typeName;
                }
            } else {
                types[i] = cachedTypeNames.getOrDefault(typeToObject.get(typeVariable), TypeVariableName.get(typeVariable));
            }
        }
        return ParameterizedTypeName.get(rawType, types);
    }

    private @NotNull Map<Type, Bounds> getBounds(
            TypeVariable<? extends GenericDeclaration> @NotNull [] typeParameters,
            @NotNull Map<Type, Object> typeToObject
    ) {
        Map<Type, Bounds> bounds = new HashMap<>();
        for (Type type : typeParameters) {
            Class<?> concrete = null;
            Object value = typeToObject.get(type);
            if (value != null) concrete = value.getClass();
            bounds.put(type, new Bounds(bounds, type, concrete));
        }

        for (TypeVariable<?> t : typeParameters) {
            for (Type t1 : t.getBounds()) {
                Bounds bounds1 = bounds.get(t1);
                if (bounds1 != null) {
                    bounds1.upper.add(t);
                }
                bounds1 = bounds.get(t);
                if (bounds1 != null) {
                    bounds1.lower.add(t1);
                }
            }
        }
        bounds.forEach((k, v) -> v.resolve());
        return bounds;
    }

    private void init() {
        typeToConsumer.put(ItemType.PRIMITIVE, this::processPrimitive);
        typeToConsumer.put(ItemType.ARRAY, this::processArray);
        typeToConsumer.put(ItemType.OBJECT, this::processObject);
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

    private static class Bounds {
        private final List<Type> upper = new ArrayList<>();
        private final List<Type> lower = new ArrayList<>();
        private final Map<Type, Bounds> typeBounds;
        private final Type type;
        private Class<?> concreteType;
        private boolean resolved;

        private Bounds(Map<Type, Bounds> typeBounds, Type type, Class<?> concreteType) {
            this.typeBounds = typeBounds;
            this.type = type;
            this.concreteType = concreteType;
        }

        private Type getType() {
            return concreteType == null ? type : concreteType;
        }

        private void resolve() {
            if (resolved) return;
            resolveLower();
            resolveUpper();
            resolved = true;
        }

        private void resolveLower() {
            for (Type t : lower) {
                if (t instanceof TypeVariable) {
                    Bounds bounds1 = typeBounds.get(t);
                    if (bounds1 != null) bounds1.resolve();
                }
            }
        }

        private void resolveUpper() {
            for (Type t : upper) {
                if (t instanceof TypeVariable) {
                    Bounds bounds1 = typeBounds.get(t);

                    if (bounds1.concreteType == null) continue;

                    concreteType = Utils.findClosestCommonSuperOrInterface(concreteType, bounds1.concreteType);
                }
            }
        }
    }
}
