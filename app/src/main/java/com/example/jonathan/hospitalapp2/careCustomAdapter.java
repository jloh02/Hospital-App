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

public class careCustomAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<careFieldItem> data;
    Context context;
    private static LayoutInflater inflater = null;

    public careCustomAdapter (Activity activity, int textViewResourceId, ArrayList<careFieldItem> dataItems) {
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
                view = inflater.inflate(R.layout.carefielditemlayout, null);
            }
            TextView nameTV = (TextView) view.findViewById(R.id.nameTV);
            TextView contactTV = (TextView) view.findViewById(R.id.cnTV);
            TextView emailTV = (TextView) view.findViewById(R.id.peTV);

            careFieldItem item = data.get(position);

            nameTV.setText(item.name);
            contactTV.setText(item.contactNumber);
            emailTV.setText(item.patientEmail);

        } catch (Exception e) {
            Log.e(TAG, "Error: ",e );
        }
        return view;
    }
}