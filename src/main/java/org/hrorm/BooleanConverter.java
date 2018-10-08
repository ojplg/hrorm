package org.hrorm;

/**
 * This {@link Converter} translates true values to "T" and false
 * values to "F".
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class BooleanConverter implements Converter<Boolean, String> {

    public static final BooleanConverter INSTANCE = new BooleanConverter();

    @Override
    public String from(Boolean aBoolean) {
        if ( aBoolean == null ) {
            return null;
        }
        return aBoolean ? "T" : "F";
    }

    @Override
    public Boolean to(String s) {
        if ( s == null ){
            return null;
        }
        switch (s) {
            case "T" : return Boolean.TRUE;
            case "F" : return Boolean.FALSE;
            default : throw new HrormException("Unsupported string: " + s);
        }
    }
}
