package me.noack.antonio.friedomobile.html;

/**
 * Created by antonio on 25.11.2017
 */

public class Cookie {

    String key, value;

    /**
     * parst einen Cookie
     * */
    public Cookie(String s){
        String x = s.split(";")[0].trim();
        int i = x.indexOf('=');
        if(i > 0){
            key = x.substring(0, i);
            value = x.substring(i+1);
        } else {
            key = x;
        }
    }
}
