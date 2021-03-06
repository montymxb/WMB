package com.jmstudios.corvallistransit.jsontools;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jmstudios.corvallistransit.models.Stop;
import com.jmstudios.corvallistransit.utils.WebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CtsJsonArrivalsTask extends AsyncTask<List<Stop>, Void, List<Stop>> {
    private static final String arrivalsUrl = "http://www.corvallis-bus.appspot.com/arrivals?stops=";
    private static final String arrivalsLogTag = "ArrivalsTask";
    private static String mRouteName;
    private ArrivalsTaskCompleted listener;
    private ProgressDialog progressDialog;
    private boolean mIsFromSwipeDown;

    public CtsJsonArrivalsTask(Context context, String routeName,
                               ArrivalsTaskCompleted listener, boolean fromSwipe) {
        mRouteName = routeName;
        this.listener = listener;
        if (!mIsFromSwipeDown) {
            progressDialog = new ProgressDialog(context);
        }
        mIsFromSwipeDown = fromSwipe;
    }

    @Override
    protected void onPreExecute() {
        if (!mIsFromSwipeDown) {
            progressDialog.setMessage("Getting Eta info...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    @Override
    protected List<Stop> doInBackground(List<Stop>... stupidSyntaxStops) {
        if (stupidSyntaxStops == null || stupidSyntaxStops[0] == null
                || stupidSyntaxStops[0].isEmpty()) {
            // do something
            // oh by the way, this probably will never happen
        }

        List<Stop> stops = stupidSyntaxStops[0];

        String url = arrivalsUrl + WebUtils.stopsToIdCsv(stops);

        return getArrivalsData(url, stops);
    }

    @Override
    protected void onPostExecute(List<Stop> stopsWithArrival) {
        if (!mIsFromSwipeDown && progressDialog.isShowing()) {
            progressDialog.hide();
        }

        listener.onArrivalsTaskCompleted(stopsWithArrival);
    }

    private List<Stop> getArrivalsData(String url, List<Stop> stopsWithoutArrival) {
        List<Stop> stopsWithArrival = new ArrayList<Stop>();
        try {
            String json = WebUtils.downloadUrl(url);
            stopsWithArrival = parseStopArrivals(json, stopsWithoutArrival);
        } catch (Exception e) {
            Log.d(arrivalsLogTag, e.getMessage());
        }

        return stopsWithArrival;
    }

    private List<Stop> parseStopArrivals(String json, List<Stop> stopsWithoutArrival) {
        try {
            JSONObject jsonObject = new JSONObject(json);

            for (Iterator<Stop> iterator = stopsWithoutArrival.iterator(); iterator.hasNext(); ) {
                Stop s = iterator.next();

                String id = String.valueOf(s.id);

                if (jsonObject.has(id)) {
                    JSONArray jsonArray = jsonObject.getJSONArray(id);
                    if (jsonArray.length() > 0) {
                        JSONObject jobj2 = jsonArray.getJSONObject(0);

                        String jsonRouteName = jobj2.getString("Route");
                        if (mRouteName.equals(jsonRouteName.trim())) {
                            s.expectedTimeString = jobj2.getString("Expected");
                            s.expectedTime = s.getScheduledTime();

                            if (s.eta() < 1) {
                                iterator.remove();
                            }
                        } else {
                            iterator.remove();
                        }
                    } else {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }
        } catch (JSONException e) {
            Log.d(arrivalsLogTag, e.getMessage());
        }

        return stopsWithoutArrival;
    }
}
