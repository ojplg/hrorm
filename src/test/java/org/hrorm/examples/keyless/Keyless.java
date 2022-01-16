package org.hrorm.examples.keyless;

import lombok.Data;
import org.hrorm.IndirectKeylessDaoBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Data
public class Keyless {

    public static final IndirectKeylessDaoBuilder<Keyless, Keyless> DAO_BUILDER =
            new IndirectKeylessDaoBuilder<>("keyless_table", Keyless::new, x->x)
                    .withStringColumn("string_column", Keyless::getStringColumn, Keyless::setStringColumn)
                    .withLongColumn("integer_column", Keyless::getIntegerColumn, Keyless::setIntegerColumn)
                    .withBigDecimalColumn("fractional_column", Keyless::getDecimalColumn, Keyless::setDecimalColumn)
                    .withBooleanColumn("boolean_column", Keyless::getBooleanColumn, Keyless::setBooleanColumn)
                    .withInstantColumn("timestamp_column", Keyless::getTimeStampColumn, Keyless::setTimeStampColumn);

    private String stringColumn;
    private Long integerColumn;
    private BigDecimal decimalColumn;
    private Boolean booleanColumn;
    private Instant timeStampColumn;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Keyless keyless = (Keyless) o;
        return integerColumn == keyless.integerColumn &&
                booleanColumn == keyless.booleanColumn &&
                Objects.equals(stringColumn, keyless.stringColumn) &&
                Objects.equals(decimalColumn, keyless.decimalColumn) &&
                Objects.equals(timeStampColumn, keyless.timeStampColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringColumn, integerColumn, decimalColumn, booleanColumn, timeStampColumn);
    }
}
