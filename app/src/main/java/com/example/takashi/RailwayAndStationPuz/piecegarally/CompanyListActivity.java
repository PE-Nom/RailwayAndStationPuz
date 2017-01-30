package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.takashi.RailwayAndStationPuz.MainActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Company;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by takashi on 2017/01/11.
 */

public class CompanyListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener{

    private static final int RESULTCODE = 1;

    private String TAG = "CompanyListActivity";
    private Context mContext;
    private ListView listView;
    private DBAdapter db;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getApplicationContext();
        setContentView(R.layout.activity_company_list_main);

        db = new DBAdapter(this);
        db.open();
        this.companies = db.getCompanies();
        Iterator<Company> ite = companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            names.add(company.getName());
        }

        this.listView = (ListView) findViewById(R.id.company_list_view);
        this.adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, names);
        this.listView.setAdapter(this.adapter);
        this.listView.setOnItemClickListener(this);

        // キャンセルとCallbackの配置
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivityForResult(intent, RESULTCODE);
                overridePendingTransition(R.anim.in_up, R.anim.out_down);
                finish();
            }
        });
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
        int companyId = this.companies.get(position).getId();
        Log.d(TAG,String.format("%d is selected",companyId));
        this.db.updateSelectedCompany(companyId);
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivityForResult(intent, RESULTCODE);
        overridePendingTransition(R.anim.in_up, R.anim.out_down);
        finish();
    }
}
