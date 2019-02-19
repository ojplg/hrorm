package org.hrorm.util;

import org.junit.Assert;

public class SimpleSqlFormatter {

    enum ParseState {
        Start,WhiteSpace,WordCharacter;
    }

    enum Quote {
        In,Out;
    }

    void handleOperatorCharacter(char c, ParseState parseState, StringBuffer buf){
        if ( parseState != ParseState.WhiteSpace ){
            buf.append(' ');
        }
        buf.append(c);
        buf.append(' ');
    }

    // NOTE: Mishandles two character operators like '>=' and '<='
    String format(String inputSql){

        ParseState parseState = ParseState.Start;
        Quote quote = Quote.Out;

        StringBuffer buf = new StringBuffer();

        for( Character c : inputSql.toCharArray() ){
            if( Character.isAlphabetic(c)){
                if ( quote == Quote.Out ) {
                    buf.append(Character.toUpperCase(c));
                    parseState = ParseState.WordCharacter;
                } else {
                    buf.append(c);
                }
            }
            else if ( Character.isWhitespace(c)  ) {
                if ( parseState != ParseState.WhiteSpace ) {
                    buf.append(" ");
                    parseState = ParseState.WhiteSpace;
                }
            }
            else if ( c.equals(',') ){
                buf.append(", ");
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals('\'')) {
                if ( quote == Quote.Out ){
                    quote = Quote.In;
                } else {
                    quote = Quote.Out;
                }
                buf.append("'");
            }
            else if ( c.equals( '=' ) ){
                handleOperatorCharacter('=', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals( '<' ) ){
                handleOperatorCharacter('<', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals( '>' ) ){
                handleOperatorCharacter('<', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals( '?' )){
                handleOperatorCharacter('?', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals( '(' )){
                handleOperatorCharacter('(', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c.equals( ')' )){
                handleOperatorCharacter(')', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else {
                buf.append(c);
            }

        }

        return buf.toString();
    }

    public static boolean equalSql(String expected, String string){
        SimpleSqlFormatter formatter = new SimpleSqlFormatter();
        String expectedFormatted = formatter.format(expected);
        String stringFormatted = formatter.format(string);
        return expectedFormatted.equals(stringFormatted);
    }

    public static void assertEqualSql(String expected, String string){
        SimpleSqlFormatter formatter = new SimpleSqlFormatter();
        String expectedFormatted = formatter.format(expected);
        String stringFormatted = formatter.format(string);
        Assert.assertEquals(expectedFormatted, stringFormatted);
    }

}
