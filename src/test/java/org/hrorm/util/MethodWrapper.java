package org.hrorm.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodWrapper implements Comparable<MethodWrapper> {

    public static List<MethodWrapper> fromClass(Class klass){
        List<MethodWrapper> methods = new ArrayList<>();
        for(Method method : klass.getMethods()){
            methods.add(new MethodWrapper(klass, method));
        }
        Collections.sort(methods);
        return methods;
    }

    private final Class klass;
    private final Method method;

    public MethodWrapper(Class klass, Method method){
        this.method = method;
        this.klass = klass;
    }

    public boolean equivalent(MethodWrapper that){
        if ( this.isFluent() && that.isFluent()) {
            return this.method.getName().equals(that.method.getName())
                    && Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes());
        }
        return this.method.getName().equals(that.method.getName())
                && Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes())
                && this.method.getReturnType().equals(that.method.getReturnType());
    }

    public String toString(){
        return "MethodWrapper " + method.getName() + " with args " + Arrays.asList(method.getParameterTypes());
    }

    public String methodName(){
        return method.getName();
    }

    public boolean isFluent(){
        return method.getReturnType().equals(klass);
    }

    @Override
    public int compareTo(MethodWrapper o) {
        return this.methodName().compareTo(o.methodName());
    }
}
