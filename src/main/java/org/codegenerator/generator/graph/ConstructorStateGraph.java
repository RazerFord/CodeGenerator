package org.codegenerator.generator.graph;

import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.codegenerator.Utils.throwIf;

public class ConstructorStateGraph {
    private final EdgeGenerator edgeGenerator = new EdgeGenerator();

    public @NotNull EdgeConstructor findPath(@NotNull TargetObject targetObject) {
        List<EdgeConstructor> edgeConstructors = edgeGenerator.generate(
                targetObject.getClazz().getConstructors(),
                targetObject.get()
        );
        return buildBeginObjectAndConstructorCall(targetObject.getObject(), edgeConstructors);
    }

    @SuppressWarnings("unused")
    public @NotNull EdgeConstructor findPath(Object finalObject) {
        TargetObject targetObject = new TargetObject(finalObject);
        return findPath(targetObject);
    }

    private @NotNull EdgeConstructor buildBeginObjectAndConstructorCall(Object finalObject, @NotNull List<EdgeConstructor> edges) {
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
