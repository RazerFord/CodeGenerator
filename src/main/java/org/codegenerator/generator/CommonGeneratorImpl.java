package org.codegenerator.generator;

import com.squareup.javapoet.JavaFile;
import org.codegenerator.generator.codegenerators.FileGenerator;
import org.codegenerator.generator.methodsequencefinders.IterablePipeline;
import org.codegenerator.generator.methodsequencefinders.MethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.PipelineMethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.concrete.JacoDBProxy;
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
    public Iterable<String> iterableCode(@NotNull Object object) {
        return iterableCode(object, packageName, className, methodName);
    }

    @Override
    public Iterable<String> iterableCode(@NotNull Object object, String className, String methodName) {
        return iterableCode(object, packageName, className, methodName);
    }

    @Override
    public Iterable<String> iterableCode(@NotNull Object object, String packageName, String className, String methodName) {
        return new IterableCodeImpl(
                iterableReflectionCalls(object),
                this,
                object,
                packageName,
                className,
                methodName
        );
    }

    @Override
    public Iterable<History<Executable>> iterableReflectionCalls(@NotNull Object object) {
        return new IterableReflectionCallsImpl(this, object);
    }

    @Override
    public Iterable<History<JcMethod>> iterableJacoDBCalls(@NotNull Object object) {
        return new IterableJacoDBCallsImpl(iterableReflectionCalls(object));
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

    private static class IterableReflectionCallsImpl implements Iterable<History<Executable>> {
        private final IterablePipeline pipeline;

        private IterableReflectionCallsImpl(
                @NotNull CommonGeneratorImpl commonGenerator,
                @NotNull Object object
        ) {
            List<Function<TargetObject, ? extends MethodSequenceFinder>> finders = commonGenerator
                    .methodSequencePipeline
                    .finders();

            pipeline = new IterablePipeline(new ArrayList<>(finders), new TargetObject(object));
        }

        @NotNull
        @Override
        public Iterator<History<Executable>> iterator() {
            return new IteratorReflectionCallsImpl(this);
        }
    }

    private static class IteratorReflectionCallsImpl implements Iterator<History<Executable>> {
        private final Iterator<History<Executable>> iterator;

        private IteratorReflectionCallsImpl(@NotNull IterableReflectionCallsImpl iterable) {
            this.iterator = iterable.pipeline.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Contract(pure = true)
        @Override
        public History<Executable> next() {
            return iterator.next();
        }
    }

    private static class IterableJacoDBCallsImpl implements Iterable<History<JcMethod>> {
        private final Iterable<History<Executable>> calls;

        private IterableJacoDBCallsImpl(Iterable<History<Executable>> calls) {
            this.calls = calls;
        }

        @NotNull
        @Override
        public Iterator<History<JcMethod>> iterator() {
            return new IteratorJacoDBCallsImpl(calls);
        }
    }

    private static class IteratorJacoDBCallsImpl implements Iterator<History<JcMethod>> {
        private final Iterator<History<Executable>> iterator;
        private final JacoDBProxy proxy;

        private IteratorJacoDBCallsImpl(@NotNull Iterable<History<Executable>> iterable) {
            this.iterator = iterable.iterator();
            proxy = new JacoDBProxy();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Contract(pure = true)
        @Override
        public @Nullable History<JcMethod> next() {
            History<Executable> history = iterator.next();
            return proxy.historyToJcHistory(history);
        }
    }

    private static class IterableCodeImpl implements Iterable<String> {
        private final Iterable<History<Executable>> iterable;
        private final CommonGeneratorImpl commonGenerator;
        private final Object object;
        private final String packageName;
        private final String className;
        private final String methodName;

        private IterableCodeImpl(
                Iterable<History<Executable>> iterable,
                @NotNull CommonGeneratorImpl commonGenerator,
                Object object,
                String packageName,
                String className,
                String methodName
        ) {
            this.iterable = iterable;
            this.commonGenerator = commonGenerator;
            this.object = object;
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return new IteratorCodeImpl(this);
        }
    }

    private static class IteratorCodeImpl implements Iterator<String> {
        private final IterableCodeImpl iterable;
        private final Iterator<History<Executable>> iterator;

        private IteratorCodeImpl(@NotNull IterableCodeImpl iterable) {
            this.iterable = iterable;
            iterator = iterable.iterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Contract(pure = true)
        @Override
        public @Nullable String next() {
            return iterable
                    .commonGenerator
                    .fileGenerator
                    .generate(
                            iterator.next(),
                            iterable.object,
                            iterable.packageName,
                            iterable.className,
                            iterable.methodName
                    )
                    .toString();
        }
    }
}
