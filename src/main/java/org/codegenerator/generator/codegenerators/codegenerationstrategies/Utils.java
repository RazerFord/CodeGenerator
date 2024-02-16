package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.converters.ConverterPrimitiveTypesAndString;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class Utils {
    private Utils() {
    }

    public static @NotNull CodeBlock createCall(
            String methodNameSuffix,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            @NotNull HistoryCall<Executable> call
    ) {
        Map<String, String> argsMap = new HashMap<>();
        StringBuilder fmt = new StringBuilder("(");
        Object[] args = call.getArgs();

        for (int i = 0; i < args.length; i++) {
            String argFmt = String.format("arg%s", i);
            argsMap.put(argFmt, toRepresentation(methodNameSuffix, call.getHistoryArg(i), stack));
            fmt.append(String.format("$%s:L,", argFmt));
        }
        if (args.length > 0) fmt.setCharAt(fmt.length() - 1, ')');
        else fmt.append(")");

        return CodeBlock.builder()
                .addNamed(fmt.toString(), argsMap)
                .build();
    }

    public static String toRepresentation(
            String methodNameSuffix,
            @NotNull HistoryNode<Executable> node,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Object arg = node.getObject();
        if (arg == null) {
            return "null";
        } else if (ConverterPrimitiveTypesAndString.INSTANCE.canConvert(arg)) {
            return ConverterPrimitiveTypesAndString.convert(arg);
        } else {
            Class<?> typeArg = arg.getClass();
            String methodName = createNewMethodName(typeArg, methodNameSuffix);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC, STATIC)
                    .returns(typeArg);

            stack.add(new Pair<>(node, methodBuilder));
            return callMethod(methodName);
        }
    }

    public static @NotNull String createNewMethodName(@NotNull Class<?> clazz, String methodNameSuffix) {
        String simpleName = clazz.getSimpleName();
        if (clazz.isArray()) {
            return String.format("createArray%s%s", StringUtils.capitalize(simpleName.replaceAll("(\\[])", "")), methodNameSuffix);
        }
        return String.format("create%s%s", StringUtils.capitalize(simpleName), methodNameSuffix);
    }

    @Contract(pure = true)
    private static @NotNull String callMethod(String methodName) {
        return methodName + "()";
    }
}
