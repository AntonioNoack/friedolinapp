package me.noack.antonio.friedomobile.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by antonio on 25.11.2017
 */

public interface Saveable {

    /**
     * wie viele Schritte sollen gespeichert werden?
     * das ist insofern interessant, dass ähnliche Daten gut komprimierbar sind
     * */
    int len();

    /**
     * speichert den step-ten Teil der Daten ab
     * */
    void save(DataOutputStream out, int step) throws IOException;

    /**
     * lädt den step-ten Teil der Daten
     * */
    void load(DataInputStream in, int step) throws IOException;

}
