package me.noack.antonio.friedomobile.html;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import me.noack.antonio.friedomobile.AllManager;

/**
 * Created by antonio on 25.11.2017
 * eine Klasse für sparsameres Internet... vielfache Anfragen werden vermieden
 */

public class Connection {

    long cacheTime;
    String url;

    InputStream in;

    /**
     * get
     * */
    public Connection(AllManager all, String url, long cacheTime) throws IOException {
        this.url = url.startsWith("http")?url:HTMLInterpreter.mainURL+url;

        long thisTime = System.currentTimeMillis();

        try {
            in = all.openFileInput(url);
            DataInputStream din = new DataInputStream(in);
            long timeStamp = din.readLong();
            if(!shallCacheBeUsed(thisTime, timeStamp)){in = null;}
        } catch(IOException e){
            in = null;
        }

        if(in == null){// wir brauchen eine Liveverbindung...
            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();


        }
    }

    /**
     * get, allerdings mit Session-Cookie, weil der wohl für die Verbindung gebraucht wird...
     * */
    public Connection(String url, Session worker, long cacheTime){
        this.url = worker.workerURL+url;

    }

    /**
     * soll der Cache benutzt werden? Spielt für Ausfallszeiten eine besondere Rolle: nach 24°° direkt, sonst nach einer gewissen Zeit
     * */
    public boolean shallCacheBeUsed(long thisTime, long lastTime){
        return lastTime + cacheTime > thisTime;
    }

    public InputStream getInputStream(){
        return null;
    }

}
