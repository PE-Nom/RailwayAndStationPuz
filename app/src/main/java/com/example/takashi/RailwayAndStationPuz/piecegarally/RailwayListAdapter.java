package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Line;

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
        TextView locationStatus;
        TextView stationStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = this.inflater.inflate(R.layout.railway_list_item,parent,false);
            holder = new ViewHolder();
            holder.pieceBorderImage = (ImageView)convertView.findViewById(R.id.piece_border_list_image_view);
            holder.railwayLineImage = (ImageView)convertView.findViewById(R.id.railway_line_list_image_view);
            holder.railwayLineName = (TextView)convertView.findViewById(R.id.railway_line_list_name);
            holder.locationStatus = (TextView)convertView.findViewById(R.id.location_status);
            holder.stationStatus = (TextView)convertView.findViewById(R.id.station_status);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Line railway = lines.get(position);
        Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(),railway.getDrawableResourceId(),null);
        holder.railwayLineImage.setImageDrawable(drawable);
        holder.railwayLineName.setText(railway.getName()+"("+railway.getLineKana()+")");
        holder.railwayLineName.setTextColor(Color.parseColor("#142d81"));

        Drawable locationStatusImage = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.location_notyet_image,null);
        locationStatusImage.setBounds(0,0,18,18);
        holder.locationStatus.setText("ロケーション進捗：");
        holder.locationStatus.setTextColor(Color.DKGRAY);
        holder.locationStatus.setCompoundDrawables(null,null,locationStatusImage,null);
        holder.locationStatus.setCompoundDrawablePadding(5);

        Drawable stationStatusImage = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.station_notyet_image,null);
        stationStatusImage.setBounds(0,0,18,18);
        holder.stationStatus.setText("ステーション進捗：");
        holder.stationStatus.setTextColor(Color.DKGRAY);
        holder.stationStatus.setCompoundDrawables(null,null,stationStatusImage,null);
        holder.stationStatus.setCompoundDrawablePadding(5);

        return convertView;
    }
}
