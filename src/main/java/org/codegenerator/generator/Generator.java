package org.codegenerator.generator;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface Generator<T> {
    void generate(@NotNull T finalObject, Path path);
}
