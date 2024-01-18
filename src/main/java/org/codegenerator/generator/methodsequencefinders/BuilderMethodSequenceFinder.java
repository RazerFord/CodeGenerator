package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BuilderMethodSequenceFinder {
    public BuilderMethodSequenceFinder(@NotNull Class<?> clazz) {
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        return Collections.emptyList();
    }

    private void checkInvariants() {
    }
}
