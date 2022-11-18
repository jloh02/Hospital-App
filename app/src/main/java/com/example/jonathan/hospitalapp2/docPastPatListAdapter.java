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

/**
 * Created by Jonathan on 6/7/2018.
 */

public class docPastPatListAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<String> namesData;
    ArrayList<String> conditionData;
    Context context;
    private static LayoutInflater inflater = null;

    public docPastPatListAdapter(Activity activity, int textViewResourceId, ArrayList<String> nD,
            ArrayList<String> cD) {
        try {
            this.activity = activity;
            this.namesData = nD;
            this.conditionData = cD;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    @Override
    public int getCount() {
        return namesData.size();
    }

    @Override
    public Object getItem(int position) {
        return namesData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                view = inflater.inflate(R.layout.docpastpatlistlayout, null);
            }
            TextView nameTV = (TextView) view.findViewById(R.id.nameTV);
            TextView condTV = (TextView) view.findViewById(R.id.cTV);

            String nameOut = namesData.get(position);
            String condOut = conditionData.get(position);

            nameTV.setText(nameOut);
            condTV.setText(condOut);

        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
        return view;
    }
}

