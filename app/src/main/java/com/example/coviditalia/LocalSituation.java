package com.example.coviditalia;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalSituation extends Situation{

    private ArrayList<Pair<LatLng, Integer>> regionsSituation = new ArrayList<Pair<LatLng, Integer>>();

    public int getRegionsCount()
    {
        return regionsSituation.size();
    }

    public LatLng getRegionCoordinates(int id)
    {
        return regionsSituation.get(id).first;
    }

    public int getRegionCases(int id)
    {
        return regionsSituation.get(id).second;
    }

    public LocalSituation(SituationUpdateListener listener)
    {
        super(listener);
    }

    @Override
    public void OnJSONReaderResults(JSONArray result)
    {
        for (int i = 0; i < result.length(); i++){

            try {
                JSONObject jsonObject = result.getJSONObject(i);

                Pair<LatLng, Integer> regionData = new Pair<LatLng, Integer>(
                        new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("long")),
                        jsonObject.getInt("totale_positivi"));

                regionsSituation.add(regionData);

            } catch (JSONException e) {
                Log.e("JSONException", e.getMessage());
                e.printStackTrace();
            }
        }

        situationUpdateListener.OnSituationUpdate();
    }
}
