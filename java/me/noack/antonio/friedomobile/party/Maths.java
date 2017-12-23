package me.noack.antonio.friedomobile.party;

/**
 * Created by antonio on 22.12.2017
 */

public class Maths {
    public static int parseInt(String s, int alt){
        if(s==null) return alt;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            return alt;
        }
    }

    public static boolean isDark(int color){
        int r = (color>>16)&255, g = (color>>8)&255, b = color&255;
        return 21*r + 72*g + 7*b < 12700;
    }
}
