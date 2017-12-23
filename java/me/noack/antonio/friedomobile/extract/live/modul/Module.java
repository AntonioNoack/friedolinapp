package me.noack.antonio.friedomobile.extract.live.modul;

import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.html.connection.ConnectionType;
import me.noack.antonio.friedomobile.stupla.TerminPlan;

/**
 * Created by antonio on 23.12.2017
 */

public class Module {
    public static void init(final AllManager all){
        all.findViewById(R.id.buttonModule).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Calendar cal = GregorianCalendar.getInstance();
                all.modulSm.setText(cal.get(Calendar.YEAR)+(cal.get(Calendar.WEEK_OF_YEAR)<30?":1":":2"));
                all.open(R.id.modulKatalogSite);
            }
        });
        all.findViewById(R.id.modulKatalogStart).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String id = all.modulID.getText().toString().trim();
                String sm = all.modulSm.getText().toString().trim().replace(":", "");
                TerminPlan.editing = null;// das Zeichen dafür, dass es um eine Party geht
                if(id.length() > 0){
                    final ModulCodeByIDExtractor modcod = new ModulCodeByIDExtractor();
                    modcod.whenFinished = new Runnable() {
                        @Override public void run() {
                            System.out.println("loading "+modcod.url);
                            all.liveEx.work(all, new ModulDataExtractor(all), modcod.url.replace("&amp;", "&"), null, ConnectionType.WITH_COOKIE);
                        }
                    };
                    String url = "state=wsearchv&search=1&subdir=veranstaltung&veranstaltung.veranstnr="+id+"&veranstaltung.semester="+sm+"&P_start=0&P_anzahl=10&P.sort=&_form=display";
                    all.liveEx.work(all, modcod, url, null, ConnectionType.WITH_COOKIE);
                    all.showChild(R.id.detailsSite);
                }
            }
        });
    }
}
