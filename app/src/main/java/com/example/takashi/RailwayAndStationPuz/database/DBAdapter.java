package com.example.takashi.RailwayAndStationPuz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by takashi on 2017/01/06.
 */

public class DBAdapter {
    static final String DATABASE_NAME = "Railway.db";
    static final int DATABASE_VERSION = 19;

    private String TAG = "DBAdapter";

    protected final Context context;
    protected DatabaseHelper dbHelper;
    protected SQLiteDatabase db;

    public DBAdapter(Context context){
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
        Log.d(TAG,"DBAdapter construct");
    }

    //
    // SQLiteOpenHelper
    //

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private String TAG = "DatabaseHelper";
        private Context context;
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            Log.d(TAG,"DatabaseHelper construct");
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            InputStream is;
            BufferedReader bfReader;
            Log.d(this.TAG,"onCreate DB");
            try {
                is = this.context.getAssets().open("init.sql");
                bfReader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while(( line = bfReader.readLine() ) != null){
                    if( line.charAt(0)=='#') continue;
                    if(! line.equals("") ){
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                for(String sql: sb.toString().split(";")){
                    Log.d(TAG,sql);
                    db.execSQL(sql);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"onUpgrade");
            db.execSQL("DROP TABLE IF EXISTS currentMode");
            db.execSQL("DROP TABLE IF EXISTS area");
            db.execSQL("DROP TABLE IF EXISTS companyType");
            db.execSQL("DROP TABLE IF EXISTS companies");
            db.execSQL("DROP TABLE IF EXISTS lines");
            db.execSQL("DROP TABLE IF EXISTS stations");
            onCreate(db);
        }
    }

    //
    // Adapter Methods
    //

    public DBAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    //
    // App Methods
    //

    // currentMode table
    public CurrentMode getCurrentMode(){
        CurrentMode mode = null;
        Cursor cursor = db.rawQuery("SELECT * from currentMode",null);
        try{
            if(cursor.moveToFirst()){
                Log.d(TAG,"get currentMode record");
                int companyId = cursor.getInt(cursor.getColumnIndex("companyId"));
                boolean displayGrid = (cursor.getInt(cursor.getColumnIndex("displayGrid"))==1);
                mode = new CurrentMode(companyId,displayGrid);
            }
            else{
                Log.d(TAG,"moveText　終了");
            }
        }finally {
            cursor.close();
        }
        return mode;
    }

    public boolean updateSelectedCompany(int companyId){
        ContentValues cv = new ContentValues();
        cv.put("companyId", companyId);
        db.update("currentMode", cv, "modeId = 1", null);
        return true;
    }

    public boolean updateDisplayMode(boolean displayGrid){
        ContentValues cv = new ContentValues();
        if(displayGrid){
            cv.put("displayGrid", 1);
        }
        else{
            cv.put("displayGrid", 0);
        }
        db.update("currentMode", cv, "modeId = 1", null);
        return true;
    }

    // companies table
    private Company extractCompany( Cursor c){
        int id = c.getInt(c.getColumnIndex("companyId"));
        int code = c.getInt(c.getColumnIndex("companyCode"));
        String name = c.getString(c.getColumnIndex("companyName"));
        String kana = c.getString(c.getColumnIndex("companyKana"));
        Company company= new Company(id,code,name,kana);
        Log.d(TAG,String.format("company : %d,%d,%s,%s",
                company.getId(),company.getCode(),
                company.getName(),company.getKana()));
        return company;
    }

    public Company getCompany(int companyId) {
        Company company = null;
        Cursor cursor = db.rawQuery("SELECT * from companies WHERE companyId=?",new String[]{String.valueOf(companyId)});
        try{
            if(cursor.moveToFirst()){
                company = extractCompany(cursor);
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return company;


    }

    public ArrayList<Company> getCompanies(){
        ArrayList<Company> companies = new ArrayList<Company>();
        Cursor cursor = db.rawQuery("SELECT * from companies",null);
        try{
            if(cursor.moveToFirst()){
                do{
                    companies.add(extractCompany(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return companies;
    }

    // lines table
    private Line extractLine(Cursor c){
        int lineId = c.getInt(c.getColumnIndex("lineId"));
        int areaCode = c.getInt(c.getColumnIndex("areaCode"));
        int companyId = c.getInt(c.getColumnIndex("companyId"));
        String lineName = c.getString(c.getColumnIndex("lineName"));
        String lineKana = c.getString(c.getColumnIndex("lineKana"));
        int type = c.getInt(c.getColumnIndex("type"));
        String drawable_resource_name = c.getString(c.getColumnIndex("drawable_resource_name"));
        String raw_resource_name = c.getString(c.getColumnIndex("raw_resource_name"));
        double correct_leftLng = c.getDouble(c.getColumnIndex("correct_leftLng"));
        double correct_topLat = c.getDouble(c.getColumnIndex("correct_topLat"));
        double correct_rightLng = c.getDouble(c.getColumnIndex("correct_rightLng"));
        double correct_bottomLat = c.getDouble(c.getColumnIndex("correct_bottomLat"));
        double scroll_max_lat = c.getDouble(c.getColumnIndex("scroll_max_lat"));
        double scroll_min_lat = c.getDouble(c.getColumnIndex("scroll_min_lat"));
        double scroll_max_lng = c.getDouble(c.getColumnIndex("scroll_max_lng"));
        double scroll_min_lng = c.getDouble(c.getColumnIndex("scroll_min_lng"));
        double init_campos_lat = c.getDouble(c.getColumnIndex("init_campos_lat"));
        double init_campos_lng = c.getDouble(c.getColumnIndex("init_campos_lng"));
        double max_zoom_level = c.getDouble(c.getColumnIndex("max_zoom_level"));
        double min_zoom_level = c.getDouble(c.getColumnIndex("min_zoom_level"));
        double init_zoom_level = c.getDouble(c.getColumnIndex("init_zoom_level"));
        boolean locationPlayingStatus = (c.getInt(c.getColumnIndex("locationPlayingStatus"))==1);
        int locationPlayingTime = c.getInt(c.getColumnIndex("locationPlayingTime"));
        boolean locationAnswerStatus = (c.getInt(c.getColumnIndex("locationAnswerStatus"))==1);
        int locationAnswerTime = c.getInt(c.getColumnIndex("locationAnswerTime"));
        boolean stationPlayingStatus = (c.getInt(c.getColumnIndex("stationPlayingStatus"))==1);
        int stationPlayingTime = c.getInt(c.getColumnIndex("stationPlayingTime"));
        boolean stationAnswerStatus = (c.getInt(c.getColumnIndex("stationAnswerStatus"))==1);
        int stationAnswerTime = c.getInt(c.getColumnIndex("stationAnswerTime"));
        int totalStations = c.getInt(c.getColumnIndex("totalStationCount"));
        int answeredStations = c.getInt(c.getColumnIndex("answeredStationCount"));
        Line line = new Line(this.context,
                lineId,areaCode,companyId,
                lineName,lineKana,type,
                drawable_resource_name,raw_resource_name,
                correct_leftLng,correct_topLat,correct_rightLng,correct_bottomLat,
                scroll_max_lat,scroll_min_lat,scroll_max_lng,scroll_min_lng,init_campos_lat,init_campos_lng,
                max_zoom_level,min_zoom_level,init_zoom_level,
                locationPlayingStatus,locationPlayingTime,locationAnswerStatus,locationAnswerTime,
                stationPlayingStatus,stationPlayingTime,stationAnswerStatus,stationAnswerTime,
                totalStations,answeredStations);
        Log.d(TAG,String.format("lines: %d,%d,%d," +
                        "%s,%s," +
                        "%d," +
                        "%s,%s," +
                        "%f,%f,%f,%f," +
                        "%f,%f,%f,%f," +
                        "%f,%f," +
                        "%f,%f,%f," +
                        "%b,%d,%b,%d," +
                        "%b,%d,%b,%d," +
                        "%d,%d",
                lineId,areaCode,companyId,
                lineName,lineKana,
                type,
                drawable_resource_name,raw_resource_name,
                correct_leftLng,correct_topLat,correct_rightLng,correct_bottomLat,
                scroll_max_lat,scroll_min_lat,scroll_max_lng,scroll_min_lng,
                init_campos_lat,init_campos_lng,
                max_zoom_level,min_zoom_level,init_zoom_level,
                locationPlayingStatus,locationPlayingTime,locationAnswerStatus,locationAnswerTime,
                stationPlayingStatus,stationPlayingTime,stationAnswerStatus,stationAnswerTime,
                totalStations,answeredStations
        ));
        return line;
    }

    public Line getLine(int lineId){
        Line line = null;
        Cursor cursor = db.rawQuery("SELECT * from lines WHERE lineId=?",new String[]{String.valueOf(lineId)});
        try{
            if(cursor.moveToFirst()){
                line = extractLine(cursor);
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return line;

    }

    public ArrayList<Line> getLineList(int companyId){
        ArrayList<Line> lines = new ArrayList<Line>();
        Cursor cursor = db.rawQuery("SELECT * from lines WHERE companyId=?",new String[]{String.valueOf(companyId)});
        try{
            if(cursor.moveToFirst()){
                do{
                    lines.add(extractLine(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return lines;
    }

    /*
     * ロケーションセットのステータス更新
     */
    public boolean updateLineLocationAnswerStatus(Line line){
        int lineId = line.getLineId();
        ContentValues cv = new ContentValues();
        if(line.isLocationCompleted()){
            cv.put("locationAnswerStatus", 1);
        }
        else{
            cv.put("locationAnswerStatus", 0);
        }
        db.update("lines", cv, "lineId = "+lineId, null);
        return true;
    }

    // stations table
    private Station extractStation(Cursor c){
        int lineId = c.getInt(c.getColumnIndex("lineId"));
        int stationOrder = c.getInt(c.getColumnIndex("stationOrder"));
        String stationName = c.getString(c.getColumnIndex("stationName"));
        String stationKana = c.getString(c.getColumnIndex("stationKana"));
        double stationLat = c.getDouble(c.getColumnIndex("stationLat"));
        double stationLng = c.getDouble(c.getColumnIndex("stationLng"));
        boolean overlaySw = (c.getInt(c.getColumnIndex("overlaySw"))==1);
        boolean answerStatus = (c.getInt(c.getColumnIndex("answerStatus"))==1);
        boolean playingStatus = (c.getInt(c.getColumnIndex("playingStatus"))==1);
        Station station = new Station(lineId,stationOrder,
                                        stationName,stationKana,
                                        stationLat,stationLng,
                                        overlaySw,answerStatus,playingStatus);
        Log.d(TAG,String.format("station: %d,%d," +
                        "%s,%s," +
                        "%f,%f," +
                        "%b,%b,%b",
                        lineId,stationOrder,
                        stationName,stationKana,
                        stationLat,stationLng,
                        overlaySw,answerStatus,playingStatus
        ));
        return station;
    }

    public ArrayList<Station> getStationList(int lineId){
        ArrayList<Station> stations = new ArrayList<Station>();
        Cursor cursor = db.rawQuery("SELECT * from stations WHERE lineId=? ORDER BY stationOrder",new String[]{String.valueOf(lineId)});
        try{
            if(cursor.moveToFirst()){
                do{
                    stations.add(extractStation(cursor));
                }while(cursor.moveToNext());
            }
            else{
                Log.d(TAG,"No record selected");
            }
        }finally {
            cursor.close();
        }
        return stations;

    }

    public boolean updateStationAnswerStatus(Station station){
        ContentValues cv = new ContentValues();
        if(station.isFinished()){
            cv.put("answerStatus", 1);
        }
        else{
            cv.put("answerStatus", 0);
        }
        db.update("stations", cv, "lineId = "+station.getLineId() + " AND stationOrder = " + station.getStationOrder(), null);
        return true;
    }

    public boolean updateStationMarkerOverlaySw(Station station){
        ContentValues cv = new ContentValues();
        if(station.isOverlaySw()){
            cv.put("overlaySw", 1);
        }
        else{
            cv.put("overlaySw", 0);
        }
        db.update("stations", cv, "lineId = "+station.getLineId() + " AND stationOrder = " + station.getStationOrder(), null);
        return true;
    }

}
