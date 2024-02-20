package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import kotlin.Pair;
import org.codegenerator.generator.converters.PrimitiveConverter;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class Utils {
    private Utils() {
    }

    public static @NotNull CodeBlock createCall(
            UniqueMethodNameGenerator nameGenerator,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            @NotNull HistoryCall<Executable> call
    ) {
        Map<String, String> argsMap = new HashMap<>();
        StringJoiner fmt = new StringJoiner(", ", "(", ")");
        Object[] args = call.getArgs();

        for (int i = 0; i < args.length; i++) {
            String argFmt = String.format("arg%s", i);
            argsMap.put(argFmt, toRepresentation(nameGenerator, call.getHistoryArg(i), stack));
            fmt.add(String.format("$%s:L", argFmt));
        }
        return CodeBlock.builder()
                .addNamed(fmt.toString(), argsMap)
                .build();
    }

    public static String toRepresentation(
            UniqueMethodNameGenerator nameGenerator,
            @NotNull HistoryNode<Executable> node,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Object arg = node.getObject();
        if (arg == null) {
            return "null";
        } else if (PrimitiveConverter.canConvert(arg)) {
            return PrimitiveConverter.convert(arg);
        } else {
            Class<?> typeArg = arg.getClass();
            String methodName = nameGenerator.generate(typeArg);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC, STATIC)
                    .returns(typeArg);

            stack.add(new Pair<>(node, methodBuilder));
            return callMethod(methodName);
        }
    }

    @Contract(pure = true)
    private static @NotNull String callMethod(String methodName) {
        return methodName + "()";
    }
}
