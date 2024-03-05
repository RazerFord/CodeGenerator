package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.FileGenerator;
import org.codegenerator.generator.methodsequencefinders.IterablePipeline;
import org.codegenerator.generator.methodsequencefinders.MethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.PipelineMethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Path;
import java.util.*;
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
        JavaFile javaFile = generateInternal(object, packageName, className, methodName);

        javaFile.writeTo(path);
    }

    @Override
    public void generateCode(@NotNull Object object, Appendable appendable) throws IOException {
        generateCode(object, packageName, className, methodName, appendable);
    }

    @Override
    public void generateCode(
            @NotNull Object object,
            String className,
            String methodName,
            Appendable appendable
    ) throws IOException {
        generateCode(object, packageName, className, methodName, appendable);
    }

    @Override
    public void generateCode(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName,
            Appendable appendable
    ) throws IOException {
        JavaFile javaFile = generateInternal(object, packageName, className, methodName);

        javaFile.writeTo(appendable);
    }

    @Override
    public Iterable<String> generateIterable(@NotNull Object object) {
        return generateIterable(object, packageName, className, methodName);
    }

    @Override
    public Iterable<String> generateIterable(@NotNull Object object, String className, String methodName) {
        return generateIterable(object, packageName, className, methodName);
    }

    @Override
    public Iterable<String> generateIterable(@NotNull Object object, String packageName, String className, String methodName) {
        return new IterableImpl(this, object, packageName, className, methodName);
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

    private @NotNull JavaFile generateInternal(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName
    ) {
        History<Executable> history = methodSequencePipeline.findReflectionCalls(new TargetObject(object));
        return fileGenerator.generate(history, object, packageName, className, methodName);
    }

    private static class IterableImpl implements Iterable<String> {
        private final IterablePipeline pipeline;
        private final CommonGeneratorImpl commonGenerator;
        private final @NotNull Object object;
        private final String packageName;
        private final String className;
        private final String methodName;

        private IterableImpl(
                @NotNull CommonGeneratorImpl commonGenerator,
                @NotNull Object object,
                String packageName,
                String className,
                String methodName
        ) {
            List<Function<TargetObject, ? extends MethodSequenceFinder>> finders = commonGenerator
                    .methodSequencePipeline
                    .finders();

            pipeline = new IterablePipeline(finders, new TargetObject(object));

            this.commonGenerator = commonGenerator;
            this.object = object;
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return new IteratorImpl(this);
        }
    }

    private static class IteratorImpl implements Iterator<String> {
        private final Iterator<History<Executable>> iterator;
        private final IterableImpl iterable;

        private IteratorImpl(@NotNull IterableImpl iterable) {
            this.iterator = iterable.pipeline.iterator();
            this.iterable = iterable;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Contract(pure = true)
        @Override
        public @Nullable String next() {
            History<Executable> executableHistory = iterator.next();
            return iterable
                    .commonGenerator
                    .fileGenerator
                    .generate(
                            executableHistory,
                            iterable.object,
                            iterable.packageName,
                            iterable.className,
                            iterable.methodName
                    )
                    .toString();
        }
    }
}
