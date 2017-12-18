package me.noack.antonio.friedomobile.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import me.noack.antonio.friedomobile.map.details.Raum;
import me.noack.antonio.friedomobile.map.details.WegPunkt;
import me.noack.antonio.friedomobile.storage.Saveable;

import static android.opengl.GLES20.*;

/**
 * Created by antonio on 25.11.2017
 * soll ein Gebäude speichern
 * natürlich ist das etwas aufwändig... hoffentlich sind es speicherbar viele...
 * wir brauchen auf jeden Fall einen Editor, in dem wir Häuser schön erstellen können :)
 *
 * fraglich ist, was wir mit Händern mit gedrehten Teilen machen...
 * genauso fraglich ist, welche Auflösung man bei den Häusern und so braucht...
 * Annahme: 10-20cm Schrittweite, maximal 200m Länge -> 2000, also 12 bit -> wir können auf 16 bit gehen
 */

public class Gebäude implements Saveable {

    public static final float PI = 3.14159265f;

    public static Gebäude testGebäude = new Gebäude();

    /**
     * theoretisch muss man hier ja gar keine Gebäude erstellen können, oder?
     * Hängt wohl ganz davon ab, ob wir einen enthaltenen Editor machen: wäre jedenfalls schon nett :D
     *
     * Gebäude muss man in den Editor dann auch wieder laden können
     * */

    public int len(){return 3;}

    @Override public void load(DataInputStream in, int step) throws IOException {
        switch(step){
            case 0:// s
                name = in.readUTF();
                spitzname = in.readUTF();
                straßenname = in.readUTF();
                hausnummer = in.readUTF();
                break;
            case 1:// i
                räume = new Raum[in.readUnsignedShort()];
                for(int i=0;i<räume.length;i++)
                    räume[i] = new Raum(in);

                wegPunkte = new WegPunkt[in.readUnsignedShort()];
                for(int i=0;i<wegPunkte.length;i++)
                    wegPunkte[i] = new WegPunkt(this, in);

                labels = new Label[in.readUnsignedShort()];
                for(int i=0;i<labels.length;i++)
                    labels[i] = new Label(in);

                dots = new int[in.readUnsignedShort()];
                normals = new int[dots.length];

                for(int i=0;i<dots.length;i++) {
                    dots[i] = in.readUnsignedShort();
                    normals[i] = in.readUnsignedShort();
                }

                x = new int[in.readUnsignedShort()];
                y = new int[x.length];
                z = new int[x.length];

                for(int i=0;i<x.length;i++){
                    x[i] = in.readShort();
                    y[i] = in.readShort();
                    z[i] = in.readShort();
                }

                nx = new int[in.readUnsignedShort()];
                ny = new int[nx.length];
                nz = new int[nx.length];

                for(int i=0;i<nx.length;i++){
                    nx[i] = in.readShort();
                    ny[i] = in.readShort();
                    nz[i] = in.readShort();
                }

                break;
            case 2:// d
                radius = in.readUnsignedShort() * .01f;
                xPosition = in.readShort();
                yPosition = in.readShort();
                for(int i=0;i<wegPunkte.length;i++)
                    wegPunkte[i] = new WegPunkt(this, in);


        }
    }

    // wände = new Wand[in.readUnsignedShort()];
    // wände sind nicht wirklich, was in ein Modell gehört: darein gehören Dreiecke und Wegpunkte für den Pfadfinder

