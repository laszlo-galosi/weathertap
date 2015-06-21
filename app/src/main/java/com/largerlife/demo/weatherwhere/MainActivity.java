package com.largerlife.demo.weatherwhere;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.CancelableCallback {

    @InjectView(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;
    @InjectView(R.id.fab) FloatingActionButton mFabMyLocation;
    private boolean mMapReady = false;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapReady = true;
        mMap = googleMap;
        LatLng targetBudapest = new LatLng(47.4812134, 19.1303031);
        CameraPosition target = CameraPosition.builder().target(targetBudapest).zoom(10).build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));

//        MarkerOptions markerHome = new MarkerOptions()
//                .position(new LatLng(47.4948, 19.0348))
//                .title("Home")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.weather_cloudy));
//        mMap.addMarker(markerHome);
        onFinish();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 5000, this);
    }

    private void makeSnackBar(int messageResId, int actionResId) {
        Snackbar.make(mCoordinatorLayout,
                getString(messageResId), Snackbar.LENGTH_LONG)
                .setAction(getString(actionResId), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: snackbar action.
                    }
                }).show();
    }

    @Override
    public void onFinish() {
        makeSnackBar(R.string.snack_tap_place, R.string.snack_action_ok);
    }

    @Override
    public void onCancel() {
        makeSnackBar(R.string.snack_tap_place, R.string.snack_action_ok);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
