package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.FileGenerator;
import org.codegenerator.generator.methodsequencefinders.MethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.PipelineMethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class CommonGeneratorImpl implements CommonGenerator {
    private static final String PACKAGE_NAME = "generatedclass";
    private static final String CLASS_NAME = "GeneratedClass";
    private static final String METHOD_NAME = "generate";
    private final String packageName;
    private final String className;
    private final String methodName;
    private final FileGenerator fileGenerator;
    private final MethodSequencePipeline methodSequencePipeline;

    public CommonGeneratorImpl() {
        this(PACKAGE_NAME, CLASS_NAME, METHOD_NAME);
    }

    public CommonGeneratorImpl(
            String packageName,
            String className,
            String methodName
    ) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;

        fileGenerator = new FileGenerator();
        methodSequencePipeline = new PipelineMethodSequencePipeline(new ArrayList<>());
    }

    public void generateCode(@NotNull Object object, Path path) throws IOException {
        generateCode(object, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(
            @NotNull Object object,
            String className,
            String methodName,
            Path path
    ) throws IOException {
        generateCode(object, packageName, className, methodName, path);
    }

    @Override
    public void generateCode(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName,
            Path path
    ) throws IOException {
        History<Executable> history = methodSequencePipeline.findReflectionCalls(new TargetObject(object));

        JavaFile javaFile = fileGenerator.generate(history, object, packageName, className, methodName);

        javaFile.writeTo(path);
    }

    @Override
    public History<Executable> generateReflectionCalls(@NotNull Object object) {
        return methodSequencePipeline.findReflectionCalls(new TargetObject(object));
    }

    @Override
    public History<JcMethod> generateJacoDBCalls(@NotNull Object object) {
        return methodSequencePipeline.findJacoDBCalls(new TargetObject(object));
    }

    @Override
    public void registerFinder(Class<?> clazz, MethodSequenceFinder finder) {
        methodSequencePipeline.registerFinderForClass(clazz, finder);
    }

    @Override
    public void registerPipeline(Collection<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderList) {
        methodSequencePipeline.register(methodSequenceFinderList);
    }

    @Override
    public void registerPipeline(Function<TargetObject, ? extends MethodSequenceFinder> methodSequenceFinder) {
        this.methodSequencePipeline.register(methodSequenceFinder);
    }

    @Override
    public void unregisterPipeline() {
        methodSequencePipeline.unregister();
    }
}
