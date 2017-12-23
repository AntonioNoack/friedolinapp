package me.noack.antonio.friedomobile.extract.live;

import java.io.IOException;
import java.io.InputStream;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.NotLoggedInException;
import me.noack.antonio.friedomobile.html.HTMLInterpreter;
import me.noack.antonio.friedomobile.html.connection.ConnectionType;
import me.noack.antonio.friedomobile.html.connection.CookieConnection;
import me.noack.antonio.friedomobile.html.LiveHTMLInterpreter;

/**
 * Created by antonio on 23.11.2017
 */

public final class LiveExtractor {

    private static final LiveHTMLInterpreter interpreter = new LiveHTMLInterpreter();

    public void work(final AllManager all, final ElementListener elementListener, final String url, ConnectionType type) {
        work(all, elementListener, url, null, type);
    }

    public void work(final AllManager all, final ElementListener elementListener, final String url, final Runnable finalky, final ConnectionType type) {

        if(type == ConnectionType.LOGGED_IN && all.connection == null){
            throw new NotLoggedInException();
        }

        if(!elementListener.isLoaded()){
            elementListener.setLoaded(true);
            elementListener.setManager(all);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch(type){
                            case WITH_COOKIE:
                                interpreter.interpret(CookieConnection.get(url, null).getInputStream(), elementListener, finalky);break;
                            case LOGGED_IN:
                                interpreter.interpret(all.connection.get(all, url).getInputStream(), elementListener, finalky);break;
                            case ANONYMOUS:
                                interpreter.load(url, elementListener, finalky);break;
                        }
                    } catch (final IOException|NullPointerException e){
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
