package com.example.takashi.RailwayAndStationPuz.station;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.location.LocationPuzzleActivity;
import com.example.takashi.RailwayAndStationPuz.piecegarally.PieceGarallyActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.example.takashi.RailwayAndStationPuz.database.Station;
import com.example.takashi.RailwayAndStationPuz.ui.PopUp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class StationPuzzleActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        OnMapReadyCallback {

    private String TAG = "StationPuzzleActivity";
    private String lineNameNone = "------------";
    private String lineName;
    private String stationNameNone = "------------";
    private DBAdapter db;
    private Line line;
    private int companyId;
    private int selectedLineId;
    private ArrayList<Station> stations = new ArrayList<Station>();

    private GoogleMap mMap;
    private TextView progressTitle;
    private ProgressBar progress;
    private MapView mMapView;
    private StationListAdapter stationsAdapter;
    private ListView stationListView;
    private AlertDialog mDialog;
    private int selectedStationIndex = -1;

    private ImageView separatorMove;
    private FrameLayout mapFrame;
    private LinearLayout transparentView;

    private int showAnswerCount = 0;
    private static final int showAnswerMax = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_station_puzzle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.station_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        Intent intent = getIntent();
        this.selectedLineId = intent.getIntExtra("SelectedLineId", 42); // デフォルトを紀勢線のlineIdにしておく

        this.db = new DBAdapter(this);
        this.db.open();
        this.line = db.getLine(this.selectedLineId);
        this.stations = db.getStationList(this.selectedLineId);

        String companyName = db.getCompany(line.getCompanyId()).getName();
        String lineName = line.getName();
        String linekana = line.getLineKana();
        this.lineName = lineName+"("+linekana+")";
        this.companyId = line.getCompanyId();

        actionBar.setTitle("Puz-Rail：Station Set");
        actionBar.setSubtitle(companyName+"／"+this.lineName);

        this.progressTitle = (TextView)findViewById(R.id.ProgressTitle);
        this.progress = (ProgressBar)findViewById(R.id.ProgressBar);
        updateProgressBar();

        this.mapFrame = (FrameLayout)findViewById(R.id.framelayout);
        this.transparentView = (LinearLayout)findViewById(R.id.linearlayout);

        this.mMapView = (MapView)findViewById(R.id.mapView);
        this.mMapView.onCreate(savedInstanceState);
        this.mMapView.getMapAsync(this);

        this.separatorMove = (ImageView)findViewById(R.id.separatorMove);
        this.separatorMove.setLongClickable(true);
        this.separatorMove.setOnTouchListener(new OnTouchListener(this));

    }

    private void updateProgressBar(){
        int finishedCnt = 0;
        Iterator<Station> ite = stations.listIterator();
        while(ite.hasNext()){
            Station station = ite.next();
            if(station.isFinished()) finishedCnt++;

        }
        this.progressTitle.setText(String.format("%s 駅名解答率 : %d/%d",this.line.getName(), finishedCnt, stations.size()));
        this.progress.setMax(stations.size());
        this.progress.setProgress(finishedCnt);
    }

    public DBAdapter getDb(){
        return this.db;
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
    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long l) {
        Log.d(TAG,String.format("onItemClick() position = %d,駅名=%s",position,stationsAdapter.getStationInfo(position).getRawName()));
        // 未正解のリストアイテムがクリックされたら駅名選択リストダイアログを表示する。
        Station station = stations.get(position);
        if(!station.isFinished()){

            // 正解の配列インデックスを保持
            this.selectedStationIndex = position;
            final ArrayList<String> remaining = new ArrayList<String>();
            final ArrayList<Station> remainStations = new ArrayList<Station>();

            // 未正解アイテムリストの生成
            // 未正解のStationInfoを抽出(stations→remainStations)
            Iterator<Station> staIte = this.stations.iterator();
            while(staIte.hasNext()){
                Station sta = staIte.next();
                if(!sta.isFinished()){
                    remainStations.add(sta);
                }
            }
            // 0～未正解件数までの整数をランダムに生成
            // それをindexとして
            // remainingの件数が未正解の件数に到達するまで
            // reimainsから駅名を取得しremainingに追加していく
            Random rnd = new Random();
            while(remaining.size()<remainStations.size()){
                // 0～未正解件数までの整数をランダムに生成
                int idx = rnd.nextInt(remainStations.size());
                Station sta = remainStations.get(idx);
                // remaining走査用のイテレータを生成
                Iterator<String> strIte = remaining.iterator();
                boolean already = false;						// 登録済フラグ
                String name = "";								// 登録する駅名
                // remainingを走査し、既に登録済みか否かを判定
                while(strIte.hasNext()){
                    name = strIte.next();
                    if(name.equals(sta.getRawName()+"("+sta.getRawKana()+")")) already = true;
                }
                if(!already){
                    remaining.add(sta.getRawName()+"("+sta.getRawKana()+")");
                }
            }
            Log.d(TAG,String.format("remaining.size() = %d, remainCnt = %d\r\n",remaining.size(),remainStations.size()));

            ArrayAdapter<String> remainStationsAdapter
                    = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,remaining);

            // 未正解アイテムのリストビュー生成
            ListView remainingStationsListView = new ListView(this);
            remainingStationsListView.setAdapter(remainStationsAdapter);
            remainingStationsListView.setOnItemClickListener(
                    // ダイアログ上の未正解アイテムがクリックされたら答え合わせする
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            mDialog.dismiss();
                            int correctAnswerIndex = StationPuzzleActivity.this.selectedStationIndex;
                            StationListAdapter adapter = StationPuzzleActivity.this.stationsAdapter;

                            Station correctStationInfo = adapter.getStationInfo(correctAnswerIndex);
                            String correctName = correctStationInfo.getRawName() + "(" + correctStationInfo.getRawKana() + ")";
                            String answerName  = remaining.get(position);

                            Log.d(TAG,String.format("answerName = %s, correctName = %s\r\n",answerName,correctName));

                            if(answerName.equals(correctName)){ // 駅名が一致する？
                                Toast.makeText(StationPuzzleActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
                                correctStationInfo.setFinishStatus();
                                StationPuzzleActivity.this.db.updateStationAnswerStatus(correctStationInfo);
                                StationPuzzleActivity.this.stationsAdapter.notifyDataSetChanged();
                                // 進捗バーの更新
                                StationPuzzleActivity.this.updateProgressBar();
                            }
                            else{
                                Toast.makeText(StationPuzzleActivity.this,"残念･･･ Σ(￣ロ￣lll)", Toast.LENGTH_SHORT).show();
                            }
                            StationPuzzleActivity.this.selectedStationIndex = -1;
                        }
                    }
            );

            // ダイアログ表示
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("駅名選択リスト")
                    .setPositiveButton("Cancel",null)
                    .setView(remainingStationsListView)
                    .create();
            mDialog.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        UiSettings mUiSetting = this.mMap.getUiSettings();
        mUiSetting.setRotateGesturesEnabled(false);
        this.mMap.setMaxZoomPreference(this.line.getMaxZoomLevel());
        this.mMap.setMinZoomPreference(this.line.getMinZoomLevel());
        // 離島除く
        LatLng north_east = new LatLng(this.line.getScrollMaxLat(),this.line.getScrollMaxLng());
        LatLng south_west = new LatLng(this.line.getScrollMinLat(),this.line.getScrollMinLng());
        LatLngBounds JAPAN = new LatLngBounds(south_west,north_east);
        this.mMap.setLatLngBoundsForCameraTarget(JAPAN);

        //  初期表示位置
        double lineCenterLng = ( this.line.getCorrectLeftLng() + this.line.getCorrectRightLng() )/2.0;
        double lineCenterLat = ( this.line.getCorrectBottomLat() + this.line.getCorrectTopLat() )/2.0;
        Log.d(TAG,String.format("##### line center   : lng = %f, lat = %f",lineCenterLng,lineCenterLat));
        double zl = (this.line.getMaxZoomLevel() + this.line.getMinZoomLevel())/2.0;

        // 路線中心座標で位置設定
        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(lineCenterLat,lineCenterLng),(float)zl));

        try{
            // 路線図のGeoJsonファイル読込
            GeoJsonLayer layer = new GeoJsonLayer(this.mMap, this.line.getRawResourceId(), this);
            // 路線図の色を変更
            GeoJsonLineStringStyle style = layer.getDefaultLineStringStyle();
            style.setWidth(5.0f);
            style.setColor(Color.BLUE);

            layer.addLayerToMap();

        } catch (IOException e) {
            Log.e(TAG, "GeoJSON file could not be read");
        } catch (JSONException e) {
            Log.e(TAG, "GeoJSON file could not be converted to a JSONObject");
        }

        // mapオブジェクトが生成された後にMarkerのOverlay初期表示を行うため、
        // stationListAdapterをOnMapReadyの最後に生成する。
        this.stationListView = (ListView)findViewById(R.id.StationNameList);
        this.stationsAdapter = new StationListAdapter(this,this.stations,this.mMap);
        this.stationListView.setAdapter(this.stationsAdapter);
        this.stationListView.setOnItemClickListener(this);
        this.stationListView.setOnItemLongClickListener(this);
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

    // scroll1操作、single tap、double tap、long tap操作のイベントハンドラ
    private class SeparatorViewGestureListener
            implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{

        public SeparatorViewGestureListener(){

        }
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            ViewGroup.LayoutParams mapFrameParam = StationPuzzleActivity.this.mapFrame.getLayoutParams();
            int baseHeight = StationPuzzleActivity.this.transparentView.getHeight()
                    - StationPuzzleActivity.this.progressTitle.getHeight()
                    - StationPuzzleActivity.this.progress.getHeight();
            int maxHeight = baseHeight*4/5;
            int minHeight = baseHeight/5;
            int currentMapHeight = mapFrameParam.height;
            int changeMapHeight = currentMapHeight+(int)e2.getY();

            Drawable drawable;
            if( changeMapHeight < minHeight ) {
                changeMapHeight = minHeight;
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_expandmapbutton,null);
            }
            else if( maxHeight < changeMapHeight ) {
                changeMapHeight = maxHeight;
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_reducemapbutton,null);
            }
            else{
                drawable = ResourcesCompat.getDrawable(StationPuzzleActivity.this.getResources(),R.drawable.ic_changemapsizebutton,null);
            }
            mapFrameParam.height = changeMapHeight;
            StationPuzzleActivity.this.mapFrame.setLayoutParams(mapFrameParam);
            StationPuzzleActivity.this.separatorMove.setImageDrawable(drawable);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"onFling");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            Log.d(TAG,"onDoubleTap");
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            Log.d(TAG,"onDoubleTapEvent");
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            Log.d(TAG,"onSingleTapConfirmed");
            return false;
        }
    }

    private class OnTouchListener implements View.OnTouchListener{
        private GestureDetector gestureDetector;
        public OnTouchListener(Context context){
            this.gestureDetector = new GestureDetector(context, new SeparatorViewGestureListener());
        }
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            this.gestureDetector.onTouchEvent(motionEvent);
            return false;
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_AboutPuzzRail) {
            PopUp.makePopup(this,this.transparentView,"file:///android_asset/puzzrail_help.html");
            return true;
        }
        else if (id == R.id.action_Help) {
            Toast.makeText(StationPuzzleActivity.this, "使い方", Toast.LENGTH_SHORT).show();
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

    // 回答クリア
    private Station longClickSelectedStation = null;
    private void answerClear(){
        new AlertDialog.Builder(this)
                .setTitle(longClickSelectedStation.getName()+" : 回答クリア")
                .setMessage("駅名をクリアします。"+"\n"+"　　よろしいですか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,String.format("%s:駅名クリア",StationPuzzleActivity.this.longClickSelectedStation.getRawName()));
                        StationPuzzleActivity.this.longClickSelectedStation.resetFinishStatus();
                        StationPuzzleActivity.this.db.updateStationAnswerStatus(StationPuzzleActivity.this.longClickSelectedStation);
                        StationPuzzleActivity.this.stationsAdapter.notifyDataSetChanged();
                        // 進捗バーの更新
                        StationPuzzleActivity.this.updateProgressBar();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        longClickSelectedStation = this.stations.get(position);

        final ArrayList<String> contextMenuList = new ArrayList<String>();
        contextMenuList.add("回答クリア");
        contextMenuList.add("回答を見る");
        contextMenuList.add("Webを検索する");

        ArrayAdapter<String> contextMenuAdapter
                = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contextMenuList);

        // 未正解アイテムのリストビュー生成
        ListView contextMenuListView = new ListView(this);
        contextMenuListView.setAdapter(contextMenuAdapter);
        contextMenuListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        mDialog.dismiss();
                        switch(position) {
                            case 0: // 回答をクリア
                                if(StationPuzzleActivity.this.longClickSelectedStation.getStationOrder()!=1 &&
                                        StationPuzzleActivity.this.longClickSelectedStation.isFinished())
                                answerClear();
                                break;
                            case 1: // 回答を見る
                                if( showAnswerCount < showAnswerMax ){
                                    final Snackbar sb = Snackbar.make(StationPuzzleActivity.this.stationListView,
                                            longClickSelectedStation.getRawName()+"("+longClickSelectedStation.getRawKana()+")",
                                            Snackbar.LENGTH_LONG);
                                    sb.setActionTextColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.background1));
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.color_10));
                                    sb.show();
                                    showAnswerCount++;
                                }
                                else{
                                    final Snackbar sb = Snackbar.make(StationPuzzleActivity.this.stationListView,
                                            "回数制限一杯!!　広告クリックを促す",
                                            Snackbar.LENGTH_LONG);
                                    sb.getView().setBackgroundColor(ContextCompat.getColor(StationPuzzleActivity.this, R.color.color_10));
                                    TextView textView = (TextView) sb.getView().findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(StationPuzzleActivity.this.getApplicationContext(), R.color.coloe_RED));
                                    sb.show();
                                    showAnswerCount =0;
                                }
                                break;
                            case 2: // Webを検索する
                                if(longClickSelectedStation.isFinished()){
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, longClickSelectedStation.getName()+"駅"); // query contains search string
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(StationPuzzleActivity.this,"駅が開設されていません。\n駅名を回答し駅を開設してください", Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", this.longClickSelectedStation.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;

    }

}
