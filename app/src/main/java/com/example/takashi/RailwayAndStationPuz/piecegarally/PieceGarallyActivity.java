package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.MainActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.CurrentMode;
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
    // 要素をArrayListで設定
    private GridView gridView;
    private MultiButtonListView listView;
    private BaseAdapter baseAdapter;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private CurrentMode currentmode;
    private GaugeView lineNameProgress,lineMapProgress,stationsProgress;
    private int selectedLineIndex = -1;

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

        this.currentmode = db.getCurrentMode();
        this.lines = db.getLineList(this.currentmode.getCompanyId());
        Log.d(TAG,String.format("lines.size() = %d",this.lines.size()));

        // GridViewのインスタンスを生成
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_main);
        Log.d(TAG,String.format("GridDisplay is %b",this.currentmode.isGridDisplaied()));

        this.lineNameProgress = (GaugeView) findViewById(R.id.lineNameProgress) ;
        updateLineNameProgress();

        this.lineMapProgress =(GaugeView) findViewById(R.id.lineMapProgress);
        this.lineMapProgress.setData(20,"%",  ContextCompat.getColor(this, R.color.color_60), 90, true);

        this.stationsProgress = (GaugeView) findViewById(R.id.stationsProgress);
        this.stationsProgress.setData(20,"%",  ContextCompat.getColor(this, R.color.color_90), 90, true);

        if(this.currentmode.isGridDisplaied()){
            createPieceGridViewAndAdapter(layout);
        }
        else{
            createPieceListViewAndAdapter(layout);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.station_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Puz-Rail：Select Railway");
        actionBar.setSubtitle(db.getCompany(this.currentmode.getCompanyId()).getName());
    }

    private void updateLineNameProgress(){
        int cnt = db.countLineNameAnswerdLines(this.currentmode.getCompanyId());
        int lineNameProgress = 100*cnt/this.lines.size();
        Log.d(TAG,String.format("%d,%d/%d",lineNameProgress,cnt,lines.size()));
        this.lineNameProgress.setData(lineNameProgress,"%",  ContextCompat.getColor(this, R.color.color_30), 90, true);
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
//                Log.d(TAG,String.format("from.size() = %d, to.size() = %d",sortedRemainLines.size(),randomizedRemainLines.size()));
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
                                PieceGarallyActivity.this.lines = PieceGarallyActivity.this.db.getLineList(PieceGarallyActivity.this.currentmode.getCompanyId());
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
        Log.d(TAG, String.format("onItemClick 路線：%s", s));
        switch(view.getId()){
//            case R.id.location :
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
        Line line = lines.get(position);
        String s = line.getName();
        Log.d(TAG, String.format("onItemLongClick 路線：%s", s));
        ContextMenuAdapter contextMenuAdapter = new ContextMenuAdapter(this, line, position);

        // 未正解アイテムのリストビュー生成
        ListView contextMenuListView = new ListView(this);
        contextMenuListView.setAdapter(contextMenuAdapter);
        contextMenuListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    mDialog.dismiss();
                    Line line = lines.get((int) id);
                    Log.d(TAG, String.format("lineId = %d, lineName = %s", line.getLineId(), line.getName()));
                    if (position == 0) {
                        Intent intent = new Intent(PieceGarallyActivity.this,LocationPuzzleActivity.class);
                        intent.putExtra("SelectedLineId",line.getLineId());
                        startActivity(intent);
                        // アニメーションの設定
                        overridePendingTransition(R.anim.in_right, R.anim.out_left);
                        db.close();
                        finish();
                    } else if (position == 1) {
//                        if (line.isLocationCompleted()) {
                            Intent intent = new Intent(PieceGarallyActivity.this, StationPuzzleActivity.class);
                            intent.putExtra("SelectedLineId",line.getLineId());
                            startActivity(intent);
                            // アニメーションの設定
                            overridePendingTransition(R.anim.in_right, R.anim.out_left);
                            db.close();
                            finish();
//                        }
                    }
                }
            }
        );

        // AlertDialogのタイトル欄の表示設定
        // 参考：http://www.android--tutorials.com/2016/10/android-alertdialog-title-custom-view.html
        // Initialize a new layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Initialize a new linear layout as alert dialog title custom view
        // This is the container of alert dialog title contents
        LinearLayout LLayout = new LinearLayout(this);

        // Set linear layout orientation
        LLayout.setOrientation(LinearLayout.VERTICAL);

        // Set title view margin
        params.setMargins(15,15,15,15);
        LLayout.setLayoutParams(params);

        // Set the padding of title view
        LLayout.setPadding(15,15,15,15);

        // Initialize a new TextView instance
        // This will display alert dialog title text
        TextView tv_title = new TextView(this);
        tv_title.setLayoutParams(params);

        // Set title text color
        tv_title.setTextColor(Color.BLUE);

        // Set title text size
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);

        // Set title text gravity/text alignment to center
        tv_title.setGravity(Gravity.CENTER_HORIZONTAL);

        // Set title spannable text
        tv_title.setText(String.format("路線：%s", s));
        tv_title.setSingleLine();
        tv_title.setEllipsize(TextUtils.TruncateAt.END);

        // Add the two views to linear layout
        LLayout.addView(tv_title);
        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
//                .setTitle(String.format("路線：%s", s))
                .setCustomTitle(LLayout)
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

    private void createPieceGridViewAndAdapter(RelativeLayout layout){
        getLayoutInflater().inflate(R.layout.activity_line_select_grid, layout);
        this.gridView = (GridView) findViewById(R.id.railway_grid_view);
        RailwayGridAdapter gridadapter = new RailwayGridAdapter(this.getApplicationContext(), this.lines);
        this.baseAdapter = gridadapter;
        this.gridView.setAdapter(gridadapter);
        this.gridView.setOnItemClickListener(this);
        this.gridView.setOnItemLongClickListener(this);
    }

    private void createPieceListViewAndAdapter(RelativeLayout layout){
        getLayoutInflater().inflate(R.layout.activity_line_select_list, layout);
        this.listView = (MultiButtonListView) findViewById(R.id.railway_list_view);
        RailwayListAdapter listadapter = new RailwayListAdapter(this.getApplicationContext(), this.lines);
        this.baseAdapter = listadapter;
        this.listView.setAdapter(listadapter);
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_main);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_garally) {
            Toast.makeText(PieceGarallyActivity.this, "ギャラリー表示が選択されました", Toast.LENGTH_SHORT).show();
            // ListViewを削除
            if (this.listView != null) {
                unregisterForContextMenu(this.listView);
                layout.removeAllViews();
                this.listView = null;
                this.db.updateDisplayMode(true);
            }
            // GridViewを再構築
            if (this.gridView == null) {
                createPieceGridViewAndAdapter(layout);
            }
            return true;
        } else if (id == R.id.action_list) {
            Toast.makeText(PieceGarallyActivity.this, "リスト表示が選択されました", Toast.LENGTH_SHORT).show();
            // GridViewを削除
            if (this.gridView != null) {
                unregisterForContextMenu(this.gridView);
                layout.removeAllViews();
                this.gridView = null;
                this.db.updateDisplayMode(false);
            }
            // ListViewを再構築
            if (this.listView == null) {
                createPieceListViewAndAdapter(layout);
            }
            return true;
        } else {
        }

        return super.onOptionsItemSelected(item);
    }
}
