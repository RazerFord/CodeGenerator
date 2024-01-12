package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.EdgeGeneratorUtils.Node;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeGeneratorConstructor {
    private final Class<?> clazz;

    public EdgeGeneratorConstructor(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<Edge> generate(Map<Class<?>, List<Object>> typeToValues) {
        typeToValues = new HashMap<>(typeToValues);
        List<Edge> edges = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            List<Node> roots = EdgeGeneratorUtils.buildGraph(constructor, typeToValues);
            List<List<Node>> listArguments = EdgeGeneratorUtils.generatePossibleArguments(roots);
            for (List<Node> arguments : listArguments) {
                // TODO replace it to constructor
                edges.add(new Edge(((Method) null), EdgeGeneratorUtils.extractArgs(arguments, typeToValues)));
            }
        }
        return edges;
    }
}
