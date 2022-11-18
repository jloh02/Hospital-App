package com.example.jonathan.hospitalapp2;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

class CustomSpinnerAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<Integer> data;
    Context context;
    private static LayoutInflater inflater = null;

    public CustomSpinnerAdapter (Activity activity, int textViewResourceId, ArrayList<Integer> imageIDs) {
        try {
            this.activity = activity;
            this.data = imageIDs;

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
                view = inflater.inflate(R.layout.spinner_element, null);
            }
            ImageView imv = view.findViewById(R.id.spinnerImage);
            Integer item = data.get(position);
            imv.setBackgroundResource(item);

        } catch (Exception e) {
            Log.e(TAG, "Error: ",e );
        }
        return view;
    }
}