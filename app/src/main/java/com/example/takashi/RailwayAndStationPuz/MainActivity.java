package com.example.takashi.RailwayAndStationPuz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.database.CurrentMode;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.database.Line;
import com.example.takashi.RailwayAndStationPuz.location.LocationPuzzleActivity;
import com.example.takashi.RailwayAndStationPuz.piecegarally.CompanyListActivity;
import com.example.takashi.RailwayAndStationPuz.piecegarally.ContextMenuAdapter;
import com.example.takashi.RailwayAndStationPuz.piecegarally.RailwayGridAdapter;
import com.example.takashi.RailwayAndStationPuz.piecegarally.RailwayListAdapter;
import com.example.takashi.RailwayAndStationPuz.station.StationPuzzleActivity;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static String TAG = "MainActivity";
    private static final int RESULTCODE = 1;
    // 要素をArrayListで設定
    private GridView gridView;
    private ListView listView;
    private DBAdapter db;
    private ArrayList<Line> lines = new ArrayList<Line>();
    private CurrentMode currentmode;

    private AlertDialog mDialog;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBAdapter(this);
        db.open();

        this.currentmode = db.getCurrentMode();
        lines = db.getLineList(this.currentmode.getCompanyId());
        Log.d(TAG,String.format("lines.size() = %d",lines.size()));

        // GridViewのインスタンスを生成
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_main);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Drawable rszIcon = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_companyselector3, null);
        fab.setImageDrawable(rszIcon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), CompanyExpandableListActivity.class);
                Intent intent = new Intent(getApplicationContext(), CompanyListActivity.class);
                startActivityForResult(intent, RESULTCODE);
                // アニメーションの設定
                overridePendingTransition(R.anim.in_down, R.anim.out_up);
                db.close();
                finish();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                        Intent intent = new Intent(MainActivity.this,LocationPuzzleActivity.class);
                        intent.putExtra("SelectedLineId",line.getLineId());
                        startActivity(intent);
                        // アニメーションの設定
                        overridePendingTransition(R.anim.in_right, R.anim.out_left);
                        db.close();
                        finish();
                    } else if (position == 1) {
//                        if (line.isLocationCompleted()) {
                            Intent intent = new Intent(MainActivity.this, StationPuzzleActivity.class);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // アイコンタップでTextViewにその名前を表示する
        Log.d(TAG, String.format("onItemLongClick i = %d", i));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void createPieceGridViewAndAdapter(RelativeLayout layout){
        getLayoutInflater().inflate(R.layout.activity_grid_main, layout);
        this.gridView = (GridView) findViewById(R.id.railway_grid_view);
        RailwayGridAdapter gridadapter = new RailwayGridAdapter(this.getApplicationContext(), this.lines);
        this.gridView.setAdapter(gridadapter);
        this.gridView.setOnItemClickListener(this);
        this.gridView.setOnItemLongClickListener(this);
    }

    private void createPieceListViewAndAdapter(RelativeLayout layout){
        getLayoutInflater().inflate(R.layout.activity_list_main, layout);
        this.listView = (ListView) findViewById(R.id.railway_list_view);
        RailwayListAdapter listadapter = new RailwayListAdapter(this.getApplicationContext(), this.lines);
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
            Toast.makeText(MainActivity.this, "ギャラリー表示が選択されました", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "リスト表示が選択されました", Toast.LENGTH_SHORT).show();
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
