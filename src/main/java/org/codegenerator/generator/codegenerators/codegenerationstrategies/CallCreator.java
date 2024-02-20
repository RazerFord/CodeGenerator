package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import kotlin.Pair;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class CallCreator {
    private final UniqueMethodNameGenerator nameGenerator;
    private final Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;

    public CallCreator(
            List<MethodSpec.Builder> methods,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        nameGenerator = new UniqueMethodNameGenerator(methods, stack);
        this.stack = stack;
    }

    CodeBlock create(HistoryCall<Executable> call) {
        return Utils.createCall(nameGenerator, stack, call);
    }
}
