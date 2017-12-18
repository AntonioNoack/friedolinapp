package me.noack.antonio.friedomobile.extract.live;

import java.io.IOException;
import java.io.InputStream;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.NotLoggedInException;
import me.noack.antonio.friedomobile.html.LiveHTMLInterpreter;

/**
 * Created by antonio on 23.11.2017
 */

public final class LiveExtractor {

    private static final LiveHTMLInterpreter interpreter = new LiveHTMLInterpreter();

    public void work(final AllManager all, final ElementListener elementListener, final String url, final boolean exklusive) {
        work(all, elementListener, url, null, exklusive);
    }

    public void work(final AllManager all, final ElementListener elementListener, final String url, final Runnable finalky, final boolean exklusive) {

        if(exklusive && all.connection == null){
            throw new NotLoggedInException();
        }

        if(!elementListener.isLoaded()){
            elementListener.setLoaded(true);
            elementListener.setManager(all);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(exklusive){
                            interpreter.interpret(all.connection.get(all, url).getInputStream(), elementListener, finalky);
                        } else
                            interpreter.load(url, elementListener, finalky);
                    } catch (final IOException e){
                        elementListener.setLoaded(false);// fehlgeschlagen
                        all.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                elementListener.onError(e);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public void work(final AllManager all, final ElementListener elementListener, final InputStream in, final Runnable finalky) throws IOException {
        if(!elementListener.isLoaded()){
            elementListener.setLoaded(true);
            elementListener.setManager(all);
            try {
                interpreter.interpret(in, elementListener, finalky);
            } catch (final IOException e){
                elementListener.setLoaded(false);// fehlgeschlagen
                all.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        elementListener.onError(e);
                    }
                });
                throw e;
            }
        }
    }
}
