package com.example.usuario.geoubicacion;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.Objects;

public class dialogFragmentMap extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng _ubicacion;
    private String _nombre;

    public dialogFragmentMap(LatLng ubicacion, String nombre)
    {
        _ubicacion = ubicacion;
        _nombre=nombre;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogmap, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);
        return view;

    }


//    public Dialog onCreateDialog(Bundle savedInstanceState)
//    {
//
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng latLng = new LatLng(_ubicacion.latitude,_ubicacion.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(_nombre);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        mMap.addMarker(markerOptions).showInfoWindow();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                if (marker.isInfoWindowShown()) {
//                    marker.hideInfoWindow();
//                } else {
//                    marker.showInfoWindow();
//                }
                marker.showInfoWindow();
                return true;
            }
        });
    }

    @TargetApi(19)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        assert getFragmentManager() != null;
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.mapa));
        FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }
}
