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

    void handleTwoCharOperator(char c, char n, ParseState parseState, StringBuffer buf){
        if ( parseState != ParseState.WhiteSpace ){
            buf.append(' ');
        }
        buf.append(c);
        buf.append(n);
        buf.append(' ');
    }

    String format(String inputSql){

        ParseState parseState = ParseState.Start;
        Quote quote = Quote.Out;

        StringBuffer buf = new StringBuffer();

        char[] charArray = inputSql.toCharArray();
        for( int idx=0 ; idx<charArray.length ; idx++ ){
            char c = charArray[idx];
            Character n;
            if( idx < charArray.length - 1) {
                n = charArray[idx + 1];
            } else {
                n = null;
            }
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
            else if ( c == ',' ){
                buf.append(", ");
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == '\'' ) {
                if ( quote == Quote.Out ){
                    quote = Quote.In;
                } else {
                    quote = Quote.Out;
                }
                buf.append("'");
            }
            else if ( c == '=' ){
                handleOperatorCharacter('=', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == ( '<' ) ){
                if( n == '>' || n == '=' ){
                    handleTwoCharOperator(c, n, parseState, buf);
                    idx++;
                } else {
                    handleOperatorCharacter('<', parseState, buf);
                }
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == ( '>' ) ){
                if (n == '=') {
                    handleTwoCharOperator(c, n, parseState, buf);
                    idx++;
                } else {
                    handleOperatorCharacter('<', parseState, buf);
                }
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == ( '?' )){
                handleOperatorCharacter('?', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == ( '(' )){
                handleOperatorCharacter('(', parseState, buf);
                parseState = ParseState.WhiteSpace;
            }
            else if ( c == ( ')' )){
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
