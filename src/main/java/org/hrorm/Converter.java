package org.hrorm;

/**
 * This interface describes mechanisms for translating objects from one
 * type to another and back.
 *
 * @param <CLASS> The type to be encoded by an implementation of this interface.
 * @param <CODE> The type that objects are encoded to.
 */
public interface Converter<CLASS,CODE> {

    /**
     * Convert <b>from</b> the supported class into its encoded value
     *
     * @param item the object to encode
     * @return the encoded value
     */
    CODE from(CLASS item);

    /**
     * Convert an encoded value <b>to</b> the supported class
     *
     * @param code the encoded value
     * @return an instance of the class matching the encoding
     */
    CLASS to(CODE code);

}
