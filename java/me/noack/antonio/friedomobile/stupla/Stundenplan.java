package me.noack.antonio.friedomobile.stupla;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.NotLoggedInException;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.extract.live.LiveExtractor;
import me.noack.antonio.friedomobile.extract.live.StundenplanExtractor;
import me.noack.antonio.friedomobile.html.connection.ConnectionType;

/**
 * Created by antonio on 28.11.2017
 */

public class Stundenplan {

    public static final int defaultColor = 0x63ABDF;

    public static void load(AllManager all, String date, SharedPreferences pref){
        // https://friedolin.uni-jena.de/qisserver/rds?state=wplan&act=show&show=plan&P.subc=plan&navigationPosition=functions%2Cschedule&breadcrumb=schedule&topitem=functions&subitem=schedule
        all.liveEx.work(all, new StundenplanExtractor(true, pref, date), "https://friedolin.uni-jena.de/qisserver/rds?state=wplan&act=show&show=plan&P.subc=plan&navigationPosition=functions%2Cschedule&breadcrumb=schedule&topitem=functions&subitem=schedule", ConnectionType.LOGGED_IN);
    }

    public static void load(String date, AllManager all, SharedPreferences pref){
        new TerminPlan(date, pref).display(all, all.stupla);
    }

    public static int dow(int dow){
        switch(dow){
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            default: return 6;
        }
    }

    public static void uninit(){
        inited = false;
    }

    private static boolean inited = false;
    public static void init(final AllManager all, final SharedPreferences pref){
        if(inited) return;

        // wir brauchen ein Force-Reload und ein Cache-Dingens...
        // wenn ein Eintrag in der Datenbank für heute drinnen steht, wird er geladen

        Calendar cal = GregorianCalendar.getInstance();

        int tag = dow(cal.get(Calendar.DAY_OF_WEEK));
        int woche0 = cal.get(Calendar.WEEK_OF_YEAR);
        int jahr = cal.get(Calendar.YEAR);

        for(int i=0;i<tag;i++){
            all.next(all.wochentagFlipper);
        }

        String woch = woche0+"_"+jahr;
        String today = tag+"_"+woch;

        // Format: tag_woche_jahr
        String todaysData = pref.getString(today, null);
        String weeks = todaysData==null?null:pref.getString("9_weeks", null);
        if(todaysData == null || weeks == null){// !!! letzteres kann später entfernt werden, muss nur einmal rein...

            load(all, woch, pref);// lädt den heutigen Tag... sollte ehrenhalber auch noch angezeigt werden...
            load(today, all, pref);

        } else {

            /**
             * fülle den Stundenplan mit den Wochen... etwas anstrengend XD
             * */

            boolean jump = true;
            for(String week:weeks.split("\0")){
                String[] wek = week.split("\n");
                pushWeek(all, wek[0], wek[1]);
                if(jump){
                    all.next(all.wochenFlipper);
                    if(wek[1].equalsIgnoreCase(woch)) jump = false;
                }
            }

            load(today, all, pref);
        }

        switcher(all, all.wochenFlipper, null);
        switcher(all, all.wochentagFlipper, all.wochenFlipper);

        inited = true;
    }

    public static void pushWeek(AllManager all, String name, String id){
        int i = weekIdById.size();
        weekIdById.put(i, id);
        TextView tv = all.headerByText(name);
        tv.setTextColor(Color.rgb(0,0,0));
        all.wochenFlipper.addView(tv);
    }

    private static HashMap<Integer, String> weekIdById = new HashMap<>();

    private static void loadWeek(final AllManager all, final String key, final Runnable run){
        if(all.pref.getString("4_"+key, null) == null){
            // Daten müssen noch geladen werden...
            all.login(new Runnable() {
                @Override public void run() {
                    all.liveEx.work(all, new StundenplanExtractor(false, all.pref, key),
                            "https://friedolin.uni-jena.de/qisserver/rds?state=wplan&act=&pool=&show=plan&P.vx=kurz&week="+key+"&submit=anzeigen", run, ConnectionType.LOGGED_IN);
                }
            }, false, null);
        } else run.run();
    }

    private static void show(final AllManager all, int delta){
        int i1 = all.wochentagFlipper.getDisplayedChild(), i2 = all.wochenFlipper.getDisplayedChild();
        int l1 = all.wochentagFlipper.getChildCount(), l2 = all.wochenFlipper.getChildCount();

        i1 += delta;
        if(i1 > 6){
            i1 -= 7;
            if(++i2 >= l2) i2 = 0;
        } else if(i1 < 0) {
            i1 += 7;
            if (--i2 <= 0) i2 = l2 - 1;
        }

        // nun setze die Nachrichten...
        String id = weekIdById.get(i2);
        final String date = i1+"_"+id;
        loadWeek(all, id, new Runnable() {
            @Override public void run() {
                all.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        load(date, all, all.pref);
                    }
                });
            }
        });
    }

    // das wird noch lustig mit den Wochen, besonders, da wir nicht mehr wissen, als in den Tabellen auch steht...
    // morgen also vielleicht ein schickes Bild von mir, ein Logo, und Onlinefunktionalitätstests, sodass das ganze bald auch mit den Luxusfunktionen schön geht und wir z.B.
    // nach dem Stundenplan im Hintergrund fragen können und so :) TODO
    public static void switcher(final AllManager all, final ViewFlipper flipper, final ViewFlipper onend){
        flipper.setOnTouchListener(new View.OnTouchListener() {
            private float lastX;
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();break;
                    case MotionEvent.ACTION_UP:
                        float delta = lastX - event.getX();
                        if(delta > all.minDragForMotion){
                            if(onend != null){
                                // show date
                                show(all, 1);
                            } else show(all, +7);
                            if(onend != null && flipper.getChildCount() == flipper.getDisplayedChild()+1)
                                all.next(onend);
                            all.next(flipper);
                        } else if(delta < -all.minDragForMotion){
                            if(onend != null){
                                // show date
                                show(all, -1);
                            } else show(all, -7);
                            if(onend != null && flipper.getDisplayedChild() == 0)
                                all.previous(onend);
                            all.previous(flipper);
                        }
                } return true;
            }
        });
    }

    public static final View.OnClickListener refresh = new View.OnClickListener() {
        @Override public void onClick(View view) {

            final AllManager all = AllManager.instance;

            new AlertDialog.Builder(all)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(all.getResources().getString(R.string.areyousure))
                    .setMessage(all.getResources().getString(R.string.deletetitle))
                    .setPositiveButton(all.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            final SharedPreferences pref = all.pref;
                            SharedPreferences.Editor edit = pref.edit();
                            for(Map.Entry<String, ?> that:pref.getAll().entrySet()){
                                if(that.getKey().indexOf('_') == 1){
                                    edit.remove(that.getKey());
                                }
                            }
                            edit.commit();
                            inited = false;
                            all.wochenFlipper.removeAllViews();
                            all.login(new Runnable() {
                                @Override public void run() {
                                    init(all, pref);
                                }
                            }, true, new Runnable() {
                                @Override public void run() {
                                    all.menu();
                                    all.info(R.string.loginPlease);
                                }
                            });
                        }
                    })
                    .setNegativeButton(all.getResources().getString(R.string.no), null)
                    .show();
        }
    };
}
