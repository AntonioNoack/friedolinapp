package me.noack.antonio.friedomobile.map.details;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.noack.antonio.friedomobile.map.Gebäude;

/**
 * 25.11.2017: ein Raum10 ist ein Objekt, das Türen, Fenster, vielleicht ein bisschen Einrichtung, Treppen und so hat... wobei Treppen eigentlich in Gebäude gehören
 * in einer perfekten Welt könnte man einem Raum10 zwar ein Rechteck zuweisen, aber wir machen das lieber über separate Wände
 *
 * 02.12.2017: ein Raum10 ist ein Scheinobjekt, das für die Suche existiert
 * */
public class Raum {

    String name, spitzname;

    float globalX, globalY, globalZ;

    int subX = 0, subY = 0, subZ = 0;
    int ctr = 0;
    public Raum(DataInputStream in) throws IOException {
        this.name = in.readUTF();
        this.spitzname = in.readUTF();
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(spitzname);
    }

    public void addWegPunkt(WegPunkt punkt){
        subX += punkt.x;
        subY += punkt.y;
        subZ += punkt.z;
        ctr++;
    }

    public void finalize(Gebäude owner){
        float f = 1f/ctr, fx = subX*f, fy = subY*f, fz = subZ*f;
        globalX = owner.globalX(fx, fy);
        globalY = owner.globalY(fx, fy);
        globalZ = owner.globalZ(fz);
    }
}
