package org.hrorm.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EnumeratedColor {
    public static final EnumeratedColor Red = new EnumeratedColor("Red");
    public static final EnumeratedColor Blue = new EnumeratedColor("Blue");
    public static final EnumeratedColor Green = new EnumeratedColor("Green");

    public static final List<EnumeratedColor> AllColors = Arrays.asList(Red, Blue, Green);

    private final String color;
    private EnumeratedColor(String color){ this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumeratedColor that = (EnumeratedColor) o;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }

    public String getColor(){
        return color;
    }
}
