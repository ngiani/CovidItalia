package com.example.coviditalia;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Situation implements JSONReaderListener
{
    protected SituationUpdateListener situationUpdateListener;

    public Situation(SituationUpdateListener listener)
    {
        situationUpdateListener = listener;
    }

}
