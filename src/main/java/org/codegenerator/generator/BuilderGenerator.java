package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.ClassCodeGenerators;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.BuilderMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final ClassCodeGenerators classCodeGenerators;
    private final BuilderMethodSequenceFinder builderMethodSequenceFinder;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz, Class<?>... classes) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME, classes);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName, Class<?>... classes) {
        builderMethodSequenceFinder = new BuilderMethodSequenceFinder(clazz, classes);
        classCodeGenerators = new ClassCodeGenerators(clazz, packageName, className, methodName);
    }

    @Override
    public void generate(@NotNull T finalObject, Path path) throws IOException {
        List<Buildable> buildableList = builderMethodSequenceFinder.find(finalObject);

        JavaFile javaFile = classCodeGenerators.generate(buildableList);

        javaFile.writeTo(path);
    }
}
