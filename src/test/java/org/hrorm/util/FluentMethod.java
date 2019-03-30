package org.hrorm.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class FluentMethod {

    private final Method method;

    public FluentMethod(Method method){
        this.method = method;
    }

    public boolean equivalent(FluentMethod that){
        return this.method.getName().equals(that.method.getName())
                && Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes());
    }

    public String toString(){
        return "FluentMethod " + method.getName() + " with args " + method.getParameterTypes();
    }

    public String methodName(){
        return method.getName();
    }
}
