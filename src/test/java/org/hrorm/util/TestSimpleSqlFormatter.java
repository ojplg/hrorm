package org.hrorm.util;

import org.junit.Assert;
import org.junit.Test;

public class TestSimpleSqlFormatter {

    @Test
    public void testWhiteSpaceNormalization(){

        String unformattedSql = "select    foo,bar, baz from  quux \n where foo = 'donuts' ; ";

        String expectedSql = "SELECT FOO, BAR, BAZ FROM QUUX WHERE FOO = 'donuts';";

        Assert.assertTrue(SimpleSqlFormatter.equalSql(expectedSql, unformattedSql));
    }

    @Test
    public void testGreaterThanOrEquals(){
        String unformattedSql = "select    foo,bar, baz from  quux \n where foo = 'donuts' and  bar >= 7 ; ";

        String expectedSql = "SELECT FOO, BAR, BAZ FROM QUUX WHERE FOO = 'donuts' AND BAR >= 7;";

        Assert.assertTrue(SimpleSqlFormatter.equalSql(expectedSql, unformattedSql));

        String formattedSql = SimpleSqlFormatter.format(unformattedSql);

        Assert.assertEquals(expectedSql, formattedSql);
    }

}
