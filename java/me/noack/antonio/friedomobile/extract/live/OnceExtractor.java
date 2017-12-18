package me.noack.antonio.friedomobile.extract.live;

import me.noack.antonio.friedomobile.AllManager;

/**
 * Created by antonio on 25.11.2017
 */

public abstract class OnceExtractor implements ElementListener {

    private boolean loaded;
    public AllManager all;

    @Override
    public void setManager(AllManager all) {
        this.all = all;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
