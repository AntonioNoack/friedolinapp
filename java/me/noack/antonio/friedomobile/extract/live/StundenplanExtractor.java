package me.noack.antonio.friedomobile.extract.live;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

import java.util.ArrayList;

import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;
import me.noack.antonio.friedomobile.stupla.Stundenplan;
import me.noack.antonio.friedomobile.stupla.Termin;
import me.noack.antonio.friedomobile.stupla.TerminPlan;

/**
 * Created by antonio on 17.12.2017
 *
 * soll alle interessanten Daten aus dem Stundenplan erfassen
 */

public class StundenplanExtractor extends OnceExtractor {

    private final TerminPlan[] dates = new TerminPlan[7];
    private final ArrayList<String> weekNames = new ArrayList<>(), weekIds = new ArrayList<>();
    private final boolean updateWeeks;
    private final SharedPreferences pref;
    private final String dateString;

    public StundenplanExtractor(boolean updateWeeks, SharedPreferences prefs, String dateString){
        for(int i=0;i<7;i++) dates[i] = new TerminPlan(i+"_"+dateString);
        this.updateWeeks = updateWeeks;
        this.pref = prefs;
        this.dateString = dateString;
    }

    int wctr = 1;
    class Seite {
        String name, value;
        public Seite(final String name, final String value, final boolean vorlesungsfrei){
            this.name = name;
            this.value = value;
            // wenn value ein Integer ist und negativ, dann ist es eine Spezialseite
            // sonst ist es eine Woche mit Unterstrich 37_2017
            // im Name steht auch drinnen von wann bis wann die Woche geht, wobei wir das selbst in die Hand nehmen sollten

            all.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int ix = value.indexOf('_');

                    String tv;

                    if(ix>-1){
                        // Woche

                        String woc = name;
                        while(woc.endsWith(" ")) woc = woc.substring(0, woc.length()-1);
                        String[] dat = woc.split(" ");
                        String from = null, to = null;
                        for(int i=dat.length-1;i>-1;i--){
                            String dati = dat[i];
                            if(dati.indexOf('.')>-1){
                                String[] datx = dat[i].split("\\.");
                                if(datx.length > 1){
                                    for(int j=0;j<2;j++){
                                        datx[j] = datx[j].length()==1?"0"+datx[j]:datx[j];
                                    }

                                    dati = datx[0]+"."+datx[1]+".";

                                    if(to == null) to = dati;
                                    else {
                                        from = dati;
                                        break;
                                    }
                                }
                            }
                        }

                        if(from != null){// Woche: 51 &nbsp;&nbsp;&nbsp;  18.12.2017 ---- 24.12.2017
                            tv = from+" - "+to+", "+value.substring(0, ix)+"/"+value.substring(ix+1)+(vorlesungsfrei?"":" *"+wctr++);
                        } else
                            tv = value.substring(0, ix) + ". Woche " + value.substring(ix + 1);

                    } else {
                        // Spezial
                        tv = name;
                    }

                    weekNames.add(tv);weekIds.add(value);
                    Stundenplan.pushWeek(all, tv, value);
                }
            });
        }
    }

    boolean optgroup;
    boolean start;
    int time;

    @Override
    public void onElementStarted(HTMLElement element) {
        if(updateWeeks){
            if("content".equalsIgnoreCase(element.getClassName())) start = true;
            if(start){
                if("optgroup".equalsIgnoreCase(element.getType())) {
                    optgroup = true;
                }
            }
        }
    }

    int ctr;

    @Override
    public void onElementFinished(HTMLElement element) {

        String className = element.getClassName();
        if(className != null && className.startsWith("plan")){
            if(element.get("rowspan") != null){
                String ctx = element.getContent();
                if(ctx.startsWith("&nbsp;")){
                    ctx = ctx.substring(6).trim();
                    try {
                        time = Integer.parseInt(ctx);
                    } catch(NumberFormatException e){
                        System.out.println("Unexpected non-number for stupla: "+ctx);
                    }
                } else {
                    int duration;
                    try {
                        duration = Integer.parseInt(element.get("rowspan"));
                        if((duration & 3) == 0 && duration > 0){
                            duration >>= 2;
                        } else System.out.println("crazy duration (stupla): "+duration);

                        if(element.hasChildren()){

                            HTMLElement par = element.getParent();
                            int index = -2;
                            if(par != null){
                                for(HTMLElement maybe:par.children()){
                                    if(maybe == element) break;
                                    else index++;
                                }
                            }

                            int put = 0;
                            if(index > -1 && index < 7) for(HTMLElement table:element.children()){// else komisch
                                if(table.hasChildren()){
                                    HTMLElement first = table.children().first();
                                    if(first.getType().equals("tbody")) table = first;
                                    if(table.hasChildren()){// children = tr
                                        tabs:for(HTMLElement tab:table.children()){
                                            if(tab.hasChildren()){// children = td
                                                tab = tab.children().first();

                                                if(tab.hasChildren()){// children = a
                                                    tab = tab.children().first();
                                                    String title = tab.get("title");
                                                    if(title != null){
                                                        dates[index].add(new Termin(time*4, duration*4, put++, Stundenplan.defaultColor, Integer.toString(ctr++, 16), all.abkürzung(withoutNumber(title)), title));
                                                    } else System.out.println("title null: "+table.getContent());
                                                    break tabs;
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch(NumberFormatException e){
                        System.out.println("Non-number rowspan "+element.get("rowspan"));
                    }
                }
            }
        } else if(updateWeeks){
            if("optgroup".equalsIgnoreCase(element.getType())){
                optgroup = false;
            } else if("option".equalsIgnoreCase(element.getType())){
                if(optgroup){
                    new Seite(element.getContent(), element.getValue(), element.getClassName() != null);
                    //lastGruppe.entries.add(s);
                }
            }
        }
    }

    static boolean num(char c){
        return c>='0' && c<='9';
    }

    static String withoutNumber(String s){
        int i=0;
        for(;i<s.length();i++){
            char c = s.charAt(i);
            if(c == ' ')break;
            else if(!num(c)) return s;
        }
        try {
            s = s.substring(i+1);i=0;
            if(num(s.charAt(i++)) && s.charAt(i++)=='-' && s.charAt(i++)=='G' && s.charAt(i++)=='r' && s.charAt(i++)=='u' && s.charAt(i++)=='p'
                    && s.charAt(i++)=='p' && s.charAt(i)=='e'){
                s = s.substring(i+1);
            }
        } catch(ArrayIndexOutOfBoundsException e){}
        return "  "+s;
    }

    @Override public void onFinished(HTMLDocument doc) {
        // speichere unsere wertvoll gesammelten Daten, immerhin wollen wir die Seite nicht oft anfragen müssen

        SharedPreferences.Editor edit = pref.edit();

        if(updateWeeks){
            String weeks = null;
            for(int i=0;i<weekNames.size();i++){
                String week = weekNames.get(i)+"\n"+weekIds.get(i);
                weeks = weeks == null ? week : weeks+"\0"+week;
            } edit.putString("9_weeks", weeks);
        }

        // !!! hierher muss noch der Boolean, ob altes mitgespeichert werden soll (persönliches)
        int i = 0;
        for(TerminPlan date:dates){
            date.save(edit, pref);
            i++;
        }

        edit.commit();

    }

    @Override public void onError(Exception e) {
        all.info("Error while loading timetable: "+e.getMessage());
        e.printStackTrace();
    }
}
