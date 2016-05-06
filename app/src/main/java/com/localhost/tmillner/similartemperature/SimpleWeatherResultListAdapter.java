package com.localhost.tmillner.similartemperature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * This class also serves as the model/contract for data that will be shown
 */
public class SimpleWeatherResultListAdapter extends ArrayAdapter<String> {
    private List<String> items;
    private LayoutInflater inflater;
    private int resourceId;

    public SimpleWeatherResultListAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.resourceId = resource;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(resourceId, null);

        try {
            String line = items.get(position);
            String[] columns = line.split(",");
            String region = columns[0];
            String country = columns[1];
            String weather = columns[2];
            String temp = columns[3];

            TextView cityText = (TextView) convertView.findViewById(R.id.result_city);
            cityText.setText(region);

            TextView countryText = (TextView) convertView.findViewById(R.id.result_country);
            countryText.setText(country);

            TextView weatherText = (TextView) convertView.findViewById(R.id.result_weather);
            weatherText.setText(weather);

            TextView degreesText = (TextView) convertView.findViewById(R.id.result_degrees);
            degreesText.setText(temp);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
