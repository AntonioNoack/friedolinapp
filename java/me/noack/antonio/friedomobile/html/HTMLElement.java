package me.noack.antonio.friedomobile.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.struct.FList;
import me.noack.antonio.friedomobile.struct.LiveList;

/**
 * Created by antonio on 20.11.2017
 */

public class HTMLElement {
    private HashMap<String, String> attributes;
    private HTMLElement parent;
    private FList<HTMLElement> children;
    private String type, source, innerHTML;
    private char[] sourcec, innerHTMLc;
    private int sourceContentEnd, innerHTMLContentEnd;
    private int sourceContentStart, innerHTMLContentStart;

    public HTMLElement(String type){
        this.type = type;
    }

    public String get(String key){
        return attributes==null?null:attributes.get(key);
    }

    public boolean hasChildren(){
        return children != null;
    }

    public String getType(){
        return type;
    }

    public String getStyle(){return get("style");}
    public String getClassName(){return get("class");}
    public String getID(){return get("id");}
    public String getName(){return get("name");}
    public String getValue(){return get("value");}
    public String getLabel(){return get("label");}


    public void addChild(HTMLElement child){
        if(children == null) children = new FList<>();
        children.add(child);
        child.parent = this;
    }

    public FList<HTMLElement> children(){
        return children;
    }

    public FList<HTMLElement> children(String type){
        return children(type, new FList<HTMLElement>());
    }

    public FList<HTMLElement> children(String type, FList<HTMLElement> ret){
        if(this.type.equals(type)) ret.add(this);
        if(children!=null) {
            for (HTMLElement child : children) {
                child.children(type, ret);
            }
        } return ret;
    }

    public HTMLElement parent(){
        return parent;
    }

    public void addAttribute(String kopf, String wert) {
        if(attributes == null) attributes = new HashMap<>();
        if("a".equalsIgnoreCase(type) && "href".equalsIgnoreCase(kopf)){
            int asii = wert.indexOf("asi=");
            if(asii > -1){
                int asie = wert.indexOf('&', asii+4), asix = wert.indexOf('#', asii+4);
                if(asix > -1 && (asie < 0 || asix < asie)) asie = asix;

                String asi;
                if(asie > -1){
                    asi = wert.substring(asii+4, asie);
                } else if(asii+4 < wert.length()) asi = wert.substring(asii+4);
                else asi = null;
                if(asi != null) AllManager.instance.setAsi(asi);
            }
        }
        attributes.put(kopf, wert);
    }

    public String toString(int indent){
        String indents = "";
        for(int i=0;i<indent;i++){
            indents+=" ";
        }

        indent++;

        String clazz = getClassName();
        if(children!=null){
            String s = indents+type+(clazz==null?":":":"+clazz+":")+getContent();
            for(HTMLElement child:children){
                s+="\n"+child.toString(indent);
            }
            return s+"\n"+indents+"/"+type;
        } else return indents+type+":"+getContent();

    }

    public FList<HTMLElement> flatten(){// eine schöne Idee, wenn es Links nicht gäbe...
        FList<HTMLElement> x = new FList<>();
        flatten(x);
        return x;
    }

    public void flatten(FList<HTMLElement> ret){
        if(children == null) ret.add(this);
        else for(HTMLElement child:children){
            child.flatten(ret);
        }
    }

    public String toString(){
        String s = type;
        if(children!=null){
            for(HTMLElement child:children){
                s+="\n"+child.toString(1);
            }
        } return s;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public HTMLElement getParent() {
        return parent;
    }

    public String getContent(){
        if(innerHTML == null && innerHTMLContentStart < innerHTMLContentEnd)
            innerHTML = rep(new String(innerHTMLc, innerHTMLContentStart, innerHTMLContentEnd-innerHTMLContentStart));
        innerHTMLc = null;// RAM freigeben
        return innerHTML;
    }

    public String getSource(){
        if(source == null && sourceContentStart < sourceContentEnd)
            source = new String(sourcec, sourceContentStart, sourceContentEnd - sourceContentStart).trim();
        sourcec = null;// RAM freigeben
        return source;
    }

    public void setContentStart(int sourceContentStart, int innerHTMLContentStart){
        this.sourceContentStart = sourceContentStart;
        this.innerHTMLContentStart = innerHTMLContentStart;
    }

    public void setContent(char[] s, int i, char[] s2, int j) {
        sourcec = s;
        innerHTMLc = s2;
        sourceContentEnd = i;
        innerHTMLContentEnd = j;
    }

    public void setContent(LiveList s, int i, ArrayList<Character> s2, int j) throws IOException {
        if(sourceContentStart < (sourceContentEnd=i)){
            source = s.get(sourceContentStart, sourceContentEnd-sourceContentStart);
        }// else null
        if(innerHTMLContentStart < (innerHTMLContentEnd=j)){
            char[] data = new char[innerHTMLContentEnd-innerHTMLContentStart];
            for(int k=innerHTMLContentStart,l=0;k<innerHTMLContentEnd;l++,k++){
                data[l] = s2.get(k);
            }

            innerHTML = rep(new String(data));
            innerHTMLContentStart = innerHTMLContentEnd;// falls innerHTML=null | als Längenangabe ist es dank .trim() und .rep(nbsp) eh nicht geeignet
        }// else null
    }

    public final String rep(String s){
        s = s
                .replace("&nbsp;"," ")
                .replace("&auml;","ä")
                .replace("&ouml;","ö")
                .replace("&uuml;","ü")
                .replace("&Auml;","Ä")
                .replace("&Ouml;","Ö")
                .replace("&Uuml;","Ü")
                .trim();
        return s.length()==0?null:s;
    }
}

