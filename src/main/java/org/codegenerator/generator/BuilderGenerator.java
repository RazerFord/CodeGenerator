package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.FileGenerator;
import org.codegenerator.generator.methodsequencefinders.MethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.PipelineMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.*;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final String packageName;
    private final String className;
    private final String methodName;
    private final FileGenerator fileGenerator;
    private final MethodSequenceFinder methodSequenceFinder;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz, Class<?>... classes) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME, classes);
    }

    public BuilderGenerator(
            @NotNull Class<?> clazz,
            String packageName,
            String className,
            String methodName,
            Class<?>... classes
    ) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;

        methodSequenceFinder = createPipeline(classes);
        fileGenerator = new FileGenerator();

        registerFinder(clazz, new BuilderMethodSequenceFinder(clazz, classes));
    }

    @Override
    public void generateCode(@NotNull T object, Path path) throws IOException {
        this.generateCode(object, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(
            @NotNull T object,
            String className,
            String methodName,
            Path path
    ) throws IOException {
        this.generateCode(object, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(
            @NotNull T object,
            String packageName,
            String className,
            String methodName,
            Path path
    ) throws IOException {
        History<Executable> history = methodSequenceFinder.findReflectionCalls(new TargetObject(object));

        JavaFile javaFile = fileGenerator.generate(history, object, packageName, className, methodName);

        javaFile.writeTo(path);
    }

    @Override
    public History<Executable> generateReflectionCalls(@NotNull T object) {
        return methodSequenceFinder.findReflectionCalls(new TargetObject(object));
    }

    @Override
    public History<JcMethod> generateJacoDBCalls(@NotNull T object) {
        return methodSequenceFinder.findJacoDBCalls(new TargetObject(object));
    }

    @Override
    public void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder) {
        methodSequenceFinder.registerFinder(clazz, finder);
    }

    private @NotNull MethodSequenceFinder createPipeline(Class<?>... classes) {
        List<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderList = new ArrayList<>();
        methodSequenceFinderList.add(to -> new NullMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new PrimitiveMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new ArrayMethodSequenceFinder());
        methodSequenceFinderList.add(to -> new BuilderMethodSequenceFinder(to.getClazz(), classes));
        methodSequenceFinderList.add(to -> new POJOMethodSequenceFinder());
        return new PipelineMethodSequenceFinder(methodSequenceFinderList);
    }
}
