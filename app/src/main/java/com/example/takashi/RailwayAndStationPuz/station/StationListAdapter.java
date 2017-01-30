package com.example.takashi.RailwayAndStationPuz.station;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Station;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by takashi on 2016/12/04.
 */

public class StationListAdapter extends BaseAdapter {

    private String TAG = "StationListAdapter";
    private ArrayList<Station> stations;
    private GoogleMap mMap;
    private StationPuzzleActivity context;

    public StationListAdapter(Activity context, ArrayList<Station> stations, GoogleMap mMap){
        super();
        this.context=(StationPuzzleActivity)context;
        this.stations = stations;
        this.mMap = mMap;
    }

    @Override
    public int getCount() {
        return this.stations.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return this.stations.get(i);
    }

    public Station getStationInfo(int position){
        return this.stations.get(position);
    }

    private class ViewHolder {
        TextView stationName;
        CheckBox mapOverlaySw;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewParent) {

        final ViewHolder holder;
        LayoutInflater inflater = this.context.getLayoutInflater();

        if(convertView == null){
            convertView = inflater.inflate(R.layout.station_list_item,null);
            holder = new ViewHolder();
            holder.stationName = (TextView)convertView.findViewById(R.id.station_name);
            holder.mapOverlaySw = (CheckBox)convertView.findViewById(R.id.mapOverlaySw);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Station station = (Station)stations.get(position);

        if(station.isFinished()){   // 当該ステーションが正解済み
            Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.ic_railway_line_a,null);
            drawable.setBounds(0,0,30,48);
            holder.stationName.setCompoundDrawables(drawable,null,null,null);
            holder.stationName.setCompoundDrawablePadding(0);
            String name = station.getName() + "(" + station.getKana() + ")";
            holder.stationName.setText(name);
            holder.mapOverlaySw.setChecked(station.isOverlaySw());
            holder.mapOverlaySw.setClickable(true);
            holder.mapOverlaySw.setOnClickListener(new cbOnClickListener(holder,position));
            if(station.isOverlaySw()){
                station.removeMarker();
                LatLng latlng = new LatLng(station.getStationLat(), station.getStationLng());
                Log.d(TAG,String.format("initialize overlay : %s, Lat = %3.4f, Lng = %3.4f",station.getName(),station.getStationLat(),station.getStationLng()));
                MarkerOptions options = new MarkerOptions().position(latlng).title(station.getName());
                Marker marker = this.mMap.addMarker(options);
                station.setMarker(marker);
            }
        }
        else{                       // 当該ステーションが未正解
            Drawable drawable = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.ic_railway_line_b,null);
            drawable.setBounds(0,0,30,48);
            holder.stationName.setCompoundDrawables(drawable,null,null,null);
            holder.stationName.setCompoundDrawablePadding(0);
            holder.stationName.setText("------------");
            holder.mapOverlaySw.setChecked(false);
            holder.mapOverlaySw.setClickable(false);
        }

        return convertView;
    }

    // checkbox操作のイベント処理
    private class cbOnClickListener implements View.OnClickListener {
        private int position;
        private ViewHolder holder;
        public cbOnClickListener(ViewHolder holder,int position){
            this.position = position;
            this.holder = holder;
        }
        public void onClick(View view){
            DBAdapter db = StationListAdapter.this.context.getDb();
            Station station = (Station)StationListAdapter.this.stations.get(this.position);
            if(this.holder.mapOverlaySw.isChecked()){
                // ここでオーバーレイ表示する
                Log.d(TAG,String.format("checked mapOverlaySw : %s, Lat = %3.4f, Lng = %3.4f",station.getName(),station.getStationLat(),station.getStationLng()));
                LatLng latlng = new LatLng(station.getStationLat(), station.getStationLng());
                MarkerOptions options = new MarkerOptions().position(latlng).title(station.getName());
                Marker marker = StationListAdapter.this.mMap.addMarker(options);
                station.setMarker(marker);
            }
            else {
                // ここでオーバーレイ表示を削除する
                Log.d(TAG, "mapOverlaySw : false");
                station.removeMarker();
            }
            db.updateStationMarkerOverlaySw(station);
        }
    }
}
