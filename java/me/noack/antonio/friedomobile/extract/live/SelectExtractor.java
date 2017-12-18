package me.noack.antonio.friedomobile.extract.live;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Set;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.extract.live.ElementListener;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;

/**
 * Created by antonio on 25.11.2017
 *
 * extrahiert <Select></Select>-Bereiche
 * ist schön ausgedacht, aber nicht ganz fertig gedacht: an sich können wir ja auch damit einfach ein ganzes Form analysieren und abbilden lassen :D
 */

public class SelectExtractor extends OnceExtractor {

    protected LinearLayout layout;
    protected HashMap<String, String> naming;

    public SelectExtractor(AllManager all, LinearLayout layout, HashMap<String, String> naming){
        this.layout = layout;
        this.naming = naming;
        this.all = all;
    }

    public void addExternalStringy(final String id, final String[] optionen){
        selectData.put(id, null);
        final Button button = new Button(all);
        String prename = naming.get(id);
        final String name = prename==null?"?":naming.get(null);
        button.setText(name);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                new android.app.AlertDialog.Builder(all).setTitle(name).setSingleChoiceItems(optionen, -1, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface diaint, final int index) {
                        // es wurde was ausgewählt :D
                        selected.put(id, optionen[index]);
                        all.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                button.setText(name+": "+optionen[index]);
                            }
                        });
                        diaint.dismiss();
                    }
                }).create().show();
            }
        });
        layout.addView(button);
    }

    protected AllManager all;

    protected HashMap<String, Integer> select;
    protected HashMap<String, String> selected = new HashMap<>();
    protected HashMap<String, HashMap<String, Integer>> selectData = new HashMap<>();

    @Override public void onElementStarted(HTMLElement element) {
        switch(element.getType()){
            case "select":
                final HashMap<String, Integer> select = this.select = new HashMap<>();
                final String id = element.getID();
                selectData.put(id, select);

                final Button button = new Button(all);
                String prename = naming.get(id);
                final String name = prename==null?"?":naming.get(null);
                button.setText(name);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {

                        Set<String> ss = select.keySet();
                        final String[] sss = ss.toArray(new String[ss.size()]);

                        new android.app.AlertDialog.Builder(all).setTitle(name).setSingleChoiceItems(sss, -1, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface diaint, final int index) {
                                // es wurde was ausgewählt :D
                                selected.put(id, sss[index]);
                                all.runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        button.setText(name+": "+sss[index]);
                                    }
                                });
                                diaint.dismiss();
                            }
                        }).create().show();
                    }
                });
                all.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.addView(button);
                    }
                });
                break;
        }
    }

    @Override public void onElementFinished(HTMLElement element) {
        if(select!=null && element.getType().equals("option")){
            String val = element.getValue();
            if(val != null){
                try {
                    select.put(element.getContent(), Integer.parseInt(val));
                } catch (NumberFormatException e){}
            }
        }
    }

    @Override public void onFinished(HTMLDocument doc){

    }

    @Override public void onError(Exception e){

    }

}
