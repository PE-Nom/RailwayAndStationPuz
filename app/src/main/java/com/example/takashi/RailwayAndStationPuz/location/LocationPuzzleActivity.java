package com.example.takashi.RailwayAndStationPuz.location;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.takashi.RailwayAndStationPuz.piecegarally.PieceGarallyActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class LocationPuzzleActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        OnLineScrollEndListener {

    private final static String TAG = "LocationPuzzleActivity";
    private String lineNameNone = "*****";
    private String lineName;
    private DBAdapter db;
    private int companyId;
    private int selectedLineId;
    private Line line;
    private GoogleMap mMap;
    private MapView mMapView;
    private GeoJsonLayer layer;
    private LineMapOverlayView mImageView;
    private ArrayList<View> views = new ArrayList<View>();

    private boolean geoJsonVisible = false;
    private Drawable mDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_location_puzzle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.location_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        Intent intent = getIntent();
        this.selectedLineId = intent.getIntExtra("SelectedLineId", 42); // デフォルトを紀勢線のlineIdにしておく

        this.db = new DBAdapter(this);
        this.db.open();
        this.line = db.getLine(this.selectedLineId);
        Log.d(TAG,String.format("selected line is %s",line.getName()));
        setUpMap(savedInstanceState);

        String companyName = db.getCompany(line.getCompanyId()).getName();
        String lineName = line.getName();
        String linekana = line.getLineKana();
        this.companyId = line.getCompanyId();

        actionBar.setTitle("Puz-Rail：Location Set");
        if(line.isNameCompleted()){
            this.lineName = lineName+"("+linekana+")";
        }
        else{
            this.lineName = lineNameNone;
        }
        actionBar.setSubtitle(companyName+"／"+this.lineName);

    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this.getApplicationContext(), PieceGarallyActivity.class);
        intent.putExtra("SelectedCompanyId", this.companyId);
        startActivityForResult(intent, 1);
        // アニメーションの設定
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        this.db.close();
    	finish();
    }

    private void setUpMap(Bundle savedInstanceState){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        this.mMapView = (MapView)findViewById(R.id.mapView);
        this.mMapView.onCreate(savedInstanceState);
        this.mMapView.getMapAsync(this);

        mImageView = (LineMapOverlayView)findViewById(R.id.imageview);
        mImageView.setOnScrollEndListener(this);
        mImageView.setLine(this.line); //
        views.add(mImageView);

        this.mMapView.addFocusables(views,View.FOCUS_FORWARD);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // マップ形式の設定
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // 表示エリアと縮尺の制限
        UiSettings mUiSetting = mMap.getUiSettings();
        mUiSetting.setRotateGesturesEnabled(false);
        mMap.setMaxZoomPreference(this.line.getMaxZoomLevel());
        mMap.setMinZoomPreference(this.line.getMinZoomLevel());
        // 離島除く
        LatLng north_east = new LatLng(this.line.getScrollMaxLat(),this.line.getScrollMaxLng());
        LatLng south_west = new LatLng(this.line.getScrollMinLat(),this.line.getScrollMinLng());
        LatLngBounds JAPAN = new LatLngBounds(south_west,north_east);
        mMap.setLatLngBoundsForCameraTarget(JAPAN);

        // EventListener
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(this.line.getInitCamposLat(),this.line.getInitCamposLng()),
                        this.line.getInitZoomLevel())
                        );

        mImageView.setMap(this.mMap); //
    }

    // GeoJsonLayerの生成とColorの指定、Mapへの登録
    private void retrieveFileFromResource() {
        try {
            // 路線図のGeoJsonファイル読込
            layer = new GeoJsonLayer(mMap, this.line.getRawResourceId(), this);

            // 路線図の色を変更
            GeoJsonLineStringStyle style = layer.getDefaultLineStringStyle();
            style.setWidth(5.0f);
            style.setColor(Color.BLUE);

        } catch (IOException e) {
            Log.e(TAG, "GeoJSON file could not be read");
        } catch (JSONException e) {
            Log.e(TAG, "GeoJSON file could not be converted to a JSONObject");
        }
    }

    private boolean isGeoJsonVisible(){
        return geoJsonVisible;
    }

    private void setGeoJsonVisible(){
        if(isGeoJsonVisible()){
            layer.removeLayerFromMap();
            layer = null;
            geoJsonVisible=false;
        }
        else{
            // Loading a local GeoJSON file.
            retrieveFileFromResource();
            layer.addLayerToMap();
            geoJsonVisible=true;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG,"onMapLongClick");
        setGeoJsonVisible();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"onMapClick");
        if(mImageView.getDrawable() == null){
            setImageDrawable();
        }
        else{
            resetImageDrawable();
        }
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG,"onCamelaIdel");
        if(mImageView.getDrawable()!=null){
            checkLocation();
        }
        CameraPosition campos = mMap.getCameraPosition();
        Log.d(TAG,String.format("カメラ現在位置 lat = %f, Lng = %f, zoom = %f", campos.target.latitude,campos.target.longitude,campos.zoom));
        Projection proj = mMap.getProjection();
        VisibleRegion vRegion = proj.getVisibleRegion();
        // 北東 = top/right, 南西 = bottom/left
        double topLatitude = vRegion.latLngBounds.northeast.latitude;
        double bottomLatitude = vRegion.latLngBounds.southwest.latitude;
        double leftLongitude = vRegion.latLngBounds.southwest.longitude;
        double rightLongitude = vRegion.latLngBounds.northeast.longitude;
        Log.d(TAG, "地図表示範囲\n緯度:" + bottomLatitude + "～" + topLatitude + "\n経度:" + leftLongitude + "～" + rightLongitude);
    }

    @Override
    public void onScrollEnd() {
        Log.d(TAG,"onScrollEnd");
        checkLocation();
    }

    private void setImageDrawable(){
        mDrawable  = ResourcesCompat.getDrawable(getResources(), this.line.getDrawableResourceId(), null);
        mImageView.setImageDrawable(mDrawable);
    }

    private void resetImageDrawable(){
        mImageView.setImageDrawable(null);
        mDrawable=null;
    }

    private boolean checkLocation(){

        RectF railwayImageRect = mImageView.getCurrentImageRect();
        Log.d(TAG,String.format("RailwayLine Image : left=%f,top=%f,right=%f,bottom=%f",
                railwayImageRect.left,railwayImageRect.top,railwayImageRect.right,railwayImageRect.bottom));
        Point screenPoint1 = new Point((int)railwayImageRect.left,(int)railwayImageRect.top);
        Point screenPoint2 = new Point((int)railwayImageRect.right,(int)railwayImageRect.bottom);
        LatLng point1 = mMap.getProjection().fromScreenLocation(screenPoint1);
        LatLng point2 = mMap.getProjection().fromScreenLocation(screenPoint2);
        Log.d(TAG,String.format("answer = %f,%f, point1 = %f,%f",
                this.line.getCorrectTopLat(),this.line.getCorrectLeftLng(),point1.latitude,point1.longitude));
        Log.d(TAG,String.format("answer = %f,%f, point2 = %f,%f",
                this.line.getCorrectBottomLat(),this.line.getCorrectRightLng(),point2.latitude,point2.longitude));
        // ロケーション正誤判定
        double error1 = Math.abs(this.line.getCorrectTopLat()-point1.latitude);
        double error2 = Math.abs(this.line.getCorrectLeftLng()-point1.longitude);
        double error3 = Math.abs(this.line.getCorrectBottomLat()-point2.latitude);
        double error4 = Math.abs(this.line.getCorrectRightLng()-point2.longitude);
        double error = error1+error2+error3+error4;
        Log.d(TAG,String.format("error = %f, %f, %f, %f, sum = %f",error1,error2,error3,error4,error));

        if(error1 < 0.05 && error2 < 0.05 && error3 < 0.05 && error4 <0.05)
        {
            // 正解
            Log.d(TAG,"ロケーションOK");
            resetImageDrawable();
            setGeoJsonVisible();
            if(!isGeoJsonVisible()){
                setGeoJsonVisible();
            }
            this.line.setLocationAnswerStatus();
            db.updateLineLocationAnswerStatus(this.line);
        }
        else{
            Log.d(TAG,"ロケーションNG");
        }
        return true;

    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }
    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

}
