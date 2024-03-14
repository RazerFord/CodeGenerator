package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Path;

/**
 * An interface for generating code that can be used to retrieve the requested object.
 */
public interface Generator {
    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object object for which you need to generateCode a sequence of methods
     * @param path   directory where you want to save the file
     * @throws IOException if an I/O error occurs
     */
    void generateCode(@NotNull Object object, Path path) throws IOException;

    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object     object for which you need to generateCode a sequence of methods
     * @param className  the name of the class into which the generated code is placed
     * @param methodName name of the method that creates the object
     * @param path       directory where you want to save the file
     * @throws IOException if an I/O error occurs
     */
    void generateCode(
            @NotNull Object object,
            String className,
            String methodName,
            Path path
    ) throws IOException;

    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object      object for which you need to generateCode a sequence of methods
     * @param packageName the name of the package in which the generated code is placed
     * @param className   the name of the class into which the generated code is placed
     * @param methodName  name of the method that creates the object
     * @param path        directory where you want to save the file
     * @throws IOException if an I/O error occurs
     */
    void generateCode(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName,
            Path path
    ) throws IOException;

    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object     object for which you need to generateCode a sequence of methods
     * @param appendable object to which the file is written
     * @throws IOException if an I/O error occurs
     */
    void generateCode(@NotNull Object object, Appendable appendable) throws IOException;

    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object     object for which you need to generateCode a sequence of methods
     * @param className  the name of the class into which the generated code is placed
     * @param methodName name of the method that creates the object
     * @param appendable object to which the file is written
     * @throws IOException if an I/O error occurs
     */
    void generateCode(
            @NotNull Object object,
            String className,
            String methodName,
            Appendable appendable
    ) throws IOException;

    /**
     * Generates code with which you can get `object`
     * and saves it in the `path` directory.
     *
     * @param object      object for which you need to generateCode a sequence of methods
     * @param packageName the name of the package in which the generated code is placed
     * @param className   the name of the class into which the generated code is placed
     * @param methodName  name of the method that creates the object
     * @param appendable  object to which the file is written
     * @throws IOException if an I/O error occurs
     */
    void generateCode(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName,
            Appendable appendable
    ) throws IOException;

    /**
     * Creates an iterable instance.
     * Each iteration object represents code that can be used to create
     * the object that comes in as input.
     *
     * @param object object for which you need to generateCode a sequence of methods
     */
    Iterable<String> iterableCode(@NotNull Object object);

    /**
     * Creates an iterable instance.
     * Each iteration object represents code that can be used to create
     * the object that comes in as input.
     *
     * @param object     object for which you need to generateCode a sequence of methods
     * @param className  the name of the class into which the generated code is placed
     * @param methodName name of the method that creates the object
     */
    Iterable<String> iterableCode(
            @NotNull Object object,
            String className,
            String methodName
    );

    /**
     * Creates an iterable instance.
     * Each iteration object represents code that can be used to create
     * the object that comes in as input.
     *
     * @param object      object for which you need to generateCode a sequence of methods
     * @param packageName the name of the package in which the generated code is placed
     * @param className   the name of the class into which the generated code is placed
     * @param methodName  name of the method that creates the object
     */
    Iterable<String> iterableCode(
            @NotNull Object object,
            String packageName,
            String className,
            String methodName
    );

    /**
     * Creates an iterable instance.
     * Each iteration object represents the {@link History<Executable>} of
     * the object received as input.
     *
     * @param object object for which you need to generate {@link History<Executable>}
     */
    Iterable<History<Executable>> iterableReflectionCalls(@NotNull Object object);

    /**
     * Creates an iterable instance.
     * Each iteration object represents the {@link History<JcMethod>} of
     * the object received as input.
     *
     * @param object object for which you need to generate {@link History<JcMethod>}
     */
    Iterable<History<JcMethod>> iterableJacoDBCalls(@NotNull Object object);

    /**
     * Finds methods that were called during the lifetime of objects.
     * Each method is represented by {@link Executable}.
     *
     * @param object object for which you need to find a sequence of methods
     * @return life history of the object
     */
    History<Executable> generateReflectionCalls(@NotNull Object object);

    /**
     * Finds methods that were called during the lifetime of objects.
     * Each method is represented by {@link JcMethod}.
     *
     * @param object object for which you need to find a sequence of methods
     * @return life history of the object
     */
    History<JcMethod> generateJacoDBCalls(@NotNull Object object);

    /**
     * Registers a finder of methods for the class.
     *
     * @param clazz  class
     * @param finder finder of methods
     */
    void registerFinder(Class<?> clazz, MethodSequenceFinder finder);
}
