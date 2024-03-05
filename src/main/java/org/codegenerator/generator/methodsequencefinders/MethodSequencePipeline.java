package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * This interface provides functionality for finding methods that may have
 * been called during the lifetime of an object, using reflection and JacoDB.
 * To find methods, the {@link MethodSequenceFinder} registered in the
 * Pipeline will be iterated one by one.
 * The method search is completed if:
 * - Failed to find methods that were called on the object
 * - All {@link MethodSequenceFinder} have been tried
 */
public interface MethodSequencePipeline {
    /**
     * Finds a list of methods that may have been called on an object.
     *
     * @param targetObject object for which you want to find methods
     * @return possible life {@link History<Executable>} of the object
     */
    History<Executable> findReflectionCalls(@NotNull TargetObject targetObject);

    /**
     * Finds a list of methods that may have been called on an object.
     *
     * @param targetObject object for which you want to find methods
     * @return possible life {@link History<JcMethod>} of the object
     */
    History<JcMethod> findJacoDBCalls(@NotNull TargetObject targetObject);

    /**
     * Registers a finder for a {@code Class<?> clazz}
     *
     * @param clazz  {@link Class} for which you need to register a {@code finder}
     * @param finder for methods
     */
    void registerFinderForClass(Class<?> clazz, MethodSequenceFinder finder);

    /**
     * Resets memorized finders for specific classes.
     */
    void resetFindersForClasses();

    /**
     * Returns registered finders.
     *
     * @return registered finders
     */
    List<Function<TargetObject, ? extends MethodSequenceFinder>> finders();

    /**
     * Register finders.
     *
     * @param methodSequenceFinderList collection of finders that will be added
     */
    void register(Collection<Function<TargetObject, ? extends MethodSequenceFinder>> methodSequenceFinderList);

    /**
     * Register finder.
     *
     * @param methodSequenceFinder finder that will be added
     */
    void register(Function<TargetObject, ? extends MethodSequenceFinder> methodSequenceFinder);

    /**
     * Unregisters all finders.
     */
    void unregister();
}
