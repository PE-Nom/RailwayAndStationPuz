package com.example.takashi.RailwayAndStationPuz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.database.Company;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.piecegarally.PieceGarallyActivity;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by takashi on 2017/01/11.
 */

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    private static final int RESULTCODE = 1;

    private String TAG = "MainActivity";
    private Context mContext;
    private ListView listView;
    private DBAdapter db;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private ArrayList<String> names = new ArrayList<String>();
    private CompanyListAdapter adapter;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        setContentView(R.layout.activity_company_select_list);

        Log.d(TAG,"MainActivityStart");
        db = new DBAdapter(this);
        db.open();
        this.companies = db.getCompanies();
        Iterator<Company> ite = companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            names.add(company.getName());
            Log.d(TAG,String.format("names : %s",company.getName()));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.station_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Puz-Rail：Select Company");
        actionBar.setSubtitle("鉄道事業者選択");

        this.listView = (ListView) findViewById(R.id.company_list_view);
//        this.adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, names);
        this.adapter = new CompanyListAdapter(this,this.companies,this.db);
        Log.d(TAG,"MainActivityStart");
        this.listView.setAdapter(this.adapter);
        Log.d(TAG,"MainActivityStart");
        this.listView.setOnItemClickListener(this);
        this.listView.setOnItemLongClickListener(this);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        int companyId = this.companies.get(position).getId();
//        Log.d(TAG,String.format("%d: %s is selected",companyId,this.companies.get(position).getName()));
//        this.db.updateSelectedCompany(companyId);
        Intent intent = new Intent(mContext, PieceGarallyActivity.class);
        intent.putExtra("SelectedCompanyId", this.companies.get(position).getId());
        startActivityForResult(intent, RESULTCODE);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        Company company = this.companies.get(position);

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
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this,String.format("position %d",position), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // ダイアログ表示
        mDialog = new AlertDialog.Builder(this)
                .setTitle(String.format("%s", company.getName()))
                .setPositiveButton("Cancel", null)
                .setView(contextMenuListView)
                .create();
        mDialog.show();
        return true;
    }
}
