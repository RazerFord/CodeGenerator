package org.codegenerator.generator.graph;

import org.codegenerator.CommonUtils;
import org.codegenerator.CustomLogger;
import org.codegenerator.exceptions.InvariantCheckingException;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.codegenerator.CommonUtils.throwIf;

public class LazyConstructorGraph {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final EdgeGenerator edgeGenerator = new EdgeGenerator();

    public @NotNull EdgeConstructor findPath(@NotNull TargetObject to) {
        List<EdgeConstructor> edgeConstructors = edgeGenerator.generate(to.getClazz().getConstructors(), to.get());
        edgeConstructors.sort(Comparator.comparingInt(em -> em.getArgs().length));
        return buildBeginObjectAndConstructorCall(to.getObject(), edgeConstructors);
    }

    private @NotNull EdgeConstructor buildBeginObjectAndConstructorCall(
            Object finalObject,
            @NotNull List<EdgeConstructor> edges
    ) {
        throwIf(edges.isEmpty(), new InvariantCheckingException(NO_CONSTRUCTORS));

        Node finalNode = ClassFieldExtractor.extract(finalObject);

        EdgeConstructor ec = null;
        Node currNode = null;
        for (EdgeConstructor tempEc : edges) {
            try {
                Object tempObject = tempEc.invoke();
                Node tempNode = ClassFieldExtractor.extract(tempObject);

                if (ec == null || finalNode.diff(currNode) > finalNode.diff(tempNode)) {
                    ec = tempEc;
                    currNode = tempNode;
                }
            } catch (Exception e) {
                LOGGER.warning(CommonUtils.message(tempEc));
            }
        }
        return Objects.requireNonNull(ec, NO_CONSTRUCTORS);
    }

    private static final String NO_CONSTRUCTORS = "No constructors found";
}
