package com.example.takashi.RailwayAndStationPuz.location;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.piecegarally.PieceGarallyActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.example.takashi.RailwayAndStationPuz.ui.PopUp;
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
import java.util.Timer;
import java.util.TimerTask;

public class LocationPuzzleActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        OnLineScrollEndListener {

    private final static String TAG = "LocationPuzzleActivity";
    private String lineName;
    private DBAdapter db;
    private int companyId;
    private int selectedLineId;
    private Line line;
    private GoogleMap mMap;
    private MapView mMapView;
    private GeoJsonLayer layer;
    private LineMapOverlayView mImageView;
    private ImageView transparent;
    private ArrayList<View> views = new ArrayList<View>();

    private Drawable mDrawable;
    private AlertDialog mDialog;

    private final static long DISPLAY_ANSWERE_TIME = 3000;
    private Timer mAnswerDisplayingTimer = null;
    private Handler mHandler = new Handler();

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
        this.lineName = lineName+"("+linekana+")";
        this.companyId = line.getCompanyId();

        actionBar.setTitle("Puz-Rail：Location Set");
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

        transparent = (ImageView)findViewById(R.id.transparent);

        this.mMapView.addFocusables(views,View.FOCUS_FORWARD);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // マップ形式の設定
        this.mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // 表示エリアと縮尺の制限
        UiSettings mUiSetting = this.mMap.getUiSettings();
        mUiSetting.setRotateGesturesEnabled(false);
//        this.mMap.setMaxZoomPreference(this.line.getMaxZoomLevel());
//        this.mMap.setMinZoomPreference(this.line.getMinZoomLevel());
        // 離島除く
        LatLng north_east = new LatLng(this.line.getScrollMaxLat(),this.line.getScrollMaxLng());
        LatLng south_west = new LatLng(this.line.getScrollMinLat(),this.line.getScrollMinLng());
        LatLngBounds JAPAN = new LatLngBounds(south_west,north_east);
        this.mMap.setLatLngBoundsForCameraTarget(JAPAN);

        // EventListener
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);
        this.mMap.setOnCameraIdleListener(this);
        // Add a marker in Sydney and move the camera
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(this.line.getInitCamposLat(),this.line.getInitCamposLng()),
                this.line.getInitZoomLevel())
        );

        mImageView.setMap(this.mMap);
        mImageView.setImageDrawable();
        // Test Code for 回答クリア、回答を見る の操作
        // 正解座標のDB登録の完了と正誤判定ロジックの実装完了後、削除する
