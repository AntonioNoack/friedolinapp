package me.noack.antonio.friedomobile.html;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import me.noack.antonio.friedomobile.extract.live.ElementListener;
import me.noack.antonio.friedomobile.struct.LiveList;

/**
 * Created by antonio on 11/23/17. <- MM, DD, YY dämlich...
 *
 * eine Klasse, die dafür sorgt,dass man große Seiten nach und nach lesen kann
 */
public class LiveHTMLInterpreter {

    public HTMLDocument load(String url, ElementListener elementListener, Runnable finalky) throws IOException {
        if(!url.startsWith("http")) return load(HTMLInterpreter.mainURL+url, elementListener, finalky);
        return interpret(new URL(url).openConnection().getInputStream(), elementListener, finalky);
    }

    public HTMLDocument interpret(InputStream in, ElementListener elementListener, Runnable finalky) throws IOException {
        HTMLDocument doc = new HTMLDocument();
        HTMLElement that = doc, nthat;

        LiveList s = new LiveList(in, 8);

        ArrayList<Character> innerHTML = new ArrayList<>(256);
        int c, d = ' ';
        all:for(int i=0;;i++){
            c = s.get(i);
            if(c < 0) break;
            switch(c){
                case '<':
                    // get tag name...
                    int j=i+1;
                    if(s.get(j)=='!' && s.get(j+1)=='-' && s.get(j+2)=='-'){
                        // Problem: gehe bis zum Ende durch
                        j+=3;
                        while(!(s.get(j)=='-' && s.get(j+1)=='-' && s.get(j+2)=='>')) j++;
                        i = j+2;
                        continue all;
                    }

                    for(c=s.get(j);c!=' ' && (c!='/' || j<i+5) && c!='>' && c>-1;c=s.get(j++));

                    // Kopf wurde gefunden: ... nun die Attribute und so...
                    int k = j;
                    int m = -1;
                    String kopf = null;
                    int startchar = s.get(i+1);
                    if(startchar == ' '){
                        innerHTML.add('<');
                        break;
                    } else if(startchar == '/'){

                        String type = s.get(i+2, j-1-(i+2));// s.substring(i+2, j-1);
                        if(type == null) type = "";
                        if(type.equalsIgnoreCase("br")){
                            nthat = new HTMLElement("br");
                            that.addChild(nthat);
                            elementListener.onElementStarted(nthat);
                            elementListener.onElementFinished(nthat);
                        } else if(!"a".equals(type) || that.getType().equals(type)){// die Seite der Module hat überflüssige </a>, die uns sonst zu einer Nullpointer führen
                            while(!type.equals(that.getType())){
                                that.setContent(s, i, innerHTML, innerHTML.size());
                                elementListener.onElementFinished(that);
                                that = that.getParent();
                            }

                            if(that != null){
                                that.setContent(s, i, innerHTML, innerHTML.size());
                                elementListener.onElementFinished(that);
                                that = that.parent();
                            }

                        }

                        for(;c!='>' && c>-1;c=s.get(k++));

                        i = k-1;

                    } else {
                        // neues Element :)
                        String type = s.get(i+1, j-i-2);//s.substring(i+1, j-1);

                        nthat = new HTMLElement(type);
                        if(that!=null) that.addChild(nthat);
                        that = nthat;

                        for(;c!='>' && c>-1;c=s.get(k++)){
                            x:switch(c){
                                case ' ':
                                    m = k;
                                    break;
                                case '=':
                                    // der Wert der Variablen sozusagen...
                                    if(m==-1 || s.get(k+2)<0){
                                        // error...
                                    } else {
                                        kopf = s.get(m, k-1-m);//s.substring(m, k-1);

                                        c = (char) s.get(k++);
                                        boolean doppel = c == '"';
                                        if(!doppel && c!='\'') break x;

                                        // gehe den Wert lang...
                                        m = k;
                                        f:for(c=s.get(k++);c>-1;c=s.get(k++)){
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
                                                    k++;// c=s[k++];
                                            }
                                        }

                                        String wert = s.get(m, k-1-m);//s.substring(m, k-1);
                                        if(kopf.equalsIgnoreCase("id"))
                                            doc.putId(that, wert);
                                        if(kopf.equalsIgnoreCase("class"))
                                            doc.putClass(that, wert);

                                        that.addAttribute(kopf, wert);
                                    }
                            }
                        }

                        elementListener.onElementStarted(that);
                        if(s.get(k-2)=='/' || that.getType().equals("link") || that.getType().equals("br") || that.getType().equals("img")){
                            elementListener.onElementFinished(that);
                            that = that.parent();
                            if(d!=' ') innerHTML.add(' ');
                        } else {
                            that.setContentStart(k, innerHTML.size());
                        }

                        i = k-1;
                    }
                    break;
                case '\r':
                case '\n':
                    break;
                case '>':// sehr seltener, komischer Fall...
                    break;
                default:
                    if(innerHTML.isEmpty() || (c!='\t' && c!=' ') || d!=' ')// löscht doppelte Tabs weg
                        innerHTML.add((char)(d = c=='\t'?' ':c));

            }
        }

        elementListener.onFinished(doc);
        if(finalky != null) finalky.run();

        return doc;
    }
}