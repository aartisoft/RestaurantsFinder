package com.example.abbieturner.restaurantsfinder.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.abbieturner.restaurantsfinder.Activities.RestaurantActivity;
import com.example.abbieturner.restaurantsfinder.Data.RestaurantModel;
import com.example.abbieturner.restaurantsfinder.Interfaces.ISendRestaurant;
import com.example.abbieturner.restaurantsfinder.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class RestaurantMap extends Fragment implements ISendRestaurant {

    MapView mMapView;
    private GoogleMap googleMap;
    private RestaurantModel restaurant;

    public RestaurantMap() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_restaurant_map, container, false);


        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                ((RestaurantActivity) getActivity()).restaurantMapReady();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RestaurantActivity) getActivity()).restaurantMapReady();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }

    private void addMarkerToMap(RestaurantModel restaurant) {
        if (restaurant.isFirebaseRestaurant()) {
            if (restaurant != null && restaurant.getFirebaseRestaurant().isLocationSet()) {
                // For dropping a marker at a point on the Map
                LatLng restaurantPosition = new LatLng(
                        restaurant.getFirebaseRestaurant().getLat(),
                        restaurant.getFirebaseRestaurant().getLng());

                googleMap.addMarker(new MarkerOptions().position(restaurantPosition).title(restaurant.getFirebaseRestaurant().getName()));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(restaurantPosition).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        } else {
            if (restaurant != null && restaurant.getZomatoRestaurant().isLocationSet()) {
                // For dropping a marker at a point on the Map
                LatLng restaurantPosition = new LatLng(
                        Double.parseDouble(restaurant.getZomatoRestaurant().getLocation().getLatitude()),
                        Double.parseDouble(restaurant.getZomatoRestaurant().getLocation().getLongitude()));

                googleMap.addMarker(new MarkerOptions().position(restaurantPosition).title(restaurant.getZomatoRestaurant().getName()));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(restaurantPosition).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }


    @Override
    public void sendRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;

        if (googleMap != null && restaurant != null) {
            addMarkerToMap(restaurant);
        }
    }
}
