package me.noack.antonio.friedomobile.extract.live;

import android.view.ViewGroup;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.extract.TerminFeatures;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 23.11.2017
 */

public class HeuteExtractor extends OnceExtractor {

    public void onElementStarted(HTMLElement element){/* egal */}

    private int oldtype;
    private TerminFeatures feat;
    private ViewGroup vg;

    public void onElementFinished(HTMLElement element){
        // odd/even
        String clazz = element.getClassName();
        if(clazz!=null){
            int type;
            switch(clazz){
                case "mod_n_odd":
                   type = 1;
                    break;
                case "mod_n_even":
                    type = 2;
                    break;
                default:
                    type = -3;
            }

            if(type > -1){

                if(type != oldtype){
                    oldtype = type;
                    if(feat != null){
                        final TerminFeatures toDraw = feat;
                        all.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {toDraw.drawOnList(all, vg);}
                        });// muss konstant bleiben :)
                    } feat = new TerminFeatures();
                }

                feat.put(element.getContent());
            }
        }
    }

    public void onFinished(HTMLDocument doc){
        if(feat!=null){
            all.runOnUiThread(new Runnable() {
                @Override public void run() {
                    feat.drawOnList(all, vg);
                    getViewGroup().addView(all.headerByHTML("---"));
                }
            });
        }
    }

    public void onError(Exception e){
        getViewGroup().addView(all.headerByHTML(e.getClass().getName()+": "+e.getMessage()));
    }

    public ViewGroup getViewGroup(){
        return all.heute;
    }

    public void setManager(AllManager all){
        this.all = all;
        this.oldtype = -1;
        this.vg = this.getViewGroup();
    }
}
