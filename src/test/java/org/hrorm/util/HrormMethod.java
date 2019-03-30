package org.hrorm.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HrormMethod {

    public static List<HrormMethod> fromClass(Class klass){
        List<HrormMethod> methods = new ArrayList<>();
        for(Method method : klass.getMethods()){
            methods.add(new HrormMethod(klass, method));
        }
        return methods;
    }

    private final Class klass;
    private final Method method;

    public HrormMethod(Class klass, Method method){
        this.method = method;
        this.klass = klass;
    }

    public boolean equivalent(HrormMethod that){
        if ( this.isFluent() && that.isFluent()) {
            return this.method.getName().equals(that.method.getName())
                    && Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes());
        }
        return this.method.getName().equals(that.method.getName())
                && Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes())
                && this.method.getReturnType().equals(that.method.getReturnType());
    }

    public String toString(){
        return "HrormMethod " + method.getName() + " with args " + method.getParameterTypes();
    }

    public String methodName(){
        return method.getName();
    }

    public boolean isFluent(){
        return method.getReturnType().equals(klass);
    }
}
