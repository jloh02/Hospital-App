package com.example.jonathan.hospitalapp2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class nurCustomAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<nurFieldItem> data;
    Context context;
    private static LayoutInflater inflater = null;

    public nurCustomAdapter (Activity activity, int textViewResourceId, ArrayList<nurFieldItem> dataItems) {
        try {
            this.activity = activity;
            this.data = dataItems;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public Object getItem(int position){
        return data.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                view = inflater.inflate(R.layout.nurfielditemlayout, null);
            }
            TextView nameTV = (TextView) view.findViewById(R.id.nameTV);
            TextView lvlTV = (TextView) view.findViewById(R.id.levelTV);
            TextView emailTV = (TextView) view.findViewById(R.id.eTV);

            nurFieldItem item = data.get(position);

            String outputLvl =  item.level.get(0) +"";
            for(int i=1;i<item.level.size();i++){
                outputLvl += "," + item.level.get(i);
            }

            nameTV.setText(item.name);
            lvlTV.setText(outputLvl);
            emailTV.setText(item.email);

        } catch (Exception e) {
            Log.e(TAG, "Error: ",e );
        }
        return view;
    }
}