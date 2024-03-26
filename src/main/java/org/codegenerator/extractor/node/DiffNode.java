package org.codegenerator.extractor.node;

import java.util.Set;

interface DiffNode extends Node {
    int diff(Node that, Set<Node> visited);

    int power(Set<Node> visited);
}
