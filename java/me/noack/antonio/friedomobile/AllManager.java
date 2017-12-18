package me.noack.antonio.friedomobile;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import me.noack.antonio.friedomobile.extract.live.NameExtractor;
import me.noack.antonio.friedomobile.extract.live.SelectExtractor;
import me.noack.antonio.friedomobile.extract.live.AusfallExtractor;
import me.noack.antonio.friedomobile.extract.Extractor;
import me.noack.antonio.friedomobile.extract.live.ElementListener;
import me.noack.antonio.friedomobile.extract.live.HeuteExtractor;
import me.noack.antonio.friedomobile.extract.NewsExtractor;
import me.noack.antonio.friedomobile.extract.TerminExtractor;
import me.noack.antonio.friedomobile.extract.live.LiveExtractor;
import me.noack.antonio.friedomobile.html.LoggedConnection;
import me.noack.antonio.friedomobile.map.Raum10;
import me.noack.antonio.friedomobile.stupla.Stundenplan;
import me.noack.antonio.friedomobile.stupla.TerminPlan;

/**
 * Created by antonio on 20.11.2017
 *
 * kümmert sich um das grobe Laden der Menüs und so
 */

public class AllManager extends AppCompatActivity {

    public static final float minDragForMotion = 30;
    public static AllManager instance;

    public ViewFlipper pageFlipper, wochenFlipper, wochentagFlipper;
    public LinearLayout newsContent, ausfall, details, detailsSite, termine, raumErgbnisse, editDateConfig, fortDone, fortTodo;
    public LinearLayout[] stupla;
    public TableLayout heute;
    private Extractor newsEx, terminEx;
    public LiveExtractor liveEx;
    private ElementListener ausfallEx, heuteEx;
    private SelectExtractor raumSelect;
    private GLSurfaceView mapView;
    public Button loginButton;
    public CheckBox keepLoggedInButton, editDateAll;
    public EditText raumName, raumAdresse, raumEinrichtung, raumGenerell,
        editName, editPasswort, editDateTitle, editDateDesc, editDateStart, editDateEnd, editDateRow,
        persVorname, persNachname, persEinr, persFunc, persInt;

    public SeekBar editDateH, editDateS, editDateV;

    private TextView examText;

