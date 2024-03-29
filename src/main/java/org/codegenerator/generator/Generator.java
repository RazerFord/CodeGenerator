package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.nio.file.Path;

public interface Generator<T> {
    /**
     * Generates code with which you can get `finalObject`
     * and saves it in the `path` directory
     *
     * @param finalObject object for which you need to generateCode a sequence of methods
     * @param path        directory where you want to save the file
     * @throws IOException – if an I/O error occurs
     */
    void generateCode(@NotNull T finalObject, Path path) throws IOException;

    /**
     * Generates code with which you can get `finalObject`
     * and saves it in the `path` directory
     *
     * @param finalObject object for which you need to generateCode a sequence of methods
     * @param packageName the name of the package in which the generated code is placed
     * @param className   the name of the class into which the generated code is placed
     * @param methodName  name of the method that creates the object
     * @param path        directory where you want to save the file
     * @throws IOException – if an I/O error occurs
     */
    void generateCode(
            @NotNull T finalObject,
            String packageName,
            String className,
            String methodName,
            Path path
    ) throws IOException;

    /**
     * Generates code with which you can get `finalObject`
     * and saves it in the `path` directory
     *
     * @param finalObject object for which you need to generateCode a sequence of methods
     * @param className   the name of the class into which the generated code is placed
     * @param methodName  name of the method that creates the object
     * @param path        directory where you want to save the file
     * @throws IOException – if an I/O error occurs
     */
    void generateCode(
            @NotNull T finalObject,
            String className,
            String methodName,
            Path path
    ) throws IOException;

    /**
     * Finds methods that were called during the lifetime of objects.
     * Each method is represented by `Executable`
     *
     * @param finalObject object for which you need to find a sequence of methods
     * @return life history of the object
     */
    History<Executable> generateReflectionCalls(@NotNull T finalObject);

    /**
     * Finds methods that were called during the lifetime of objects.
     * Each method is represented by `JcMethod`
     *
     * @param finalObject object for which you need to find a sequence of methods
     * @return life history of the object
     */
    History<JcMethod> generateJacoDBCalls(@NotNull T finalObject);

    /**
     * Registers a finder of methods for the class
     *
     * @param clazz class
     * @param finder finder of methods
     */
    void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder);
}
