package org.codegenerator.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.*;

public class POJOGenerator<T> {
    private static final String THIS = "this";
    private final Class<?> clazz;
    private final Constructor<?> defaultConstructor;
    private final Method[] methods;
    private final Map<Integer, List<List<Integer>>> combinationsWithPermutations;
    private final String dbname = POJOGenerator.class.getCanonicalName();

    @Contract(pure = true)
    public POJOGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        defaultConstructor = getConstructorWithoutArgs();
        methods = clazz.getDeclaredMethods();
        throwIf(defaultConstructor == null, new RuntimeException(NO_CONSTRUCTOR_WITHOUT_ARG));
        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        combinationsWithPermutations = generateCombinationsWithPermutations(clazz.getDeclaredFields().length, maxArguments);
    }

    public void generate(@NotNull T object, Path path) {
        Object beginObject = callSupplierWrapper(defaultConstructor::newInstance);
        findPath(beginObject, object);
        //        Map<String, JcMethod> setters = new HashMap<>();
        //        extractClassOrInterface(setters);
        //        Map<String, String> currentFieldValues = getCurrentFieldValues(object);
        //        generateCode(generateCodeBlocks(currentFieldValues, setters), path);
    }

    public void findPath(Object beginObject, Object finalObject) {
        Node finalState = ClassFieldExtractor.extract(finalObject);

        Set<Object> visited = new HashSet<>();
        Queue<Object> queue = new ArrayDeque<>();
        queue.add(beginObject);

        List<Edge> edges = generateEdges(finalState);
        while (!queue.isEmpty()) {
            Object currentState = queue.poll();

            if (visited.contains(currentState)) {
                continue;
            }
            visited.add(currentState);

            for (Edge edge : edges) {
                edge.invoke(currentState);
            }
        }
    }

    private @NotNull List<CodeBlock> generateCodeBlocks(@NotNull Map<String, String> currentFieldValues, Map<String, JcMethod> setters) {
        List<CodeBlock> codeBlocks = new ArrayList<>();

        codeBlocks.add(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());

        for (Map.Entry<String, String> entry : currentFieldValues.entrySet()) {
            CodeBlock codeBlock = CodeBlock.builder().add("object.$L($L)", setters.get(entry.getKey()).getName(), entry.getValue()).build();
            codeBlocks.add(codeBlock);
        }
        return codeBlocks;
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

            // извлекаем список полей объекта
            Set<String> fields = new HashSet<>();
            jcClassOrInterface.getDeclaredFields().forEach(it -> fields.add(String.join(".", THIS, it.getName())));

            // затем провеяем методы с помощью jacodb, на наличие инструкций на присваивание значений - это будет сеттер. (this.? = ?)
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
        } catch (IOException | ExecutionException | InterruptedException | URISyntaxException e) {
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

    private @NotNull List<Edge> generateEdges(@NotNull Node node) {
        List<Edge> edges = new ArrayList<>();
        List<Map.Entry<Object, Node>> entries = new ArrayList<>(node.entrySet());

        for (Method method : methods) {
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

    private @Nullable Constructor<?> getConstructorWithoutArgs() {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

    private static Map<Integer, List<List<Integer>>> generateCombinationsWithPermutations(int numberProperties, int maxArguments) {
        List<Integer> sequence = new ArrayList<>(numberProperties);
        for (int i = 0; i < numberProperties; i++) {
            sequence.add(i);
        }
        return combinationsWithPermutations(sequence, maxArguments).stream()
                .collect(Collectors.groupingBy(List::size, Collectors.toList()));
    }

    private static final String NO_CONSTRUCTOR_WITHOUT_ARG = "There is no constructor without arguments";
    private static final int MAX_ARGS = 5;

    private static final class Edge {
        private final Method method;
        private final Object[] args;

        private Edge(Method method, Object... args) {
            this.method = method;
            this.args = args;
        }

        private Object invoke(Object object) {
            return callSupplierWrapper(() -> method.invoke(object, args));
        }
    }
}
