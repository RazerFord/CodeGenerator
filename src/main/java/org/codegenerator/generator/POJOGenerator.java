package org.codegenerator.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.graph.StateGraph;
import org.jacodb.api.JcClassOrInterface;
import org.jacodb.api.JcDatabase;
import org.jacodb.api.JcMethod;
import org.jacodb.api.cfg.JcAssignInst;
import org.jacodb.api.cfg.JcFieldRef;
import org.jacodb.api.cfg.JcInst;
import org.jacodb.api.cfg.JcInstList;
import org.jacodb.impl.JacoDB;
import org.jacodb.impl.JcSettings;
import org.jacodb.impl.features.InMemoryHierarchy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class POJOGenerator<T> {
    private final Class<?> clazz;
    private final String dbname = POJOGenerator.class.getCanonicalName();
    private final StateGraph stateGraph;

    @Contract(pure = true)
    public POJOGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        stateGraph = new StateGraph(clazz);
    }

    public void generate(@NotNull T object, Path path) {
        List<MethodCall> pathNode = stateGraph.findPath(object);
        generateCode(generateCodeBlocks(pathNode), path);
    }

    private @NotNull List<CodeBlock> generateCodeBlocks(@NotNull List<MethodCall> methodCalls) {
        List<CodeBlock> codeBlocks = new ArrayList<>();

        codeBlocks.add(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());

        for (MethodCall methodCall : methodCalls) {
            codeBlocks.add(generateCodeBlock(methodCall));
        }
        return codeBlocks;
    }

    private @NotNull CodeBlock generateCodeBlock(@NotNull MethodCall methodCall) {
        Map<String, String> args = new HashMap<>();
        args.put(PREFIX_METHOD, methodCall.getMethod().getName());
        StringBuilder format = new StringBuilder("object.$func:L");
        format.append("(");
        Object[] methodArgs = methodCall.getArgs();
        for (int i = 0; i < methodCall.getArgs().length; i++) {
            String argFormat = String.format("%s%s", PREFIX_ARG, i);
            args.put(argFormat, methodArgs[i].toString());
            format.append(String.format("$%s:L,", argFormat));
        }
        if (format.charAt(format.length() - 1) == ','){
            format.setCharAt(format.length() - 1, ')');
        }
        return CodeBlock.builder().addNamed(format.toString(), args).build();
    }

    private void generateCode(@NotNull List<CodeBlock> codeBlocks, Path path) {
        MethodSpec.Builder mainBuilder = MethodSpec.methodBuilder("main")
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args");

        codeBlocks.forEach(mainBuilder::addStatement);

        MethodSpec main = mainBuilder.build();

        TypeSpec generatedClass = TypeSpec.classBuilder("GeneratedClass")
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("generatedclass", generatedClass)
                .build();

        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractClassOrInterface(Map<String, JcMethod> setters) {
        try (JcDatabase db = loadOrCreateDataBase(dbname)) {
            List<File> fileList = Collections.singletonList(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            db.asyncLoad(fileList).get();
            JcClassOrInterface jcClassOrInterface = db.asyncClasspath(fileList).get().findClassOrNull(clazz.getTypeName());

            if (jcClassOrInterface == null) {
                throw new RuntimeException();
            }

            Set<String> fields = new HashSet<>();
            jcClassOrInterface.getDeclaredFields().forEach(it -> fields.add(String.join(".", THIS, it.getName())));

            for (JcMethod jcMethod : jcClassOrInterface.getDeclaredMethods()) {
                JcInstList<JcInst> instructions = jcMethod.getInstList();
                for (JcInst inst : instructions) {
                    if (inst instanceof JcAssignInst &&
                            fields.contains(((JcAssignInst) inst).getLhv().toString()) &&
                            ((JcAssignInst) inst).getLhv() instanceof JcFieldRef
                    ) {
                        String name = ((JcFieldRef) ((JcAssignInst) inst).getLhv()).getField().getName();
                        setters.put(name, jcMethod);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private JcDatabase loadOrCreateDataBase(String dbname) throws ExecutionException, InterruptedException {
        return JacoDB.async(new JcSettings()
                .useProcessJavaRuntime()
                .persistent(dbname)
                .installFeatures(InMemoryHierarchy.INSTANCE)
        ).get();
    }

    private static final String THIS = "this";
    private static final String PREFIX_METHOD = "func";
    private static final String PREFIX_ARG = "arg";
}
