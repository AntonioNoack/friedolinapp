package me.noack.antonio.friedomobile.stupla;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Set;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.party.Maths;
import me.noack.antonio.friedomobile.party.Party;
import me.noack.antonio.friedomobile.party.SendMessage;
import me.noack.antonio.friedomobile.struct.FList;

/**
 * Created by antonio on 17.12.2017
 */

public class TerminPlan {

    public static int height, multiply;

    private ArrayList<Termin>[] termine = new ArrayList[4];
    private final String date;

    public void save(SharedPreferences.Editor edit, SharedPreferences pref){
        String indices = null;
        for(ArrayList<Termin> termins:termine){
            for(Termin termin:termins){
                indices = indices==null?termin.getIndex()+"":indices+","+termin.getIndex();
            }
        }
        if(indices == null) indices="";
        edit.putString(date, indices);

        for(ArrayList<Termin> termins:termine){
            for(Termin termin:termins){
                termin.save(date, edit);
            }
        }
    }

    public TerminPlan(String date){
        for(int i=0;i<termine.length;i++){
            termine[i] = new ArrayList<>();
        } this.date = date;
    }

    public TerminPlan(String date, SharedPreferences pref){
        this(date);
        String indices = pref.getString(date, null);
        if(indices != null && indices.length() > 0){
            for(String index:indices.split(",")){
                Termin termin = new Termin(date, index, pref);
                if(termin.getRow() < termine.length)
                    termine[termin.getRow()].add(termin);
            }
        }
    }

    public void add(Termin termin){
        termine[termin.getRow()].add(termin);
    }
    public void remove(Termin termin){
        for(ArrayList<Termin> termins:termine){
            termins.remove(termin);
        }
    }

    public void display(final AllManager all, final LinearLayout[] lins){

        actualPlan = this;

        int startTime = 15*4;
        int endTime = 9*4;

        for(ArrayList<Termin> termins:termine){
            for(Termin termin:termins){
                int start = termin.getStart()-4, end = termin.getEnd();
                if(start < startTime) startTime = start;
                if(end > endTime) endTime = end;
            }
        }

        if(startTime > endTime){
            int k = startTime;
            startTime = endTime;
            endTime = k;
        }

        while(endTime - startTime < 24){
            startTime -= 4;
            endTime += 4;
        }

        for(LinearLayout lin:lins) lin.removeAllViews();

        for(int i=startTime/4*4-2;i<endTime;i+=4){// funktioniert linear
            TextView tv = all.headerByText((i>>2)+"");
            tv.setWidth(multiply * 4);
            lins[0].addView(tv);
        }

        int lini = 1;
        for(ArrayList<Termin> termins:termine){// funktioniert nicht linear! Muss erst sortiert werden und ggf müssen Stopper einbezogen werden
            LinearLayout toFill = lins[lini++];

            // füge die Daten hinzu...
            Collections.sort(termins, new Comparator<Termin>() {
                @Override
                public int compare(Termin a, Termin b) {
                    return a.getStart()-b.getStart();
                }
            });

            int pos = startTime;
            FList<TextView> tvs = new FList<>();
            if(termins.size() == 0){
                TextView sp = all.headerByText("");
                sp.setWidth(10);
                sp.setMinHeight(height);
                sp.setMaxHeight(height);
                toFill.addView(sp);
            } else
            for(final Termin termin:termins){

                int dif = termin.getStart()-pos;
                if(dif > 0){
                    // wir brauchen einen Platzhalter
                    TextView sp = all.headerByText("");
                    sp.setWidth(dif*multiply);
                    toFill.addView(sp);

                }// if dif < 0 Problem

                TextView tv = all.headerByText(termin.getName());
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setWidth(termin.getDuration()*multiply);
                tv.setMinHeight(height);
                tv.setMaxHeight(height);
                int color = termin.getColor();
                tv.setBackgroundColor(Color.rgb((color >> 16) & 255, (color >> 8) & 255, color & 255));
                toFill.addView(tv);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        edit(all, date, termin, lins);
                    }
                });
                tvs.add(tv);

