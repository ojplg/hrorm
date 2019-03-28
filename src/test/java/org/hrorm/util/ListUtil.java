package org.hrorm.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtil {

    public static <T,U>List<U> map(List<T> list, Function<T, U> f){
        return list.stream().map(f).collect(Collectors.toList());
    }

}
