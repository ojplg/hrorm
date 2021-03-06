package org.hrorm;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * Some {@link Converter} implementations and factory methods.
 * Hrorm uses some of these internally for standard conversions.
 */
public class Converters {

    /**
     * This <code>Converter</code> translates true values to "T" and false values to "F".
     */
    public static Converter<Boolean, String> T_F_BOOLEAN_STRING_CONVERTER =  booleanConverter("T","F");

    /**
     * This <code>Converter</code> translates true values to 1 and false values to 0.
     */
    public static Converter<Boolean, Long> ONE_ZERO_BOOLEAN_LONG_CONVERTER = booleanConverter(1L, 0L);

    /**
     * This <code>Converter</code> translates between the <code>Instant</code>
     * class and the <code>Timestamp</code> and is used by the built-in
     * support for <code>Instant</code>.
     */
    public static Converter<Instant, Timestamp> INSTANT_TIMESTAMP_CONVERTER = new InstantTimestampConverter();

    /**
     * Produces an empty <code>Converter</code> that does no transformations.
     *
     * @param <T> The type supported by the <code>Converter</code>
     * @return An instance of the <code>Converter</code> interface that passes objects through
     * without change
     */
    public static <T> Converter<T,T> identity(){ return new IdentityConverter<>(); }

    /**
     * Produces a converter instance for encoding boolean values as <code>String</code> values.
     *
     * @param trueValue Representation for the value for true.
     * @param falseValue Representation for the value for false.
     * @return A converter using the given values.
     */
    public static Converter<Boolean, String> booleanConverter(String trueValue, String falseValue){
        return new BooleanStringConverter(trueValue, falseValue);
    }

    /**
     * Produces a converter instance for encoding boolean values as <code>Long</code> values.
     *
     * @param trueValue Representation for the value for true.
     * @param falseValue Representation for the value for false.
     * @return A converter using the given values.
     */
    public static Converter<Boolean, Long> booleanConverter(Long trueValue, Long falseValue){
        return new BooleanLongConverter(trueValue, falseValue);
    }

    /**
     * This <code>Converter</code> translates between <code>Boolean</code> values and <code>String</code> values.
     */
    private static class BooleanStringConverter implements Converter<Boolean, String> {

        private final String trueRepresentation;
        private final String falseRepresentation;

        private BooleanStringConverter(String trueRepresentation, String falseRepresentation) {
            this.trueRepresentation = trueRepresentation;
            this.falseRepresentation = falseRepresentation;
        }

        @Override
        public String from(Boolean aBoolean) {
            if ( aBoolean == null ) {
                return null;
            }
            return aBoolean ? trueRepresentation : falseRepresentation;
        }

        @Override
        public Boolean to(String s) {
            if ( s == null ){
                return null;
            }
            if (Objects.equals(s, trueRepresentation)) {
                return Boolean.TRUE;
            } else if (Objects.equals(s, falseRepresentation)) {
                return Boolean.FALSE;
            }
            throw new HrormException("Unsupported string: " + s);
        }
    }

    /**
     * This <code>Converter</code> translates between <code>Boolean</code> values and <code>Long</code> values.
     */
    private static class BooleanLongConverter implements Converter<Boolean, Long> {

        private final long trueRepresentation;
        private final long falseRepresentation;

        private BooleanLongConverter(long trueRepresentation, long falseRepresentation){
            this.trueRepresentation = trueRepresentation;
            this.falseRepresentation = falseRepresentation;
        }

        @Override
        public Long from(Boolean aBoolean) {
            if ( aBoolean == null ) {
                return null;
            }
            return aBoolean ? trueRepresentation : falseRepresentation;
        }

        @Override
        public Boolean to(Long s) {
            if ( s == null ){
                return null;
            }
            if (s == trueRepresentation ){
                return Boolean.TRUE;
            } else if (s == falseRepresentation) {
                return Boolean.FALSE;
            }
            throw new HrormException("Unsupported value: " + s);
        }
    }

    /**
     * This <code>Converter</code> translates between <code>Instant</code> values and <code>Timestamp</code> values.
     */
    private static class InstantTimestampConverter implements Converter<Instant, Timestamp> {
        @Override
        public Timestamp from(Instant item) {
            if( item == null ){
                return null;
            }
            return Timestamp.from(item);
        }

        @Override
        public Instant to(Timestamp timestamp) {
            if ( timestamp == null ) {
                return null;
            }
            return timestamp.toInstant();
        }
    }

    private static class IdentityConverter<T> implements Converter<T,T> {
        @Override
        public T from(T t) {
            return t;
        }

        @Override
        public T to(T t) {
            return t;
        }
    }
}
