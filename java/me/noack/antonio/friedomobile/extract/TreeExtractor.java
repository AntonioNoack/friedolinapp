package me.noack.antonio.friedomobile.extract;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.extract.live.OnceExtractor;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;
import me.noack.antonio.friedomobile.struct.Twig;

/**
 * Created by antonio on 25.11.2017
 */

public class TreeExtractor extends Extractor {
    // https://friedolin.uni-jena.de/qisserver/rds?state=tsearch&treetype=2&val=1&field=eid&moduleParameter=raumSearch&subdir=raum&menuid=&change_mode=&expand=a&asi=

    public Twig<String> tree = new Twig<>(null), thatTwig;

    @Override
    public void work(AllManager all, HTMLDocument doc) {

    }

    @Override
    public void onError(AllManager all, Exception e) {
        // :/
    }
}
