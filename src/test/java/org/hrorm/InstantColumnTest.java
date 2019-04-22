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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Random;

public class InstantColumnTest {

    @Test
    public void testSetValueHandlesNulls() throws SQLException  {

        Column<Timestamp, Instant, Columns,Columns> column = DataColumnFactory.instantColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        column.setValue(columns, 1, preparedStatement);

        Mockito.verify(preparedStatement).setNull(1, Types.TIMESTAMP);
        Mockito.verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    public void testPopulateHandlesNulls() throws SQLException {

        Column<Timestamp, Instant, Columns,Columns> column = DataColumnFactory.instantColumn(
                "TIME COLUMN", "A", Columns::getTimeStampThing, Columns::setTimeStampThing, true);

        Columns columns = new Columns();

        ResultSet resultSet = Mockito.mock(ResultSet.class);

        column.populate(columns, resultSet);

        Mockito.verify(resultSet).getTimestamp("ATIME COLUMN");
        Mockito.verify(resultSet).wasNull();
        Mockito.verifyNoMoreInteractions(resultSet);
    }

    @Test
    public void testPreventsNullsWhenSet() throws SQLException {
        Column<Timestamp, Instant, Columns,Columns> column = DataColumnFactory.instantColumn(
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
        return Objects.equals(instant, recoveredInstant);
    }

    /*
    @Test
    public void testUnderstandingTimestamps(){
        int year = 2018;
        int month = 3;
        int day = 11;
        int hour = 2;
        int minute = 0;
        int second = 0;
        int nanos = 0;

        LocalDateTime instant = LocalDateTime.of(year, month, day, hour, minute, second, nanos);
        Assert.assertEquals(2, instant.getHour());
        Timestamp timestamp = Timestamp.valueOf(instant);
        Assert.assertEquals(3, timestamp.getHours());
        LocalDateTime recoveredLocalDateTIme = timestamp.toLocalDateTime();
        Assert.assertEquals(3, recoveredLocalDateTIme.getHour());

        Instant instant = Instant.from(ZonedDateTime.of(instant, ZoneId.systemDefault()));

    }

    @Test
    public void testUnderstandingInstants(){
        int year = 2018;
        int month = 3;
        int day = 11;
        int hour = 2;
        int minute = 0;
        int second = 0;
        int nanos = 0;

        LocalDateTime instant = LocalDateTime.of(year, month, day, hour, minute, second, nanos);
        Assert.assertEquals(2, instant.getHour());
        Instant instant = Instant.from(ZonedDateTime.of(instant, ZoneId.systemDefault()));
        LocalDateTime recoveredLocalDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        Assert.assertEquals(3, recoveredLocalDateTime.getHour());
    }
    */

    @Test
    public void testInstantConversions(){
        Random random = new Random();

        int failCount = 0;
        int successCount = 0;

        ZoneId[] zones = new ZoneId[]{ZoneId.of("America/Chicago"), ZoneId.of("UTC") };

        int testCount = 0;

        for(ZoneId zoneId : zones ) {
            for(int year=1950; year<2050; year++){
                for(int month=1; month<=12; month++){
                    for(int day=1; day<=25; day+=5){
                        for(int hour=1;hour<23; hour+=1){
                            testCount++;
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
        Assert.assertEquals(testCount, successCount);
    }

}
