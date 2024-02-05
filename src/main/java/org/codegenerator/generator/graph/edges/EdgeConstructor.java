package org.codegenerator.generator.graph.edges;

import org.codegenerator.Utils;
import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import static org.codegenerator.Utils.callSupplierWrapper;

public class EdgeConstructor implements Edge<Constructor<?>> {
    private final Constructor<?> constructor;
    private final Object[] args;

    public EdgeConstructor(Constructor<?> constructor, Object... args) {
        this.constructor = constructor;
        this.args = args;
    }

    @Override
    public Object invoke(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke() {
        return callSupplierWrapper(() -> constructor.newInstance(args));
    }

    @Override
    public Constructor<?> getMethod() {
        return constructor;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public JcMethod toJcMethod(@NotNull JcLookup<JcField, JcMethod> lookup) {
        return lookup.method("<init>", Utils.buildDescriptor(constructor));
    }
}
