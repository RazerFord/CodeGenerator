package org.codegenerator.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.graph.Edge;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.*;

public class POJOGenerator<T> {
    private final Class<?> clazz;
    private final String dbname = POJOGenerator.class.getCanonicalName();
    private final StateGraph stateGraph;
    private final Constructor<?> defaultConstructor;
    private final Map<Integer, List<List<Integer>>> combinationsWithPermutations;

    @Contract(pure = true)
    public POJOGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        defaultConstructor = getConstructorWithoutArgs();
        throwIf(defaultConstructor == null, new RuntimeException(NO_CONSTRUCTOR_WITHOUT_ARG));

        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        int numberFields = clazz.getDeclaredFields().length;
        throwIf(maxArguments > numberFields, new RuntimeException(NUM_ARG_GREATER_THEN_NUM_FIELDS));

        combinationsWithPermutations = generateCombinationsWithPermutations(numberFields, maxArguments);
        stateGraph = new StateGraph();
    }

    public void generate(@NotNull T finalObject, Path path) {
        Object beginObject = callSupplierWrapper(defaultConstructor::newInstance);
        List<Edge> edges = generateEdges(ClassFieldExtractor.extract(finalObject));
        List<MethodCall> pathNode = stateGraph.findPath(beginObject, finalObject, edges, this::copyObject);
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
        if (methodCall.getArgs().length > 0) {
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

    private Object copyObject(Object o) {
        Object instance = callSupplierWrapper(defaultConstructor::newInstance);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            callRunnableWrapper(() -> field.set(instance, callSupplierWrapper(() -> field.get(o))));
        }
        return instance;
    }

    private @Nullable Constructor<?> getConstructorWithoutArgs() {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

    private @NotNull List<Edge> generateEdges(@NotNull Node node) {
        List<Edge> edges = new ArrayList<>();
        List<Map.Entry<Object, Node>> entries = new ArrayList<>(node.entrySet());

        for (Method method : clazz.getDeclaredMethods()) {
            edges.addAll(generateEdges(entries, method));
        }
        return edges;
    }

    private @NotNull List<Edge> generateEdges(@NotNull List<Map.Entry<Object, Node>> values, @NotNull Method method) {
        List<Edge> edges = new ArrayList<>();
        List<List<Integer>> sequences = combinationsWithPermutations.getOrDefault(method.getParameterCount(), Collections.emptyList());
        Class<?>[] argsTypes = new Class[method.getParameterCount()];
        for (List<Integer> sequence : sequences) {
            Object[] args = new Object[method.getParameterCount()];
            int j = 0;
            for (int i : sequence) {
                argsTypes[j] = ((Field) values.get(i).getKey()).getType();
                args[j++] = values.get(i).getValue().getValue();
            }
            if (equalsArgs(argsTypes, method.getParameterTypes())) {
                edges.add(new Edge(method, args));
            }
        }
        return edges;
    }

    @Contract(pure = true)
    private boolean equalsArgs(Class<?> @NotNull [] l, Class<?> @NotNull [] r) {
        if (l.length != r.length) {
            return false;
        }
        for (int i = 0; i < l.length; i++) {
            if (l[i] != r[i]) {
                return false;
            }
        }
        return true;
    }

    private static Map<Integer, List<List<Integer>>> generateCombinationsWithPermutations(int numberProperties, int maxArguments) {
        List<Integer> sequence = new ArrayList<>(numberProperties);
        for (int i = 0; i < numberProperties; i++) {
            sequence.add(i);
        }
        return combinationsWithPermutations(sequence, maxArguments).stream()
                .collect(Collectors.groupingBy(List::size, Collectors.toList()));
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
    private static final String NO_CONSTRUCTOR_WITHOUT_ARG = "There is no constructor without arguments";
    private static final String NUM_ARG_GREATER_THEN_NUM_FIELDS = "The number of arguments is greater than the number of fields";
}
