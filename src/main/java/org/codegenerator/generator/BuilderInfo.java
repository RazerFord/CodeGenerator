package org.codegenerator.generator;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

public class BuilderInfo {
    private final Class<?> builderClazz;
    private final Executable builderConstructor;
    private final Method builderBuildMethod;

    public BuilderInfo(
            Class<?> builderClazz,
            Executable builderConstructor,
            Method builderBuildMethod
    ) {
        this.builderClazz = builderClazz;
        this.builderConstructor = builderConstructor;
        this.builderBuildMethod = builderBuildMethod;
    }

    public Class<?> builder() {
        return builderClazz;
    }

    public Executable constructor() {
        return builderConstructor;
    }

    public Method method() {
        return builderBuildMethod;
    }
}
