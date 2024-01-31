package org.codegenerator.generator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface Generator<T> {
    /**
     * Generates code with which you can get `finalObject`
     * and saves it in the `path` directory
     *
     * @param finalObject object for which you need to generate a sequence of methods
     * @param path directory where you want to save the file
     * @throws IOException – if an I/O error occurs
     */
    void generate(@NotNull T finalObject, Path path) throws IOException;
}
