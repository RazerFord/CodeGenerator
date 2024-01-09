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

public class GeneratedCodeCompiler {
    private final JavaCompiler javaCompiler;

    public GeneratedCodeCompiler() {
        javaCompiler = ToolProvider.getSystemJavaCompiler();
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
