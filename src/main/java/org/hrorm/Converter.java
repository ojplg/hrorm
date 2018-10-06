package org.hrorm;

/**
 * This interface describes mechanisms for translating objects from one
 * type to another and back.
 *
 * @param <CLASS> The type to be encoded by an implementation of this interface.
 * @param <CODE> The type that objects are encoded to.
 */
public interface Converter<CLASS,CODE> {

    CODE from(CLASS item);
    CLASS to(CODE code);

}
