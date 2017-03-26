package com.example.takashi.RailwayAndStationPuz;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.database.Company;
import com.example.takashi.RailwayAndStationPuz.database.DBAdapter;
import com.example.takashi.RailwayAndStationPuz.station.StationListAdapter;
import com.example.takashi.RailwayAndStationPuz.ui.GaugeView;
import com.example.takashi.RailwayAndStationPuz.ui.SimpleGaugeView;

import java.util.ArrayList;

/**
 * Created by takashi on 2017/03/26.
 */

public class CompanyListAdapter extends BaseAdapter {

    private String TAG="CompanyAdapter";
    private MainActivity context;
    private ArrayList<Company> companies = new ArrayList<Company>();
    private DBAdapter dbAdapter;

    public CompanyListAdapter(Context context, ArrayList<Company> companies, DBAdapter dbAdapter){
        this.context = (MainActivity)context;
        this.companies = companies;
        this.dbAdapter = dbAdapter;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        Log.d(TAG,String.format("getCount = %d",this.companies.size()));
        return this.companies.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        Log.d(TAG,String.format("getItem position = %d",position));
        return this.companies.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        Log.d(TAG,String.format("getItemId position = %d",position));
        return this.companies.get(position).getId();
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = this.context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.company_list_item,null);
            holder = new ViewHolder();
            holder.companyName = (TextView)convertView.findViewById(R.id.companyName);
            holder.lineNameProgressTitle = (TextView)convertView.findViewById(R.id.nameProgTitle);
            holder.trackLayingProgressTitle = (TextView)convertView.findViewById(R.id.trackLayingProgTitle);
            holder.stationOpenProgressTtitle = (TextView)convertView.findViewById(R.id.stationProgTitle);
            holder.lineNameProgress = (GaugeView)convertView.findViewById(R.id.lineNameProgress);
            holder.trackLayingProgress = (GaugeView)convertView.findViewById(R.id.trackLayingProgress);
            holder.stationOpenProgress = (GaugeView)convertView.findViewById(R.id.stationProgress);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Company company = this.companies.get(position);
        String companyName = company.getName()+"("+company.getKana()+")";
        Log.d(TAG,String.format("companyName = %s",companyName));
        holder.companyName.setText(companyName);

        // 路線名の進捗
        Log.d(TAG,"nameProgress");
        int nameProgress = 0;
        holder.lineNameProgress.setData(nameProgress,"%", ContextCompat.getColor(this.context, R.color.color_90),90,true);

        // 敷設工事の進捗
        Log.d(TAG,"tracklayingProgress");
        int trackLayingProgress = 0;
        holder.trackLayingProgress.setData(trackLayingProgress,"%",ContextCompat.getColor(this.context, R.color.color_60),90,true);

        // 駅開設の進捗
        Log.d(TAG,"stationProgress");
        int stationOpenProgress = 0;
        holder.stationOpenProgress.setData(stationOpenProgress,"%",ContextCompat.getColor(this.context, R.color.color_30),90,true);

        return convertView;
    }

    private class ViewHolder {
        TextView companyName;
        TextView lineNameProgressTitle;
        GaugeView lineNameProgress;
        TextView trackLayingProgressTitle;
        GaugeView trackLayingProgress;
        TextView stationOpenProgressTtitle;
        GaugeView stationOpenProgress;
    }
}