    private NameExtractor nameExtractor = new NameExtractor(new Runnable() {
        @Override public void run() {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    loginButton.setText(getResources().getString(R.string.welcome)
                            .replace("@name", nameExtractor.strings.get(0))
                            .replace("@rolle", nameExtractor.strings.get(2)));
                }
            });
        }
    });

    public LoggedConnection connection;
    public SharedPreferences pref;

    @Override public void onDestroy(){
        Stundenplan.uninit();
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout._all);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// keine Tastatur zu Anfang

        instance = this;

        pageFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        newsContent = (LinearLayout) findViewById(R.id.newsContent);
        editDateConfig = (LinearLayout) findViewById(R.id.editDateConfig);
        heute = (TableLayout) findViewById(R.id.heute);
        ausfall = (LinearLayout) findViewById(R.id.ausfall);
        details = (LinearLayout) findViewById(R.id.details);
        detailsSite = (LinearLayout) findViewById(R.id.detailsSite);
        termine = (LinearLayout) findViewById(R.id.termine);
        wochenFlipper = (ViewFlipper) findViewById(R.id.stuplaWoche);
        wochentagFlipper = (ViewFlipper) findViewById(R.id.stuplaWochentag);
        raumErgbnisse = (LinearLayout) findViewById(R.id.raumSuchergebnisse);
        raumName = (EditText) findViewById(R.id.raumSucheName);
        raumAdresse = (EditText) findViewById(R.id.raumSucheAdresse);
        raumEinrichtung = (EditText) findViewById(R.id.raumSucheEinrichtung);
        raumGenerell = (EditText) findViewById(R.id.raumSucheGenerell);
        mapView = (GLSurfaceView) findViewById(R.id.mapView);
        editName = (EditText) findViewById(R.id.editName);
        editPasswort = (EditText) findViewById(R.id.editPasswort);
        keepLoggedInButton = (CheckBox) findViewById(R.id.keepLoggedInButton);
        editDateTitle = (EditText) findViewById(R.id.editDateTitle);
        editDateDesc = (EditText) findViewById(R.id.editDateDesc);
        editDateStart = (EditText) findViewById(R.id.editDateStart);
        editDateEnd = (EditText) findViewById(R.id.editDateEnd);
        editDateAll = (CheckBox) findViewById(R.id.editDateAll);
        editDateRow = (EditText) findViewById(R.id.editDateRow);
        examText = (TextView) findViewById(R.id.examText);
        persVorname = (EditText) findViewById(R.id.persVorname);
        persNachname = (EditText) findViewById(R.id.persNachname);
        persEinr = (EditText) findViewById(R.id.persEinr);
        editDateH = (SeekBar) findViewById(R.id.editDateH);
        editDateS = (SeekBar) findViewById(R.id.editDateS);
        editDateV = (SeekBar) findViewById(R.id.editDateV);
        stupla = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.stupla0),
                (LinearLayout) findViewById(R.id.stupla1),
                (LinearLayout) findViewById(R.id.stupla2),
                (LinearLayout) findViewById(R.id.stupla3),
                (LinearLayout) findViewById(R.id.stupla4),
        };

        heuteEx = new HeuteExtractor();
        ausfallEx = new AusfallExtractor();
        newsEx = new NewsExtractor();
        terminEx = new TerminExtractor();
        liveEx = new LiveExtractor();

        final AllManager all = this;
        final HashMap<String, String> raumNaming = new HashMap<>();// macht für den Campus und den Raumverwalter Sinn,
        // die Raumart wird hardgecoded, und die Einrichtung lasse ich wohl erst mal weg...
        // vielleicht ein Wunschknopf :D
        raumNaming.put("k_campus.id", "Campus auswählen");
        raumNaming.put("personal.pid", "Raumverwalter auswählen");
        raumNaming.put("raumart", "Raumart");

        //raumSelect = new SelectExtractor(all, (LinearLayout) findViewById(R.id.raumDropdowns), raumNaming);
        //raumSelect.addExternalStringy("raumart", new String[]{"beliebig", "Hörsaal", "Seminarraum", "Labor", "PC-Pool", "Lesesaal", "Sporthalle", "Dienstzimmer", "Besprechungsraum", "Sonstiger Raum10", "Freianlage", "Schwimmhalle", "Sportraum", "Kursraum", "Behandlungsraum", "Divers", "Foyer", "MMZ", "Veranstaltungsraum"});

        findViewById(R.id.stuplaRefreshButton).setOnClickListener(Stundenplan.refresh);

        ((EditText) findViewById(R.id.heuteSuche)).addTextChangedListener(textSearch(heute));
        ((EditText) findViewById(R.id.ausfallSuche)).addTextChangedListener(textSearch(ausfall));


        findViewById(R.id.personenStartButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //
                //            https://friedolin.uni-jena.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&choice.personal.status=y&choice.r_funktion.pfid=y&personal.vorname=Anna&r_funktion.pfid=7&personal.nachname=Richter&personal.status=I&einrichtung.dtxt=Oro&P_start=0&P_anzahl=10&_form=display
                //String url = "https://friedolin.uni-jena.de/qisserver/rds?state=wsearchv&search=7&purge=y&moduleParameter=person/person&personal.nachname=Richter&P_start=0&P_anzahl=50&_form=display";


                // https://friedolin.uni-jena.de/qisserver/rds?state=change&type=6&moduleParameter=personalSelect&nextdir=change&next=SearchSelect.vm&target=personSearch&subdir=person&init=y&source=state%3Dchange%26type%3D5%26moduleParameter%3DpersonSearch%26nextdir%3Dchange%26next%3Dsearch.vm%26subdir%3Dperson%26_form%3Ddisplay%26topitem%3Dfunctions%26subitem%3Dmembers%26field%3DNachname&targetfield=Nachname&_form=display


                //liveEx.work(all, new PersonenExtractor(), url, null, false);

                info(R.string.not_supported);
            }
        });

        TextWatcher raumSuchAktivator = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Raum10.init(all);

                all.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        raumErgbnisse.removeAllViews();

                        final String[]
                                name = specTrimSplit(raumName.getText().toString().toLowerCase(), ","),
                                addr = specTrimSplit(raumAdresse.getText().toString().toLowerCase(), ","),
                                eiri = specTrimSplit(raumEinrichtung.getText().toString().toLowerCase(), ","),
                                gene = specTrimSplit(raumGenerell.getText().toString().toLowerCase(), ",");

                        if(name.length + addr.length + eiri.length + gene.length == 0) return;// zu viele Suchergebnisse ^^, nein für die Konsistenz

                        int ctr=0;
                        rs:for(Raum10 r: Raum10.räume){
                            for(int i=0;i<name.length;i++) if(r.lcn==null || !r.lcn.contains(name[i])) continue rs;
                            for(int i=0;i<addr.length;i++) if(r.lca==null || !r.lca.contains(addr[i])) continue rs;
                            for(int i=0;i<eiri.length;i++) if(r.lce==null || !r.lce.contains(eiri[i])) continue rs;
                            for(int i=0;i<gene.length;i++){
                                String gen = gene[i];
                                if(
                                        !(r.lcn != null && r.lcn.contains(gen)) &&
                                        !(r.lca != null && r.lca.contains(gen)) &&
                                        !(r.lce != null && r.lce.contains(gen)) &&
                                        !(r.lcm != null && r.lcm.contains(gen)) &&
                                        !(r.lcw != null && r.lcw.contains(gen)) &&
                                        !(r.lcb != null && r.lcb.contains(gen))){
                                    continue rs;
                                }
                            }

                            // wurde erfüllt :)
                            r.drawOnList(all, raumErgbnisse);
                            if(ctr++ > 100) break;
                        }
                    }
                });
            }
        };

        // für die Räume nützlich: https://friedolin.uni-jena.de/qisserver/rds?state=wplan&act=Raum&pool=Raum&raum.rgid=29541

        ((EditText) findViewById(R.id.raumSucheName)).addTextChangedListener(raumSuchAktivator);
        ((EditText) findViewById(R.id.raumSucheAdresse)).addTextChangedListener(raumSuchAktivator);
        ((EditText) findViewById(R.id.raumSucheEinrichtung)).addTextChangedListener(raumSuchAktivator);
        ((EditText) findViewById(R.id.raumSucheGenerell)).addTextChangedListener(raumSuchAktivator);

        editDateH.setOnSeekBarChangeListener(TerminPlan.seekli);
        editDateS.setOnSeekBarChangeListener(TerminPlan.seekli);
        editDateV.setOnSeekBarChangeListener(TerminPlan.seekli);

        findViewById(R.id.editDateDelete).setOnClickListener(TerminPlan.delete);
        findViewById(R.id.editDateSave).setOnClickListener(TerminPlan.save);
        findViewById(R.id.stuplaKnopf).setOnClickListener(TerminPlan.newTermin);

        View.OnClickListener back = new View.OnClickListener() {
            @Override public void onClick(View view) {
                menu();
            }
        };
        findViewById(R.id.back001).setOnClickListener(back);
        findViewById(R.id.back002).setOnClickListener(back);
        findViewById(R.id.back003).setOnClickListener(back);
        findViewById(R.id.back004).setOnClickListener(back);
        findViewById(R.id.back005).setOnClickListener(back);
        findViewById(R.id.back006).setOnClickListener(back);
        findViewById(R.id.back007).setOnClickListener(back);
        findViewById(R.id.back008).setOnClickListener(back);
        findViewById(R.id.back009).setOnClickListener(back);
        findViewById(R.id.back010).setOnClickListener(back);
        findViewById(R.id.back011).setOnClickListener(back);
        findViewById(R.id.back012).setOnClickListener(back);

        keepLoggedInButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                saveNameNPassword(editName.getText().toString(), editPasswort.getText().toString(), b);
            }
        });

        (loginButton = (Button) findViewById(R.id.loginButton)).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                login(new Runnable() {// schon irgendwie absurd ^^
                    @Override
                    public void run() {
                        throw new NotLoggedInException();
                    }
                }, false, new Runnable() {
                    @Override public void run(){}
                });
            }
        });

        pref = getPreferences(Context.MODE_PRIVATE);
        String pwd = pref.getString("q", ""), nme = pref.getString("p", "");
        editName.setText(nme);
        editPasswort.setText(pwd);
        keepLoggedInButton.setChecked(pwd.length() != 0);

        if(pwd.length() != 0){
            connection = new LoggedConnection(nme, pwd);
            if(!connection.load(all, pref)) connection = null;
        }

        /**
         * Noten
         * */
        findViewById(R.id.examTextButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(examText.getVisibility()==View.VISIBLE){
                    examText.setVisibility(View.GONE);
                } else examText.setVisibility(View.VISIBLE);
            }
        });

        /**
         * Date
         * */
        findViewById(R.id.dateSelectAusfall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = GregorianCalendar.getInstance();
                // final String heute = cal.get(Calendar.DAY_OF_MONTH)+"."+(cal.get(Calendar.MONTH)+1)+"."+cal.get(Calendar.YEAR);
                DatePickerDialog dia = new DatePickerDialog(all, 0, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String date = day+"."+(month+1)+"."+year;
                        ausfall.removeAllViews();
                        liveEx.work(all, ausfallEx, "state=currentLectures&type=1&next=CurrentLectures.vm&nextdir=ressourcenManager&navigationPosition=functions%2CcanceledLectures&breadcrumb=canceledLectures&topitem=locallinks&subitem=canceledLectures&&HISCalendar_Date="+date+"&asi=", false);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dia.show();
            }
        });

        findViewById(R.id.dateSelectHeute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = GregorianCalendar.getInstance();

                DatePickerDialog dia = new DatePickerDialog(all, 0, new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String date = day+"."+(month+1)+"."+year;
                        ausfall.removeAllViews();
                        liveEx.work(all, heuteEx, "state=currentLectures&type=0&next=CurrentLectures.vm&nextdir=ressourcenManager&navigationPosition=functions%2CcurrentLectures&breadcrumb=currentLectures&topitem=locallinks&subitem=currentLectures&&HISCalendar_Date="+date+"&asi=", false);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dia.show();

                //https://friedolin.uni-jena.de/qisserver/rds?
            }
        });

        /**
         * kleine Seiten
         * */
        findViewById(R.id.buttonNews).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                newsEx.work(all, "state=user&type=0");
                open(R.id.newsSite);}
        });

        findViewById(R.id.notenButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                // muss noch die Daten laden... liveEx? ist gemütlicher :)
                // wohlmöglich muss man zwei Seiten laden -.-
                // siehe asi-Schlüssel...
                // https://friedolin.uni-jena.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D82%2Cstgnr%3D1&expand=0&asi=B1eFn7OUdt6Mz0gBuYwE#auswahlBaum%7Cabschluss%3Aabschl%3D82%2Cstgnr%3D1
                open(R.id.notenSite);
            }
        });

        findViewById(R.id.buttonHeute).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                liveEx.work(all, heuteEx, "state=currentLectures&type=0&next=CurrentLectures.vm&nextdir=ressourcenManager&navigationPosition=functions%2CcurrentLectures&breadcrumb=currentLectures&topitem=locallinks&subitem=currentLectures&asi=", false);
                open(R.id.heuteSite);}
        });

        findViewById(R.id.buttonAusfall).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                liveEx.work(all, ausfallEx, "state=currentLectures&type=1&next=CurrentLectures.vm&nextdir=ressourcenManager&navigationPosition=functions%2CcanceledLectures&breadcrumb=canceledLectures&topitem=locallinks&subitem=canceledLectures&asi=", false);
                open(R.id.ausfallSite);}
        });

        findViewById(R.id.buttonPersonen).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                // es wurde auf den Personenknopf gedrückt: das heißt nicht, dass die Seite fertig geladen ist: wir brauchen erst die Liste der Personen, die verfügbar sind
                // auch der Campi

                // irgendwie ist es etwas absurd :D

                open(R.id.personenSite);
            }
        });

        findViewById(R.id.buttonOrte).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                // liveEx.work(all, raumSelect, "state=change&type=5&moduleParameter=raumSearch&nextdir=change&next=search.vm&subdir=raum&_form=display&purge=y&navigationPosition=functions%2Cfacilities&breadcrumb=facilities&topitem=locallinks&subitem=facilities");
                open(R.id.orteSite);
            }
        });
        findViewById(R.id.buttonTermine).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                terminEx.work(all, "state=template&template=termininfo&navigationPosition=functions%2Chilfe_5&breadcrumb=termininfo&topitem=locallinks&subitem=hilfe_5");
                open(R.id.terminSite);}
        });
        findViewById(R.id.buttonStundenplan).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                login(new Runnable() {
                    @Override
                    public void run() {
                        Stundenplan.init(all, pref);
                        open(R.id.stundenplanSite);
                    }
                }, true, null);
            }
        });

        findViewById(R.id.buttonAboutme).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                open(R.id.aboutmeSite);}
        });

        findViewById(R.id.buttonFAQ).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                open(R.id.faqSite);}
        });

        findViewById(R.id.buttonMap).setOnClickListener(new View.OnClickListener() {// TODO reenable and build that map...
            @Override public void onClick(View view) {
                info(R.string.not_supported);
                // open(R.id.mapView);
            }
        });

        findViewById(R.id.joahButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                open(R.id.partySite);
            }
        });

        findViewById(R.id.modulButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                info(R.string.not_supported);
            }
        });
    }

    public void loginLogic(final Runnable run, final boolean onUI, Runnable onError){
        try {
            run.run();
        } catch(NotLoggedInException e){
            final String name = editName.getText().toString(), password = editPasswort.getText().toString();
            if(name.length() > 0 && password.length() > 0){
                saveNameNPassword(name, password, keepLoggedInButton.isChecked());
                new Thread(new Runnable() {
                    @Override public void run() {
                        connection = new LoggedConnection(name, password);
                        try {
                            InputStream in = connection.connect();
                            if(in == null){
                                info("Invalid name/password!");
                                connection = null;
                            } else {
                                liveEx.work(AllManager.this, nameExtractor, in, new Runnable() {
                                    @Override public void run() {
                                        if(onUI){
                                            AllManager.this.runOnUiThread(run);
                                        } else {
                                            try {
                                                run.run();
                                            } catch(NotLoggedInException e){/* :/ */}
                                        }
                                    }
                                });
                            }
                        } catch(IOException e){
                            info("Maybe you should enable your internet connection? ;)");
                            connection = null;
                        }
                    }
                }).start();
            } else {
                if(onError == null) info(R.string.loginPlease);
                else onError.run();
            }
        }
    }

    public void login(final Runnable run, final boolean onUI, final Runnable onError){
        if(!onUI){
            new Thread(new Runnable() {
                @Override public void run() {
                    loginLogic(run, onUI, onError);
                }
            }).start();
        } else loginLogic(run, onUI, onError);
    }

    public void saveNameNPassword(String name, String password, boolean save){
        if(!save) name = password = "";
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("p", name);
        editor.putString("q", password);
        editor.commit();
    }

    public TextWatcher textSearch(final LinearLayout list){
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(final Editable editable) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        String[] search = specTrimSplit(editable.toString().toLowerCase(), ",");
                        for(int i=0, l = list.getChildCount();i<l;i++){
                            TextView tv = (TextView) list.getChildAt(i);
                            String txt = tv.getText().toString().toLowerCase();

                            boolean included = true;
                            for(String s:search){
                                if(!txt.contains(s)){included = false;break;}}

                            if(tv.getVisibility() == View.VISIBLE) {
                                if (!included) {
                                    tv.setVisibility(View.GONE);}
                            } else if(included){
                                tv.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        };
    }

    public void showChild(View v){
        pageFlipper.removeView(v);
        pageFlipper.addView(v, 2);

        next();
    }

    public void showChild(int id){
        showChild(findViewById(id));
    }

    public void open(int id){

        View v = findViewById(id);

        pageFlipper.removeView(v);
        pageFlipper.addView(v, 1);

        next();
    }

    public void next(final ViewFlipper flipper){
        flipper.setInAnimation(this, R.anim.slide_in_from_right);
        flipper.setOutAnimation(this, R.anim.slide_out_from_right);

        flipper.showNext();
    }

    public void previous(final ViewFlipper flipper){
        flipper.setInAnimation(this, R.anim.slide_in_from_left);
        flipper.setOutAnimation(this, R.anim.slide_out_from_left);

        flipper.showPrevious();
    }

    private void next(){
        if(pageFlipper.getDisplayedChild() == pageFlipper.getChildCount() - 1) return;// weiter rechts geht nicht

        next(pageFlipper);
    }

    public boolean menu(){
        if(pageFlipper.getDisplayedChild() == 0) return false;// weiter links geht nicht

        closeKeyboard();
        previous(pageFlipper);
        return true;
    }

    public TextView textByText(String text){
        TextView textv = new TextView(this);
        textv.setText(text);
        textv.setLayoutParams(WW);
        return textv;
    }

    public TextView textByHTML(String html){
        TextView text = new TextView(this);
        text.setText(html==null?"null":html.indexOf('<')>-1||html.indexOf('&')>-1? Html.fromHtml(html):html);// damit ist es halbwegs ok :), auch wenn noch eine Zeile frei unter dem Text nett wäre; genauso wie padding und so
        text.setLayoutParams(WW);
        return text;
    }

    public TextView headerByText(String text){
        TextView header = new TextView(this);
        header.setText(text);
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(MW);
        header.setTypeface(null, Typeface.BOLD);
        return header;
    }

    public TextView headerByHTML(String html){
        TextView header = new TextView(this);
        header.setText(Html.fromHtml(html));
        header.setGravity(Gravity.CENTER);
        header.setLayoutParams(MW);
        header.setTypeface(null, Typeface.BOLD);
        return header;
    }

    public TableRow tablerowByHTML(boolean sameWidth, String... content){
        TableRow row = new TableRow(this);
        row.setLayoutParams(TRWW);
        for(String element:content){
            row.addView(textByHTML(element));
        } return row;
    }

    static final ViewGroup.LayoutParams WW = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            MW = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            TRWW = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT),
            TR0W = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT),
            TR11 = new TableRow.LayoutParams(1, 1);

    @Override public void onResume(){
        super.onResume();
        if(mapView!=null)
            mapView.onResume();
    }

    @Override public void onPause(){
        super.onPause();
        if(mapView!=null)
            mapView.onPause();
    }

    @Override public void onBackPressed() {
        if(!menu()){
            super.onBackPressed();
        }
    }

    public String[] specTrimSplit(String src, String def){
        src = src.trim();
        if(src.length() < 1) return new String[0];
        String[] r = src.split(def);
        for(int i=0;i<r.length;i++){
            r[i] = r[i].trim();
        } return r;
    }

    public String shortened(String source, int length){
        if(source == null) return null;
        if(source.length() < length) return source;
        else return source.substring(0, length-3)+"...";
    }

    public String abkürzung(String source){
        if(source == null) return "";
        int l = source.length();
        if(l==0) return "";
        String ret = ""+source.charAt(0);
        for(int i=2;i<l;i++){
            char c = source.charAt(i);
            if(c==' '){
                if(++i<l && (c=source.charAt(i))!='"' && Character.toLowerCase(c)!=Character.toUpperCase(c)){
                    ret += c;
                }
            } else if(Character.toLowerCase(c) != c){
                ret += c;
            }
        } return ret;
    }

    public void info(int id){info(getResources().getString(id));}
    public void info(final String msg){
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
