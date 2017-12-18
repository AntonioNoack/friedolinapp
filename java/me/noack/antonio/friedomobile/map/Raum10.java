package me.noack.antonio.friedomobile.map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.extract.Features;

/**
 * Created by antonio on 27.11.2017
 */

public class Raum10 extends Features {

    @Override public void drawDetails(AllManager all) {

        LinearLayout linearLayout = all.details;
        linearLayout.removeAllViews();

        TableLayout tl = new TableLayout(all);
        tl.setLayoutParams(linMW);

        if(name != null) linearLayout.addView(all.headerByText(name));

        linearLayout.addView(tl);

        if(addr != null) tl.addView(keyValuePair2(all, all.textByText("Adresse:"), all.textByText(addr)));
        if(geba != null) tl.addView(keyValuePair2(all, all.textByText("Gebäude:"), all.textByText(geba)));
        if(eiri != null) tl.addView(keyValuePair2(all, all.textByText("Einrichtung:"), all.textByText(eiri)));
        if(emai != null) tl.addView(keyValuePair2(all, all.textByText("Email:"), all.textByText(emai)));
        if(webs != null) tl.addView(keyValuePair2(all, all.textByText("Website:"), all.textByText(webs)));

        if(beme != null) linearLayout.addView(all.headerByText(beme));

        all.showChild(R.id.detailsSite);
    }

    @Override public void drawOnList(AllManager all, ViewGroup list) {
        list.addView(layout);
    }

    public static ArrayList<Raum10> räume;

    public String name, addr, geba, eiri, beme, emai, webs,
            lcn, lca, lcg, lce, lcb, lcm, lcw;
    View layout;
    private Raum10(){

    }

    private void makeLayout(final AllManager all){
        layout = all.textByHTML(name);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                all.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        drawDetails(all);
                    }
                });
            }
        });
    }

    private static boolean inited = false;
    public static void init(AllManager all){
        if(inited) return;
        else inited = true;

        try {

            DataInputStream in = new DataInputStream(all.getResources().openRawResource(R.raw.raum_27_11_2017));

            int size = in.readShort();
            räume = new ArrayList<>(size);
            String[]
            name = new String[size], lcn = new String[size],
            addr = new String[256], lca = new String[256],
            geba = new String[256], lcg = new String[256],
            eiri = new String[256], lce = new String[256],
            beme = new String[256], lcb = new String[256],
            emai = new String[256], lcm = new String[256],
            webs = new String[256], lcw = new String[256];

            int namei=0, addri=0, gebai=0, eirii=0, bemei=0, emaii=0, websi=0;
            int tmp;
            for(int i=0;i<size;i++){
                Raum10 r = new Raum10();
                // Name
                if((tmp=in.readShort())>namei){
                    r.lcn = lcn[tmp] = (r.name = name[++namei] = in.readUTF().replace("#", "")).toLowerCase();
                } else {r.name = name[tmp];r.lcn = lcn[tmp];}
                // Adresse
                if((tmp=in.readUnsignedByte())>addri){
                    r.lca = lca[tmp] = (addr[++addri] = in.readUTF()).toLowerCase();
                } else {r.lca = lca[tmp];r.addr = addr[tmp];}
                // Gebäude
                if((tmp=in.readUnsignedByte())>gebai){
                    r.lcg = lcg[tmp] = (geba[++gebai] = in.readUTF()).toLowerCase();
                } else {r.lcg = lcg[tmp];r.geba = geba[tmp];}
                // Einrichtungen
                if((tmp=in.readUnsignedByte())>eirii){
                    r.lce = lca[tmp] = (eiri[++eirii] = in.readUTF()).toLowerCase();
                } else {r.lce = lca[tmp];r.eiri = eiri[tmp];}
                // Bemerkungen
                if((tmp=in.readUnsignedByte())>bemei){
                    r.lcb = lca[tmp] = (beme[++bemei] = in.readUTF()).toLowerCase();
                } else {r.lcb = lca[tmp];r.beme = beme[tmp];}
                // Emails
                if((tmp=in.readUnsignedByte())>emaii){
                    r.lcm = lca[tmp] = (emai[++emaii] = in.readUTF()).toLowerCase();
                } else {r.lcm = lca[tmp];r.emai = emai[tmp];}
                // Website
                if((tmp=in.readUnsignedByte())>websi){
                    r.lcw = lca[tmp] = (webs[++websi] = in.readUTF()).toLowerCase();
                } else {r.lcw = lca[tmp];r.webs = webs[tmp];}

                räume.add(r);
            }

            for(Raum10 r:räume){
                r.makeLayout(all);
            }

            in.close();
        } catch(IOException e){
            System.out.println("Fehler beim Laden der Raumdatei :(");
        }
    }

}
