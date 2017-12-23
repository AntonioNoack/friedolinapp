package me.noack.antonio.friedomobile.extract;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import me.noack.antonio.friedomobile.AllManager;

/**
 * Created by antonio on 27.11.2017
 */

public abstract class Features {
    public abstract void drawOnList(final AllManager all, ViewGroup list);
    public abstract void drawDetails(AllManager all);

    public static final ViewGroup.LayoutParams
            linMW = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),

    relWW = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT),
            txtWW = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            tabWW = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT),
            rowWW = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                    ;

    public static TableRow keyValuePair2(AllManager all, View key, View val){
        TableRow row = new TableRow(all);

        row.setLayoutParams(tabWW);
        key.setLayoutParams(rowWW);
        key.setPadding(0, 0, dp2px(all, 10), 0);// ltrb
        val.setLayoutParams(rowWW);

        row.addView(key);
        row.addView(val);

        return row;
    }

    public static int dp2px(AllManager all, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, all.getResources().getDisplayMetrics());
    }
}
