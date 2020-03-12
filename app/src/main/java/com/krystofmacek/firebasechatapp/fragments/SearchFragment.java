package com.krystofmacek.firebasechatapp.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ProfileAdapter;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private final static int ONE_MINUTE_INTERVAL = 60000;
    private RecyclerView profileRecycler;
    private TextView locationOutput;
    private Spinner spinner;
    private Button searchUsersBtn;

    private FirebaseUser signedUser;
    private FirestoreService firestoreService;


    //GeoLocation
    private Map<String, String> address;
    private FusedLocationProviderClient locationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // ui
        profileRecycler = view.findViewById(R.id.fSearch_recycler);
        locationOutput = view.findViewById(R.id.fSearch_locationOutput);
        spinner = view.findViewById(R.id.fSearch_locationSpinner);
        searchUsersBtn = view.findViewById(R.id.fSearch_searchUserBtn);

        // firebase obj
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreService = new FirestoreService();

        address = new HashMap<>();

        requestLocation();
        setupLocationSpinner();
        setupSearchUsers();

        return view;
    }

    private void requestLocation() {
        //Kontrola opravneni pro ziskani lokace
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }

        } else {
            // Ziskani lokace
            locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(ONE_MINUTE_INTERVAL);
            locationRequest.setFastestInterval(2000);
            locationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();

                    // V emulatoru vraci 37.421998 -122.084 = Google HQ
                    //  50.208917, 15.831632 = HK
                    if(location.getLatitude() == 37.421998 && location.getLongitude() == -122.084) {
                        location.setLatitude(50.208917);
                        location.setLongitude(15.831632);
                    }

                    Log.i("Location", location.getLatitude() + " + " + location.getLongitude());
                    if(location != null) {
                        extractLocation(location);
                    }
                }
            }, Looper.getMainLooper());

            if(address.size() == 0) {
                locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            extractLocation(location);
                        }
                    }
                });
                if(address.size() == 0)
                // aktualni lokace nebyla dostupna -> nacteme z firebase
                        firestoreService.getSignedUserDocumentRef()
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Map<String, String> fLocation = documentSnapshot.toObject(User.class).getLocation();

                                if(fLocation != null && fLocation.size() > 0) {
                                    address.put("Country", fLocation.get("Country"));
                                    address.put("Region", fLocation.get("Region"));
                                    address.put("City", fLocation.get("City"));
                                }
                            }
                        });
            }
            if(address.size() != 0) {
                locationOutput.setText(address.get("City"));
            } else {
                locationOutput.setText("Couldn\'t get your location");
            }
        }
    }

    private void extractLocation (Location location) {
        Address currentAddress;
        Geocoder geocoder = new Geocoder(getContext(), Locale.ENGLISH);
        try {
            // prevedeni souradnic na adresu
            currentAddress = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1).get(0);


            // naplneni mapy lokaci, daty z adresy
            if(currentAddress!=null) {
                address.put("Country", currentAddress.getCountryName());
                address.put("Region", currentAddress.getAdminArea());
                address.put("City", currentAddress.getSubAdminArea());

                setupLocationSpinner();
            }
            // aktualizace lokace ve firestore
            firestoreService.updateField("Profiles", signedUser.getUid(), "location", address);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupLocationSpinner() {
        // vytvoreni spinneru pro vyber lokace
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.location_options,
                R.layout.support_simple_spinner_dropdown_item
        );
        spinnerAdapter.setDropDownViewResource(
                R.layout.support_simple_spinner_dropdown_item
        );
        //Vychozi select = city
        spinner.setAdapter(spinnerAdapter);

        //Nastaveni vyberu lokaci
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(address!=null && address.size() > 0) {
                        switch (position) {
                            case 0:
                                locationOutput.setText(address.get("City"));
                                break;
                            case 1:
                                locationOutput.setText(address.get("Region"));
                                break;
                            case 2:
                                locationOutput.setText(address.get("Country"));
                                break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
    }


    private void setupSearchUsers() {
        // dle vyberu lokace je zavolana metoda queryProfiles
        searchUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (spinner.getSelectedItemPosition()){
                    case 0:
                        queryProfiles("location.City", locationOutput.getText().toString());
                        break;
                    case 1:
                        queryProfiles("location.Region", locationOutput.getText().toString());
                        break;
                    case 2:
                        queryProfiles("location.Country", locationOutput.getText().toString());
                        break;
                }
            }
        });
    }

    private void queryProfiles(final String locationField, final String userLocation) {

        final List<User> profiles = new ArrayList<>();
        //nacteni uzivatele
        firestoreService
                .getSignedUserDocumentRef()
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final User user = documentSnapshot.toObject(User.class);
                if(user != null && user.getTags().size() > 0) {
                    // nacteni profilu kde je stejna vybrana lokace (city=city / region=region...)
                    firestoreService.searchUsersQuery(locationField, userLocation, user.getTags())
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(DocumentSnapshot profile : queryDocumentSnapshots.getDocuments()) {
                                    User u = profile.toObject(User.class);
                                    // uzivatel je pridan pokud != prihlasenemu a pokud jiz neni jeho friendslistu
                                    if(u!=null && !profile.getId().equals(signedUser.getUid()) &&
                                        !user.getFriends().contains(u.getUid())) {
                                        profiles.add(u);
                                    }
                                }
                                ProfileAdapter adapter = new ProfileAdapter(getContext(), profiles);

                                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                profileRecycler.setLayoutManager(layoutManager);
                                profileRecycler.setAdapter(adapter);
                            }

                        });
                }
            }
        });
    }
}
