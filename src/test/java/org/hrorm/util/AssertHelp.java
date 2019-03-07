package org.hrorm.util;

import org.junit.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AssertHelp {

    public static final <T> void sameContents(Collection<T> expected, Collection<T> found){
        Assert.assertEquals(expected.size(), found.size());
        Assert.assertTrue(expected.containsAll(found));
        Assert.assertTrue(found.containsAll(expected));
    }

    public static final <T> void sameContents(T[] expectedArray, Collection<T> found){
        List<T> expected = Arrays.asList(expectedArray);
        sameContents(expected, found);
    }
}
