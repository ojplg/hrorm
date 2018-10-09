package org.hrorm.examples;

import org.hrorm.Converter;

class HairColorConverter implements Converter<HairColor, String> {
    @Override
    public String from(HairColor item) {
        return item.getColorName();
    }

    @Override
    public HairColor to(String s) {
        return HairColor.forColorName(s);
    }
}
