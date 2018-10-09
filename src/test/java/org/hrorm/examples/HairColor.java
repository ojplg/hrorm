package org.hrorm.examples;

public class HairColor {

    public static HairColor Brown = new HairColor("Brown");
    public static HairColor Black = new HairColor("Black");
    public static HairColor Blonde = new HairColor("Blonde");
    public static HairColor Red = new HairColor("Red");

    public static HairColor forColorName(String s) {
        switch (s){
            case "Brown" : return Brown;
            case "Black" : return Black;
            case "Blonde" : return Blonde;
            case "Red" : return Red;
        }
        throw new IllegalArgumentException("Bad string "  + s);
    }

    private final String colorName;

    public HairColor(String colorName){
        this.colorName = colorName;
    }

    public String getColorName(){
        return colorName;
    }
}
