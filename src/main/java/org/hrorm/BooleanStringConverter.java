package org.hrorm;

import java.util.Objects;

/**
 * This {@link Converter} translates true values to "T" and false
 * values to "F".
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class BooleanStringConverter implements Converter<Boolean, String> {

    private final String trueRepresentation;
    private final String falseRepresentation;

    public BooleanStringConverter(String trueRepresentation, String falseRepresentation) {
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
