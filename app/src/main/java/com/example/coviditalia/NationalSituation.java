package com.example.coviditalia;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class NationalSituation extends Situation
{
    private ArrayList<Integer> positives = new ArrayList<Integer>();
    private ArrayList<Integer> recovered = new ArrayList<Integer>();
    private ArrayList<Integer> deaths = new ArrayList<Integer>();
    private ArrayList<Integer> total_cases = new ArrayList<Integer>();

    public NationalSituation(SituationUpdateListener listener) {
        super(listener);
    }

    public int getTodayPositives() { return positives.get(positives.size() - 1); }

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

    public int getNewPositives()
    {
        if (positives.size() > 1)
            return positives.get(positives.size() - 1) - positives.get(positives.size() - 2);
        else
            return 0;
    }

    public int[] getPositivesHistory()
    {
        int[] history = new int[positives.size()];

        for (int i = 0; i < positives.size(); i++ )
        {
            history[i] = positives.get(i).intValue();
        }

        return history;
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

    public int getPositivesPeak()
    {
        return Collections.max(positives);
    }

    public int getRecoveredPeak()
    {
        return Collections.max(recovered);
    }

    public int getDeathsPeak()
    {
        return Collections.max(deaths);
    }

    public int getTotalCasesPeak()
    {
        return Collections.max(total_cases);
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
                positives.add(jsonObject.getInt("totale_positivi"));

            } catch (JSONException e) {
                Log.e("JSONException", e.getMessage());
                e.printStackTrace();
            }
        }

        situationUpdateListener.OnSituationUpdate();
    }
}
