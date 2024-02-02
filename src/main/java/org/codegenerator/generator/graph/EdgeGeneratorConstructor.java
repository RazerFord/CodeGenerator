package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.EdgeGeneratorUtils.Node;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeGeneratorConstructor {
    private final Class<?> clazz;

    public EdgeGeneratorConstructor(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<EdgeConstructor> generate(Map<Class<?>, List<Object>> typeToValues) {
        typeToValues = new HashMap<>(typeToValues);
        List<EdgeConstructor> edges = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                edges.add(new EdgeConstructor(constructor));
            } else {
                List<Node> roots = EdgeGeneratorUtils.buildGraph(constructor, typeToValues);
                List<List<Node>> listArguments = EdgeGeneratorUtils.generatePossibleArguments(roots);
                for (List<Node> arguments : listArguments) {
                    edges.add(new EdgeConstructor(constructor, EdgeGeneratorUtils.extractArgs(arguments, typeToValues)));
                }
            }
        }
        return edges;
    }
}
