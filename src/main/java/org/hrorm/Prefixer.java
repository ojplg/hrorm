package org.hrorm;

public class Prefixer {
    private String[] prefixes = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n",
                                                "o","p","q","r","s","t","u","v","w","x","y","z"};

    private int index = 0;

    public String nextPrefix(){
        return prefixes[index++];
    }
}
