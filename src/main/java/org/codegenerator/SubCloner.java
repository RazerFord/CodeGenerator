package org.codegenerator;

import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class SubCloner extends Cloner {
    @Override
    protected void registerFastCloners() {
        super.registerFastCloners();
        unregisterFastCloner(ArrayList.class);
        registerFastCloner(ArrayList.class, new FastClonerArrayList());
    }

    public static class FastClonerArrayList implements IFastCloner {
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
            ArrayList al = (ArrayList) t;
            ArrayList l = new ArrayList();
            for (Object o : al) {
                l.add(cloner.deepClone(o, clones));
            }
            return l;
        }
    }

    public static @NotNull Cloner standard() {
        return new SubCloner();
    }
}
