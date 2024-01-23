package org.codegenerator.generator;

import org.codegenerator.generator.codegenerators.ClassCodeGenerators;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.POJOMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class POJOGenerator<T> implements Generator<T> {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final ClassCodeGenerators classCodeGenerators;
    private final POJOMethodSequenceFinder pojoMethodSequenceFinder;

    @Contract(pure = true)
    public POJOGenerator(@NotNull Class<?> clazz) {
        this(clazz, PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public POJOGenerator(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        classCodeGenerators = new ClassCodeGenerators(clazz, packageName, className, methodName);
        pojoMethodSequenceFinder = new POJOMethodSequenceFinder(clazz);
    }

    public void generate(@NotNull T finalObject, Path path) {
        List<Buildable> pathNode = pojoMethodSequenceFinder.find(finalObject);
        classCodeGenerators.generate(pathNode, path);
    }
}
