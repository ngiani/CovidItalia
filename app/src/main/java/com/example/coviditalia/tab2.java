package com.example.coviditalia;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link tab2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class tab2 extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MapView situationMap;

    private LocalSituation localSituation;

    public tab2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment tab2.
     */
    // TODO: Rename and change types and number of parameters
    public static tab2 newInstance(String param1, String param2) {
        tab2 fragment = new tab2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d("DBG", "ON CREATE TAB 2 VIEW !");

        View view = inflater.inflate(R.layout.fragment_tab2, container, false);

        situationMap = (MapView)view.findViewById(R.id.mapView);
        situationMap.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).setCurrentPageInstance(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        situationMap.onResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        situationMap.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        situationMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        situationMap.onLowMemory();
        super.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d("DBG","ON MAP READY !");

        googleMap.clear();

        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);

        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.902782, 12.496366), 5));

        for (int i = 0; i < localSituation.getRegionsCount(); i++)
        {
            googleMap.addCircle(new CircleOptions().center(localSituation.getRegionCoordinates(i)).radius(localSituation.getRegionCases(i) * 2.5).strokeColor(Color.BLACK).strokeWidth(2).fillColor(Color.RED).visible(true));
        }

     }

    public void GetLocalSituationMap(LocalSituation localSituation)
    {
        this.localSituation = localSituation;
        situationMap.getMapAsync(this);
    }
}
