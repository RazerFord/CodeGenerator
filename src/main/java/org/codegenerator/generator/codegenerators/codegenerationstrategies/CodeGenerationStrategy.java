package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;

import java.lang.reflect.Executable;
import java.util.Deque;

public interface CodeGenerationStrategy {
    CodeGenerationStrategy generate(
            TypeSpec.Builder typeBuilder,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    );
}
