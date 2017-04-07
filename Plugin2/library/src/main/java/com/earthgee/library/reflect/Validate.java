package com.earthgee.library.reflect;

/**
 * Created by zhaoruixuan on 2017/4/7.
 */
public class Validate {

    static void isTrue(final boolean expression,
                       final String message,final Object... values){
        if(expression==false){
            throw new IllegalArgumentException(String.format(message,values));
        }
    }

}
