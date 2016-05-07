package com.localhost.tmillner.similartemperature;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import java.util.concurrent.TimeUnit;

/**
 * Created by macbookpro on 4/25/16.
 */
public class AutocompleteAdapter extends ArrayAdapter<String> {
    private static final String TAG = "AutoCompleteAdapter";
    private GoogleApiClient googleApiClient;
    Handler uiHandler = null;

    public AutocompleteAdapter(Context context, int resource, Handler uiHandler) {
        super(context, resource);
        this.uiHandler = uiHandler;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (googleApiClient == null || !googleApiClient.isConnected()) {
                    Toast.makeText(getContext(), "Not connected", Toast.LENGTH_SHORT).show();
                    return null;
                }
                final CharSequence theConstraint = constraint;
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        clear();
                        displayResults("" + theConstraint);
                    }
                });
                return null;


            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        };
    }

    private void displayResults(String query) {
        Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query, null, null)
                .setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                    @Override
                    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
                        if (autocompletePredictions == null) {
                            return;
                        }
                        if (autocompletePredictions.getStatus().isSuccess()) {
                            for(AutocompletePrediction prediction : autocompletePredictions) {
                                String[] chunks = prediction.getFullText(null).toString().split(",");
                                if (chunks.length == 3 || chunks.length == 2) {
                                    add("" + prediction.getFullText(null));
                                }
                            }
                        }
                        autocompletePredictions.release();
                    }
                }, 30, TimeUnit.SECONDS);
    }

    static class ViewHolder {
        public TextView textView;
    }
}
