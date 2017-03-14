package com.example.takashi.RailwayAndStationPuz.piecegarally;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.takashi.RailwayAndStationPuz.R;
import com.example.takashi.RailwayAndStationPuz.database.Line;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by takashi on 2016/12/19.
 */

public class RailwayGridAdapter extends BaseAdapter {

    private static String TAG = "RailwayGridAdapter";
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Line> lines;

    public RailwayGridAdapter(Context context, ArrayList<Line> lines) {
        super();
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        this.lines = new ArrayList<Line>(Arrays.asList(lines));
        this.lines = lines;
    }

    @Override
    public int getCount() {
        // 全要素数を返す
        return this.lines.size();
    }

    @Override
    public Object getItem(int position) {
        return this.lines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.lines.get(position).getDrawableResourceId();
    }

    private class ViewHolder {
        ImageView piece_border_image;
        ImageView railway_line_image;
        TextView line_name;
        ImageView location_status_image;
        ImageView station_status_image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // main.xml の <GridView .../> に railway_grid_items.xml を inflate して convertView とする
            convertView = inflater.inflate(R.layout.railway_grid_items, parent, false);
            // ViewHolder を生成
            holder = new ViewHolder();
            holder.piece_border_image = (ImageView)convertView.findViewById(R.id.piece_border_grid_image_view);
            holder.railway_line_image = (ImageView) convertView.findViewById(R.id.railway_line_grid_image_view);
            holder.line_name = (TextView)convertView.findViewById(R.id.railway_line_grid_name);
            holder.line_name.setTextColor(Color.parseColor("#142d81"));
            holder.location_status_image = (ImageView)convertView.findViewById(R.id.location_status_image);
            holder.station_status_image = (ImageView)convertView.findViewById(R.id.station_status_image);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Line line = this.lines.get(position);
        Drawable lineDrawable = ResourcesCompat.getDrawable(this.context.getResources(), line.getDrawableResourceId(), null);
        holder.railway_line_image.setImageDrawable(lineDrawable);

        String name = line.getName();
        if(!line.isNameCompleted()){
            name = "***";
        }
        Log.d(TAG,"Line name = " + name);
        holder.line_name.setText(name);

        // ロケーション、ステーションのステータス表示
        statusImageSetup(holder);

        return convertView;
    }

    private void statusImageSetup(ViewHolder holder){

        ImageView statusIcView;
        Drawable drawable;
        Matrix imageMatrix;

        // ロケーションスタータスの表示
        statusIcView = holder.location_status_image;
        drawable = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.location_notyet_image,null);
        statusIcView.setImageDrawable(drawable);
//        statusIcView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        imageMatrix = statusIcView.getImageMatrix();
//        statusIcView.setScaleType(ImageView.ScaleType.MATRIX);
//        imageMatrix.setTranslate(128,160);
//        statusIcView.setImageMatrix(imageMatrix);

        // ステーションステータスの表示
        statusIcView = holder.station_status_image;
        drawable = ResourcesCompat.getDrawable(this.context.getResources(),R.drawable.station_notyet_image,null);
        statusIcView.setImageDrawable(drawable);
/*
        statusIcView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageMatrix = statusIcView.getImageMatrix();
        statusIcView.setScaleType(ImageView.ScaleType.MATRIX);
//        imageMatrix.setTranslate(80,80);
        imageMatrix.setTranslate(160,160);
        statusIcView.setImageMatrix(imageMatrix);
        */
    }
}
