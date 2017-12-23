package me.noack.antonio.friedomobile.extract.live;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.html.HTMLDocument;
import me.noack.antonio.friedomobile.html.HTMLElement;
import me.noack.antonio.friedomobile.stupla.TerminPlan;

/**
 * Created by antonio on 21.12.2017
 */

public class LazyFortschrittsExtractor extends OnceExtractor {

    int type, ul;

    @Override
    public void onElementStarted(HTMLElement element) {}

    private void work(HTMLElement element, String last, final LinearLayout parlin){
        if(element.hasChildren()){
            LinearLayout chilin = null;
            String that = null;
            boolean poison = false;
            for(HTMLElement child:element.children()){
                if(child.getType().equalsIgnoreCase("img")){
                    // Type
                    String src = child.get("src");
                    if(src == null){
                        type = 0;
                    } else {
                        src = src.toLowerCase();
                        if(src.contains("be")) type = all.getResources().getColor(R.color.green);
                        else if(src.contains("pr")){
                            type = all.warnColor;
                            poison = true;
                        } else
                            type = all.getResources().getColor(R.color.colorPrimary);
                    }
                } else if(child.getType().equalsIgnoreCase("font")){
                    // Text
                    that = trimi(child.getContent());
                    final String text = that;
                    final String[] x = that.split("ECTS-Punkte: ");
                    for(int i=0;i<x.length && i<2;i++){
                        x[i] = trimi(x[i]);
                    }

                    if(last!=null && x[0].startsWith(last)) x[0] = x[0].substring(last.length()).trim();
                    if(x[0].startsWith(":")) x[0] = x[0].substring(1).trim();

                    if(x.length > 1){
                        chilin = new LinearLayout(all);
                        chilin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        chilin.setOrientation(LinearLayout.VERTICAL);
                    }

                    that = x[0];

                    final LinearLayout cl = chilin;

                    final int u = ul, t = type;
                    final boolean p = poison;
                    all.runOnUiThread(new Runnable() {
                        @Override public void run() {

                            LinearLayout lin = new LinearLayout(all);
                            lin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            lin.setOrientation(LinearLayout.HORIZONTAL);

                            TextView sp = all.textByText("");
                            sp.setWidth(TerminPlan.multiply);
                            sp.setBackgroundColor(t);
                            lin.addView(sp);

                            if(p && parent != null){
                                parent.setBackgroundColor(all.warnColor);
                            }

                            parent = sp;

                            if(u != 0){
                                sp = all.textByText(x.length==1||x[1].equalsIgnoreCase("0")?"":x[1]);
                                sp.setGravity(Gravity.CENTER);
                                sp.setWidth(TerminPlan.multiply * u);
                                lin.addView(sp);
                            }

                            TextView tv = all.textByText(u==0?" "+text:x.length<3?x[0]:text);
                            lin.addView(tv);

                            if(cl!=null){
                                tv.setOnClickListener(new View.OnClickListener() {
                                    @Override public void onClick(View view) {
                                        if(cl.getVisibility() == LinearLayout.VISIBLE){
                                            cl.setVisibility(LinearLayout.GONE);
                                        } else cl.setVisibility(LinearLayout.VISIBLE);
                                    }
                                }); cl.setVisibility(LinearLayout.GONE);
                            }

                            parlin.addView(lin);
                            if(cl!=null) parlin.addView(cl);
                        }
                    });
                } else if(child.getType().equalsIgnoreCase("ul")){
                    if(child.hasChildren()){
                        ul++;
                        for(HTMLElement grandchild:child.children()) work(grandchild, that, chilin==null?parlin:chilin);
                        ul--;
                    }
                }
            }
        }
    }

    TextView parent;// probably wont work always...

    @Override
    public void onElementFinished(HTMLElement element) {
        if(element.getType().equalsIgnoreCase("li")) {
            HTMLElement par0 = element.getParent(), par1;
            if(par0 == null) par1 = par0;
            else par1 = par0.getParent();
            if(par1 != null){
                if(!par1.getType().equalsIgnoreCase("li")){
                    // Baum gefunden :)
                    ul = 0;
                    work(element, null, all.fortLazy);
                }
            }
        }
    }

    private String trimi(String s){
        s = s.trim()
                .replace("Konto A Konto A", "Konto A")
                .replace("Konto B Konto B", "Konto B")
                .replace("Konto C Konto C", "Konto C")
                .replace("Konto D Konto D", "Konto D")
                .replace("Konto E Konto E", "Konto E")
                .replace("Konto F Konto F", "Konto F");

        if(s.endsWith(" ")) s=s.substring(0, s.length()-1);
        return s;
    }

    @Override
    public void onFinished(HTMLDocument doc) {
        all.runOnUiThread(new Runnable() {// add the typical OK-message
            @Override public void run() {
                all.fortLazy.addView(all.headerByHTML("--- fertig geladen; wenn ihr ein nettes Diagramm wollt, guckt im echten Friedolin nach, oder fragt freundlich :) ---"));
            }
        });
    }

    @Override
    public void onError(Exception e) {
        all.fortLazy.addView(all.headerByText("Internal error while loading page: "+e.getMessage()));
    }
}
