package com.example.abbieturner.restaurantsfinder.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abbieturner.restaurantsfinder.API.API;
import com.example.abbieturner.restaurantsfinder.Adapters.CuisineJsonAdapter;
import com.example.abbieturner.restaurantsfinder.Adapters.EmptyRecyclerView;
import com.example.abbieturner.restaurantsfinder.Adapters.FavouriteAdapter;
import com.example.abbieturner.restaurantsfinder.Adapters.ModelConverter;
import com.example.abbieturner.restaurantsfinder.Adapters.PopularRestaurantsAdapter;
import com.example.abbieturner.restaurantsfinder.Adapters.RecommendedAdapter;
import com.example.abbieturner.restaurantsfinder.Data.Cuisine;
import com.example.abbieturner.restaurantsfinder.Data.Cuisines;
import com.example.abbieturner.restaurantsfinder.Data.CuisinesSingleton;
import com.example.abbieturner.restaurantsfinder.Data.Restaurant;
import com.example.abbieturner.restaurantsfinder.Database.AppDatabase;
import com.example.abbieturner.restaurantsfinder.Dialogs.GetLocationDialog;
import com.example.abbieturner.restaurantsfinder.FirebaseAccess.Listeners.RecommendedRestaurantsListener;
import com.example.abbieturner.restaurantsfinder.FirebaseAccess.Listeners.UserListener;
import com.example.abbieturner.restaurantsfinder.FirebaseAccess.PopularRestaurants;
import com.example.abbieturner.restaurantsfinder.FirebaseAccess.RecommendedRestaurants;
import com.example.abbieturner.restaurantsfinder.FirebaseAccess.User;
import com.example.abbieturner.restaurantsfinder.FirebaseModels.Friend;
import com.example.abbieturner.restaurantsfinder.FirebaseModels.PopularRestaurant;
import com.example.abbieturner.restaurantsfinder.FirebaseModels.RecommendedRestaurant;
import com.example.abbieturner.restaurantsfinder.FirebaseModels.UserFirebaseModel;
import com.example.abbieturner.restaurantsfinder.R;
import com.example.abbieturner.restaurantsfinder.Singletons.DeviceLocation;
import com.example.abbieturner.restaurantsfinder.Singletons.LocationSharedPreferences;
import com.example.abbieturner.restaurantsfinder.StartSnapHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeActivity extends BaseActivity
        implements
        FavouriteAdapter.RestaurantItemClick,
        NavigationView.OnNavigationItemSelectedListener,
        PopularRestaurants.PopularRestaurantsListener,
        PopularRestaurantsAdapter.RestaurantItemClick,
        SharedPreferences.OnSharedPreferenceChangeListener,
        UserListener,
        RecommendedRestaurantsListener,
        RecommendedAdapter.RecommendRestaurantClick {

    @BindView(R.id.home_popular_recycler_view)
    EmptyRecyclerView popularRecyclerView;
    @BindView(R.id.home_favourites_recycler_view)
    EmptyRecyclerView favouritesRecyclerView;
    @BindView(R.id.btn_all_cuisines)
    ImageView allCuisines;
    @BindView(R.id.btn_manage_favourites)
    TextView btnManageFavourites;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.autocomplete_cuisines)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.btn_clear)
    Button btnClear;
    @BindView(R.id.pb_popular_restaurants)
    ProgressBar pbPopularRestaurants;
    @BindView(R.id.pb_load_cuisines)
    ProgressBar pbLoadCuisines;


    @BindView(R.id.home_recommended_recycler_view)
    EmptyRecyclerView recommendedRecyclerView;
    @BindView(R.id.recommended_empty_view)
    LinearLayout recommendedEmptyView;
    @BindView(R.id.pb_recommended_restaurants)
    ProgressBar pbRecommendedRestaurants;


    private List<Restaurant> favoritesRestaurants;
    private FavouriteAdapter favouriteAdapter;
    private PopularRestaurantsAdapter popularAdapter;
    private LinearLayoutManager favouriteLayoutManager, popularLayoutManager, recommendedLayoutManager;
    private ModelConverter converter;
    private AppDatabase database;
    private PopularRestaurants popularRestaurantsDataAccess;
    private String TAG_RESTAURANT_ID, SHARED_PREFERENCES_DEFAULT_LOCATION, BASE_URL, TAG_USER_ID, TAG_IS_FIREBASE_RESTAURANT;
    private DeviceLocation locationSingleton;
    private API.ZomatoApiCalls service;
    private GetLocationDialog getLocationDialog;
    private Gson gson;
    private Retrofit retrofit;
    private FirebaseAuth mAuth;
    private LocationSharedPreferences locationSharedPreferences;
    private SharedPreferences mPrefs;
    private FirebaseUser currentUser;
    private User userDataAccess;
    private String location_shared_preferences_name;
    private RecommendedRestaurants recommendedRestaurantsDataAccess;
    private RecommendedAdapter recommendedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_bar_home);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        createNewInstances();
        setUpNavigationDrawer();
        favoritesRestaurants = getFavouriteRestaurants();

        setUpProfile();
        setUpPopularRecyclerView();
        setUpFavouritesRecyclerView();
        setUpRecommendedRecyclerView();
        setUpOnClickListeners();
        getLocationsFromSharedPreferences();
    }

    private void createNewInstances() {
        location_shared_preferences_name = getResources().getString(R.string.location_shared_preferences_name);
        userDataAccess = new User(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = AppDatabase.getInstance(this);
        converter = ModelConverter.getInstance();
        TAG_RESTAURANT_ID = getResources().getString(R.string.TAG_RESTAURANT_ID);
        SHARED_PREFERENCES_DEFAULT_LOCATION = getResources().getString(R.string.SHARED_PREFERENCES_DEFAULT_LOCATION);
        TAG_IS_FIREBASE_RESTAURANT = getResources().getString(R.string.TAG_IS_FIREBASE_RESTAURANT);
        TAG_USER_ID = getResources().getString(R.string.TAG_USER_ID);
        popularRestaurantsDataAccess = new PopularRestaurants(this);
        locationSingleton = DeviceLocation.getInstance();
        getLocationDialog = new GetLocationDialog(this, false);
        gson = new GsonBuilder()
                .registerTypeAdapter(Cuisine.class, new CuisineJsonAdapter())
                .create();
        BASE_URL = getResources().getString(R.string.BASE_URL_API);
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(API.ZomatoApiCalls.class);
        mPrefs = this.getSharedPreferences(location_shared_preferences_name, Context.MODE_PRIVATE);
        locationSharedPreferences = LocationSharedPreferences.getInstance();
        recommendedRestaurantsDataAccess = new RecommendedRestaurants(this);
    }


    private void setUpNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpOnClickListeners() {
        allCuisines.setOnClickListener(allCuisinesOnClickListener);
        btnClear.setOnClickListener(btnClearOnClickListener);
        btnManageFavourites.setOnClickListener(btnManageFavouritesOnClickListener);
    }

    private List<Restaurant> getFavouriteRestaurants() {
        return converter.convertToRestaurants(database.restaurantsDAO().getRestaurants());
    }

    private void setUpRecommendedRecyclerView(){
        recommendedLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recommendedAdapter = new RecommendedAdapter(this);
        recommendedAdapter.setList(null);
        recommendedRecyclerView.setLayoutManager(recommendedLayoutManager);

        recommendedRecyclerView.setEmptyView(recommendedEmptyView);
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        SnapHelper favouriteSnapHelper = new StartSnapHelper();
        favouriteSnapHelper.attachToRecyclerView(recommendedRecyclerView);
    }

    private void setUpFavouritesRecyclerView() {
        favouriteLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        favouriteAdapter = new FavouriteAdapter(this, this, R.layout.favourite_restaurant_item);
        favouriteAdapter.setCuisineList(favoritesRestaurants);
        favouritesRecyclerView.setLayoutManager(favouriteLayoutManager);

        View favouritesEmptyView = findViewById(R.id.favourites_empty_view);
        favouritesRecyclerView.setEmptyView(favouritesEmptyView);
        favouritesRecyclerView.setAdapter(favouriteAdapter);

        SnapHelper favouriteSnapHelper = new StartSnapHelper();
        favouriteSnapHelper.attachToRecyclerView(favouritesRecyclerView);
    }

    private void setUpPopularRecyclerView() {
        popularLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        popularAdapter = new PopularRestaurantsAdapter(this);
        popularAdapter.setList(new ArrayList<PopularRestaurant>());
        popularRecyclerView.setLayoutManager(popularLayoutManager);

        View popularEmptyView = findViewById(R.id.popular_empty_view);
        popularRecyclerView.setEmptyView(popularEmptyView);
        popularRecyclerView.setAdapter(popularAdapter);
        SnapHelper popularSnapHelper = new StartSnapHelper();
        popularSnapHelper.attachToRecyclerView(popularRecyclerView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    Log.d("focus", "touchevent");
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onRestaurantItemClick(Restaurant restaurant) {
        Gson gS = new Gson();
        String jsonRestaurant = gS.toJson(restaurant);

        Intent intent = new Intent(HomeActivity.this, RestaurantActivity.class);
        intent.putExtra(getResources().getString(R.string.TAG_RESTAURANT), jsonRestaurant);
        startActivity(intent);
    }

    private void setUpAutocomplete(List<Cuisine> cuisineList) {
        ArrayAdapter<Cuisine> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cuisineList);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Cuisine selected = (Cuisine) arg0.getAdapter().getItem(arg2);

                Intent intent = new Intent(HomeActivity.this, RestaurantsActivity.class);
                intent.putExtra("cuisine_id", selected.getCuisine_id());
                intent.putExtra(getResources().getString(R.string.TAG_CUISINE_NAME), selected.getCuisine_name());
                startActivity(intent);
            }
        });
    }

    private View.OnClickListener btnManageFavouritesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomeActivity.this, FavouritesActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        favoritesRestaurants = getFavouriteRestaurants();
        favouriteAdapter.setCuisineList(favoritesRestaurants);
        getPopularRestaurants();
        getRecommendedRestaurants();

        if (isDeviceLocationSet()) {
            pbLoadCuisines.setVisibility(View.VISIBLE);
            fetchCuisines();
        } else if (hasUserDefaultLocationSet()) {
            useDefaultLocations();
            pbLoadCuisines.setVisibility(View.VISIBLE);
            fetchCuisines();
        } else {
            getLocationFromUser();
        }
    }

    private void useDefaultLocations() {
        locationSingleton.setLocation(locationSharedPreferences.getUsersLocation());
    }

    private boolean hasUserDefaultLocationSet() {
        return isUserLoggedIn() && locationSharedPreferences.userHasLocationsSet();
    }


    private boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void fetchCuisines() {

        service.getCuisineId("332", String.valueOf(locationSingleton.getLocation().latitude),
                String.valueOf(locationSingleton.getLocation().longitude))
                .enqueue(new Callback<Cuisines>() {
                    @Override
                    public void onResponse(Call<Cuisines> call, Response<Cuisines> response) {
                        assert response.body() != null;
                        CuisinesSingleton.getInstance().setCuisines(response.body().cuisinesList);
                        setUpAutocomplete(CuisinesSingleton.getInstance().getCuisines());
                        pbLoadCuisines.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<Cuisines> call, Throwable t) {
                        t.printStackTrace();
                        pbLoadCuisines.setVisibility(View.GONE);
                    }
                });
    }

    private void getLocationFromUser() {
        getLocationDialog.showDialog();
    }

    private boolean isDeviceLocationSet() {
        return locationSingleton.isLocationSet();
    }

    private void getPopularRestaurants() {
        if (isNetworkAvailable()) {
            pbPopularRestaurants.setVisibility(View.VISIBLE);
            popularRestaurantsDataAccess.getPopularRestaurants();
        }
    }

    private void getRecommendedRestaurants(){
        if(isNetworkAvailable() && isUserLoggedIn()){
            pbRecommendedRestaurants.setVisibility(View.VISIBLE);
            recommendedRestaurantsDataAccess.getRecommendedRestaurants(mAuth.getUid());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_create_restaurant) {
            Intent intent = new Intent(HomeActivity.this, CreateRestaurantActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_fave) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Hey check out this cool restaurant finder app!";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Restaurant Finder!");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_contact) {
            new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                    .setTopColorRes(R.color.design_default_color_primary)
                    .setButtonsColorRes(R.color.white)
                    .setIcon(R.drawable.phone_black_24dp)
                    .setTitle("Select a contact method.")
                    .setMessage("How do you wish to contact us?")
                    .setButtonsColor(getResources().getColor(R.color.colorPrimary))
                    .setPositiveButton("Email", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", "info@restaurantfinder.com", null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                            startActivity(Intent.createChooser(emailIntent, "Send us an Email"));

                        }
                    })
                    .setNegativeButton("Phone Us", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:01145627382"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (id == R.id.nav_loginout) {
            if (mAuth != null) {
                clearSharedPreferences();
                finish();
                mAuth.signOut();
                AuthUI.getInstance().signOut(getApplicationContext());
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Not Logged In.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_profile){
            if(currentUser != null){
                Intent intent = new Intent(HomeActivity.this, Profile.class);
                intent.putExtra(TAG_USER_ID, mAuth.getCurrentUser().getUid());
                startActivity(intent);
            }else{
                Toast.makeText(HomeActivity.this, "Login required!", Toast.LENGTH_LONG).show();
            }
        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private View.OnClickListener allCuisinesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HomeActivity.this, CuisineActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener btnClearOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            autoCompleteTextView.setText("");
        }
    };

    @Override
    public void onRestaurantsLoaded(List<PopularRestaurant> list, boolean hasFailed) {
        pbPopularRestaurants.setVisibility(View.GONE);
        if (hasFailed) {
            String s = null;
        } else {
            popularAdapter.setList(list);
        }
    }

    @Override
    public void onRestaurantUpdated() {

    }

    @Override
    public void onRestaurantItemClick(PopularRestaurant restaurant) {
        Intent intent = new Intent(HomeActivity.this, RestaurantActivity.class);
        intent.putExtra(TAG_RESTAURANT_ID, restaurant.getRestaurantId());
        intent.putExtra(TAG_IS_FIREBASE_RESTAURANT, isFirebaseRestaurantId(restaurant.getRestaurantId()));
        startActivity(intent);
    }

    private boolean isFirebaseRestaurantId(String restaurantId){
        final int uuidLength = 36;
        return restaurantId.length() == uuidLength;
    }

    public void locationSetFromUser(LatLng location) {

        saveLocationToSharedPreferences(location);

        if (isDeviceLocationSet()) {
            pbLoadCuisines.setVisibility(View.VISIBLE);
            fetchCuisines();
        }
    }

    private void saveLocationToSharedPreferences(LatLng location){
        locationSharedPreferences.setLocation(location);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        if (locationSharedPreferences.userHasLocationsSet()) {
            String json = gson.toJson(locationSharedPreferences.getUsersLocation());
            prefsEditor.putString(getLocationPreferencesKey(), json);
            prefsEditor.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getLocationsFromSharedPreferences() {
        String key = getLocationPreferencesKey();
        String json = mPrefs.getString(key, "");
        Gson localGson = new Gson();
        LatLng defaultLocation = localGson.fromJson(json, LatLng.class);

        if (defaultLocation != null) {
            locationSharedPreferences.setLocation(defaultLocation);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String getLocationPreferencesKey() {
        return mAuth.getUid() + SHARED_PREFERENCES_DEFAULT_LOCATION;
    }

    private void clearSharedPreferences() {
        locationSharedPreferences.setLocation(null);
        locationSingleton.setLocation(null);
    }

    private void setUpProfile(){
        if(currentUser != null){
            userDataAccess.createProfileIfDoesNotExist(currentUser.getUid());
        }
    }

    @Override
    public void OnUserLoaded(UserFirebaseModel user, boolean hasFailed) {

    }

    @Override
    public void OnUserUpdated(boolean hasFailed) {

    }

    @Override
    public void OnUsersLoaded(List<Friend> users, boolean hasFailed) {

    }

    @Override
    public void OnAddRecommendedRestaurantCompleted(boolean hasFailed) {

    }

    @Override
    public void OnGetRecommendedRestaurantsCompleted(List<RecommendedRestaurant> restaurants, boolean hasFailed) {
        pbRecommendedRestaurants.setVisibility(View.GONE);
        if(hasFailed){
            Toast.makeText(this, "Failed to load recommended restaurants", Toast.LENGTH_LONG).show();
        }else{
            recommendedAdapter.setList(restaurants);
        }
    }

    @Override
    public void onRecommendRestaurantClick(RecommendedRestaurant restaurant) {
        Intent intent = new Intent(HomeActivity.this, RestaurantActivity.class);
        intent.putExtra(TAG_RESTAURANT_ID, restaurant.getRestaurantId());
        intent.putExtra(TAG_IS_FIREBASE_RESTAURANT, isFirebaseRestaurantId(restaurant.getRestaurantId()));
        startActivity(intent);
    }
}