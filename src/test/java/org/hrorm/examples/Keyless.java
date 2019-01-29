package org.hrorm.examples;

import lombok.Data;
import org.hrorm.KeylessDaoBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Keyless {

    public static final KeylessDaoBuilder<Keyless> DAO_BUILDER =
            new KeylessDaoBuilder<>("keyless_table", Keyless::new)
                    .withStringColumn("string_column", Keyless::getStringColumn, Keyless::setStringColumn)
                    .withIntegerColumn("integer_column", Keyless::getIntegerColumn, Keyless::setIntegerColumn)
                    .withBigDecimalColumn("decimal_column", Keyless::getDecimalColumn, Keyless::setDecimalColumn)
                    .withBooleanColumn("boolean_column", Keyless::isBooleanColumn, Keyless::setBooleanColumn)
                    .withLocalDateTimeColumn("timestamp_column", Keyless::getTimeStampColumn, Keyless::setTimeStampColumn);

    private String stringColumn;
    private long integerColumn;
    private BigDecimal decimalColumn;
    private boolean booleanColumn;
    private LocalDateTime timeStampColumn;


}
