package me.noack.antonio.friedomobile.extract.live.modul;

import me.noack.antonio.friedomobile.extract.live.OnceExtractor;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 22.12.2017
 */

public class ModulCodeByIDExtractor extends OnceExtractor {

    public String url;
    public Runnable whenFinished;

    public ModulCodeByIDExtractor(){}

    @Override
    public void onElementFinished(HTMLElement element) {
        if("td".equalsIgnoreCase(element.getType()) && "mod_n_odd".equalsIgnoreCase(element.getClassName()) && element.hasChildren() && url==null){
            // gefunden <3, allerdings ist es nur ein Link...
            // im zweiten Element sollte der Link sein
            for(HTMLElement child:element.children()){
                if(child.getType().equalsIgnoreCase("a")){
                    // gefunden :D
                    url = child.get("href");
                    if(url!=null){
                        whenFinished.run();
                        whenFinished = null;
                        break;
                    }
                }
            }
        }
    }

    @Override public void onElementStarted(HTMLElement element) {}
    @Override public void onFinished(HTMLDocument doc) {
        System.out.println("finished "+(whenFinished==null?"success":"missing"));
    }

    @Override public void onError(Exception e) {
        all.info("Error: "+e.getMessage());
    }
}
