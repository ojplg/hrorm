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

}