//        this.line.setLocationAnswerStatus();
//        this.db.updateLineLocationAnswerStatus(this.line);
        //
        if(hasAlreadyLocated()) setGeoJsonVisible();

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

    private boolean hasAlreadyLocated(){
        return this.line.isLocationCompleted();
    }

    private void setGeoJsonVisible(){
        retrieveFileFromResource();
        layer.addLayerToMap();
    }

    private void resetGeoJsonVisible(){
        if(layer!=null){
            layer.removeLayerFromMap();
            layer = null;
        }
    }

    // 回答表示の消去
    private class displayTimerElapse extends TimerTask {
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            mHandler.post(new Runnable(){
                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    resetGeoJsonVisible();
                    mAnswerDisplayingTimer = null;
                }
            });
        }
    }

    // 回答の表示と消去タイマ起動
    private void answerDisplay(){
        if (mAnswerDisplayingTimer == null) {
            setGeoJsonVisible();
            mAnswerDisplayingTimer = new Timer(true);
            mAnswerDisplayingTimer.schedule(new displayTimerElapse(),DISPLAY_ANSWERE_TIME);
        }
    }

    // 回答クリア
    private void answerClear(){
        new AlertDialog.Builder(this)
                .setTitle(this.line.getName()+" : 回答クリア")
                .setMessage("敷設回答をクリアします。"+"\n"+"　　よろしいですか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,String.format("%s:敷設回答クリア",LocationPuzzleActivity.this.line.getName()));
                        LocationPuzzleActivity.this.line.resetLocationAnswerStatus();
                        LocationPuzzleActivity.this.db.updateLineLocationAnswerStatus(LocationPuzzleActivity.this.line);
                        LocationPuzzleActivity.this.resetGeoJsonVisible();
                        LocationPuzzleActivity.this.mImageView.resetImageDrawable();
                        LocationPuzzleActivity.this.mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(LocationPuzzleActivity.this.line.getInitCamposLat(),
                                        LocationPuzzleActivity.this.line.getInitCamposLng()),
                                        LocationPuzzleActivity.this.line.getInitZoomLevel())
                                );
                        LocationPuzzleActivity.this.mImageView.setImageDrawable();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(TAG,"onMapLongClick");

        final ArrayList<String> contextMenuList = new ArrayList<String>();
        contextMenuList.add("回答クリア");
        contextMenuList.add("回答を見る");
        contextMenuList.add("最初の位置に戻す");
        contextMenuList.add("Webを検索する");

        ArrayAdapter<String> contextMenuAdapter
                = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contextMenuList);

        // 未正解アイテムのリストビュー生成
        ListView contextMenuListView = new ListView(this);
        contextMenuListView.setAdapter(contextMenuAdapter);
        contextMenuListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        LocationPuzzleActivity.this.mDialog.dismiss();
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>)adapterView.getAdapter();
                        switch(position){
                            case 0: // 回答をクリア（回答済みの場合）
                                if(LocationPuzzleActivity.this.hasAlreadyLocated()){
                                    answerClear();
                                }
                                break;
                            case 1: // 回答を見る（未回答の場合）
                                if(!LocationPuzzleActivity.this.hasAlreadyLocated()){
                                    final Snackbar sb = Snackbar.make(LocationPuzzleActivity.this.transparent,
                                            LocationPuzzleActivity.this.line.getRawName()+"("+LocationPuzzleActivity.this.line.getRawKana()+")",
                                            Snackbar.LENGTH_LONG);
                                    sb.setActionTextColor(ContextCompat.getColor(LocationPuzzleActivity.this, R.color.background1));
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(LocationPuzzleActivity.this, R.color.color_10));
                                    sb.show();
                                    answerDisplay();
                                }
                                break;
                            case 2: // 最初の位置に戻す
                                LocationPuzzleActivity.this.mMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(LocationPuzzleActivity.this.line.getInitCamposLat(),
                                                        LocationPuzzleActivity.this.line.getInitCamposLng()),
                                                LocationPuzzleActivity.this.line.getInitZoomLevel())
                                );
                                LocationPuzzleActivity.this.mImageView.resetImageDrawable();
                                LocationPuzzleActivity.this.mImageView.setImageDrawable();;
                                break;
                            case 3: // Webを検索する
                                if(LocationPuzzleActivity.this.line.isNameCompleted()){
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, LocationPuzzleActivity.this.line.getName()); // query contains search string
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(LocationPuzzleActivity.this,"路線名が未回答です。\n路線名を先に回答してください", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        this.mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", this.line.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        this.mDialog.show();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"onMapClick");
        // ToDo
        // タイムトライアルのタイマー開始／停止操作実装
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

    private boolean checkLocation(){
        double error[] = mImageView.computeLocationError();
        double err = error[0]+error[1]+error[2]+error[3];
        Log.d(TAG,String.format("error = %f, %f, %f, %f, sum = %f",error[0],error[1],error[2],error[3],err));
        double errRange[] = this.line.getErrRange();
        if(error[0] < errRange[LineMapOverlayView.ERR_RANGE_LEVEL0]
                && error[1] < errRange[LineMapOverlayView.ERR_RANGE_LEVEL0]
                && error[2] < errRange[LineMapOverlayView.ERR_RANGE_LEVEL0]
                && error[3] < errRange[LineMapOverlayView.ERR_RANGE_LEVEL0] ) {
            // 正解
            mImageView.resetImageDrawable();
            setGeoJsonVisible();
            this.line.setLocationAnswerStatus();
            db.updateLineLocationAnswerStatus(this.line);
            Toast.makeText(LocationPuzzleActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
        }
        else{
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

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_AboutPuzzRail) {
            PopUp.makePopup(this,this.mImageView,"file:///android_asset/puzzrail_help.html");
            return true;
        }
        else if (id == R.id.action_Help) {
            Toast.makeText(LocationPuzzleActivity.this, "使い方", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_Ask) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "ib65629@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "パズレールについてのお問い合わせ");
            startActivity(Intent.createChooser(intent, ""));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
