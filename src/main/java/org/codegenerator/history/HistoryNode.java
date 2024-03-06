package org.codegenerator.history;

import java.util.List;

/**
 * Wrapper over an object and a list of methods that may
 * have been called during its lifetime.
 *
 * @param <T> type of methods in {@link  HistoryCall<T>}
 */
public interface HistoryNode<T> {
    /**
     * Returns an object.
     *
     * @return object
     */
    HistoryNode<T> nextNode();

    /**
     * Returns an object.
     *
     * @return object
     */
    Object getObject();

    /**
     * Returns a list of methods that may have been called
     * during its existence.
     *
     * @return list of methods
     */
    List<HistoryCall<T>> getHistoryCalls();

    /**
     * The list that the getHistoryCalls method returns is not always complete.
     * Therefore, sometimes it is necessary to use reflection. Returns a list
     * of reflexive setters that can be used to set the state of an object.
     *
     * @return list of setters with the help of reflection
     */
    List<SetterUsingReflection<T>> getSetterUsingReflections();

    /**
     * @return {@link ItemType}
     */
    ItemType getType();

    /**
     * Returns the {@link Class} that created the {@link HistoryNode<T>}.
     *
     * @return {@link Class}
     */
    Class<?> getCreatorType();

    /**
     * Returns <tt>true</tt> if the object is Generic.
     *
     * @return <tt>true</tt> if the object is Generic
     */
    boolean isGeneric();
}
