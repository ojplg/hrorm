package org.hrorm.util;

import org.junit.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static final <T,U> void containsAllItems(T[] expected, Collection<U> found, Function<U,T> mapFunction){
        List<T> mapped = found.stream().map(mapFunction).collect(Collectors.toList());
        sameContents(expected, mapped);
    }
}
