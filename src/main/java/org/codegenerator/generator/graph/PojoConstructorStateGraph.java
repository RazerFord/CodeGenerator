package org.codegenerator.generator.graph;

import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.codegenerator.Utils.throwIf;

public class PojoConstructorStateGraph {
    private final EdgeGeneratorConstructor edgeGeneratorConstructor;

    public PojoConstructorStateGraph(Class<?> clazz) {
        edgeGeneratorConstructor = new EdgeGeneratorConstructor(clazz);
    }

    public @NotNull EdgeConstructor findPath(@NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper) {
        List<EdgeConstructor> edgeConstructors = edgeGeneratorConstructor.generate(assignableTypePropertyGrouper.get());
        return buildBeginObjectAndMethodCall(assignableTypePropertyGrouper.getObject(), edgeConstructors);
    }

    @SuppressWarnings("unused")
    public @NotNull EdgeConstructor findPath(Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        return findPath(assignableTypePropertyGrouper);
    }

    private @NotNull EdgeConstructor buildBeginObjectAndMethodCall(Object finalObject, @NotNull List<EdgeConstructor> edges) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);
        throwIf(edges.isEmpty(), new InvariantCheckingException(NO_CONSTRUCTORS));
        EdgeConstructor edgeConstructor = edges.get(0);
        Object currObject = edgeConstructor.invoke();
        Node currNode = ClassFieldExtractor.extract(currObject);
        for (int i = 1, length = edges.size(); i < length; i++) {
            EdgeConstructor tempEdgeConstructor = edges.get(i);
            Object tempObject = tempEdgeConstructor.invoke();
            Node tempNode = ClassFieldExtractor.extract(tempObject);
            if (finalNode.diff(currNode) > finalNode.diff(tempNode)) {
                edgeConstructor = tempEdgeConstructor;
                currNode = tempNode;
            }
        }
        return edgeConstructor;
    }

    private static final String NO_CONSTRUCTORS = "No constructors found";
}
