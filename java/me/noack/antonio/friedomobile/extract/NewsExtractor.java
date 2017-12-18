package me.noack.antonio.friedomobile.extract;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 20.11.2017
 *
 * interpretiert die Hauptseite von Friedolin und schreibt die News auf die Newsseite
 * */
public class NewsExtractor extends Extractor {

    public void work(AllManager that, HTMLDocument doc){
        HTMLElement e = doc.getById("FSUnewsbox1");
        if(e!=null && e.hasChildren()){
            for(HTMLElement c:e.children()){
                String content = c.getContent();
                if(content!=null){
                    if(c.getType().startsWith("h")){
                        // Überschrift -> neues Thema
                        that.newsContent.addView(that.headerByHTML(c.getContent()));
                    } else {
                        // Text :)
                        String s = c.getContent();
                        for(int i=1;i<s.length();i++){
                            if(s.charAt(i-1)==','){
                                char x = s.charAt(i);
                                if(Character.toLowerCase(x) != Character.toUpperCase(x)){
                                    s = s.substring(0, i)+" "+s.substring(i);
                                }
                            }
                        }
                        that.newsContent.addView(that.textByHTML(s
                                .replace("2017Lie", "2017 | Lie")
                                .replace("2018Lie", "2018 | Lie")
                                .replace("2019Lie", "2019 | Lie")
                                .replace("2020Lie", "2020 | Lie")
                                .replace("2021Lie", "2021 | Lie")));
                    }
                }
            }
        }
    }

    public void onError(AllManager all, Exception e){
        all.newsContent.addView(all.headerByHTML(e.getClass().getName()+": "+e.getMessage()));
    }
}
