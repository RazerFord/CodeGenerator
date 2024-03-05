package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.concrete.*;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Generators {
    private Generators() {
    }

    /**
     * Creates a standard generator that first tries to apply a method finder
     * for Builders, then for POJO.
     *
     * @param classes to use in JacoDB
     * @return generator
     */
    public static @NotNull CommonGenerator standard(Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl();
        commonGenerator.registerPipeline(createFunctionsForBuilder(classes));
        return commonGenerator;
    }

    /**
     * Creates a standard generator.
     * Generates a class in the {@param packageName} package named {@param className}.
     * The generated class contains the {@param methodName} method, which can be used
     * to recreate the requested object
     *
     * @param packageName package name
     * @param className   class name
     * @param methodName  method name
     * @param classes     to use in JacoDB
     * @return generator
     * @see Generators#standard(Class[]) )
     */
    public static @NotNull CommonGenerator standard(String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl(packageName, className, methodName);
        commonGenerator.registerPipeline(createFunctionsForBuilder(classes));
        return commonGenerator;
    }

    /**
     * Creates a custom generator.
     * The list of finders for this generator is empty.
     *
     * @return generator
     */
    public static @NotNull CommonGenerator custom() {
        return new CommonGeneratorImpl();
    }

    /**
     * Creates a custom generator.
     * The list of finders for this generator is empty.
     *
     * @param packageName package name
     * @param className   class name
     * @param methodName  method name
     * @return generator
     * @see Generators#custom()
     * @see Generators#standard(String, String, String, Class[])
     */
    public static @NotNull CommonGenerator custom(String packageName, String className, String methodName) {
        return new CommonGeneratorImpl(packageName, className, methodName);
    }

    /**
     * Creates a generator for builder.
     *
     * @param target  class for which you want to generate code
     * @param classes to use in JacoDB
     * @return generator
     */
    public static @NotNull Generator forBuilder(Class<?> target, Class<?>... classes) {
        CommonGenerator commonGenerator = standard(classes);
        commonGenerator.registerFinder(target, new BuilderMethodSequenceFinder(target, classes));
        return commonGenerator;
    }

    /**
     * Creates a generator for Pojo.
     *
     * @param target      class for which you want to generate code
     * @param packageName package name
     * @param className   class name
     * @param methodName  method name
     * @param classes     to use in JacoDB
     * @return generator
     * @see Generators#standard(String, String, String, Class[])
     */
    public static @NotNull Generator forBuilder(Class<?> target, String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = standard(packageName, className, methodName, classes);
        commonGenerator.registerFinder(target, new BuilderMethodSequenceFinder(target, classes));
        return commonGenerator;
    }

    /**
     * Creates a generator for Pojo.
     *
     * @param pojo    class for which you want to generate code
     * @param classes to use in JacoDB
     * @return generator
     * @see Generators#standard(String, String, String, Class[])
     */
    public static @NotNull Generator forPojo(Class<?> pojo, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl();
        commonGenerator.registerPipeline(createFunctionsForPojo(classes));
        commonGenerator.registerFinder(pojo, new POJOMethodSequenceFinder());
        return commonGenerator;
    }

    /**
     * Creates a generator for Pojo.
     *
     * @param pojo        class for which you want to generate code
     * @param packageName package name
     * @param className   class name
     * @param methodName  method name
     * @param classes     to use in JacoDB
     * @return generator
     */
    public static @NotNull Generator forPojo(Class<?> pojo, String packageName, String className, String methodName, Class<?>... classes) {
        CommonGenerator commonGenerator = new CommonGeneratorImpl(packageName, className, methodName);
        commonGenerator.registerPipeline(createFunctionsForPojo(classes));
        commonGenerator.registerFinder(pojo, new POJOMethodSequenceFinder());
        return commonGenerator;
    }

    private static @NotNull List<Function<TargetObject, ? extends MethodSequenceFinder>> createFunctionsForBuilder(Class<?>... classes) {
        List<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderList = new ArrayList<>();

        methodSequenceFinderList.add(to -> new NullMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new PrimitiveMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new ArrayMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new BuilderMethodSequenceFinder(to.getClazz(), classes));
        methodSequenceFinderList.add(to -> new POJOMethodSequenceFinder());

        return methodSequenceFinderList;
    }

    private static @NotNull List<Function<TargetObject, ? extends MethodSequenceFinder>> createFunctionsForPojo(Class<?>... classes) {
        List<Function<TargetObject, ? extends MethodSequenceFinder>> functions = createFunctionsForBuilder(classes);
        int size = functions.size();
        Collections.swap(functions, size - 2, size - 1);
        return functions;
    }
}
