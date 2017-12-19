package me.noack.antonio.friedomobile.html;

import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;

/**
 * 16.12.2017, Erkenntnis und zum beim nächsten Mal angucken:
 *
 * wenn Opera die Anfrage schickt, bekommt es einen 302-Verweis: wohlmöglich werden an die nächste Anfrage nicht die Kekse gesendet, sodass das Einloggen ignoriert wird
 * um Klarheit zu schaffen, sollten wir und HTTPSURLConnection angucken, oder als Server selbst einen 302-Verweis setzen und schauen, was am Ende noch bei uns ankommt.
 * Dass das eigentliche Einloggen klappt, sehen wir ja :). Kein Keks würde auch den neuen Sachbearbeiter erklären.
 *
 * 17.12.2017: wir sollten unsere Hausaufgaben machen, aber es hat endlich geklappt :D
 * */

public class LoggedConnection {

    private static final long timeout = 25000*60;
    private long lastLogin = 0;
    private final String name, password;
    public String loginName;
    public LoggedConnection(String name, String password){
        this.name = name;
        this.password = password;
    }

    public boolean load(AllManager all, SharedPreferences pref){
        String precookie = pref.getString("cookies", null);
        loginName = pref.getString("login", null);
        lastLogin = pref.getLong("cookit", 0);
        if(precookie != null && loginName != null && System.currentTimeMillis()-lastLogin < timeout){
            String[] cookies = precookie.split("\0");
            cookieString = cookies[0];
            for(int i=1;i<cookies.length;i++){
                String cookie = cookies[i];
                int ci = cookie.indexOf('=');
                if(ci > -1){
                    cookieMap.put(cookie.substring(0, ci), cookie.substring(ci+1));
                }// else crazy error
            }

            String[] log = loginName.split("\0");
            all.loginButton.setText(all.getResources().getString(R.string.welcome)
                    .replace("@name", log[0])
                    .replace("@rolle", log[1]));

            return true;
        } return false;
    }

    public void save(SharedPreferences pref){

        if(cookieString == null) return;

        SharedPreferences.Editor edit = pref.edit();

        String dat = cookieString;
        for(Map.Entry<String, String> entry:cookieMap.entrySet()){
            dat+="\0"+entry.getKey()+"="+entry.getValue();
        }

        edit.putString("cookies", dat);
        edit.putString("login", loginName);
        edit.putLong("cookit", lastLogin);
        edit.commit();
    }

    private HashMap<String, String> cookieMap = new HashMap<>();
    private String cookieString;
    private List<String> cookies;

    /**
     * userId=igbrown; sessionId=SID77689211949; isAuthenticated=true
     * */
    public void readCookies(HttpsURLConnection con){
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

    private void setCookies(HttpsURLConnection con){
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36 OPR/49.0.2725.39");
        con.setRequestProperty("Upgrade-Insecure-Requests", "1");
        con.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        // con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        if(cookieString != null)
            con.setRequestProperty("Cookie", cookieString);
    }

    public InputStream connect() throws IOException {

        HttpsURLConnection con = (HttpsURLConnection) new URL("https://friedolin.uni-jena.de/qisserver/rds?state=user&type=0").openConnection();

        con.setRequestMethod("GET");
        setCookies(con);

        con.setDoInput(true);

        readCookies(con);

        out(con.getInputStream(), false);

        HttpURLConnection.setFollowRedirects(false);
        HttpsURLConnection.setFollowRedirects(false);
        String url = "https://friedolin.uni-jena.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm";// ;jsessionid="+jsessionid+" mal gesehen, scheint aber nicht wichtig zu sein...
        con = (HttpsURLConnection) new URL(url).openConnection();

        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(false);// <3 works now

        String value = "asdf="+name+"&fdsa="+password+"&submit=Anmelden";

        setCookies(con);

        con.setRequestProperty("Content-Length", ""+value.length());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Origin", "null");
        con.setRequestProperty("Referer", "https://friedolin.uni-jena.de/qisserver/rds?state=user&type=0");

        con.setDoOutput(true);
        con.setDoInput(true);

        OutputStream out = con.getOutputStream();
        out.write(value.getBytes());
        out.flush();

        readCookies(con);

        if(!url.equalsIgnoreCase(con.getURL().toString())){
            throw new IOException("Illegally followed a redirect!");
        }

        int res = con.getResponseCode();
        String loc = con.getHeaderField("Location");
        if(res == 302 && loc != null){

            con = (HttpsURLConnection) new URL(loc).openConnection();
            con.setRequestMethod("GET");

            setCookies(con);

            con.setDoInput(true);

            HttpURLConnection.setFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);

            lastLogin = System.currentTimeMillis();

            return con.getInputStream();

        } else return null;
    }

    public HttpsURLConnection get(AllManager all, String url) throws IOException {

        boolean ok = true;

        if(System.currentTimeMillis() - lastLogin > timeout){
            ok = connect() != null;// ist hoffentlich erfolgreich...
        }

        HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
        setCookies(con);
        if(ok){
            lastLogin = System.currentTimeMillis();
            save(all.pref);
        } return con;
    }

    // no longer needed
	/*private void read(HttpsURLConnection con){
		for(Map.Entry<String, List<String>> i:con.getHeaderFields().entrySet()){
			System.out.println(i.getKey());
			for(String v:i.getValue()){
				System.out.println("\t"+v);
			}
		}
	}*/

    private void out(final InputStream in, final boolean t){
        new Thread(new Runnable(){
            @Override public void run() {
                try {
                    int i;int last = 0;
                    while((i=in.read())>-1){
                        if(i=='\r') continue;
                        if(i=='\t')i=' ';
                        if(i!=' ' || (last!=' ' && last!='\n')){
                            if(i!='\n' || last!='\n'){
                                if(t) System.out.print((char) i);
                                last = i;
                            }
                        }
                    }
                } catch(IOException e){}
            }
        }).start();
    }
}
