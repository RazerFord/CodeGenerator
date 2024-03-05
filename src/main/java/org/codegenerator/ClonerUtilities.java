package org.codegenerator;

import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class ClonerUtilities {
    private ClonerUtilities() {
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
        Cloner cloner = new Cloner();
        cloner.unregisterFastCloner(ArrayList.class);
        cloner.registerFastCloner(ArrayList.class, new FastClonerArrayList());
        return cloner;
    }
}
