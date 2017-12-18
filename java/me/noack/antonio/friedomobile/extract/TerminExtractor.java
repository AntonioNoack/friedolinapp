package me.noack.antonio.friedomobile.extract;

import android.widget.TextView;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;
import me.noack.antonio.friedomobile.struct.FList;

/**
 * Created by antonio on 11/22/17.
 */

public class TerminExtractor extends Extractor {
    public void work(AllManager all, HTMLDocument doc){
        FList<HTMLElement> exs = doc.getByClass("content");
        if(exs == null) exs = doc.getByClass("content_max_portal_qis");
        if(exs == null) exs = doc.getByClass("divcontent");
        if(exs != null){
            for(HTMLElement ex:exs){
                work(all, ex);
            }
            System.out.println("done :)");
        } else {
            System.out.println("Still everything is null ;(");
            System.out.println(doc);
        }
    }

    public void work(AllManager all, HTMLElement ex){
        if(ex.hasChildren()){
            for(HTMLElement e:ex.children()){
                String type = e.getType().toLowerCase();
                if(type.equals("div")){
                    work(all, e);
                } else if(type.equals("h2") || type.equals("h1")){
                    String style = e.getStyle();
                    TextView tv = all.headerByHTML(e.getContent());
                    if(style!=null && style.indexOf("color") > -1)
                        tv.setTextColor(all.getResources().getColor(R.color.colorAccent));
                    all.termine.addView(tv);
                } else if(type.equals("table")){
                    // komplizierter
                    for(HTMLElement tr:e.children("tr")){

                        // eine Reihe :)
                        FList<HTMLElement> tds = tr.children("td");
                        if(tds!=null){
                            String s = null;
                            for(HTMLElement td:tds){
                                s=s==null?td.getContent():s+" "+td.getContent();
                            }

                            all.termine.addView(all.textByHTML(s));
                        }
                    }
                } else {
                    String ctx = e.getContent();
                    if(ctx!=null && ctx.length()>0){
                        all.termine.addView(all.textByHTML(e.getSource()));
                    }
                }
            }
        }
    }

    public void onError(AllManager all, Exception e){
        all.newsContent.addView(all.headerByHTML(e.getClass().getName()+": "+e.getMessage()));
    }
}
