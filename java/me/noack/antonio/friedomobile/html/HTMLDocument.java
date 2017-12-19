package me.noack.antonio.friedomobile.html;

import java.util.HashMap;

import me.noack.antonio.friedomobile.struct.FList;

/**
 * Created by antonio on 20.11.2017
 */

public class HTMLDocument extends HTMLElement {

    private HashMap<String, HTMLElement> byId = new HashMap<>();
    private HashMap<String, FList<HTMLElement>> byClass = new HashMap<>();

    public HTMLDocument(){
        super(null);
    }

    public void putId(HTMLElement element, String id){
        byId.put(id.toLowerCase(), element);
    }

    public void putClass(HTMLElement element, String clazz) {
        clazz = clazz.toLowerCase();
        FList<HTMLElement> r = byClass.get(clazz);
        if(r == null) byClass.put(clazz, r = new FList<HTMLElement>());
        r.add(element);
    }

    public HTMLElement getById(String id){
        return byId.get(id.toLowerCase());
    }
    public FList<HTMLElement> getByClass(String id){
        return byClass.get(id.toLowerCase());
    }
}
