package me.noack.antonio.friedomobile.extract.live;

import java.util.ArrayList;

import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 18.12.2017
 */

public class FortschrittExtractor extends OnceExtractor {

    boolean start, start2;
    HTMLElement first;
    ArrayList<String> done = new ArrayList<>(), todo = new ArrayList<>();

    @Override
    public void onElementStarted(HTMLElement element) {
        if(!start && "content".equalsIgnoreCase(element.getClassName())){
            start = true;
        } else
        if(!start2 && "treelist".equalsIgnoreCase(element.getClassName())){
            start2 = true;
            first = element;
        }
    }

    @Override
    public void onElementFinished(HTMLElement element) {
        if(element == first){
            // <3 wir haben den ersten wiedergefunden und können nun den Baum aufbauen
            // nur das Erstellen vom Dingens wird etwas elends...



            // 1. baue den Baum/die Bäume

            // 2. baue das Layout

        } else if(element.getType().equalsIgnoreCase("li") && "treelist".equalsIgnoreCase(element.getClassName()) && element.hasChildren()){
            for(HTMLElement e:element.children()){
                if(e.getType().equalsIgnoreCase("img") && e.get("alt")!=null){
                    switch(e.get("alt").toLowerCase()){
                        case "prüfung":
                        case "konto":
                        case "modul bestanden":
                        case "sp":// Sollprüfung
                    }
                }
            }
        }
    }

    @Override
    public void onFinished(HTMLDocument doc) {

    }

    @Override
    public void onError(Exception e) {

    }
}
