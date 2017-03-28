package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.MainActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.example.takashi.RailwayAndStationPuz.location.LocationPuzzleActivity;
import com.example.takashi.RailwayAndStationPuz.station.StationPuzzleActivity;
import com.example.takashi.RailwayAndStationPuz.ui.GaugeView;
import com.example.takashi.RailwayAndStationPuz.ui.MultiButtonListView;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class PieceGarallyActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static String TAG = "PieceGarallyActivity";
    private static final int RESULTCODE = 1;
    private MultiButtonListView listView;
    private BaseAdapter baseAdapter;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private TextView lineNameProgValue,lineMapProgValue,stationProgValue;
    private GaugeView lineNameProgress, lineMapProgress,stationsProgress;
    private int selectedLineIndex = -1;
    private int companyId;

    private AlertDialog mDialog;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_garally);

        db = new DBAdapter(this);
        db.open();

        Intent intent = getIntent();
        this.companyId = intent.getIntExtra("SelectedCompanyId", 3); // デフォルトを西日本旅客鉄道のIdにしておく

        this.lines = db.getLineList(this.companyId);

        this.lineNameProgValue = (TextView) findViewById(R.id.lineNameProgValue);
        this.lineNameProgress = (GaugeView) findViewById(R.id.lineNameProgress) ;
        updateLineNameProgress();

        this.lineMapProgValue = (TextView) findViewById(R.id.lineMapProgValue);
        this.lineMapProgress =(GaugeView) findViewById(R.id.lineMapProgress);
        int answeredLines = db.countLocationAnsweredLines(this.companyId);
        int locationProgress = 100*answeredLines/lines.size();
        this.lineMapProgress.setData(locationProgress,"%",  ContextCompat.getColor(this, R.color.color_60), 90, true);
        this.lineMapProgValue.setText(String.format("%d/%d",answeredLines,lines.size()));

        this.stationProgValue = (TextView) findViewById(R.id.stationProgValue);
        this.stationsProgress = (GaugeView) findViewById(R.id.stationsProgress);
        int answeredStations = db.countAnsweredStationsInCompany(this.companyId);
        int totalStations = db.countTotalStationsInCompany(this.companyId);
        int stationAnsweredProgress = 100*answeredStations/totalStations;
        this.stationsProgress.setData(stationAnsweredProgress,"%",  ContextCompat.getColor(this, R.color.color_30), 90, true);
        this.stationProgValue.setText(String.format("%d/%d",answeredStations,totalStations));

        // GridViewのインスタンスを生成
        this.listView = (MultiButtonListView) findViewById(R.id.railway_list_view);
        RailwayListAdapter listadapter = new RailwayListAdapter(this.getApplicationContext(), this.lines, db);
        this.baseAdapter = listadapter;
        this.listView.setAdapter(listadapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.station_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Puz-Rail：Select Railway");
        actionBar.setSubtitle(db.getCompany(this.companyId).getName());
    }

    private void updateLineNameProgress(){
        int cnt = db.countLineNameAnsweredLines(this.companyId);
        int lineNameProgress = 100*cnt/this.lines.size();
        this.lineNameProgress.setData(lineNameProgress,"%",  ContextCompat.getColor(this, R.color.color_90), 90, true);
        this.lineNameProgValue.setText(String.format("%d/%d",cnt,this.lines.size()));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, RESULTCODE);
        // アニメーションの設定
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
        db.close();
        finish();
    }

    private void selectLineName(int position){
        // アイコンタップでTextViewにその名前を表示する
        Log.d(TAG, String.format("onItemLongClick position = %d", position));
        Line line = this.lines.get(position);
        if(!line.isNameCompleted()){
            this.selectedLineIndex = position;
            final ArrayList<Line> sortedRemainLines = new ArrayList<Line>();
            final ArrayList<String> randomizedRemainLines = new ArrayList<String>();

            //路線名　未正解の路線を抽出（lines→sortedRemainLines)
            Iterator<Line> lineIte = this.lines.iterator();
            while(lineIte.hasNext()){
                Line ln = lineIte.next();
                if(!ln.isNameCompleted()){
                    sortedRemainLines.add(ln);
                }
            }

            Random rnd = new Random();
            while(randomizedRemainLines.size()<sortedRemainLines.size()){
                // 0～未正解件数までの整数をランダムに生成
                int idx = rnd.nextInt(sortedRemainLines.size());
                Line fromLine = sortedRemainLines.get(idx);
                Iterator<String> li = randomizedRemainLines.iterator();
                boolean already = false;
                while(li.hasNext()){
                    String toLineName = li.next();
                    if(toLineName.equals(fromLine.getName()+"("+fromLine.getLineKana()+")")){
                        already = true;
                        break;
                    }
                }
                if(!already){
                    randomizedRemainLines.add(fromLine.getName()+"("+fromLine.getLineKana()+")");
                }
            }

            ArrayAdapter<String> remainLinesAdapter
                    = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,randomizedRemainLines);

            // 未正解アイテムのリストビュー生成
            ListView remainLinesListView = new ListView(this);
            remainLinesListView.setAdapter(remainLinesAdapter);
            remainLinesListView.setOnItemClickListener(
                    // ダイアログ上の未正解アイテムがクリックされたら答え合わせする
                    new AdapterView.OnItemClickListener(){
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            mDialog.dismiss();
                            int correctAnswerIdx = PieceGarallyActivity.this.selectedLineIndex;
                            Line correctLine = (Line)(PieceGarallyActivity.this.baseAdapter.getItem(correctAnswerIdx));
                            String correctLineName = correctLine.getName()+"("+correctLine.getLineKana()+")";
                            String selectedLineName = randomizedRemainLines.get(position);
                            Log.d(TAG,String.format("correct %s, selected %s",correctLineName,selectedLineName));
                            //正解判定
                            if(correctLineName.equals(selectedLineName)){
                                Toast.makeText(PieceGarallyActivity.this,"正解!!! v(￣Д￣)v ", Toast.LENGTH_SHORT).show();
                                correctLine.setNameAnswerStatus();
                                PieceGarallyActivity.this.db.updateLineNameAnswerStatus(correctLine);
                                PieceGarallyActivity.this.lines = PieceGarallyActivity.this.db.getLineList(PieceGarallyActivity.this.companyId);
                                PieceGarallyActivity.this.baseAdapter.notifyDataSetChanged();
                                PieceGarallyActivity.this.updateLineNameProgress();
                            }
                            else{
                                Toast.makeText(PieceGarallyActivity.this,"残念･･･ Σ(￣ロ￣lll)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

            // ダイアログ表示
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("路線名選択リスト")
                    .setPositiveButton("Cancel",null)
                    .setView(remainLinesListView)
                    .create();
            mDialog.show();

        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Line line = this.lines.get(position);
        String s = line.getName();
        switch(view.getId()){
            case R.id.mapImageButton: {
                    Intent intent = new Intent(PieceGarallyActivity.this, LocationPuzzleActivity.class);
                    intent.putExtra("SelectedLineId", line.getLineId());
                    startActivity(intent);
                    // アニメーションの設定
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                    db.close();
                    finish();
                }
                break;
            case R.id.stationImageButton : {
                    Intent intent = new Intent(PieceGarallyActivity.this, StationPuzzleActivity.class);
                    intent.putExtra("SelectedLineId", line.getLineId());
                    startActivity(intent);
                    // アニメーションの設定
                    overridePendingTransition(R.anim.in_right, R.anim.out_left);
                    db.close();
                    finish();
                }
                break;
            default:
                selectLineName(position);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String s;
        Line line = lines.get(position);
        if(line.isNameCompleted()){
            s = line.getName()+"("+line.getLineKana()+")";
        }
        else{
            s = "------------";
        }
        Log.d(TAG, String.format("onItemLongClick 路線：%s", s));

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
                        Toast.makeText(PieceGarallyActivity.this,String.format("position %d",position), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", s))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_AboutPuzzRail) {
            Toast.makeText(PieceGarallyActivity.this, "パズレールについて", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_Help) {
            Toast.makeText(PieceGarallyActivity.this, "使い方", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_Ask) {
            Toast.makeText(PieceGarallyActivity.this, "お問い合わせ", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
