package me.noack.antonio.friedomobile.party;

import android.support.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;

import me.noack.antonio.friedomobile.AllManager;
import me.noack.antonio.friedomobile.NotLoggedInException;

/**
 * Created by antonio on 21.12.2017
 * schreibt eine Nachricht
 *
 * es müssen außerdem Kommentare sendbar sein!
 */

public class SendMessage {

    public static String parseDate(String date){
        String[] d = date.replace('-', '/').replace('.', '/').split("/");
        int day, mon;

        Calendar cal = GregorianCalendar.getInstance();

        // Tage starten bei 1, Monate bei 0 ...

        day = d[0].length()==0 ? cal.get(Calendar.DAY_OF_MONTH) : Integer.parseInt(d[0]);
        if(d.length < 2 || d[1].length()<1){
            mon = ((cal.get(Calendar.MONTH)+(day<cal.get(Calendar.DAY_OF_MONTH)?1:0))%12)+1;
        } else mon = Integer.parseInt(d[1]);

        System.out.println(day+"."+mon+".");

        return (char)(day+31)+""+(char)(mon+31);
    }

    public static void write(final AllManager all, final Party party) throws NotLoggedInException {
        if(all.connection == null){// wer sich nicht authentifiziert hat, darf auch nicht schreiben...
            throw new NotLoggedInException();
        } else {
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        String tos = "s="+URLEncoder.encode(
                                (char)(party.getSlot()+32)+
                                parseDate(all.editDateDate.getText().toString())+
                                (char)(party.getStart()+32)+
                                (char)(party.getDuration()+32)+
                                all.editName.getText().toString()+
                                all.connection.loginName.replace("\0", ", ")+"\0"+// Ja: wer eine Veranstaltung erstellt, wird mit dessen Namen sichtbar Und außerdem ja: man kann es fälschen... der einzige Weg darum ist, dass ihr mir euer Passwort sendet und das wollt ihr vermutlich doch eher nicht
                                party.getName()+"\0"+party.getColor()+"\0"+party.getDesc().replace("\n", "\\n"), "UTF-8");

                        HttpURLConnection msg = (HttpURLConnection) new URL("http://phychi.com/fsu/events?"+tos).openConnection();
                        msg.setRequestMethod("GET");

                        BufferedReader read = new BufferedReader(new InputStreamReader(msg.getInputStream()));

                        String status = read.readLine();
                        if(status.equalsIgnoreCase("ok")){
                            all.runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    all.menu();
                                }
                            });
                            ReadMessage.read(all);
                        } else {
                            all.info("Error "+status);
                        }

                        read.close();

                    } catch (IOException|NumberFormatException e){

                    }
                }
            }).start();
        }
    }
}