                pos = termin.getEnd();
            }
        }
    }

    public static String num(int i){
        return i<10?"0"+i:""+i;
    }

    public static String time(int i){
        int p = i&3;
        return num(i>>2)+":"+(p<2?p<1?"00":"15":p<3?"30":"45");
    }

    public void edit(AllManager all, String date, Termin termin, LinearLayout[] lins){

        editing = termin;

        all.editDateTitle.setText(termin.getName());
        all.editDateDesc.setText(termin.getDesc());
        all.editDateStart.setText(time(termin.getStart()));
        all.editDateEnd.setText(time(termin.getEnd()));
        all.editDateAll.setChecked(true);
        all.editDateRow.setText((termin.getRow()+1)+"");
        double[] hsv = toHSV(termin.getColor());
        all.editDateH.setProgress((int) (hsv[0]*1000));
        all.editDateS.setProgress((int) (hsv[1]*1000));
        all.editDateV.setProgress((int) (hsv[2]*1000));
        int tcol = termin.isDark()?-1:0xff000000;
        for(TextView v:all.editDate) v.setTextColor(tcol);

        all.editDateDate.setVisibility(View.GONE);
        all.editDateAll.setVisibility(View.VISIBLE);

        all.showChild(R.id.editDate);

    }

    public static Termin editing;
    public static TerminPlan actualPlan;

    public static final View.OnClickListener delete = new View.OnClickListener() {
        @Override public void onClick(View view) {
            AllManager all = AllManager.instance;

            if(editing == null){// Party

                // ein Datum, das nicht existiert -> wird nicht angezeigt :) und zufällig auf dem Server irgendwann gelöscht
                Calendar cal = GregorianCalendar.getInstance();
                cal.add(Calendar.MONTH, 6);
                all.editDateDate.setText((cal.get(Calendar.DAY_OF_MONTH))+"."+(cal.get(Calendar.MONTH)+1));
                save.onClick(view);

            } else {
                SharedPreferences.Editor edit = all.pref.edit();
                all.closeKeyboard();
                actualPlan.remove(editing);
                actualPlan.save(edit, all.pref);
                edit.commit();
                actualPlan.display(all, all.stupla);
                all.menu();
            }
        }
    }, save = new View.OnClickListener() {// TODO muss noch testen, ob es für alle gleichen Namens gelten soll und welche das ggf sind
        @Override public void onClick(View view) {

            AllManager all = AllManager.instance;
            boolean isParty = editing == null;

            SharedPreferences.Editor edit = isParty?null:all.pref.edit();

            all.closeKeyboard();

            boolean applyToAll = !isParty && all.editDateAll.isChecked();// alle mit gleichem Namen UND gleichem Startdatum und gleichem Enddatum werden überschrieben

            String
                    n = all.editDateTitle.getText().toString(),
                    t = all.editDateDesc.getText().toString();
            int
                    s = toTime(all.editDateStart.getText().toString(), isParty?0:editing.getStart()),
                    d = toTime(all.editDateEnd.getText().toString(), isParty?s+8:editing.getEnd()) - s,// endlich den Fehler hier gefunden :D
                    r = toInt(all.editDateRow.getText().toString(), isParty?1:editing.getRow()+1)-1,
                    c = rgb(all);

            if(isParty){
                SendMessage.write(all, new Party(r, 0, 0, s, d, r, c, null, n, t));
                return;
            }

            String oldTitle = editing.getName();
            int oldStart = editing.getStart(), oldDura = editing.getDuration();

            if(applyToAll){
                for(String week:all.pref.getString("9_weeks", null).split("\0")){
                    String[] wek = week.split("\n");// name, value
                    for(int i=0;i<7;i++){
                        String date = i+"_"+wek[1];
                        TerminPlan tp = new TerminPlan(date, all.pref);
                        for(ArrayList<Termin> termins:tp.termine){
                            for(int j=0;j<termins.size();j++){
                                Termin termin = termins.get(j);
                                if(oldTitle.equals(termin.getName()) && termin.getStart() == oldStart && termin.getDuration() == oldDura){
                                    tp.remove(termin);
                                    tp.add(termin);
                                    termin.setName(n);
                                    termin.setDesc(t);
                                    termin.setStart(s);
                                    termin.setDuration(d);
                                    termin.setRow(r);
                                    termin.setColor(c);
                                    termin.save(date, edit);
                                }
                            }
                        }
                    }
                }
            }

            editing.setName(n);
            editing.setDesc(t);
            editing.setStart(s);
            editing.setDuration(d);
            editing.setRow(r);
            editing.setColor(c);
            editing.save(actualPlan.date, edit);

            actualPlan.remove(editing);
            actualPlan.add(editing);
            actualPlan.save(edit, all.pref);

            edit.commit();

            actualPlan.display(all, all.stupla);
            all.menu();
        }
    }, newTermin = new View.OnClickListener() {
        @Override public void onClick(View view) {
            AllManager all = AllManager.instance;
            actualPlan.edit(all, actualPlan.date, new Termin(10*4, 2*4, 3, Stundenplan.defaultColor, ((float) Math.random())+"", "", ""), all.stupla);
        }
    };

    static int toTime(String s, int alt){
        try {
            String[] parts = s.split(":");
            if(parts.length==1){
                return Integer.parseInt(parts[0])*4;
            } else
                return Integer.parseInt(parts[0])*4+(Integer.parseInt(parts[1])+7)/15;
        } catch(NumberFormatException e){
            return alt;
        }
    }

    static int toInt(String s, int alt){
        try {
            return Integer.parseInt(s);
        } catch(NumberFormatException e){
            return alt;
        }
    }

    static final double f255 = 1./255., deg60 = 1./6., fdeg60=6;
    public static double[] toHSV(int rgb){
        double r = (rgb>>16)&255, g = (rgb>>8)&255, b = rgb&255;
        r*=f255;g*=f255;b*=f255;
        double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), delta = max-min;
        double hue = delta==0?0:max==r?((g-b)/delta)%6.:max==g?(b-r)/delta+2:(r-g)/delta+4;
        return new double[]{hue*deg60, max==0?0:delta/max, max};
    }

    static int toRGB(double h, double s, double v){
        double c = v*s;
        double x = c*(1-Math.abs(((h*fdeg60)%2)-1));
        double m = v-c;

        double r, g, b;
        if(h < deg60){
            r=c;g=x;b=0;
        } else if(h<2*deg60){
            r=x;g=c;b=0;
        } else if(h<3*deg60){
            r=0;g=c;b=x;
        } else if(h<4*deg60){
            r=0;g=x;b=c;
        } else if(h<5*deg60){
            r=x;g=0;b=c;
        } else {
            r=c;g=0;b=x;
        }

        r+=m;g+=m;b+=m;
        r*=255;g*=255;b*=255;

        return ((int) r << 16) + ((int) g << 8) + (int) b;
    }

    static int rgb(AllManager all){
        return toRGB(all.editDateH.getProgress()*.001, all.editDateS.getProgress()*.001, all.editDateV.getProgress()*.001);
    }

    public static final SeekBar.OnSeekBarChangeListener seekli = new SeekBar.OnSeekBarChangeListener() {
        @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            AllManager all = AllManager.instance;
            int rgb = rgb(all);
            all.closeKeyboard();
            all.editDateConfig.setBackgroundColor(Color.rgb((rgb>>16)&255, (rgb>>8)&255, rgb&255));
            int tcol = Maths.isDark(rgb)?-1:0xff000000;
            for(TextView v:all.editDate) v.setTextColor(tcol);
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    };
}
