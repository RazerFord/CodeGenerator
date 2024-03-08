package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.history.HistoryCall;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class CallCreator {
    private final UniqueMethodNameGenerator nameGenerator;
    private final Deque<MethodContext<Executable>> stack;

    public CallCreator(
            List<MethodSpec.Builder> methods,
            Deque<MethodContext<Executable>> stack
    ) {
        nameGenerator = new UniqueMethodNameGenerator(methods, stack);
        this.stack = stack;
    }

    CodeBlock create(HistoryCall<Executable> call) {
        return Utils.createCall(nameGenerator, stack, call);
    }
}
