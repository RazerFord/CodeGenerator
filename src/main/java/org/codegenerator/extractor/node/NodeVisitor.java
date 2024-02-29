package org.codegenerator.extractor.node;

public interface NodeVisitor {
    void visit(ArrayNode node);

    void visit(InnerNode node);

    void visit(Leaf node);
}
