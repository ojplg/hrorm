package org.hrorm;

/**
 * Utility for providing unique prefixes in a fixed pattern.
 *
 * <p>
 *
 *  Most users of hrorm will have no need to directly use this.
 */
public class Prefixer {
    private String[] prefixes = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n",
                                                "o","p","q","r","s","t","u","v","w","x","y","z"};

    private int index = 0;

    public String nextPrefix(){
        return prefixes[index++];
    }
}
