package me.noack.antonio.friedomobile.stupla;

import android.content.SharedPreferences;

import me.noack.antonio.friedomobile.party.Maths;

/**
 * Created by antonio on 17.12.2017
 */

public class Termin {

    private int row, start, duration, color;
    private String name, desc, index;
    public Termin(int start, int duration, int row, int color, String index, String title, String etc){
        this.row = row;
        this.start = start;
        this.duration = duration;
        this.index = index;
        this.name = title;
        this.desc = etc;
        this.color = color;
    }

    public boolean isDark(){
        return Maths.isDark(color);
    }

    public Termin(String date, String index, SharedPreferences pref){
        String mainKey = index+"."+date;
        name = pref.getString(mainKey+"/", null);
        desc = pref.getString(mainKey+"#", null);
        start = pref.getInt(mainKey+"+", 0);
        duration = pref.getInt(mainKey+"-", 0);
        this.index = pref.getString(mainKey+"*", null);
        row = pref.getInt(mainKey+"=", 0);
        color = pref.getInt(mainKey+"!", Stundenplan.defaultColor);
    }

    public int getColor(){return color;}
    public void setColor(int color){this.color = color;}

    public String getName(){
        return name;
    }
    public void setName(String name){this.name = name;}

    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc){this.desc = desc;}

    public void save(String date, SharedPreferences.Editor edit){
        String mainKey = index+"."+date;
        edit.putString(mainKey+"/", name);
        edit.putString(mainKey+"#", desc);
        edit.putInt(mainKey+"+", start);
        edit.putInt(mainKey+"-", duration);
        edit.putString(mainKey+"*", index);
        edit.putInt(mainKey+"=", row);
        edit.putInt(mainKey+"!", color);
    }

    public String getIndex(){
        return index;
    }

    public int getStart(){
        return start;
    }
    public void setStart(int start){this.start = start;}

    public int getDuration(){
        return duration;
    }
    public void setDuration(int dur){this.duration = dur;}

    public int getEnd(){
        return start+duration;
    }

    public int getRow(){
        return row;
    }
    public void setRow(int row){this.row = row;}
}
