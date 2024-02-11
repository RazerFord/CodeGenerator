package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.converters.Converter;
import org.codegenerator.generator.converters.ConverterPrimitiveTypesAndString;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;

import static javax.lang.model.element.Modifier.*;

public class FileGenerator {
    private final Converter converterPrimitive = new ConverterPrimitiveTypesAndString();
    private final String packageName;
    private final String className;
    private final String methodName;

    public FileGenerator(String packageName, String className, String methodName) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
    }

    public JavaFile generate(@NotNull History<Executable> history, @NotNull Object source) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(source.getClass());

        process(history, typeBuilder, methodBuilder, source);

        TypeSpec type = typeBuilder.build();
        return JavaFile.builder(packageName, type).build();
    }

    private void process(
            @NotNull History<Executable> history,
            TypeSpec.Builder typeBuilder,
            MethodSpec.Builder methodBuilder,
            Object source
    ) {
        Deque<Object> stackObject = new ArrayDeque<>(Collections.singleton(source));
        Deque<MethodSpec.Builder> stackMethod = new ArrayDeque<>(Collections.singleton(methodBuilder));

        HistoryNode<Executable> historyObject = null;
        methodBuilder = null;

        State state = State.BEGIN_METHOD;
        while (!(state == State.BEGIN_METHOD && (stackObject.isEmpty() || stackMethod.isEmpty()))) {
            switch (state) {
                case BEGIN_METHOD:
                    historyObject = history.get(stackObject.poll());
                    methodBuilder = stackMethod.poll();

                    state = State.PROCESSING_BODY;
                    break;
                case PROCESSING_BODY:
                    assert historyObject != null && methodBuilder != null;
                    Object object = historyObject.getObject();
                    Class<?> typeObject = object.getClass();

                    switch (historyObject.getType()) {
                        case OBJECT: {
                            List<HistoryCall<Executable>> calls = historyObject.getHistoryCalls();
                            String variableName = "object";
                            for (HistoryCall<Executable> call : calls) {
                                Executable executable = call.getMethod();
                                CodeBlock codeBlock;
                                if (executable instanceof Constructor<?>) {
                                    codeBlock = CodeBlock.builder()
                                            .add("$1T $2L = new $1T", typeObject, variableName)
                                            .add(createCall(typeBuilder, stackMethod, stackObject, methodBuilder, call.getArgs()))
                                            .build();
                                } else if (executable instanceof Method) {
                                    codeBlock = CodeBlock.builder()
                                            .add("$L.$L", variableName, executable.getName())
                                            .add(createCall(typeBuilder, stackMethod, stackObject, methodBuilder, call.getArgs()))
                                            .build();
                                } else {
                                    throw new IllegalStateException();
                                }
                                methodBuilder.addStatement(codeBlock);
                            }
                            methodBuilder.addStatement(
                                    "return $L",
                                    variableName
                            );
                            break;
                        }
                        case ARRAY: {
                            String variableName = "array";
                            Class<?> componentType = typeObject.getComponentType();
                            int deep = StringUtils.countMatches(componentType.getName(), "[");
                            Class<?> typeArray = getArrayType(typeObject);

                            CodeBlock codeBlock = createCodeBlockOfCreatingVariable(variableName, typeObject, typeArray, Array.getLength(object), deep);
                            methodBuilder.addStatement(codeBlock);
                            for (int i = 0, length = Array.getLength(object); i < length; i++) {
                                Object element = Array.get(object, i);
                                String call = createCallRecursively(componentType, element, typeBuilder, stackMethod, stackObject);
                                methodBuilder.addStatement("$L[$L] = $L", variableName, i, call);
                            }
                            methodBuilder.addStatement("return $L", variableName);
                            break;
                        }
                        case PRIMITIVE: {
                            methodBuilder.addStatement(
                                    "return $L",
                                    converterPrimitive.convert(historyObject.getObject(), typeBuilder, methodBuilder)
                            );
                            break;
                        }
                    }
                    typeBuilder.addMethod(methodBuilder.build());
                    state = State.BEGIN_METHOD;
                    break;
                case PROCESSING_ARG:
                    break;
            }
        }
    }

    private @NotNull CodeBlock createCall(
            TypeSpec.Builder typeBuilder,
            Deque<MethodSpec.Builder> stackMethod,
            Deque<Object> stackObject,
            MethodSpec.Builder ignored,
            Object @NotNull ... args
    ) {
        Map<String, String> argumentMap = new HashMap<>();
        StringBuilder format = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            String argFormat = String.format("arg%s", i);
            if (converterPrimitive.canConvert(args[i])) {
                argumentMap.put(argFormat, converterPrimitive.convert(args[i], typeBuilder, ignored));
            } else if (args[i] == null) {
                argumentMap.put(argFormat, "null");
            } else {
                String myMethodName = String.format("createVar%s", typeBuilder.methodSpecs.size() + stackMethod.size());
                argumentMap.put(argFormat, myMethodName + "()");
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(myMethodName).addModifiers(PUBLIC, STATIC).returns(args[i].getClass());
                stackMethod.add(methodBuilder);
                stackObject.add(args[i]);
            }
            format.append(String.format("$%s:L,", argFormat));
        }
        if (args.length > 0) {
            format.setCharAt(format.length() - 1, ')');
        } else {
            format.append(")");
        }
        return CodeBlock.builder().addNamed(format.toString(), argumentMap).build();
    }

    private Class<?> getArrayType(Class<?> clazz) {
        Class<?> typeArray = null;
        for (; clazz != null; clazz = clazz.getComponentType()) {
            typeArray = clazz;
        }
        return typeArray;
    }

    private @NotNull CodeBlock createCodeBlockOfCreatingVariable(
            String variableName,
            Class<?> typeVariable,
            Class<?> typeArray,
            int length,
            int deep
    ) {
        return CodeBlock.builder()
                .add(
                        "$T $L = new $T[$L]$L",
                        typeVariable,
                        variableName,
                        typeArray,
                        length,
                        StringUtils.repeat("[]", deep)
                )
                .build();
    }

    @Contract(pure = true)
    private @Nullable String createCallRecursively(
            @NotNull Class<?> componentType,
            Object element,
            TypeSpec.Builder typeBuilder,
            @NotNull Deque<MethodSpec.Builder> stackMethod,
            @NotNull Deque<Object> stackObject
    ) {
        if (element == null) {
            return "null";
        } else if (converterPrimitive.canConvert(element)) {
            return converterPrimitive.convert(element, typeBuilder, null);
        } else {
            String myMethodName = "createArray" + (typeBuilder.methodSpecs.size() + stackMethod.size());
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(myMethodName).addModifiers(PUBLIC, STATIC).returns(componentType);
            stackObject.add(element);
            stackMethod.add(methodBuilder);
            return myMethodName + "()";
        }
    }

    enum State {
        BEGIN_METHOD,
        PROCESSING_BODY,
        PROCESSING_ARG,
    }

    enum Arg {
        PROCESSED,
        DONE,
    }
}

/*
class GeneratedClass {
    public static Object generate() {
        A a = new A();
        a.set(createArray());
        a.set(createPojo());
        AB ab = a.build();
        try {
          ..
        } catch(IE ) {}
        return ab
    }
}
*/