package com.localhost.tmillner.similartemperature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class also serves as the model/contract for data that will be shown
 */
public class WeatherResultListAdapter extends ArrayAdapter<JSONObject> {
    private JSONObject[] items;
    private LayoutInflater inflater;
    private int resourceId;

    public WeatherResultListAdapter(Context context, int resource, JSONObject[] items) {
        super(context, resource, items);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.resourceId = resource;
    }

    @Override
    public int getCount() {
        return this.items.length;
    }

    @Override
    public JSONObject getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(resourceId, null);

        try {
            TextView cityText = (TextView) convertView.findViewById(R.id.result_city);
            cityText.setText(items[position].getString("city"));

            TextView countryText = (TextView) convertView.findViewById(R.id.result_country);
            countryText.setText(items[position].getString("country"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
