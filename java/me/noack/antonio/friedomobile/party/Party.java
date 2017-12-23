package me.noack.antonio.friedomobile.party;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Iterator;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.NotLoggedInException;
import me.noack.antonio.friedomobile.R;
import me.noack.antonio.friedomobile.struct.FList;
import me.noack.antonio.friedomobile.stupla.Termin;

import static me.noack.antonio.friedomobile.stupla.TerminPlan.num;
import static me.noack.antonio.friedomobile.stupla.TerminPlan.time;
import static me.noack.antonio.friedomobile.stupla.TerminPlan.toHSV;

/**
 * Created by antonio on 22.12.2017
 */

public class Party extends Termin {

    private static Dialog dia;

    public static void showEditor(AllManager all, Party party, int slot){

        boolean z = party == null;

        all.editDateTitle.setText(z?"":party.getName());
        all.editDateDesc.setText(z?"":party.getDesc());
        all.editDateStart.setText(z?"":time(party.getStart()));
        all.editDateEnd.setText(z?"":time(party.getEnd()));
        all.editDateDate.setText(z?"":num(party.getDay()+1)+"."+num(party.getMonth()+1)+".");
        all.editDateRow.setText(Integer.toString(z?slot+1:party.getSlot()+1));

        double[] hsv = z?new double[]{.57, .55, .9}:toHSV(party.getColor());
        all.editDateH.setProgress((int) (hsv[0]*1000));
        all.editDateS.setProgress((int) (hsv[1]*1000));
        all.editDateV.setProgress((int) (hsv[2]*1000));
        int tcol = !z && party.isDark()?-1:0xff000000;
        for(TextView v:all.editDate) v.setTextColor(tcol);

        all.editDateDate.setVisibility(View.VISIBLE);
        all.editDateAll.setVisibility(View.GONE);
        all.showChild(R.id.editDate);
    }

    public static void init(final AllManager all){
        all.findViewById(R.id.partyReload).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override public void run() {
                        ReadMessage.read(all);
                    }
                }).start();
            }
        });
        all.findViewById(R.id.buttonParty).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(!ReadMessage.inited){
                    new Thread(new Runnable() {
                        @Override public void run() {
                            ReadMessage.read(all);
                        }
                    }).start();
                } all.open(R.id.partySite);
            }
        });
        all.findViewById(R.id.partyCreate).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                all.login(new Runnable() {
                    @Override public void run() {
                        if(all.connection == null){
                            all.info(R.string.gonnaLogIn);
                            throw new NotLoggedInException();
                        }

                        /** frage den Nutzer, ob er ein bestehendes Event bearbeiten möchte, oder ein neues erstellen und setze dann entsprechend die voreingestellten Werte */
                        // außerdem ist der Nutzer ja jetzt eingeloggt und es kann getestet werden, welche Partys zu uns gehören :)
                        final FList<Party> own = new FList<Party>();
                        String ownName = all.connection.loginName.split("\0")[0]+",";
                        for(Party maybe:ReadMessage.partys){
                            if(maybe.getIndex().startsWith(ownName)){
                                own.add(maybe);
                            }
                        }

                        if(own.isEmpty()){
                            showEditor(all, null, 0);
                        } else {
                            final int count = own.count();
                            String[] choices = new String[count>3?4:count+1];int i=0;
                            for(Party p:own) choices[i++] = p.getName();
                            if(i<3)
                                choices[i] = all.getResources().getString(R.string.neue_party);

                            int slot = 0;
                            for(i=0;i<4;i++) for(Party p:own) if(p.getSlot() == slot) slot++;
                            final int cslot = slot;

                            dia = new AlertDialog.Builder(all).setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialogInterface, int i) {
                                    if(i == count){
                                        showEditor(all, null, cslot);
                                    } else {
                                        Iterator<Party> p = own.iterator();
                                        while(i-- > 0) p.next();
                                        showEditor(all, p.next(), -1);
                                    }

                                    dia.dismiss();

                                }
                            }).setTitle(all.getResources().getString(R.string.choose_party)).setCancelable(true).create();
                            dia.show();
                        }
                    }
                }, true, null);
            }
        });
        ((EditText) all.findViewById(R.id.partySearch)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) {
                String query = editable.toString().toLowerCase();

                for(int i=0,l=all.partyList.getChildCount();i<l;i++){
                    View v = all.partyList.getChildAt(i);
                    if(v instanceof Button){
                        Button b  = (Button) v;
                        if(b.getText().toString().toLowerCase().contains(query)){
                            if(b.getVisibility() != View.VISIBLE) {
                                b.setVisibility(View.VISIBLE);
                            } i++;
                        }
                    } else if(v instanceof TextView){
                        TextView t = (TextView) v;
                        if(t.getText().toString().toLowerCase().contains(query)){
                            // vis
                            if(i>0)
                                all.partyList.getChildAt(i-1).setVisibility(View.VISIBLE);
                        } else {
                            // gon
                            if(i>0)
                                all.partyList.getChildAt(i-1).setVisibility(View.GONE);
                            t.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    private int day, month, slot;// 0 = 1st

    public int getDay(){return day;}
    public int getMonth(){return month;}
    public int getSlot(){return slot;}
    public int compareStart(){
        return (month*50 + day)*200+this.getStart();
    }
    // public boolean isOwner(AllManager all){return this.getIndex().split(",")[0].equalsIgnoreCase(all.editName.getText().toString());}

    public Party(int slot, int day, int month, int start, int duration, int row, int color, String author, String title, String message){
        super(start, duration, row, color, author, title, message);
        this.day = day;
        this.month = month;
        this.slot = slot;
    }

    @Nullable
    public static Party get(String s){
        try {
            int i;
            int ind = Maths.parseInt(s.substring(0, i=s.indexOf('|')), 0);
            int day = Maths.parseInt(s.substring(i+1, i=s.indexOf('|', i+1)), 0);
            int mon = Maths.parseInt(s.substring(i+1, i=s.indexOf('|', i+1)), 0);
            int sta = Maths.parseInt(s.substring(i+1, i=s.indexOf('|', i+1)), 0);
            int dur = Maths.parseInt(s.substring(i+1, i=s.indexOf('|', i+1)), 0);
            String
                    author = s.substring(i+1, i=s.indexOf('|', i+1)),
                    title = s.substring(i+1, i=s.indexOf('|', i+1)),
                    message = s.substring(i+1);
            ;
            int ix = message.indexOf('\0'), col = 0;
            if(ix > -1){
                for(i=0;i<ix;i++){
                    char c = message.charAt(i);
                    if(c >='0' && c<='9'){
                        col = col*10+c-'0';
                    } else {
                        col = 0;// transparent
                        ix = -1;
                        break;
                    }
                }
            }

            if(ix > -1) message = message.substring(ix+1);// jap, damit wird es möglich, eine leere Nachricht doch zu senden...
            return new Party(ind, day, mon, sta, dur, 0, col, author, title, message.replace("\\n", "\n"));
        } catch(Exception e){
            return null;
        }
    }
}
