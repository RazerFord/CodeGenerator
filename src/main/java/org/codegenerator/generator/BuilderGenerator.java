package org.codegenerator.generator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.POJOCodeGenerators;
import org.codegenerator.generator.codegenerators.POJOGraphPathSearch;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graphbuilder.EdgeMethod;
import org.codegenerator.generator.graphbuilder.StateGraph;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final Class<?> clazz;
    private final POJOCodeGenerators pojoCodeGenerators;
    private final POJOGraphPathSearch pojoGraphPathSearch;
    private final Class<?> builderClazz;
    private final Supplier<?> constructorBuilder;
    private final Executable constructorExecutableBuilder;
    private final Method methodBuildBuilder;
    private final StateGraph stateGraph;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        this.clazz = clazz;
        pojoCodeGenerators = new POJOCodeGenerators(clazz, packageName, className, methodName);
        pojoGraphPathSearch = new POJOGraphPathSearch(clazz);
        builderClazz = findBuilder();
        constructorExecutableBuilder = findBuilderConstructor();
        constructorBuilder = createConstructorSupplier(constructorExecutableBuilder);
        methodBuildBuilder = findBuildMethod(builderClazz);
        stateGraph = new StateGraph(builderClazz, constructorBuilder, methodBuildBuilder);
        checkInvariants();
    }


    @Override
    public void generate(@NotNull T finalObject, Path path) {
        ArrayList<EdgeMethod> edgeMethods;
        {
            Deque<EdgeMethod> methodCalls = stateGraph.findPath(finalObject);
            edgeMethods = new ArrayList<>(methodCalls);
        }
        Buildable constructor = new BeginChainingMethod(clazz, "object", constructorExecutableBuilder);

        List<Buildable> buildableList = new ArrayList<>(Collections.singleton(constructor));
        boolean end = false;

        for (int i = 0; i < edgeMethods.size(); i++) {
            EdgeMethod edgeMethod = edgeMethods.get(i);
            if (!end && edgeMethod.getMethod().getReturnType() == builderClazz && i == edgeMethods.size() - 1) {
                buildableList.add(new MiddleChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
            } else if (!end) {
                buildableList.add(new FinalChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
                end = true;
            } else {
                buildableList.add(new MethodCall(edgeMethod.getMethod(), edgeMethod.getArgs()));
            }
        }
        buildableList.add(new Return("object"));
        pojoCodeGenerators.generate(buildableList, path);
        // todo: Select builder constructor!!!
        // todo: User.builder()
        // todo: or
        // todo: new UserBuilder()
        // todo: generate begin chain method
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

    private Executable findBuilderConstructor() {
        for (Constructor<?> constructor : builderClazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) return constructor;
        }
        return Arrays.stream(ArrayUtils.addAll(clazz.getClasses(), clazz))
                .map(this::findBuilderConstructor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(BUILDER_CONSTRUCTOR_FOUND));
    }

    @Contract(pure = true)
    private @NotNull Supplier<?> createConstructorSupplier(Executable executable) {
        if (executable instanceof Method) {
            return () -> Utils.callSupplierWrapper(() -> ((Method) executable).invoke(null));
        }
        if (executable instanceof Constructor<?>) {
            return () -> Utils.callSupplierWrapper(((Constructor<?>) executable)::newInstance);
        }
        throw new IllegalArgumentException();
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
