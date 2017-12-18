package me.noack.antonio.friedomobile.extract.live;

import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 18.12.2017
 */

public class PersonenExtractor extends OnceExtractor {

    @Override
    public void onElementStarted(HTMLElement element) {

    }

    @Override
    public void onElementFinished(HTMLElement element) {

    }

    @Override
    public void onFinished(HTMLDocument doc) {
        this.setLoaded(false);
    }

    @Override
    public void onError(Exception e) {

    }
}
