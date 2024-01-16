package org.codegenerator.generator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.POJOCodeGenerators;
import org.codegenerator.generator.codegenerators.POJOGraphPathSearch;
import org.codegenerator.generator.graphbuilder.StateGraph;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final Class<?> clazz;
    private final POJOCodeGenerators pojoCodeGenerators;
    private final POJOGraphPathSearch pojoGraphPathSearch;
    private final StateGraph stateGraph;
    private final Class<?> builderClazz;
    private final Supplier<?> builderConstructor;
    private final Method buildMethod;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        this.clazz = clazz;
        pojoCodeGenerators = new POJOCodeGenerators(clazz, packageName, className, methodName);
        pojoGraphPathSearch = new POJOGraphPathSearch(clazz);
        stateGraph = new StateGraph(clazz);
        builderClazz = findBuilder();
        builderConstructor = findBuilderConstructor();
        buildMethod = findBuildMethod(builderClazz);
        checkInvariants();
    }


    @Override
    public void generate(@NotNull T finalObject, Path path) {
        stateGraph.findPath(finalObject);
    }

    private Class<?> findBuilder() {
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses()))
                .filter(cls -> findBuildMethod(cls) != null)
                .findFirst().orElseThrow(() -> new RuntimeException(BUILDER_NOT_FOUND));
    }

    private Method findBuildMethod(@NotNull Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .filter(m -> ClassUtils.isAssignable(clazz, m.getReturnType()))
                .findFirst().orElse(null);
    }

    private @NotNull Supplier<?> findBuilderConstructor() {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) return () -> Utils.callSupplierWrapper(constructor::newInstance);
        }
        Method method = Arrays.stream(ArrayUtils.addAll(clazz.getClasses(), clazz))
                .map(this::findBuilderConstructor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(BUILDER_CONSTRUCTOR_FOUND));
        return () -> Utils.callSupplierWrapper(() -> method.invoke(null));
    }

    private Method findBuilderConstructor(@NotNull Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> ClassUtils.isAssignable(builderClazz, method.getReturnType()))
                .findFirst().orElse(null);
    }

    private void checkInvariants() {
        throwIf(clazz.getConstructors().length > 0, new RuntimeException(CONSTRUCTOR_FOUND));
        throwIf(builderClazz == null, new RuntimeException(BUILDER_NOT_FOUND));
    }

    private static final String CONSTRUCTOR_FOUND = "The constructor has been found. You can use a POJO generator";
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
