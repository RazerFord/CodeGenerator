package org.codegenerator.generator.graph;

import org.jetbrains.annotations.NotNull;
import org.codegenerator.generator.graph.EdgeGeneratorUtils.Node;

import java.lang.reflect.Method;
import java.util.*;

public class EdgeGeneratorMethod {
    private final Class<?> clazz;

    public EdgeGeneratorMethod(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<EdgeMethod> generate(Map<Class<?>, List<Object>> typeToValues) {
        typeToValues = new HashMap<>(typeToValues);
        List<EdgeMethod> edgeMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            List<Node> roots = EdgeGeneratorUtils.buildGraph(method, typeToValues);
            List<List<Node>> listArguments = EdgeGeneratorUtils.generatePossibleArguments(roots);
            for (List<Node> arguments : listArguments) {
                edgeMethods.add(new EdgeMethod(method, EdgeGeneratorUtils.extractArgs(arguments, typeToValues)));
            }
        }
        return edgeMethods;
    }
}
