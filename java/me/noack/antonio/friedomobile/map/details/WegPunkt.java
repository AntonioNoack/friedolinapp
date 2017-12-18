package me.noack.antonio.friedomobile.map.details;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.noack.antonio.friedomobile.map.Gebäude;

/**
 * Created by antonio on 02.12.2017
 */

public class WegPunkt {
    double x, y, z;
    int belongsToIndex;
    public WegPunkt(Gebäude owner, DataInputStream in) throws IOException {
        float fx = in.readShort(), fy = in.readShort();
        this.x = owner.globalX(fx, fy);
        this.y = owner.globalY(fx, fy);
        this.z = owner.globalZ(in.readShort());
        // Raum10-Index; deren mittlere Position lässt einen den Raummittelpunkt ausrechnen :)
        owner.räume[belongsToIndex = in.readUnsignedShort()].addWegPunkt(this);
    }

    public void write(DataOutputStream out){

    }
}
