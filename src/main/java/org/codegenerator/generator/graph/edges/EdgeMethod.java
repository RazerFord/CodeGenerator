package org.codegenerator.generator.graph.edges;

import com.rits.cloning.Cloner;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.CommonUtils;
import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

import static org.codegenerator.CommonUtils.callSupplierWrapper;

public final class EdgeMethod implements Edge<Method> {
    private final Cloner cloner = ClonerUtilities.standard();
    private final Method method;
    private final Object[] args;

    public EdgeMethod(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    @Override
    public Object invoke(Object object) {
        Object[] cloneArgs = cloner.deepClone(args);
        return callSupplierWrapper(() -> method.invoke(object, cloneArgs));
    }

    @Override
    public Object invoke() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public JcMethod toJcMethod(@NotNull JcLookup<JcField, JcMethod> lookup) {
        return lookup.method(method.getName(), CommonUtils.buildDescriptor(method));
    }
}
