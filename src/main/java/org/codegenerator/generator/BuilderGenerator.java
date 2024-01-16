package org.codegenerator.generator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.generator.codegenerators.POJOCodeGenerators;
import org.codegenerator.generator.codegenerators.POJOGraphPathSearch;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final Class<?> clazz;
    private final Class<?> builderClazz;
    private final POJOCodeGenerators pojoCodeGenerators;
    private final POJOGraphPathSearch pojoGraphPathSearch;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        this.clazz = clazz;
        pojoCodeGenerators = new POJOCodeGenerators(clazz, packageName, className, methodName);
        pojoGraphPathSearch = new POJOGraphPathSearch(clazz);
        builderClazz = findBuilder();
        checkInvariants();
    }


    @Override
    public void generate(@NotNull T finalObject, Path path) {

    }

    private Class<?> findBuilder() {
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses(), clazz))
                .filter(cls -> findBuilder(cls) != null)
                .findFirst().orElse(null);
    }

    private Class<?> findBuilder(@NotNull Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .map(Method::getReturnType)
                .filter(returnType -> ClassUtils.isAssignable(clazz, returnType))
                .findFirst().orElse(null);
    }

    private @Nullable Supplier<?> findBuildMethod() {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return () -> constructor;
            }
        }
        Class<?> foundCls = Arrays.stream(ArrayUtils.addAll(clazz.getClasses(), clazz))
                .filter(cls -> findBuilderMethod(cls) != null)
                .findFirst().orElse(null);
        if (foundCls == null) return null;
        return () -> findBuilderMethod(foundCls);
    }

    private Method findBuilderMethod(@NotNull Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> ClassUtils.isAssignable(builderClazz, method.getReturnType()))
                .findFirst().orElse(null);
    }

    private void checkInvariants() {
        throwIf(clazz.getConstructors().length > 0, new RuntimeException(CONSTRUCTOR_FOUND));
        throwIf(builderClazz == null, new RuntimeException(BUILDER_NOT_FOUND));
    }

    private static final String CONSTRUCTOR_FOUND = "The constructor has been found. You can use a POJO generator";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
