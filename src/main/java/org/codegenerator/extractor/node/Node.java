package org.codegenerator.extractor.node;

import java.lang.reflect.Field;
import java.util.Map;

public interface Node extends Map<Object, Node> {
    Class<?> getClassOfValue();

    Object getValue();

    void extract() throws IllegalAccessException;

    NodeType nodeType();

    enum NodeType {
        ARRAY,
        INNER,
        LEAF,
    }
}
