package me.noack.antonio.friedomobile.extract.live.modul;

import android.widget.LinearLayout;
import android.widget.TableLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.extract.Features;
import me.noack.antonio.friedomobile.extract.live.OnceExtractor;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;
import me.noack.antonio.friedomobile.party.Maths;

import static me.noack.antonio.friedomobile.extract.Features.linMW;

/**
 * Created by antonio on 22.12.2017
 */

public class ModulDataExtractor extends OnceExtractor {

    HashMap<String, HashMap<String, String>> datas = new HashMap<>();
    HashMap<String, String> data;
    Queue<String> keys = new LinkedList<>();// ermöglicht horizontale wie auch vertikale Tabellen (was was ist, darf sich jeder selbst aussuchen ;P)

    LinearLayout lin;
    TableLayout tl;

    public ModulDataExtractor(final AllManager all){
        datas.put(null, data);
        lin = all.details;
        all.runOnUiThread(new Runnable() {
            @Override public void run() {
                tl = new TableLayout(all);
                tl.setLayoutParams(linMW);
                lin.addView(tl);
            }
        });
    }

    @Override public void onElementStarted(HTMLElement element) {}

    @Override
    public void onElementFinished(HTMLElement element) {
        if("th".equalsIgnoreCase(element.getType()) && tl!=null){
            int l = Maths.parseInt(element.get("rowspan"), 1);
            for(int i=0;i<l;i++) keys.offer(element.getContent());
        } else if("td".equalsIgnoreCase(element.getType()) && !keys.isEmpty()){
            final String key = keys.poll();
            final String ctx = element.getContent();
            if(ctx!=null){
                data.put(key, ctx);
                all.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        tl.addView(Features.keyValuePair2(all, all.textByText(key), all.textByText(ctx)));
                    }
                });
            }
        } else if("caption".equalsIgnoreCase(element.getType())){
            data = new HashMap<>();
            final String cap = element.getContent();
            datas.put(cap, data);
            keys.clear();
            all.runOnUiThread(new Runnable() {
                @Override public void run() {
                    tl.addView(Features.keyValuePair2(all, all.headerByText(cap), all.textByText("")));
                }
            });
        }
    }

    @Override
    public void onFinished(HTMLDocument doc) {

    }

    @Override
    public void onError(Exception e) {

    }
}
