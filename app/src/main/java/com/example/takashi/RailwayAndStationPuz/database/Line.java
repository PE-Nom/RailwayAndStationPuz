package com.example.takashi.RailwayAndStationPuz.database;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by takashi on 2016/11/24.
 */

public class Line {
    private Context context;
    private Resources res;

    private String TAG = "Line";
    private int lineId;
    private int areaCode;
    private int companyId;
    private String lineName;
    private String lineKana;
    private int type;
    private String drawable_resource_name;
    private String raw_resource_name;
    private double correct_leftLng;
    private double correct_topLat;
    private double correct_rightLng;
    private double correct_bottomLat;
    private double scroll_max_lat;
    private double scroll_min_lat;
    private double scroll_max_lng;
    private double scroll_min_lng;
    private double init_campos_lat;
    private double init_campos_lng;
    private double max_zoom_level;
    private double min_zoom_level;
    private double init_zoom_level;

    private boolean locationPlayingStatus;
    private int locationPlayingTime;
    private boolean locationAnswerStatus;
    private int locationAnswerTime;

    private boolean stationPlayingStatus;
    private int stationPlayingTime;
    private boolean stationAnswerStatus;
    private int stationAnswerTime;

    private int totalStations;
    private int answeredStations;

    public Line(Context context,
                int lineId,
                int areaCode,
                int companyId,
                String lineName,
                String lineKana,
                int type,
                String drawable_resource_name,
                String raw_resource_name,
                double correct_leftLng,
                double correct_topLat,
                double correct_rightLng,
                double correct_bottomLat,
                double scroll_max_lat,
                double scroll_min_lat,
                double scroll_max_lng,
                double scroll_min_lng,
                double init_campos_lat,
                double init_campos_lng,
                double max_zoom_level,
                double min_zoom_level,
                double init_zoom_level,
                boolean locationPlayingStatus,
                int locationPlayingTime,
                boolean locationAnswerStatus,
                int locationAnswerTime,
                boolean stationPlayingStatus,
                int stationPlayingTime,
                boolean stationAnswerStatus,
                int stationAnswerTime,
                int totalStations,
                int answeredStations) {

        this.context = context;
        this.res = this.context.getResources();
        this.lineId = lineId;
        this.areaCode = areaCode;
        this.companyId = companyId;
        this.lineName = lineName;
        this.lineKana = lineKana;
        this.type = type;
        this.drawable_resource_name = drawable_resource_name;
        this.raw_resource_name = raw_resource_name;
        this.correct_leftLng = correct_leftLng;
        this.correct_topLat = correct_topLat;
        this.correct_rightLng = correct_rightLng;
        this.correct_bottomLat = correct_bottomLat;
        this.scroll_max_lat = scroll_max_lat;
        this.scroll_max_lng = scroll_max_lng;
        this.scroll_min_lat = scroll_min_lat;
        this.scroll_min_lng = scroll_min_lng;
        this.init_campos_lat = init_campos_lat;
        this.init_campos_lng = init_campos_lng;
        this.max_zoom_level = max_zoom_level;
        this.min_zoom_level = min_zoom_level;
        this.init_zoom_level = init_zoom_level;
        this.locationPlayingStatus = locationPlayingStatus;
        this.locationPlayingTime = locationPlayingTime;
        this.locationAnswerStatus = locationAnswerStatus;
        this.locationAnswerTime = locationAnswerTime;
        this.stationPlayingStatus = stationPlayingStatus;
        this.stationPlayingTime = stationPlayingTime;
        this.stationAnswerStatus = stationAnswerStatus;
        this.stationAnswerTime = stationAnswerTime;
        this.totalStations = totalStations;
        this.answeredStations = answeredStations;
    }

    public int getCompanyId(){ return this.companyId; }
    public int getLineId(){ return this.lineId; }
    public String getName(){
        return this.lineName;
    }
    public String getLineKana() { return this.lineKana; }
    public int getDrawableResourceId() {
        Log.d(TAG,String.format("Line drawable resource name : %s", this.drawable_resource_name));
        return this.res.getIdentifier(this.drawable_resource_name, "drawable", this.context.getPackageName());
    }
    public int getRawResourceId() {
        return this.res.getIdentifier(this.raw_resource_name, "raw", this.context.getPackageName());
    }
    public float getMaxZoomLevel(){
        return (float)this.max_zoom_level;
    }
    public float getMinZoomLevel(){
        return (float)this.min_zoom_level;
    }
    public float getInitZoomLevel(){
        return (float)this.init_zoom_level;
    }
    public double getCorrectLeftLng(){
        return this.correct_leftLng;
    }
    public double getCorrectTopLat(){
        return this.correct_topLat;
    }
    public double getCorrectRightLng(){
        return this.correct_rightLng;
    }
    public double getCorrectBottomLat(){
        return this.correct_bottomLat;
    }
    public double getScrollMaxLat(){
        return this.scroll_max_lat;
    }
    public double getScrollMinLat(){
        return this.scroll_min_lat;
    }
    public double getScrollMaxLng(){
        return this.scroll_max_lng;
    }
    public double getScrollMinLng(){
        return this.scroll_min_lng;
    }
    public double getInitCamposLat(){
        return this.init_campos_lat;
    }
    public double getInitCamposLng(){
        return this.init_campos_lng;
    }

    public boolean isLocationCompleted(){
        return this.locationAnswerStatus;
    }
    public void setLocationAnswerStatus(){
        this.locationAnswerStatus=true;
    }
    public void resetLocationAnswerStatus(){
        this.locationAnswerStatus=false;
    }

    public boolean isStationCompleted(){
        return this.stationAnswerStatus;
    }
}