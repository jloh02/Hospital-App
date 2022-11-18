package com.example.jonathan.hospitalapp2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class patientApptCustomAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<scheduleItem> data;
    Context context;
    private static LayoutInflater inflater = null;

    String TAG = "patientApptCustomAdapter";

    public patientApptCustomAdapter(Activity activity, ArrayList<scheduleItem> dataItems) {
        try {
            this.activity = activity;
            this.data = dataItems;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
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
                view = inflater.inflate(R.layout.pat_appt_custom_adapter_layout, null);
            }
            final TextView timeTV = (TextView) view.findViewById(R.id.timeTV);
            final TextView locationTV = (TextView) view.findViewById(R.id.locTV);

            final scheduleItem item = data.get(position);

            Date d = new Date(item.timestamp * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String ddmmyyyyhhmmTime = sdf.format(d);

            timeTV.setText(ddmmyyyyhhmmTime);
            locationTV.setText("Location: \n" + item.location);

        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
        return view;
    }
}