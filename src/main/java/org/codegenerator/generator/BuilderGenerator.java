package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.ClassCodeGenerators;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.*;
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
    private final ClassCodeGenerators classCodeGenerators;
    private final MethodSequenceFinder methodSequenceFinder;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz, Class<?>... classes) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME, classes);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName, Class<?>... classes) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;

        methodSequenceFinder = createPipeline(clazz, classes);
        classCodeGenerators = new ClassCodeGenerators(clazz);
    }

    @Override
    public void generateCode(@NotNull T finalObject, Path path) throws IOException {
        this.generateCode(finalObject, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(@NotNull T finalObject, String className, String methodName, Path path) throws IOException {
        this.generateCode(finalObject, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(
            @NotNull T finalObject,
            String packageName,
            String className,
            String methodName,
            Path path
    ) throws IOException {
        List<Buildable> buildableList = methodSequenceFinder.findBuildableList(finalObject);

        JavaFile javaFile = classCodeGenerators.generate(buildableList, packageName, className, methodName);

        javaFile.writeTo(path);
    }

    @Override
    public History<Executable> generateReflectionCalls(@NotNull T finalObject) {
        return methodSequenceFinder.findReflectionCalls(finalObject);
    }

    @Override
    public History<JcMethod> generateJacoDBCalls(@NotNull T finalObject) {
        return methodSequenceFinder.findJacoDBCalls(finalObject);
    }

    private @NotNull MethodSequenceFinder createPipeline(Class<?> clazz, Class<?>... classes) {
        List<Function<Object, ? extends MethodSequenceFinderInternal>> methodSequenceFinderList = new ArrayList<>();
        methodSequenceFinderList.add(o -> new NullMethodSequenceFinder());
        methodSequenceFinderList.add(o -> new PrimitiveMethodSequenceFinder());
        methodSequenceFinderList.add(o -> new ArrayMethodSequenceFinder());
        methodSequenceFinderList.add(o -> new BuilderMethodSequenceFinder(o.getClass(), classes));
        methodSequenceFinderList.add(o -> new POJOMethodSequenceFinder());
        return new PipelineMethodSequenceFinder(methodSequenceFinderList);
    }
}
