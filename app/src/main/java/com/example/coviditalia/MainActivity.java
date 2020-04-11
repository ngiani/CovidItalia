package com.example.coviditalia;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.coviditalia.ui.main.SectionsPagerAdapter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements  SituationUpdateListener{

    private Situation situation;

    InputStream is;

    AlertDialog connectionAlertDialog;
    private boolean alreadyDownloadedData;

    private final int DIALOG_CLOSE = 0;
    private final int DIALOG_OK = 1;
    private boolean canShowDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        situation = new Situation(this);

        //SETUP DIALOGS
        connectionAlertDialog =  new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alert_connection_title)
                .setMessage(R.string.alert_connection_body).create();

        @SuppressLint("HandlerLeak") final Handler mDialogHandler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case DIALOG_CLOSE:
                        connectionAlertDialog.dismiss();
                    case DIALOG_OK:
                        if (canShowDialog)
                            connectionAlertDialog.show();
                }

            };
        };

        //SCHEDULE ACTIONS
        Timer timer = new Timer();


        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if (isConnected())
                {
                    if (!alreadyDownloadedData)
                    {

                        String url = "https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json";

                        JSONReaderTask jsonReaderTask = new JSONReaderTask();
                        jsonReaderTask.setResultsListener(situation);
                        jsonReaderTask.execute(url);

                        alreadyDownloadedData = true;
                    }
                    mDialogHandler.sendEmptyMessage(DIALOG_CLOSE);

                    Log.d("DBG", "Connected");

                    canShowDialog = false;
                }

                else
                {
                    Log.d("DBG", "Not connected");
                    mDialogHandler.sendEmptyMessage(DIALOG_OK);

                    canShowDialog = true;
                }
            };
        };

        timer.schedule(timerTask, 0, 2000);
    }

    boolean isConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    class JSONReaderTask extends AsyncTask<String, Void, String>
    {

        private JSONReaderListener resultsListener;

        public void setResultsListener(JSONReaderListener jsonReaderListener)
        {
            resultsListener = jsonReaderListener;
        }


        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                is = new URL(urls[0]).openStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);

                return jsonText;
            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                Log.d("JSONDB", "JSON RESULT : " + result);
                JSONArray json = new JSONArray(result);
                resultsListener.OnJSONReaderResults(json);
            } catch (JSONException e) {
                Log.d("IOException", e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void OnSituationUpdate() {

        //Set current situation counters
        TextView total_cases_counter = (TextView)findViewById(R.id.textView4);
        total_cases_counter.setText(String.format("%s (+%s)", String.format("%,d", situation.getTodayTotalCases()), String.format("%d", situation.getNewTotalCases())));

        TextView recovered_counter = (TextView)findViewById(R.id.textView6);
        recovered_counter.setText(String.format("%s (+%s)", String.format("%,d", situation.getTodayRecovered()), String.format("%d", situation.getNewRecovered())));

        //TextView deaths_counter = (TextView)findViewById(R.id.textView8);
        //deaths_counter.setText(String.format("%s (+%s)", String.format("%.d", situation.getTodayDeaths()), String.format("%d", situation.getNewDeaths())));

        ShowGraphs();
    }

    void ShowGraphs()
    {
        GraphView graph_1 = (GraphView)findViewById(R.id.graph);
        LineGraphSeries<DataPoint> total_cases_series = new LineGraphSeries<>();

        for (int i = 0 ; i < situation.getTotalCasesHistory().length; i++)
        {
            total_cases_series.appendData(new DataPoint(i, situation.getTotalCasesHistory()[i]), true, 200);
        }

        graph_1.getViewport().setXAxisBoundsManual(true);
        graph_1.getViewport().setMinX(0);
        graph_1.getViewport().setMaxX(situation.getTotalCasesHistory().length - 1);
        graph_1.getViewport().setYAxisBoundsManual(true);
        graph_1.getViewport().setMinY(0);
        graph_1.getViewport().setMaxY(situation.getTodayTotalCases());

        graph_1.addSeries(total_cases_series);


        GraphView graph_2 = (GraphView)findViewById(R.id.graph2);
        LineGraphSeries<DataPoint> recovered_series = new LineGraphSeries<>();

        for (int i = 0 ; i < situation.getRecoveredHistory().length; i++)
        {
            recovered_series.appendData(new DataPoint(i, situation.getRecoveredHistory()[i]), true, 200);
        }

        graph_2.getViewport().setXAxisBoundsManual(true);
        graph_2.getViewport().setMinX(0);
        graph_2.getViewport().setMaxX(situation.getRecoveredHistory().length - 1);
        graph_2.getViewport().setYAxisBoundsManual(true);
        graph_2.getViewport().setMinY(0);
        graph_2.getViewport().setMaxY(situation.getTodayRecovered());

        graph_2.addSeries(recovered_series);

        /*GraphView graph_3 = (GraphView)findViewById(R.id.graph3);
        LineGraphSeries<DataPoint> death_series = new LineGraphSeries<>();

        for (int i = 0 ; i < situation.getDeathsHistory().length; i++)
        {
            death_series.appendData(new DataPoint(i, situation.getDeathsHistory()[i]), true, 200);
        }

        graph_3.getViewport().setXAxisBoundsManual(true);
        graph_3.getViewport().setMinX(0);
        graph_3.getViewport().setMaxX(situation.getDeathsHistory().length - 1);
        graph_3.getViewport().setYAxisBoundsManual(true);
        graph_3.getViewport().setMinY(0);
        graph_3.getViewport().setMaxY(situation.getTodayDeaths());

        graph_3.addSeries(death_series);*/
    }
}