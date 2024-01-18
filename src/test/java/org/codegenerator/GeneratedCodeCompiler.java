package org.codegenerator;

import org.jetbrains.annotations.NotNull;

import javax.tools.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneratedCodeCompiler {
    private final JavaCompiler javaCompiler;
    private final String outputDirectory;
    private final String classPathPrefix;
    private final String classNamePrefix;
    private final String methodName;

    public GeneratedCodeCompiler(
            String outputDirectory,
            String classPathPrefix,
            String classNamePrefix,
            String methodName
    ) {
        javaCompiler = ToolProvider.getSystemJavaCompiler();
        this.outputDirectory = outputDirectory;
        this.classPathPrefix = classPathPrefix;
        this.classNamePrefix = classNamePrefix;
        this.methodName = methodName;
    }

    @SuppressWarnings("unchecked")
    public <R> R createObject(String generatedClassName) {
        try {
            String absolutePathToClass = Paths.get(outputDirectory, classPathPrefix, generatedClassName + ".java").toAbsolutePath().normalize().toString();
            String className = classNamePrefix + generatedClassName;

            assertTrue(compile(outputDirectory, absolutePathToClass));

            Class<?> clazz = loadClass(outputDirectory, className);
            Object o = clazz.getConstructors()[0].newInstance();
            return (R) clazz.getMethod(methodName).invoke(o);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public boolean compile(String classPath, String absolutePathToClass) throws IOException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(diagnostics, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(absolutePathToClass));

            List<String> options = new ArrayList<>(Arrays.asList("-classpath", String.format("%s:%s", System.getProperty("java.class.path"), classPath)));
            JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);

            boolean success = task.call();
            if (!success) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    System.err.println(diagnostic.toString());
                }
            }
            return success;
        }
    }

    public Class<?> loadClass(String classPath, String className) throws IOException, ClassNotFoundException {
        try (URLClassLoader loader = new URLClassLoader(new URL[]{getUrl(classPath)})) {
            return loader.loadClass(className);
        }
    }


    private @NotNull URL getUrl(String classPath) throws MalformedURLException {
        return Paths.get(classPath).normalize().toUri().toURL();
    }
}
