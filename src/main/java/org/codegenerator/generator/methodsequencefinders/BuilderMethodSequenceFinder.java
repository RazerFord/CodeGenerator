package org.codegenerator.generator.methodsequencefinders;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.AssignableTypePropertyGrouper;
import org.codegenerator.generator.graph.EdgeMethod;
import org.codegenerator.generator.graph.StateGraph;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.codegenerator.Utils.throwIf;

public class BuilderMethodSequenceFinder {
    private final Class<?> clazz;
    private final Class<?> builderClazz;
    private final Supplier<Object> constructorBuilder;
    private final Executable constructorExecutableBuilder;
    private final Method builderMethodBuild;
    private final StateGraph stateGraph;
    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        builderClazz = findBuilder();
        constructorExecutableBuilder = findBuilderConstructor();
        constructorBuilder = createConstructorSupplier(constructorExecutableBuilder);
        builderMethodBuild = findBuildMethod(builderClazz);
        stateGraph = new StateGraph(builderClazz);
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        ArrayList<EdgeMethod> edgeMethods;
        {
            Function<Object, Object> termination = o -> Utils.callSupplierWrapper(() -> builderMethodBuild.invoke(o));
            AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
            @NotNull List<EdgeMethod> methodCalls = stateGraph.findPath(
                    assignableTypePropertyGrouper,
                    constructorBuilder,
                    termination
            );
            edgeMethods = new ArrayList<>(methodCalls);
        }
        Buildable constructor = new BeginChainingMethod(builderClazz, VARIABLE_NAME, constructorExecutableBuilder);

        List<Buildable> buildableList = new ArrayList<>(Collections.singleton(constructor));
        boolean end = false;

        for (int i = 0; i < edgeMethods.size(); i++) {
            EdgeMethod edgeMethod = edgeMethods.get(i);
            if (!end && edgeMethod.getMethod().getReturnType() == builderClazz && i < edgeMethods.size() - 1) {
                buildableList.add(new MiddleChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
            } else if (!end) {
                buildableList.add(new FinalChainingMethod(edgeMethod.getMethod(), edgeMethod.getArgs()));
                end = true;
            } else {
                buildableList.add(new MethodCall(edgeMethod.getMethod(), edgeMethod.getArgs()));
            }
        }
        buildableList.add(new Return(String.format("%s.%s()", VARIABLE_NAME, builderMethodBuild.getName())));
        return buildableList;
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
    private @NotNull Supplier<Object> createConstructorSupplier(Executable executable) {
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

    private static final String VARIABLE_NAME = "object";
    private static final String CONSTRUCTOR_FOUND = "The constructor has been found. You can use a POJO generator";
    private static final String BUILDER_CONSTRUCTOR_FOUND = "Builder constructor not found";
    private static final String BUILDER_NOT_FOUND = "Builder not found";
}
