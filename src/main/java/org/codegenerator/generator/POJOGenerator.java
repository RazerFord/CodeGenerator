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
import java.util.stream.Stream;

import static org.codegenerator.Utils.*;

public class POJOGenerator<T> {
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
        List<Edge> pathNode = findPath(beginObject, object);
        generateCode(generateCodeBlocks(pathNode), path);
    }

    private @NotNull List<Edge> findPath(Object beginObject, Object finalObject) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);

        Set<Node> visited = new HashSet<>(Collections.singleton(finalNode));

        Queue<PathNode> queuePath = new ArrayDeque<>(Collections.singleton(new PathNode(null, null, 0)));
        Queue<Object> queue = new ArrayDeque<>(Collections.singleton(beginObject));

        PathNode finalPathNode = null;
        List<Edge> edges = generateEdges(finalNode);
        while (!queue.isEmpty()) {
            Object currentState = queue.poll();
            PathNode pathNode = Objects.requireNonNull(queuePath.poll());

            Node currentNode = ClassFieldExtractor.extract(currentState);
            if (currentNode.equals(finalNode)) {
                finalPathNode = pathNode;
                break;
            }
            if (visited.contains(currentNode)) {
                continue;
            }
            visited.add(currentNode);

            for (Edge edge : edges) {
                Object instance = copyObject(currentState);
                edge.invoke(instance);
                queue.add(instance);
                queuePath.add(new PathNode(pathNode, edge));
            }
        }
        if (finalPathNode == null) {
            return Collections.emptyList();
        }
        Deque<Edge> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edge != null) {
            path.addFirst(finalPathNode.edge);
            finalPathNode = finalPathNode.prevPathNode;
        }
        return new ArrayList<>(path);
    }

    private @NotNull List<CodeBlock> generateCodeBlocks(@NotNull List<Edge> edges) {
        List<CodeBlock> codeBlocks = new ArrayList<>();

        codeBlocks.add(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());

        for (Edge edge : edges) {
            Map<String, String> args = new HashMap<>();
            args.put("func0", edge.method.getName());
            int i = 1;
            Arrays.stream(edge.args).forEach(it -> args.put(String.format("arg%s", i), it.toString()));
            CodeBlock codeBlock = CodeBlock.builder().addNamed("object.$func0:L($arg1:L)", args).build();
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

    private static Map<Integer, List<List<Integer>>> generateCombinationsWithPermutations(int numberProperties, int maxArguments) {
        List<Integer> sequence = new ArrayList<>(numberProperties);
        for (int i = 0; i < numberProperties; i++) {
            sequence.add(i);
        }
        return combinationsWithPermutations(sequence, maxArguments).stream()
                .collect(Collectors.groupingBy(List::size, Collectors.toList()));
    }

    private static final String NO_CONSTRUCTOR_WITHOUT_ARG = "There is no constructor without arguments";
    private static final String THIS = "this";

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

    private static final class PathNode {
        private final PathNode prevPathNode;
        private final Edge edge;
        private final int depth;

        @Contract(pure = true)
        private PathNode(@NotNull PathNode prevPathNode, Edge edge) {
            this.prevPathNode = prevPathNode;
            this.edge = edge;
            depth = prevPathNode.depth + 1;
        }

        @Contract(pure = true)
        private PathNode(PathNode prevPathNode, Edge edge, int depth) {
            this.prevPathNode = prevPathNode;
            this.edge = edge;
            this.depth = depth;
        }
    }
}
