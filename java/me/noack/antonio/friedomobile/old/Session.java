package me.noack.antonio.friedomobile.old;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by antonio on 25.11.2017
 * closed on 18.12.2017 -> was solved in LoggedConnection
 */

public class Session {

    /*public String workerURL;
    public String worker, jsessionID;

    public Session() throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) new URL(HTMLInterpreter.mainURL+"state=user&type=0").openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);
        con.setDoOutput(true);

        con.connect();

        List<String> cookieStrings = con.getHeaderFields().get("Set-Cookie");

        for(String cookieString:cookieStrings){
            Cookie cookie = new Cookie(cookieString);
            System.out.println(cookieString+" = "+cookie.key+":"+cookie.value);
            switch(cookie.key){
                case "ROUTEID":worker = cookie.value;break;
                case "JSESSIONID":
                    workerURL = "https://friedolin.uni-jena.de/qisserver/rds;jsessionid="+cookie.value+"?";
                    jsessionID = cookie.value;
                    break;
            }
        }

        // state=change&type=5&moduleParameter=raumSearch&nextdir=change&next=search.vm&subdir=raum&_form=display&purge=y&navigationPosition=functions%2Cfacilities&breadcrumb=facilities&topitem=locallinks&subitem=facilities
        if(worker == null || jsessionID == null) throw new IOException("Keksengpass!");


    }

    static long lastTime = 0;
    static Session lastSession;

    public static Session get(HTMLInterpreter interpreter){
        long thisTime = System.currentTimeMillis();
        if(lastTime == 0 || thisTime-lastTime < 25*60*1000){
            try {
                lastSession = new Session();
                lastTime = thisTime;
            } catch (IOException e){}
            return lastSession;
        } else {
            lastTime = thisTime;
            return lastSession;
        }


    }*/
}
