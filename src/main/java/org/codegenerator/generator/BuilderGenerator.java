package org.codegenerator.generator;

import org.codegenerator.generator.codegenerators.ClassCodeGenerators;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.BuilderMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class BuilderGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final ClassCodeGenerators classCodeGenerators;
    private final BuilderMethodSequenceFinder builderMethodSequenceFinder;

    @Contract(pure = true)
    public BuilderGenerator(@NotNull Class<?> clazz) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public BuilderGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        builderMethodSequenceFinder = new BuilderMethodSequenceFinder(clazz);
        classCodeGenerators = new ClassCodeGenerators(clazz, packageName, className, methodName);
    }

    @Override
    public void generate(@NotNull T finalObject, Path path) {
        List<Buildable> buildableList = builderMethodSequenceFinder.find(finalObject);
        classCodeGenerators.generate(buildableList, path);
    }
}
