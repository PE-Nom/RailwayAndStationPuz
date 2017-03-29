package com.example.takashi.RailwayAndStationPuz.database;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by takashi on 2016/12/04.
 */

public class Station {
    private int companyId;
    private int lineId;
    private int stationOrder;
    private String stationName;
    private String stationKana;
    private double stationLng;
    private double stationLat;
    private boolean overlaySw = false; // 地図上のマーカーオーバレイ表示
    private boolean answerStatus = false; // false:未回答、true:回答済み

    private Marker mMarker = null;

    public Station(int companyId,
                   int lineId,
                   int stationOrder,
                   String stationName,
                   String stationKana,
                   double stationLat,
                   double stationLng,
                   boolean overlaySw,
                   boolean answerStatus){
        this.companyId = companyId;
        this.lineId = lineId;
        this.stationOrder = stationOrder;
        this.stationName = stationName;
        this.stationKana = stationKana;
        this.stationLng = stationLng;
        this.stationLat = stationLat;
        this.overlaySw = overlaySw;
        this.answerStatus=answerStatus;
    }

    public String getName(){
        return this.stationName;
    }
    public String getKana() { return this.stationKana; }

    public double getStationLng(){
        return this.stationLng;
    }
    public double getStationLat(){
        return this.stationLat;
    }

    public int getCompanyId() { return  this.companyId; }
    public int getLineId(){
        return this.lineId;
    }
    public int getStationOrder() {return  this.stationOrder; }

    public boolean isFinished(){
        return this.answerStatus;
    }

    public void setFinishStatus(){
        this.answerStatus=true;
    }
    public void resetFinishStatus() { this.answerStatus=false; }

    public boolean isOverlaySw(){return this.overlaySw;}

    public void setMarker(Marker marker){
        if(this.mMarker!=null){
            this.mMarker.remove();
        }
        this.mMarker = marker;
        this.overlaySw = true;
    }

    public void removeMarker(){
        if(this.mMarker!=null){
            this.mMarker.remove();
            this.mMarker = null;
            this.overlaySw = false;
        }
    }
}
