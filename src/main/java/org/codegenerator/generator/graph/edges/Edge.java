package org.codegenerator.generator.graph.edges;

public interface Edge<T> {
    Object invoke(Object object);

    Object invoke();

    T getMethod();

    Object[] getArgs();
}
