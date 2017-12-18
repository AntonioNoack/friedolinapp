package me.noack.antonio.friedomobile.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by antonio on 11/20/17.
 *
 * zum Glück will die Seite ja javascript-los arbeiten :D
 *
 */

public class HTMLInterpreter {

    static int random(int len){
        return random(0, len);
    }

    static int random(int start, int len){
        return (int) (Math.random() * len) + start;
    }

    // https://friedolin.uni-jena.de/qisserver/rds?state=user&type=0&topitem=&breadCrumbSource=portal&topitem=functions
    public static final String mainURL = "https://friedolin.uni-jena.de/qisserver/rds?";

    private static String safver = random(400, 130)+"."+random(60);
    public static String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/"+safver+" (KHTML, like Gecko) Chrome/62.0."+random(3200)+"."+random(60)+" Safari/"+safver;

    public InputStream loginStream(String name, String passwort) throws IOException {
        String httpsURL = mainURL+"state=user&type=1&category=auth.login&startpage=portal.vm";

        String query = "asdf="+URLEncoder.encode(name,"UTF-8")+"&fdsa="+URLEncoder.encode(passwort,"UTF-8")+"&submit=Anmelden";

        URL url = new URL(httpsURL);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        con.setRequestProperty("Content-length", String.valueOf(query.length()));
        con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

        con.setRequestProperty("User-Agent", userAgent);
        con.setDoOutput(true);
        con.setDoInput(true);

        OutputStream out = con.getOutputStream();

        out.write(query.getBytes());
        out.flush();
        out.close();

        return con.getInputStream();
    }

    public static boolean login(String name, String passwort){



        return true;
    }

    public HTMLDocument load(String url) throws IOException {
        if(!url.startsWith("http")) return load(mainURL+url);

        ArrayList<String> lines = new ArrayList<>(256);
        BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));// es ist fraglich, ob wir damit auch das Passwort transportiert bekommen... vermutlich ja nicht...
        String ln = r.readLine();
        while(ln!=null) {
            lines.add(ln);
            ln = r.readLine();
        } r.close();
        return interpret(lines);
    }

    public HTMLDocument load(InputStream in) throws IOException {
        ArrayList<String> lines = new ArrayList<>(256);
        BufferedReader r = new BufferedReader(new InputStreamReader(in));// es ist fraglich, ob wir damit auch das Passwort transportiert bekommen... vermutlich ja nicht...
        String ln = r.readLine();
        while(ln!=null) {
            lines.add(ln.replace("&nbsp;", " "));
            ln = r.readLine();
        } r.close();
        return interpret(lines);
    }

    public HTMLDocument interpret(ArrayList<String> lines){
        HTMLDocument doc = new HTMLDocument();
        HTMLElement that = doc, nthat;

        // Preprocessor: <!-- --> muss entfernt werden
        for(int ln=0;ln<lines.size();ln++){
            String s = lines.get(ln);
            int i = s.indexOf("<!--");
            while(i > -1){
                int j = s.indexOf("-->");
                if(j < 0){
                    lines.set(ln, s.substring(0, i));
                    while(j<0 && ln < lines.size()){
                        j = (s=lines.get(++ln)).indexOf("-->");
                        if(j < 0) lines.set(ln, "");
                    }

                } else {
                    lines.set(ln, s = s.substring(0, i)+s.substring(j+3));
                }

                i = s.indexOf("<!--", j);
            }
        }

        int l = 0;
        for(String s:lines)
            l+=s.length();
        char[] s = new char[l];

        int pos=0;
        for(String x:lines){
            char[] sub = x.toCharArray();
            System.arraycopy(sub, 0, s, pos, sub.length);
            pos+=sub.length;
        }

        char[] innerHTML = new char[l];
        char c, d = ' ';
        int innerHTMLIndex = 0;
        for(int i=0;i<l;i++){
            c = s[i];
            switch(c){
                case '<':
                    // get tag name...
                    int j=i+1;
                    for(c=s[j];c!=' '&& (c!='/' || j<i+5) && c!='>' && j<l;c=s[j++]);

                    // Kopf wurde gefunden: ... nun die Attribute und so...
                    int k = j;
                    int m = -1;
                    String kopf = null;

                    if(s[i+1] == '/'){

                        String type = new String(s, i+2, j-1-(i+2));// s.substring(i+2, j-1);
                        if(type.equalsIgnoreCase("br")){
                            that.addChild(new HTMLElement("br"));
                        } else {
                            if(!type.equals(that.getType())){
                                // error!!!...
                            }

                            if(that!=null){
                                that.setContent(s, i, innerHTML, innerHTMLIndex);
                                that = that.parent();
                            }

                        }

                        for(;c!='>' && k<l;c=s[k++]){}

                        i = k-1;

                    } else {
                        // neues Element :)
                        String type = new String(s, i+1, j-i-2);//s.substring(i+1, j-1);

                        nthat = new HTMLElement(type);
                        if(that!=null) that.addChild(nthat);
                        that = nthat;

                        for(;c!='>' && k<l;c=s[k++]){
                            x:switch(c){
                                case ' ':
                                    m = k;
                                    break;
                                case '=':
                                    // der Wert der Variablen sozusagen...
                                    if(m==-1 || k>l-2){
                                        // error...
                                    } else {
                                        kopf = new String(s, m, k-1-m);//s.substring(m, k-1);

                                        c = s[k++];
                                        boolean doppel = c == '"';
                                        if(!doppel && c!='\'') break x;

                                        // gehe den Wert lang...
                                        m = k;
                                        f:for(c=s[k++];k<l;c=s[k++]){
                                            switch(c){
                                                case '\'':
                                                    if(!doppel){
                                                        break f;
                                                    }break;
                                                case '"':
                                                    if(doppel){
                                                        break f;
                                                    }break;
                                                case '\\':
                                                    if(k<l) c=s[k++];
                                            }
                                        }

                                        String wert = new String(s, m, k-1-m);//s.substring(m, k-1);
                                        if(kopf.equalsIgnoreCase("id"))
                                            doc.putId(that, wert);
                                        if(kopf.equalsIgnoreCase("class"))
                                            doc.putClass(that, wert);


                                        that.addAttribute(kopf, wert);
                                    }
                            }
                        }

                        if(s[k-2]=='/' || that.getType().equals("link") || that.getType().equals("br") || that.getType().equals("img")){
                            that = that.parent();
                        } else that.setContentStart(k, innerHTMLIndex);

                        i = k-1;
                    }
                    break;
                case '\r':
                case '\n':
                    break;
                case '>':// sehr seltener, komischer Fall...
                    break;
                default:
                    if(innerHTMLIndex == 0 || (c!='\t' && c!=' ') || d!=' ')// löscht doppelte Tabs weg
                        innerHTML[innerHTMLIndex++] = d = c=='\t'?' ':c;

            }
        }

        /*nthat = that;
        while(nthat.hasParent()){
            nthat = nthat.getParent();
        } System.out.println(nthat);*/

        return doc;
    }
}
