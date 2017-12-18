package me.noack.antonio.friedomobile.extract.live;

import java.lang.reflect.Array;
import java.util.ArrayList;

import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 17.12.2017
 */

public class NameExtractor extends OnceExtractor {

    boolean start;
    public ArrayList<String> strings = new ArrayList<>();
    Runnable onFinished;

    public NameExtractor(Runnable onFinished){
        this.onFinished = onFinished;
    }

    @Override public void onElementStarted(HTMLElement element) {
        if("FSUloginstatusinternal".equalsIgnoreCase(element.getClassName())){
            start = true;
        }
    }

    @Override public void onElementFinished(HTMLElement element) {
        if("FSUloginstatusinternal".equalsIgnoreCase(element.getClassName())){
            onFinished.run();
            start = false;
        } else if(start){
            if(!element.hasChildren() && !"a".equalsIgnoreCase(element.getType())){
                String ctx = element.getContent();
                if(ctx!=null && ctx.length()>1) {
                    while(ctx.endsWith(" ")) ctx = ctx.substring(0, ctx.length()-1);
                    strings.add(ctx);
                }
            }
        }
    }

    @Override public void onFinished(HTMLDocument doc) {
        if(strings.size() == 0){
            all.info("Irgendwas ist schief gegangen :/");
        } else if(strings.size()>2){
            all.connection.loginName = strings.get(0)+"\0"+strings.get(2);
            all.connection.save(all.pref);
        }
    }

    @Override
    public void onError(Exception e) {
        all.info("Error: "+e.getMessage());
    }
}
