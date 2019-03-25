package org.hrorm;

import org.hrorm.examples.Columns;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LocalDateTimeColumnTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException  {

        AbstractColumn<Instant,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setNull(1, Types.TIMESTAMP);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        AbstractColumn<Instant,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getTimestamp("ATIME COLUMN");
        Mockito.verifyNoMoreInteractions(resultSet);
    }

    @Test
    public void testPreventsNullsWhenSet() throws SQLException {
        AbstractColumn<Instant,Columns,Columns> column = DataColumnFactory.localDateTimeColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);
        column.notNull();

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        try {
            column.setValue(columns, 1, preparedStatement);
            Assert.fail("Should not allow null value");
        } catch (HrormException expected){
        }
        Mockito.verifyZeroInteractions(preparedStatement);
    }

    private boolean checkInstantConversion(int year, int month, int day, int hour, int minute, int second, int nano, ZoneId zoneId){
        ZonedDateTime startTime = ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId);
        Instant instant = Instant.from(startTime);
        Timestamp timestamp = Timestamp.from(instant);
        Instant recoveredInstant = timestamp.toInstant();
        ZonedDateTime recoveredZonedDateTime = ZonedDateTime.ofInstant(recoveredInstant, zoneId);
        return Objects.equals(startTime, recoveredZonedDateTime);
    }

    @Test
    public void testZonedDateTimeConversions(){
        Random random = new Random();

        int failCount = 0;
        int successCount = 0;

        ZoneId[] zones = new ZoneId[]{ZoneId.of("America/Chicago"), ZoneId.of("UTC") };

        for(ZoneId zoneId : zones ) {
            for(int year=1950; year<2050; year++){
                for(int month=1; month<=12; month++){
                    for(int day=1; day<=25; day+=5){
                        for(int hour=1;hour<23; hour+=1){
                            int minute = random.nextInt(60);
                            int second = random.nextInt(60);
                            int nanos = random.nextInt(100000);
                            boolean success = checkInstantConversion(year, month, day, hour, minute, second, nanos, zoneId);
                            if( success ){
                                successCount++;
                            } else {
                                failCount++;
                            }
                        }
                    }
                }
            }
        }

        Assert.assertEquals(0, failCount);
        Assert.assertEquals(264000, successCount);
    }

}
