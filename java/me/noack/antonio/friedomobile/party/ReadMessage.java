package me.noack.antonio.friedomobile.party;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import me.noack.antonio.friedomobile.AllManager;

import static me.noack.antonio.friedomobile.stupla.TerminPlan.num;
import static me.noack.antonio.friedomobile.stupla.TerminPlan.time;

/**
 * Created by antonio on 21.12.2017
 * liest alle aktuellen Partys aus der Liste
 */

public class ReadMessage {

    public static ArrayList<Party> partys;
    public static boolean inited = false;

    public static void read(final AllManager all) {

        inited = true;
        partys = new ArrayList<>();
        Calendar cal = GregorianCalendar.getInstance(), sec = GregorianCalendar.getInstance();
        Date date = cal.getTime();
        date.setTime(date.getTime()-3*3600*1000);// 3h Zeitdifferenz
        cal.setTime(date);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://phychi.com/fsu/events").openConnection();
            con.setRequestMethod("GET");
            InputStream in = con.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            String s;
            while((s=r.readLine())!=null){
                Party party = Party.get(s);
                if(party != null){
                    int t = party.getEnd();
                    sec.set(cal.get(Calendar.YEAR), party.getMonth(), party.getDay()+1, t>>2, (t&3)*15);
                    if(cal.compareTo(sec) > 0){
                        sec.add(Calendar.YEAR, 1);
                    }
                    sec.add(Calendar.MONTH, -3);
                    // nun ist sec in diesem, oder dem nächsten Jahr...: ist denn this+3 Monate auch vor sec?
                    if(cal.compareTo(sec) > 0){
                        partys.add(party);
                    }
                }
            }

            Collections.sort(partys, new Comparator<Party>() {
                @Override public int compare(Party a, Party b) {
                    return a.compareStart() - b.compareStart();
                }
            });

            all.runOnUiThread(new Runnable() {
                @Override public void run() {
                    all.partyList.removeAllViews();
                    for(final Party party:partys){
                        // Zeige die Party an

                        Button tv = new Button(all);
                        tv.setText(party.getName());
                        tv.setLayoutParams(all.LMW);

                        final TextView ds = new TextView(all);
                        ds.setText(party.getDesc()+"\n"+party.getIndex()+"\n"+time(party.getStart())+" - "+time(party.getEnd())+", "+num(party.getDay()+1)+"."+num(party.getMonth()+1)+".");
                        ds.setVisibility(View.GONE);
                        ds.setGravity(Gravity.CENTER);
                        ds.setLayoutParams(all.LMW);

                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View view) {
                                // editiere das Event, wenn es uns gehört; da sich der Besitzer wechseln kann, wird er erst hier getestet
                                // und sonst zeige seine Details?... aufklappbar hat Stil :D
                                // edit: naja, der Nutzer wird schon 4 verwalten können, auch wenn ich große Zweifel daran habe, dass das der Wahrheit entspricht
                                ds.setVisibility(ds.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                            }
                        });
                        tv.setBackgroundColor(0xff000000 | party.getColor());
                        if(party.isDark()) tv.setTextColor(-1);

                        all.partyList.addView(tv);
                        all.partyList.addView(ds);
                    }
                }
            });
        } catch(IOException e){
            all.info("Error: "+e.getMessage());
            inited = false;
        }
    }
}
