package org.codegenerator.generator.graph.edges;

import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

public interface Edge<T> {
    Object invoke(Object object);

    Object invoke();

    T getMethod();

    Object[] getArgs();

    JcMethod toJcMethod(@NotNull JcLookup<JcField, JcMethod> lookup);
}
