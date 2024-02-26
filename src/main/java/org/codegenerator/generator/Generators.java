package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.internal.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Generators {
    public static @NotNull CommonGenerator standart(Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl();
        commonGenerator.registerPipeline(createFunctionsForBuilder(classes));
        return commonGenerator;
    }

    public static @NotNull CommonGenerator standart(String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl(packageName, className, methodName);
        commonGenerator.registerPipeline(createFunctionsForBuilder(classes));
        return commonGenerator;
    }

    public static @NotNull CommonGenerator custom() {
        return new CommonGeneratorImpl();
    }

    public static @NotNull CommonGenerator custom(String packageName, String className, String methodName) {
        return new CommonGeneratorImpl(packageName, className, methodName);
    }

    public static @NotNull Generator forBuilder(Class<?> builder, Class<?>... classes) {
        CommonGenerator commonGenerator = standart(classes);
        commonGenerator.registerFinder(builder, new BuilderMethodSequenceFinder(builder, classes));
        return commonGenerator;
    }

    public static @NotNull Generator forBuilder(Class<?> builder, String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = standart(packageName, className, methodName, classes);
        commonGenerator.registerFinder(builder, new BuilderMethodSequenceFinder(builder, classes));
        return commonGenerator;
    }

    public static @NotNull Generator forPojo(Class<?> pojo, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl();
        commonGenerator.registerPipeline(createFunctionsForPojo(classes));
        commonGenerator.registerFinder(pojo, new POJOMethodSequenceFinder());
        return commonGenerator;
    }

    public static @NotNull Generator forPojo(Class<?> pojo, String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl(packageName, className, methodName);
        commonGenerator.registerPipeline(createFunctionsForPojo(classes));
        commonGenerator.registerFinder(pojo, new POJOMethodSequenceFinder());
        return commonGenerator;
    }

    private static @NotNull List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> createFunctionsForBuilder(Class<?>... classes) {
        List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderList = new ArrayList<>();

        methodSequenceFinderList.add(to -> new NullMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new PrimitiveMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new ArrayMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new BuilderMethodSequenceFinder(to.getClazz(), classes));
        methodSequenceFinderList.add(to -> new POJOMethodSequenceFinder());

        return methodSequenceFinderList;
    }

    private static @NotNull List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> createFunctionsForPojo(Class<?>... classes) {
        List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> functions = createFunctionsForBuilder(classes);
        int size = functions.size();
        Collections.swap(functions, size - 2, size - 1);
        return functions;
    }
}
