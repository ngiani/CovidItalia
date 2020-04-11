package com.example.coviditalia;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Situation implements JSONReaderListener
{
    private SituationUpdateListener situationUpdateListener;

    private ArrayList<Integer> total_cases = new ArrayList<>();
    private ArrayList<Integer> recovered = new ArrayList<>();
    private ArrayList<Integer> deaths = new ArrayList<>();


    public Situation(SituationUpdateListener listener)
    {
        situationUpdateListener = listener;

    }

    public int getTodayRecovered()
    {
        return recovered.get(recovered.size() - 1);
    }

    public int getTodayTotalCases()
    {
        return total_cases.get(total_cases.size() - 1);
    }

    public int getTodayDeaths()
    {
        return deaths.get(deaths.size() - 1);
    }

    public int getNewTotalCases()
    {
        if (total_cases.size() > 1)
            return total_cases.get(total_cases.size() - 1) - total_cases.get(total_cases.size() - 2);
        else
            return 0;
    }

    public int getNewRecovered()
    {
        if (recovered.size() > 1)
            return recovered.get(recovered.size() - 1) - recovered.get(recovered.size() - 2);
        else
            return 0;
    }

    public int getNewDeaths()
    {
        if (deaths.size() > 1)
            return deaths.get(deaths.size() - 1) - deaths.get(deaths.size() - 2);
        else
            return 0;
    }

    public int[] getTotalCasesHistory()
    {
        int[] history = new int[total_cases.size()];

        for (int i = 0; i < total_cases.size(); i++ )
        {
            history[i] = total_cases.get(i).intValue();
        }

        return history;
    }

    public int[] getRecoveredHistory()
    {
        int[] history = new int[recovered.size()];

        for (int i = 0; i < recovered.size(); i++ )
        {
            history[i] = recovered.get(i).intValue();
        }

        return history;
    }

    public int[] getDeathsHistory()
    {
        int[] history = new int[deaths.size()];

        for (int i = 0; i < deaths.size(); i++ )
        {
            history[i] = deaths.get(i).intValue();
        }

        return history;
    }

    @Override
    public void OnJSONReaderResults(JSONArray result)
    {


        for (int i = 0; i < result.length(); i++){

            try {
                JSONObject jsonObject = result.getJSONObject(i);

                total_cases.add(jsonObject.getInt("totale_casi"));
                recovered.add(jsonObject.getInt("dimessi_guariti"));
                deaths.add(jsonObject.getInt("deceduti"));


            } catch (JSONException e) {
                Log.e("JSONException", e.getMessage());
                e.printStackTrace();
            }
        }

        situationUpdateListener.OnSituationUpdate();
    }
}
