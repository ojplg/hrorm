package org.hrorm.examples;

import org.hrorm.Converter;

public class EnumeratedColorConverter implements Converter<EnumeratedColor, String> {
    @Override
    public String from(EnumeratedColor item) {
        return item.getColor();
    }

    @Override
    public EnumeratedColor to(String s) {
        switch(s){
            case "Red" : return EnumeratedColor.Red;
            case "Blue" : return EnumeratedColor.Blue;
            case "Green" : return EnumeratedColor.Green;
            default : throw new RuntimeException("Unrecognized color " + s);
        }
    }
}
