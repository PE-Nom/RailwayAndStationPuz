package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.MainActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Line;

/**
 * Created by takashi on 2017/01/04.
 */

public class ContextMenuAdapter extends BaseAdapter {

    private String TAG = "ContextMenuAdapter";
    private String[] contextMenuItems= new String[] {
        "ロケーションセット",
        "ステーションセット",
    };
    private int[] imageResourceId = new int[] {
        R.drawable.ic_railwayselector,
        R.drawable.ic_stationselector,
    };
    private MainActivity context;
    private Line line;
    private int id;

    public ContextMenuAdapter(Activity context, Line line, int id) {
        this.context = (MainActivity)context;
        this.line = line;
        this.id = id;
    }

    @Override
    public int getCount() {
        return contextMenuItems.length;
    }

    @Override
    public Object getItem(int position) {
        return contextMenuItems[position];
    }

    @Override
    public long getItemId(int position) {
        return this.id;
    }

    private class ViewHolder {
        TextView contextMenuItemTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            TextView temp = new TextView(context);
            convertView = temp;
        }

        TextView textView = (TextView) convertView;
        textView.setText(this.contextMenuItems[position]);
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,120);
        mlp.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(mlp);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(),this.imageResourceId[position], null);
//        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        drawable.setBounds(10,0,44,34);
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setCompoundDrawablePadding(40);

        if(position == 1 && !this.line.isLocationCompleted()){
            textView.setTextColor(Color.GRAY);
        }
        else{
            textView.setTextColor(Color.BLACK);
        }
        return textView;
    }
}
