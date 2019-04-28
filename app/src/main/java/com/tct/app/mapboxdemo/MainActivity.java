package com.tct.app.mapboxdemo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.search.MapboxSearch;
import com.mapbox.android.search.SearchCallback;
import com.mapbox.android.search.SearchRequest;
import com.mapbox.android.search.SearchResult;
import com.mapbox.android.search.autocomplete.MapboxAutocompleteView;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.camera.NavigationCamera;
import com.mapbox.services.android.navigation.ui.v5.instruction.InstructionView;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.services.android.navigation.v5.utils.LocaleUtils;
import com.tct.app.mapboxdemo.logfile.LogFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationEngineCallback<LocationEngineResult> {
    private static final String TAG = "@MapboxDemo/MainActivity ";
    private static final int CAMERA_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_CAMERA_ZOOM = 16;
    private static final int CHANGE_SETTING_REQUEST_CODE = 1;
    private static final int INITIAL_ZOOM = 16;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    private static final double DEFAULT_ZOOM = 12.0;
    private static final double DEFAULT_BEARING = 0.0;
    private static final double DEFAULT_TILT = 0.0;
    private static final int TWO_SECONDS = 2000;
    private static final int ONE_SECOND = 1000;
    private static final int ZERO_PADDING = 0;
    private static final int BOTTOMSHEET_MULTIPLIER = 4;

    private LocationEngine locationEngine;
    private MapboxMap mMapboxMap;
    private PermissionsManager permissionsManager;

    @BindView(R.id.mapView)
    MapView mapView;
    LocationComponent locationComponent;

    View mAutocompleteBottomSheet;
    MapboxAutocompleteView mAutocompleteView;
    ViewGroup mMainLayout;
    InstructionView instructionView;
    View mLocationFab;
    View mDirectionsFab;
    View mNavigationFab;
    View mCancelFab;
    Location mLocation;
    Point mDestination;
    DirectionsRoute mPrimaryRoute;
    NavigationMapboxMap mNavigationMapboxMap;
    MapboxNavigation mMapboxNavigation;
    private LocationEngineCallback<LocationEngineResult> mLocationEngineCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        setupWith(savedInstanceState);

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(MainActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(MainActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }

        mLocationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Log.w("renfei", "onSuccess: " + result.getLastLocation().toString());
                mLocation = result.getLastLocation();
                mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(result.getLastLocation().getLatitude(),
                                result.getLastLocation().getLongitude()), 12.0));
                locationEngine.removeLocationUpdates(mLocationEngineCallback);
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        };

    }

    private void setupWith(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);

        mMainLayout = (ViewGroup) findViewById(R.id.mainLayout);
        instructionView = (InstructionView) findViewById(R.id.instructionView);
        instructionView.retrieveFeedbackButton().hide();
        instructionView.retrieveSoundButton().hide();

        mAutocompleteBottomSheet = (View) findViewById(R.id.autocompleteBottomSheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(mAutocompleteBottomSheet);
        behavior.setPeekHeight(R.dimen.bottom_sheet_peek_height);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                Log.w("renfei", "onStateChanged: state " + i);
                //updateLocationFabVisibility();
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                Log.w("renfei", "onSlide: ");
            }
        });

        mAutocompleteView = (MapboxAutocompleteView) findViewById(R.id.autocompleteView);
        mAutocompleteView.setAdapter(new ExampleAutocompleteAdapter(this));
        mAutocompleteView.setFeatureClickListener(carmenFeature -> {
            onDestinationFound(carmenFeature);
        });

        mLocationFab = (View) findViewById(R.id.locationFab);
        mLocationFab.setOnClickListener(clickedView -> {
            Log.w("renfei", "onClick: mLocationFab");
            requestLocation();
        });

        mDirectionsFab = (View) findViewById(R.id.directionsFab);
        mDirectionsFab.setOnClickListener(clickedView -> {
            Log.w("renfei", "onClick: mDirectionsFab");
            findRouteToDestination();
        });

        mNavigationFab = (View) findViewById(R.id.navigationFab);
        mNavigationFab.setOnClickListener(clickedView -> {
            Log.w("renfei", "onClick: mNavigationFab " + mPrimaryRoute.toString());
            onNavigationFabClick();
        });

        mCancelFab = (View) findViewById(R.id.cancelFab);
        mCancelFab.setOnClickListener(clickedView -> {
            Log.w("renfei", "onClick: mCancelFab");
            onCancelFabClick();
        });
    }

    private void updateInstructionViewVisibility(int visibility) {
        instructionView.setVisibility(visibility);
    }

    private void updateInstructionViewWith(RouteProgress progress) {
        instructionView.updateDistanceWith(progress);
    }

    private void updateInstructionViewWith(Milestone milestone) {
        instructionView.updateBannerInstructionsWith(milestone);
    }

    private void addMapProgressChangeListener(MapboxNavigation navigation) {
       mNavigationMapboxMap.addProgressChangeListener(navigation);
    }

    private void updateLocationRenderMode(int renderMode) {
        mNavigationMapboxMap.updateLocationLayerRenderMode(renderMode);
    }

    private void updateCameraTrackingMode(int trackingMode) {
        mNavigationMapboxMap.updateCameraTrackingMode(trackingMode);
    }

    private void adjustMapPaddingForNavigation() {
        int mapViewHeight = mapView.getHeight();
        int bottomSheetHeight = (int)(getResources().getDimension(R.dimen.bottom_sheet_peek_height));
        int topPadding = mapViewHeight - bottomSheetHeight * BOTTOMSHEET_MULTIPLIER;
        int[] customPadding = new int[]{ZERO_PADDING, topPadding, ZERO_PADDING, ZERO_PADDING};
        mNavigationMapboxMap.adjustLocationIconWith(customPadding);
    }

    private void removeRoute() {
        mNavigationMapboxMap.removeRoute();
    }

    private void onCancelFabClick() {
        mMapboxNavigation.stopNavigation();
        removeRoute();
        clearMarkers();
        resetMapPadding();
        updateLocationRenderMode(RenderMode.NORMAL);
        updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);
        updateCancelFabVisibility(false);
        updateLocationFabVisibility(true);
        updateInstructionViewVisibility(View.INVISIBLE);
    }

    private void resetMapPadding() {
        int[] zeroPadding = new int[]{ZERO_PADDING, ZERO_PADDING, ZERO_PADDING, ZERO_PADDING};
        mNavigationMapboxMap.adjustLocationIconWith(zeroPadding);
    }

    private void onNavigationFabClick() {
        showAlternativeRoutes(false);
        addMapProgressChangeListener(mMapboxNavigation);
        updateNavigationFabVisibility(false);
        updateCancelFabVisibility(true);
        updateInstructionViewVisibility(View.VISIBLE);
        updateLocationRenderMode(RenderMode.GPS);
        updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
        adjustMapPaddingForNavigation();
        mMapboxNavigation.startNavigation(mPrimaryRoute);
    }

    private void findRouteToDestination() {
        if (mLocation != null && mDestination != null) {
            findRoute(mLocation, mDestination);
        }
    }

    private void findRoute(Location location, Point destination) {
        Point origin = Point.fromLngLat(location.getLongitude(),
                location.getLatitude());
        Double bearing = Double.valueOf(location.getBearing());
        Log.w("renfei", "findRoute: origin "
                + origin.toString()
                + " mDestination " + destination.toString());
        NavigationRoute.builder(this)
                .accessToken(MapboxDemoApplication.ACCESS_TOKEN)
                .origin(origin, bearing, 90.0)
                .destination(destination)
                .profile(DirectionsCriteria.PROFILE_WALKING)//PROFILE_WALKING)//(DirectionsCriteria.PROFILE_CYCLING)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse> () {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call,
                                           Response<DirectionsResponse> response) {
                        //handle(response.body());
                        Log.w("renfei", "onResponse: ");
                        if (response != null
                                && response.body() != null
                                && response.body().routes() != null) {
                            mPrimaryRoute = response.body().routes().get(0);
                            onRouteFound(response.body().routes());
                        } else {
                            updateNavigationFabVisibility(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call,
                                          Throwable throwable) {
                        Log.w("renfei", "onFailure: ");
                    }
                });
    }

    private void onDestinationFound(CarmenFeature carmenFeature) {
        Log.w("renfei", "onDestinationFound: " + carmenFeature.center());
        if (carmenFeature != null && carmenFeature.center() != null) {
            mDestination = carmenFeature.center();
            updateMapCamera(buildCameraUpdateFrom(mDestination), TWO_SECONDS);
            updateLocationFabVisibility(false);
            updateDirectionFabVisibility(true);
        }
    }

    private void clearMarkers() {
        mNavigationMapboxMap.clearMarkers();
    }

    private void updateDestinationMarker(Point destination) {
        mNavigationMapboxMap.addDestinationMarker(destination);
    }

    private void updateMapCamera(CameraUpdate cameraUpdate, int duration) {
        mNavigationMapboxMap.retrieveMap().animateCamera(cameraUpdate, duration);
    }

    private CameraUpdate buildCameraUpdateFrom(Point point) {
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .zoom(DEFAULT_ZOOM)
                .target(new LatLng(point.latitude(), point.longitude()))
                .bearing(DEFAULT_BEARING)
                .tilt(DEFAULT_TILT)
                .build());
    }

    private void transition() {
        TransitionManager.beginDelayedTransition(mMainLayout);
    }

    private void showAlternativeRoutes(Boolean alternativesVisible) {
        mNavigationMapboxMap.showAlternativeRoutes(alternativesVisible);
    }

    private void updateRoutes(List<DirectionsRoute> routes) {
        mNavigationMapboxMap.drawRoutes(routes);
    }

    private void updateMapCameraFor(LatLngBounds bounds, int[] padding, int duration) {
        if (mNavigationMapboxMap != null && mNavigationMapboxMap.retrieveMap() != null) {
            CameraPosition position = mMapboxMap.getCameraForLatLngBounds(bounds, padding);
            if (position != null) {
                mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),
                        duration);
            }
        }
    }

    private void moveCameraToInclude(Point destination) {
        LatLng origin = new LatLng(mLocation);
        LatLngBounds bounds = new LatLngBounds.Builder().include(origin)
                .include(new LatLng(destination.latitude(), destination.longitude())).build();

        int left = (int)(this.getResources()
                .getDimension(R.dimen.route_overview_padding_left));
        int top = (int)(this.getResources().
                getDimension(R.dimen.route_overview_padding_top));
        int right = (int)(this.getResources().
                getDimension(R.dimen.route_overview_padding_right));
        int bottom = (int)(this.getResources().
                getDimension(R.dimen.route_overview_padding_bottom));
        updateMapCameraFor(bounds, new int[]{left, top, right, bottom}, 2);
    }

    private void onRouteFound(List<DirectionsRoute> routes) {
        if (routes != null) {
           transition();
           showAlternativeRoutes(true);
           updateRoutes(routes);
           updateDirectionFabVisibility(false);
           updateNavigationFabVisibility(true);
           moveCameraToInclude(mDestination);
        }
    }

    private void updateLocationFabVisibility(boolean visible) {
        Log.w("renfei", "updateLocationFabVisibility: " + visible);
        mLocationFab.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateDirectionFabVisibility(boolean visible) {
        Log.w("renfei", "updateDirectionFabVisibility: " + visible);
        mDirectionsFab.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateNavigationFabVisibility(boolean visible) {
        Log.w("renfei", "updateNavigationFabVisibility: " + visible);
        mNavigationFab.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateCancelFabVisibility(boolean visible) {
        Log.w("renfei", "updateCancelFabVisibility: " + visible);
        mCancelFab.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void searchDest(String dest) {
        MapboxSearch.search(new SearchRequest(dest),
                new SearchCallback() {
                    @Override
                    public void onResponse(SearchResult searchResult) {
                        Log.w("renfei", "onResponse: " + searchResult.getResponse());
                        updateNavigationFabVisibility(!TextUtils.isEmpty(searchResult.getResponse().toString()));
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.w("renfei", "onFailure: " + throwable);
                    }
                });
    }

    protected String loadJsonFromAsset(String filename) {
        try {
            InputStream is = this.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    private void myLocation() {
        LogFile.putMsg(TAG + "getLastLocation");
        locationComponent.getLocationEngine().getLastLocation(this);
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        Log.w("renfei", "requestLocation: ");
        locationEngine.requestLocationUpdates(buildEngineRequest(),
                mLocationEngineCallback, null);
    }

    private LocationEngineRequest buildEngineRequest() {
        return new LocationEngineRequest.Builder(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .build();
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(result.getLastLocation().getLatitude(),
                        result.getLastLocation().getLongitude()), 12.0));
    }

    public void onFailure(Exception e) {
        //noop
    }

    private void myNavigation() {
        boolean simulateRoute = true;
        DirectionsRoute testRoute = DirectionsRoute.fromJson(loadJsonFromAsset("lancaster-1.json"));

// Create a NavigationLauncherOptions object to package everything together
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .directionsRoute(testRoute)
                .shouldSimulateRoute(simulateRoute)
                .build();
// Call this method with Context from within an Activity
        NavigationLauncher.startNavigation(this, options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        LogFile.putMsg(TAG + "onMapReady");
        mMapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUrl(getString(R.string.navigation_guidance_day)),//Style.MAPBOX_STREETS,
                style -> activateLocationComponent(style));
    }

    @SuppressLint("MissingPermission")
    private void activateLocationComponent(@NonNull Style style) {
        locationComponent = mMapboxMap.getLocationComponent();
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        mNavigationMapboxMap = new NavigationMapboxMap(mapView, mMapboxMap);
        mMapboxNavigation = new MapboxNavigation(this, MapboxDemoApplication.ACCESS_TOKEN);
        mMapboxNavigation.setLocationEngine(locationEngine);
        LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this)
                .elevation(5)
                .accuracyAlpha(.6f)
                .accuracyColor(Color.GREEN)
                .foregroundDrawable(R.drawable.mapbox_logo_helmet)
                .build();

        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(this, style)
                .locationComponentOptions(locationComponentOptions)
                .useDefaultLocationEngine(true)
                .build();

        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.NORMAL);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        LogFile.putMsg(TAG + "activateLocationComponent");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // activity uses singleInstance for testing purposes
                // code below provides a default navigation when using the app
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // activity uses singleInstance for testing purposes
        // code below provides a default navigation when using the app
        finish();
    }
}
