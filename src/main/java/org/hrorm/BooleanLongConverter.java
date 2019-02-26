package org.hrorm;

/**
 * This {@link Converter} translates true values to 1 and false
 * values to 0.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class BooleanLongConverter implements Converter<Boolean, Long> {

    public static final BooleanLongConverter INSTANCE = new BooleanLongConverter();

    private final long trueRepresentation = 1;
    private final long falseRepresentation = 0;

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
