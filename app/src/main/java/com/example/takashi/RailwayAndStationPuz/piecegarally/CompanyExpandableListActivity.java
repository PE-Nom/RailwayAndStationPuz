package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.takashi.RailwayAndStationPuz.MainActivity;
import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Company;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by takashi on 2016/12/21.
 */

public class CompanyExpandableListActivity extends AppCompatActivity
        implements ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupExpandListener {

    private String TAG = "CompanyExpandableListActivity";
    private static final int RESULTCODE = 1;
    private Context mContext;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    private DBAdapter db;
    private ArrayList<Company> companies = new ArrayList<Company>();

    private String[] companyCodes = new String[] {
            "日本旅客鉄道",
            "公営鉄道各社",
            "民営鉄道各社",
            "第三セクター各社"
    };
/*
    private Company[] companies = new Company[] {
            // JR各社
            new Company(1,2,"九州旅客鉄道"),
            new Company(2,2,"四国旅客鉄道"),
            new Company(3,2,"西日本旅客鉄道"),
            new Company(4,2,"東海旅客鉄道"),
            new Company(5,2,"東日本旅客鉄道"),
            new Company(6,2,"北海道旅客鉄道"),
            // 公営各社
            new Company(7,3,"一般財団法人青函トンネル記念館"),
            new Company(8,3,"横浜市"),
            new Company(9,3,"京都市"),
            new Company(10,3,"熊本市"),
            new Company(11,3,"札幌市"),
            new Company(12,3,"鹿児島市"),
            new Company(13,3,"神戸すまいまちづくり公社"),
            new Company(14,3,"神戸市"),
            new Company(15,3,"仙台市"),
            new Company(16,3,"大阪市"),
            new Company(17,3,"東京都"),
            new Company(18,3,"函館市"),
            new Company(19,3,"福岡市"),
            new Company(20,3,"名古屋市"),
            // 民営各社
            new Company(21,4,"アルピコ交通"),
            new Company(22,4,"スカイレールサービス"),
            new Company(23,4,"とさでん交通"),
            new Company(24,4,"鞍馬寺"),
            new Company(25,4,"伊賀鉄道"),
            new Company(26,4,"伊豆急行"),
            new Company(27,4,"伊豆箱根鉄道"),
            new Company(28,4,"伊予鉄道"),
            new Company(29,4,"一畑電車"),
            new Company(30,4,"叡山電鉄"),
            new Company(31,4,"遠州鉄道"),
            new Company(32,4,"岡山電気軌道"),
            new Company(33,4,"岡本製作所"),
            new Company(34,4,"岳南電車"),
            new Company(35,4,"関西電力"),
            new Company(36,4,"関東鉄道"),
            new Company(37,4,"紀州鉄道"),
            new Company(38,4,"京王電鉄"),
            new Company(39,4,"京阪電気鉄道"),
            new Company(40,4,"京成電鉄"),
            new Company(41,4,"京浜急行電鉄"),
            new Company(42,4,"京福電気鉄道"),
            new Company(43,4,"近畿日本鉄道"),
            new Company(44,4,"近江鉄道"),
            new Company(45,4,"熊本電気鉄道"),
            new Company(46,4,"御岳登山鉄道"),
            new Company(47,4,"広島電鉄"),
            new Company(48,4,"弘南鉄道"),
            new Company(49,4,"江ノ島電鉄"),
            new Company(50,4,"高松琴平電気鉄道"),
            new Company(51,4,"高尾登山電鉄"),
            new Company(52,4,"黒部峡谷鉄道"),
            new Company(53,4,"嵯峨野観光鉄道"),
            new Company(54,4,"阪急電鉄"),
            new Company(55,4,"阪堺電気軌道"),
            new Company(56,4,"阪神電気鉄道"),
            new Company(57,4,"皿倉鉄道"),
            new Company(58,4,"三岐鉄道"),
            new Company(59,4,"山万"),
            new Company(60,4,"山陽電気鉄道"),
            new Company(61,4,"四国ケーブル"),
            new Company(62,4,"小田急電鉄"),
            new Company(63,4,"小湊鐵道"),
            new Company(64,4,"湘南モノレール"),
            new Company(65,4,"上信電鉄"),
            new Company(66,4,"上田電鉄"),
            new Company(67,4,"上毛電気鉄道"),
            new Company(68,4,"新京成電鉄"),
            new Company(69,4,"神戸電鉄"),
            new Company(70,4,"水間鉄道"),
            new Company(71,4,"西日本鉄道"),
            new Company(72,4,"西武鉄道"),
            new Company(73,4,"静岡鉄道"),
            new Company(74,4,"相模鉄道"),
            new Company(75,4,"大井川鐵道"),
            new Company(76,4,"大山観光電鉄"),
            new Company(77,4,"丹後海陸交通"),
            new Company(78,4,"筑波観光鉄道"),
            new Company(79,4,"筑豊電気鉄道"),
            new Company(80,4,"秩父鉄道"),
            new Company(81,4,"銚子電気鉄道"),
            new Company(82,4,"長崎電気軌道"),
            new Company(83,4,"長野電鉄"),
            new Company(84,4,"津軽鉄道"),
            new Company(85,4,"島原鉄道"),
            new Company(86,4,"東海交通事業"),
            new Company(87,4,"東京モノレール"),
            new Company(88,4,"東京急行電鉄"),
            new Company(89,4,"東京地下鉄"),
            new Company(90,4,"東武鉄道"),
            new Company(91,4,"南海電気鉄道"),
            new Company(92,4,"能勢電鉄"),
            new Company(93,4,"箱根登山鉄道"),
            new Company(94,4,"比叡山鉄道"),
            new Company(95,4,"富山地方鉄道"),
            new Company(96,4,"富士急行"),
            new Company(97,4,"舞浜リゾートライン"),
            new Company(98,4,"福井鉄道"),
            new Company(99,4,"福島交通"),
            new Company(100,4,"豊橋鉄道"),
            new Company(101,4,"北神急行電鉄"),
            new Company(102,4,"北陸鉄道"),
            new Company(103,4,"名古屋鉄道"),
            new Company(104,4,"養老鉄道"),
            new Company(105,4,"立山黒部貫光"),
            new Company(106,4,"流鉄"),
            new Company(107,4,"六甲山観光"),
            new Company(108,4,"和歌山電鐵"),
            // 第三セクタ各社
            new Company(109,5,"IRいしかわ鉄道"),
            new Company(110,5,"WILLER　TRAINS"),
            new Company(111,5,"アイジーアールいわて銀河鉄道"),
            new Company(112,5,"あいの風とやま鉄道"),
            new Company(113,5,"いすみ鉄道"),
            new Company(114,5,"えちごトキめき鉄道"),
            new Company(115,5,"えちぜん鉄道"),
            new Company(116,5,"くま川鉄道"),
            new Company(117,5,"しなの鉄道"),
            new Company(118,5,"のと鉄道"),
            new Company(119,5,"ひたちなか海浜鉄道"),
            new Company(120,5,"ゆりかもめ"),
            new Company(121,5,"わたらせ渓谷鐵道"),
            new Company(122,5,"阿佐海岸鉄道"),
            new Company(123,5,"阿武隈急行"),
            new Company(124,5,"愛知環状鉄道"),
            new Company(125,5,"愛知高速交通"),
            new Company(126,5,"伊勢鉄道"),
            new Company(127,5,"井原鉄道"),
            new Company(128,5,"横浜シーサイドライン"),
            new Company(129,5,"横浜高速鉄道"),
            new Company(130,5,"沖縄都市モノレール"),
            new Company(131,5,"会津鉄道"),
            new Company(132,5,"甘木鉄道"),
            new Company(133,5,"錦川鉄道"),
            new Company(134,5,"広島高速交通"),
            new Company(135,5,"埼玉高速鉄道"),
            new Company(136,5,"埼玉新都市交通"),
            new Company(137,5,"三陸鉄道"),
            new Company(138,5,"山形鉄道"),
            new Company(139,5,"四日市あすなろう鉄道"),
            new Company(140,5,"鹿島臨海鉄道"),
            new Company(141,5,"芝山鉄道"),
            new Company(142,5,"若桜鉄道"),
            new Company(143,5,"首都圏新都市鉄道"),
            new Company(144,5,"秋田内陸縦貫鉄道"),
            new Company(145,5,"松浦鉄道"),
            new Company(146,5,"信楽高原鐵道"),
            new Company(147,5,"真岡鐵道"),
            new Company(148,5,"神戸新交通"),
            new Company(149,5,"水島臨海鉄道"),
            new Company(150,5,"青い森鉄道"),
            new Company(151,5,"仙台空港鉄道"),
            new Company(152,5,"千葉都市モノレール"),
            new Company(153,5,"泉北高速鉄道"),
            new Company(154,5,"多摩都市モノレール"),
            new Company(155,5,"大阪高速鉄道"),
            new Company(156,5,"樽見鉄道"),
            new Company(157,5,"智頭急行"),
            new Company(158,5,"長良川鉄道"),
            new Company(159,5,"天竜浜名湖鉄道"),
            new Company(160,5,"土佐くろしお鉄道"),
            new Company(161,5,"東京臨海高速鉄道"),
            new Company(162,5,"東葉高速鉄道"),
            new Company(163,5,"南阿蘇鉄道"),
            new Company(164,5,"肥薩おれんじ鉄道"),
            new Company(165,5,"富山ライトレール"),
            new Company(166,5,"平成筑豊鉄道"),
            new Company(167,5,"北越急行"),
            new Company(168,5,"北九州高速鉄道"),
            new Company(169,5,"北条鉄道"),
            new Company(170,5,"北総鉄道"),
            new Company(171,5,"北大阪急行電鉄"),
            new Company(172,5,"万葉線"),
            new Company(173,5,"名古屋ガイドウェイバス"),
            new Company(174,5,"名古屋臨海高速鉄道"),
            new Company(175,5,"明知鉄道"),
            new Company(176,5,"野岩鉄道"),
            new Company(177,5,"由利高原鉄道")
    };
*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_company_list_main);

        db = new DBAdapter(this);
        db.open();
        this.companies = db.getCompanies();

    	/*=========*/
        //参考URL
        //http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new RailwayExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listener Set
        expListView.setOnGroupClickListener(this);
        expListView.setOnGroupExpandListener(this);
        expListView.setOnGroupCollapseListener(this);
        expListView.setOnChildClickListener(this);
    	/*=========*/

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

    // Listview Group click listener
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        // Toast.makeText(getApplicationContext(),
        // "Group Clicked " + listDataHeader.get(groupPosition),
        // Toast.LENGTH_SHORT).show();
        return false;
    }

    // Listview Group expanded listener
    @Override
    public void onGroupExpand(int groupPosition) {
        Toast.makeText(getApplicationContext(),listDataHeader.get(groupPosition) + " Expanded", Toast.LENGTH_SHORT).show();
    }

    // Listview Group collasped listener
    @Override
    public void onGroupCollapse(int groupPosition) {
        Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " Collapsed", Toast.LENGTH_SHORT).show();
    }

    // Listview on child click listener
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        // TODO Auto-generated method stub
        Toast.makeText( getApplicationContext(),listDataHeader.get(groupPosition) + " : " + listDataChild.get( listDataHeader.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
        return false;
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
//        ArrayList<Company> companies;
        Iterator<Company> ite;

//        companies = new ArrayList<Company>(Arrays.asList(this.companies));

        // Adding child data
        listDataHeader.add(companyCodes[0]);	// ＪＲ各社
        listDataHeader.add(companyCodes[1]);	// 公営鉄道各社
        listDataHeader.add(companyCodes[2]);	// 私鉄各社
        listDataHeader.add(companyCodes[3]);	// 第3セクター各社

        // Adding child data

        // ＪＲ各社
        List<String> jr = new ArrayList<String>();
        ite = this.companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            if(company.getCode() == 2 ){
                Log.d(TAG,String.format("JR company : %s",company.getName()));
                jr.add(company.getName());
            }
        }

        // 公営鉄道各社
        List<String> kouei = new ArrayList<String>();
        ite = this.companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            if(company.getCode() == 3 ){
                kouei.add(company.getName());
            }
        }

        // 私鉄各社
        List<String> shitetu = new ArrayList<String>();
        ite = this.companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            if(company.getCode() == 4 ){
                shitetu.add(company.getName());
            }
        }

        // 第3セクター各社
        List<String> sansec = new ArrayList<String>();
        ite = this.companies.iterator();
        while(ite.hasNext()){
            Company company = ite.next();
            if(company.getCode() == 5 ){
                sansec.add(company.getName());
            }
        }

        // Header, Child data
        listDataChild.put(listDataHeader.get(0), jr);
        listDataChild.put(listDataHeader.get(1), kouei);
        listDataChild.put(listDataHeader.get(2), shitetu);
        listDataChild.put(listDataHeader.get(3), sansec);
    }
}
