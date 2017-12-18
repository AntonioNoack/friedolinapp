package me.noack.antonio.friedomobile.extract;

import java.io.IOException;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLInterpreter;

/**
 * Created by antonio on 22.11.2017
 */

public abstract class Extractor {

    private static final HTMLInterpreter interpreter = new HTMLInterpreter();

    private boolean loaded = false;

    public void work(final AllManager all, final String url){
        if(!loaded){
            loaded = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final HTMLDocument doc = interpreter.load(url);
                        if(doc != null){
                            all.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //try {
                                        work(all, doc);
                                   /* } catch(Exception e){
                                        onError(all, e);
                                    }*/
                                }
                            });
                        } else {
                            loaded = false;// fehlgeschlagen...
                            all.runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    onError(all, new NullPointerException("doc = null"));
                                }
                            });
                        }
                    } catch (final IOException e){
                        loaded = false;// fehlgeschlagen
                        all.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                onError(all, e);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public abstract void work(AllManager all, HTMLDocument doc);
    public abstract void onError(AllManager all, Exception e);
}
