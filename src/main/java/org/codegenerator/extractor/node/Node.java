package org.codegenerator.extractor.node;

import java.lang.reflect.Field;
import java.util.Map;

public interface Node extends Map<Field, Node> {
    Class<?> getClassOfValue();

    Object getValue();

    void extract() throws IllegalAccessException;

    boolean isLeaf();
}
