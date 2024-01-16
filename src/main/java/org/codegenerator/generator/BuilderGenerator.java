package org.codegenerator.generator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.POJOCodeGenerators;
import org.codegenerator.generator.codegenerators.POJOGraphPathSearch;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final Class<?> clazz;
    private final Class<?> builderClazz;
    private final Supplier<?> builderConstructor;
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
        builderConstructor = findBuilderConstructor();
        checkInvariants();
    }


    @Override
    public void generate(@NotNull T finalObject, Path path) {

    }

    private Class<?> findBuilder() {
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses()))
                .filter(cls -> findBuilder(cls) != null)
                .findFirst().orElse(null);
    }

    private Class<?> findBuilder(@NotNull Class<?> cls) {
        return Arrays.stream(cls.getMethods())
                .map(Method::getReturnType)
                .filter(returnType -> ClassUtils.isAssignable(clazz, returnType))
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
