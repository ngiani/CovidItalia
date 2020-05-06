package com.example.coviditalia;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
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

public class MainActivity extends AppCompatActivity implements  SituationUpdateListener, ViewPager.OnPageChangeListener {

    private Situation currentSituation;

    InputStream is;

    private boolean canDownloadData = true;

    AlertDialog connectionAlertDialog;
    private final int DIALOG_CLOSE = 0;
    private final int DIALOG_OK = 1;
    private boolean canShowDialog;

    private ViewPager viewPager = null;
    private SectionsPagerAdapter sectionsPagerAdapter = null;
    private Fragment currentPageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(this);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

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

                //Download and show data (or simple close connection alert) if connected to the internet
                if (isConnected())
                {
                    if (canDownloadData)
                    {

                        String url = "";

                        if (viewPager.getCurrentItem() == 0)
                        {
                            currentSituation = new NationalSituation(MainActivity.this);
                            url = "https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json";
                        }

                        else if (viewPager.getCurrentItem() == 1)
                        {
                            currentSituation = new LocalSituation(MainActivity.this);
                            url = "https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-regioni-latest.json";
                        }

                        JSONReaderTask jsonReaderTask = new JSONReaderTask();
                        jsonReaderTask.setResultsListener(currentSituation);
                        jsonReaderTask.execute(url);

                        canDownloadData = false;
                    }
                    mDialogHandler.sendEmptyMessage(DIALOG_CLOSE);

                    Log.d("DBG", "Connected");

                    canShowDialog = false;
                }

                //Show connection alert if not connected to the internet
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("DBG", "ON PAGE SELECTED !");
        canDownloadData = true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
                Log.d("JSONException", e.getMessage());
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

        if (viewPager.getCurrentItem() == 0)
        {
//Set current situation counters
            TextView positives_counter = (TextView)findViewById(R.id.positive_counter);

            NationalSituation nationalSituation = (NationalSituation)currentSituation;

            if (nationalSituation.getNewPositives() > 0 )
                positives_counter.setText(String.format("%s (+%s)", String.format("%,d", nationalSituation.getTodayPositives()), String.format("%d", nationalSituation.getNewPositives())));
            else if (nationalSituation.getNewPositives() < 0)
                positives_counter.setText(String.format("%s (%s)", String.format("%,d", nationalSituation.getTodayPositives()), String.format("%d", nationalSituation.getNewPositives())));
            else
                positives_counter.setText(String.format("%,d", nationalSituation.getTodayPositives()));

            TextView total_cases_counter = (TextView)findViewById(R.id.total_cases_counter);

            if (nationalSituation.getNewTotalCases() > 0)
                total_cases_counter.setText(String.format("%s (+%s)", String.format("%,d", nationalSituation.getTodayTotalCases()), String.format("%d", nationalSituation.getNewTotalCases())));
            else if (nationalSituation.getNewTotalCases() < 0)
                total_cases_counter.setText(String.format("%s (%s)", String.format("%,d", nationalSituation.getTodayTotalCases()), String.format("%d", nationalSituation.getNewTotalCases())));
            else
                total_cases_counter.setText(String.format("%,d", nationalSituation.getTodayTotalCases()));


            TextView recovered_counter = (TextView)findViewById(R.id.recovered_counter);

            if (nationalSituation.getNewTotalCases() > 0)
                recovered_counter.setText(String.format("%s (+%s)", String.format("%,d", nationalSituation.getTodayRecovered()), String.format("%d", nationalSituation.getNewRecovered())));
            else if (nationalSituation.getNewTotalCases() < 0)
                recovered_counter.setText(String.format("%s (%s)", String.format("%,d", nationalSituation.getTodayRecovered()), String.format("%d", nationalSituation.getNewRecovered())));
            else
                recovered_counter.setText(String.format("%,d", nationalSituation.getTodayRecovered()));

            TextView deaths_counter = (TextView)findViewById(R.id.deaths_counter);

            if (nationalSituation.getNewDeaths() > 0)
                deaths_counter.setText(String.format("%s (+%s)", String.format("%,d", nationalSituation.getTodayDeaths()), String.format("%d", nationalSituation.getNewDeaths())));
            else if (nationalSituation.getNewDeaths() < 0)
                deaths_counter.setText(String.format("%s (%s)", String.format("%,d", nationalSituation.getTodayDeaths()), String.format("%d", nationalSituation.getNewDeaths())));
            else
                deaths_counter.setText(String.format("%,d", nationalSituation.getTodayDeaths()));

            ShowGraphs();
        }



        else if (viewPager.getCurrentItem() == 1)
        {
            LocalSituation localSituation = (LocalSituation)currentSituation;
            ((tab2)currentPageFragment).GetLocalSituationMap(localSituation);
        }
    }

    void ShowGraphs()
    {
        NationalSituation situation = (NationalSituation)currentSituation;

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
        graph_1.getViewport().setMaxY(situation.getTotalCasesPeak());

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
        graph_2.getViewport().setMaxY(situation.getRecoveredPeak());

        graph_2.addSeries(recovered_series);

        GraphView graph_3 = (GraphView)findViewById(R.id.graph3);
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
        graph_3.getViewport().setMaxY(situation.getDeathsPeak());

        graph_3.addSeries(death_series);

        GraphView graph_4 = (GraphView)findViewById(R.id.graph4);
        LineGraphSeries<DataPoint> positives_series = new LineGraphSeries<>();

        for (int i = 0 ; i < situation.getPositivesHistory().length; i++)
        {
            positives_series.appendData(new DataPoint(i, situation.getPositivesHistory()[i]), true, 200);
        }

        graph_4.getViewport().setXAxisBoundsManual(true);
        graph_4.getViewport().setMinX(0);
        graph_4.getViewport().setMaxX(situation.getPositivesHistory().length - 1);
        graph_4.getViewport().setYAxisBoundsManual(true);
        graph_4.getViewport().setMinY(0);
        graph_4.getViewport().setMaxY(situation.getPositivesPeak());

        graph_4.addSeries(positives_series);
    }

    public void setCurrentPageInstance(Fragment pageFragment)
    {
        currentPageFragment = pageFragment;
    }
}