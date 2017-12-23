package me.noack.antonio.friedomobile.html.connection;

import android.content.SharedPreferences;
import android.icu.util.Output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;

/**
 * Created by antonio on 22.12.2017
 */

public class CookieConnection {

    private static final long timeout = 25000*60;
    private static long lastLogin = 0;

    private static HashMap<String, String> cookieMap = new HashMap<>();
    private static String cookieString;
    private static List<String> cookies;

    private static void readCookies(HttpsURLConnection con){
        cookies = con.getHeaderFields().get("Set-Cookie");
        if(cookies!=null) for(String s:cookies){
            String[] cookieContent = s.split(" ")[0].split("=");
            String cookieName = cookieContent[0], cookieValue = cookieContent[1];
            if(cookieValue.endsWith(";")) cookieValue = cookieValue.substring(0, cookieValue.length()-1);
            cookieMap.put(cookieName, cookieValue);
        }
        cookieString = null;
        for(Map.Entry<String, String> s:cookieMap.entrySet()){
            String cookieName = s.getKey(), cookieValue = s.getValue();
            cookieString = cookieString == null?cookieName+"="+cookieValue:cookieString+"; "+cookieName+"="+cookieValue;
        }
    }

    private static void setCookies(HttpsURLConnection con){
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36 OPR/49.0.2725.39");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");
        con.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        // con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        if(cookieString != null)
            con.setRequestProperty("Cookie", cookieString);
    }

    public static HttpsURLConnection get(String url, String post) throws IOException {

        long currently = System.currentTimeMillis();
        if(currently - lastLogin > timeout){
            // Neuverbinden erforderlich
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://friedolin.uni-jena.de/qisserver/rds?state=user&type=0").openConnection();
            con.setRequestMethod("GET");
            setCookies(con);
            readCookies(con);
            lastLogin = currently;
        }

        HttpsURLConnection con = (HttpsURLConnection) new URL(url.startsWith("http")?url:"https://friedolin.uni-jena.de/qisserver/rds?"+url).openConnection();
        con.setRequestMethod(post == null ? "GET":"POST");
        setCookies(con);
        if(post != null){
            byte[] bytes = post.getBytes();
            con.setRequestProperty("Content-Length", bytes.length+"");
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
        }
        readCookies(con);
        return con;
    }
}
