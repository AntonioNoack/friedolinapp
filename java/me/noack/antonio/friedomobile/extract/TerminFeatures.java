package me.noack.antonio.friedomobile.extract;

import android.util.LayoutDirection;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;

import me.noack.antonio.friedomobile.AllManager;

/**
 * Created by antonio on 23.11.2017
 */

public class TerminFeatures extends Features {

    protected ArrayList<String> data = new ArrayList<>();

    public void put(String value){
        data.add(value);
    }

    @Override public void drawOnList(final AllManager all, ViewGroup list){
        if(data.size() > 3){
            String title = data.get(3);
            View v = all.textByText(all.shortened(data.get(0)+"-"+data.get(1)+" "+all.abkürzung(title)+" "+title, 60));
            v.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    drawDetails(all);
                }
            });
            list.addView(v);
        }// andernfalls verkrüppeltes Element...
    }

    final static RelativeLayout.LayoutParams
            relWWLeft = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT),
            relWWRight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
    ;

    static {
        relWWLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        relWWLeft.addRule(RelativeLayout.ALIGN_BOTTOM);
        relWWRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        relWWRight.addRule(RelativeLayout.ALIGN_BOTTOM);
    }

    public void drawDetails(AllManager all){
        LinearLayout v = all.details;
        v.removeAllViews();

        v.addView(all.headerByText(data.get(3)));

        TableLayout tl = new TableLayout(all);
        tl.setLayoutParams(linMW);

        int s = data.size()-1;
        String nr = s<2?null:data.get(2), ort = s<4?null:data.get(4), raum = s<5?null:data.get(5), parallelgruppe = s<6?null:data.get(6), lehrperson = s<7?null:data.get(7), bemerkung = s<8?null:data.get(8);

        if(lehrperson != null)
            lehrperson = lehrperson.replace("&nbsp;", " ").trim();

        v.addView(tl);

        tl.addView(keyValuePair2(all, all.textByText("Zeit:"), all.textByText(data.get(0)+" - "+data.get(1))));
        if(nr!=null)
            tl.addView(keyValuePair2(all, all.textByText("Nr.:"), all.textByText(nr)));
        if(ort!=null || raum!=null) // wird in Zukunft bestimmt noch klickbar :)
            tl.addView(keyValuePair2(all, all.textByText("Ort:"), all.textByText(ort==null?raum:raum==null?ort:ort+" "+raum)));
        if(parallelgruppe!=null)
            tl.addView(keyValuePair2(all, all.textByText("PGruppe:"), all.textByText(parallelgruppe)));
        if(lehrperson!=null && lehrperson.length() > 2)
            tl.addView(keyValuePair2(all, all.textByText("Lehrperson:"), all.textByText(lehrperson)));

        if(bemerkung!=null)
            v.addView(all.headerByText(bemerkung));

        all.showChild(all.detailsSite);
    }

    public String toString(){
        if(data.isEmpty()) return "{}";
        String r = data.get(0);
        for(int i=1,l=data.size();i<l;i++) r+=", "+data.get(i);
        return r;
    }
}
