package me.noack.antonio.friedomobile.extract.live;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 23.11.2017
 */

public interface ElementListener {
    /**
     * ein Element inklusiver seiner Attribute wurde begonnen
     * */
    void onElementStarted(HTMLElement element);
    /**
     * ein Element inklusiver seiner Attribute, Kinder und seines Inhaltes wurde fertiggestellt
     * */
    void onElementFinished(HTMLElement element);
    /**
     * das Dokument wurde zuende gelesen
     * */
    void onFinished(HTMLDocument doc);
    /**
     * es ist was schief gegangen: der Nutzer sollte vielleicht informiert werden
     * */
    void onError(Exception e);
    void setManager(AllManager all);
    void setLoaded(boolean bool);
    boolean isLoaded();
}
