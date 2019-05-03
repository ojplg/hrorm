package org.hrorm.util;

import org.junit.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssertHelp {

    public static final <T> void sameContents(Collection<T> expected, Collection<T> found){
        Assert.assertEquals("Counts mismatched ", expected.size(), found.size());
        for(T t : found ){
            Assert.assertTrue("Extra item found " + t + " not in " + expected, expected.contains(t));
        }
        for(T t : expected){
            Assert.assertTrue("Missing item " + t, found.contains(t));
        }
    }

    public static final <T> void sameContents(T[] expectedArray, Collection<T> found){
        List<T> expected = Arrays.asList(expectedArray);
        sameContents(expected, found);
    }

    public static final <T,U> void sameContents(List<T> expected, Collection<U> found, Function<U,T> mapFunction){
        sameContents(expected, found.stream(), mapFunction);
    }

    public static final <T,U> void sameContents(List<T> expected, Stream<U> found, Function<U,T> mapFunction){
        List<T> mapped = found.map(mapFunction).collect(Collectors.toList());
        sameContents(expected, mapped);
    }

}