    @Override public void save(DataOutputStream out, int step) throws IOException {
        switch(step){
            case 0:// s
                out.writeUTF(name);
                out.writeUTF(spitzname);
                out.writeUTF(straßenname);
                out.writeUTF(hausnummer);
                break;
            case 1:// i

                out.writeShort(räume.length);
                for(Raum raum:räume)
                    raum.write(out);

                out.writeShort(wegPunkte.length);
                for(WegPunkt wegPunkt:wegPunkte)
                    wegPunkt.write(out);


                /*wegPunkte = new WegPunkt[in.readUnsignedShort()];

                labels = new Label[in.readUnsignedShort()];
                for(int i=0;i<labels.length;i++){
                    labels[i] = new Label(in);
                }

                dots = new int[in.readUnsignedShort()];
                normals = new int[dots.length];

                for(int i=0;i<dots.length;i++) {
                    dots[i] = in.readUnsignedShort();
                    normals[i] = in.readUnsignedShort();
                }

                x = new int[in.readUnsignedShort()];
                y = new int[x.length];
                z = new int[x.length];

                for(int i=0;i<x.length;i++){
                    x[i] = in.readShort();
                    y[i] = in.readShort();
                    z[i] = in.readShort();
                }

                nx = new int[in.readUnsignedShort()];
                ny = new int[nx.length];
                nz = new int[nx.length];

                for(int i=0;i<nx.length;i++){
                    nx[i] = in.readShort();
                    ny[i] = in.readShort();
                    nz[i] = in.readShort();
                }*/

                out.writeShort(räume.length);
                out.writeShort(wegPunkte.length);
                break;
            case 2:// d
                out.writeShort((int) (radius * 100));
                out.writeShort((int) xPosition);
                out.writeShort((int) yPosition);
        }
    }

    public class Label {
        int x, y, z, sz, phi;
        String text;
        public Label(DataInputStream in) throws IOException {
            text = in.readUTF();
            x = in.readShort();
            y = in.readShort();
            z = in.readShort();
            sz = in.readShort();
            phi = in.readShort();
        }
    }

    private int[] x, y, z, nx, ny, nz;
    private int[] dots, normals;
    private Label[] labels;
    public Raum[] räume;
    public WegPunkt[] wegPunkte;

    public void upload(int buffer){

        final int byteLength = dots.length * 2 * 3;

        ShortBuffer sb = ByteBuffer.allocateDirect(byteLength).asShortBuffer();
        for(int i=0;i<dots.length;i++){
            int j = dots[i];
            int k = normals[i];
            sb.put((short)  x[j]).put((short)  y[j]).put((short)  z[j])
              .put((short) nx[k]).put((short) ny[k]).put((short) nz[k]);
        }

        glBindBuffer(GL_ARRAY_BUFFER, buffer);
        glBufferData(GL_ARRAY_BUFFER, byteLength, sb, GL_STATIC_DRAW);
    }

    public void draw(int buffer){
        glBindBuffer(GL_ARRAY_BUFFER, buffer);

        glVertexAttribPointer(0, 3, GL_SHORT, false, 12, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_SHORT, false, 12, 6);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_TRIANGLES, 0, dots.length);
    }

    static final float f32k = 1f/0x8000f;

    public float shortToOne(int i){
        return i * f32k - 1f;
    }

    /**
     * nicht für Grafik, sondern für globale Dinge! Sollte nicht misbraucht werden
     * */
    public float globalX(float x, float y){return + x * cosphi32k + y * sinphi32k - 2f;}
    public float globalY(float x, float y){return - x * sinphi32k + y * cosphi32k;}
    public float globalZ(float z){return z * f32k * sizeZ + zPosition;}

    // public Treppe[] treppen;// zu speziell, kann ohne Probleme verallgemeinert werden


    int id;// vllt... schon irgendwie praktischer | Hausnummer ist String wegen 11c
    public String name, spitzname, straßenname, hausnummer;
    public float radius, sizeZ, phi, sinphi, sinphi32k, cosphi, cosphi32k, xPosition, yPosition, zPosition;

    static float f(int i){
        return i;
    }

    static int i(float f){
        return Math.round(f);
    }

    /*class Treppe {

        public Treppe(int sx, int sy, int sz, int ex, int ey, int ez, int stufenanzahl, int breite){
            this.sx = f(sx);
            this.sy = f(sy);
            this.sz = f(sz);
            this.ex = f(ex);
            this.ey = f(ey);
            this.ez = f(ez);
            this.stufenanzahl = stufenanzahl;
            this.breite = f(breite);
        }

        int stufenanzahl;
        float sx, sy, sz, ex, ey, ez, breite;
    }*/
}
