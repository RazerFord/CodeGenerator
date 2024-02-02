package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.Call;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public class PipelineMethodSequenceFinder implements MethodSequenceFinder {
    private final List<? extends MethodSequenceFinder> methodSequenceFinderList;

    public PipelineMethodSequenceFinder(List<? extends MethodSequenceFinder> methodSequenceFinderList) {
        this.methodSequenceFinderList = methodSequenceFinderList;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object finalObject) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findBuildableList(finalObject);
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    @Override
    public List<Call<Executable>> findReflectionCalls(@NotNull Object finalObject) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findReflectionCalls(finalObject);
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }

    @Override
    public List<Call<JcMethod>> findJacoDBCalls(@NotNull Object finalObject) {
        for (MethodSequenceFinder methodSequenceFinder : methodSequenceFinderList) {
            try {
                return methodSequenceFinder.findJacoDBCalls(finalObject);
            } catch (Exception ignored) {
                // this code block is empty
            }
        }
        throw new MethodSequenceNotFoundException();
    }
}
