package org.codegenerator.generator.graph;

import org.codegenerator.CustomLogger;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.codegenerator.CommonUtils.throwIf;

public class LazyConstructorGraph {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final EdgeGenerator edgeGenerator = new EdgeGenerator();

    public @NotNull EdgeConstructor findPath(@NotNull TargetObject targetObject) {
        List<EdgeConstructor> edgeConstructors = edgeGenerator.generate(
                targetObject.getClazz().getConstructors(),
                targetObject.get()
        );
        return buildBeginObjectAndConstructorCall(targetObject.getObject(), edgeConstructors);
    }

    private @NotNull EdgeConstructor buildBeginObjectAndConstructorCall(
            Object finalObject,
            @NotNull List<EdgeConstructor> edges
    ) {
        throwIf(edges.isEmpty(), new InvariantCheckingException(NO_CONSTRUCTORS));

        Node finalNode = ClassFieldExtractor.extract(finalObject);
        EdgeConstructor edgeConstructor = null;
        Node currNode = null;

        int start = 0;
        for (int size = edges.size(); start < size; start++) {
            try {
                edgeConstructor = edges.get(start);
                Object tempObject = edgeConstructor.invoke();
                currNode = ClassFieldExtractor.extract(tempObject);
                start++;
                break;
            } catch (Exception e) {
                LOGGER.warning(edges.get(start).getMethod().getName());
            }
        }

        for (int size = edges.size(); start < size; start++) {
            try {
                Object tempObject = edges.get(start).invoke();
                Node tempNode = ClassFieldExtractor.extract(tempObject);

                if (finalNode.diff(currNode) > finalNode.diff(tempNode)) {
                    edgeConstructor = edges.get(start);
                    currNode = tempNode;
                }
            } catch (Exception e) {
                LOGGER.warning(edges.get(start).getMethod().getName());
            }
        }
        return Objects.requireNonNull(edgeConstructor, NO_CONSTRUCTORS);
    }

    private static final String NO_CONSTRUCTORS = "No constructors found";
}
