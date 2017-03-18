package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.example.takashi.RailwayAndStationPuz.ui.MultiButtonListView;
import com.example.takashi.RailwayAndStationPuz.ui.SimpleGaugeView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by takashi on 2016/12/19.
 */

public class RailwayListAdapter extends BaseAdapter {
    private static String TAG = "RailwayListAdapter";
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Line> lines;

    public RailwayListAdapter(Context context, ArrayList<Line> lines){
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.lines = lines;
    }

    @Override
    public int getCount() {
        return this.lines.size();
    }

    @Override
    public Object getItem(int i) {
        return this.lines.get(i);
    }

    @Override
    public long getItemId(int i) {
        return this.lines.get(i).getDrawableResourceId();
    }

    private class ViewHolder {
        ImageView railwayLineImage;
        ImageView pieceBorderImage;
        TextView railwayLineName;
//        Button mapBtn;
        ImageButton mapImageBtn;
        ImageButton staImageBtn;
        SimpleGaugeView progGauge;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        MultiButtonListView list = null;
        try{
            list = (MultiButtonListView)parent;
        }catch(Exception e){
            e.printStackTrace();
        }

        if(convertView == null){
            convertView = this.inflater.inflate(R.layout.railway_list_item,parent,false);
            holder = new ViewHolder();
            holder.pieceBorderImage = (ImageView)convertView.findViewById(R.id.piece_border_list_image_view);
            holder.railwayLineImage = (ImageView)convertView.findViewById(R.id.railway_line_list_image_view);
            holder.railwayLineName = (TextView) convertView.findViewById(R.id.linename);

            holder.mapImageBtn = (ImageButton)convertView.findViewById(R.id.mapImageButton);
            holder.staImageBtn = (ImageButton)convertView.findViewById(R.id.stationImageButton);
            holder.progGauge = (SimpleGaugeView) convertView.findViewById(R.id.stationProgress) ;

            holder.mapImageBtn.setOnClickListener(list);
            holder.staImageBtn.setOnClickListener(list);
            holder.progGauge.setData(40,"%", ContextCompat.getColor(this.context, R.color.color_90));

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Line line = lines.get(position);
        Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(),line.getDrawableResourceId(),null);
        holder.railwayLineImage.setImageDrawable(drawable);
        if(line.isNameCompleted()){
            holder.railwayLineName.setText(line.getName()+"("+line.getLineKana()+")");
        }
        else{
            holder.railwayLineName.setText("*****");
        }
        holder.railwayLineName.setTextColor(Color.parseColor("#142d81"));

        holder.mapImageBtn.setImageResource(R.mipmap.ic_tracklaying_complete);
        holder.mapImageBtn.setTag(position);
        holder.staImageBtn.setImageResource(R.mipmap.ic_station_complete);
        holder.staImageBtn.setTag(position);

        holder.progGauge.setData(40,"%", ContextCompat.getColor(this.context, R.color.color_90));

        return convertView;
    }
}
