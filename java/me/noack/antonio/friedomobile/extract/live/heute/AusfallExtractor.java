package me.noack.antonio.friedomobile.extract.live.heute;

import android.view.ViewGroup;

/**
 * Created by antonio on 23.11.2017
 */

public class AusfallExtractor extends HeuteExtractor {
    public ViewGroup getViewGroup(){
        return all.ausfall;
    }
}
