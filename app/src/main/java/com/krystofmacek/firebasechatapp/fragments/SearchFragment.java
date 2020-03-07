package com.krystofmacek.firebasechatapp.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ProfileAdapter;
import com.krystofmacek.firebasechatapp.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchFragment extends Fragment {


    private RecyclerView profileRecycler;
    private TextView locationOutput;
    private Spinner spinner;
    private Button searchUsersBtn;

    private FirebaseFirestore firestore;
    private DocumentReference currentProfileRef;
    private FirebaseUser signedUser;

    private CollectionReference profilesCollectionRef;

    //GeoLocation
    private Map<String, String> address = new HashMap<>();
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
        firestore = FirebaseFirestore.getInstance();
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        // references
        profilesCollectionRef = firestore.collection("Profiles");
        currentProfileRef = profilesCollectionRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid());

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
            locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Address currentAddress;
                    if(location != null) {
                        Geocoder geocoder = new Geocoder(getContext());
                        try {
                            // prevedeni souradnic na adresu
                            currentAddress = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1
                            ).get(0);

                            // naplneni mapy lokaci, daty z adresy
                            address.put("Country", currentAddress.getCountryName());
                            address.put("Region", currentAddress.getAdminArea());
                            address.put("City", currentAddress.getSubAdminArea());

                            // aktualizace lokace ve firestore
                            currentProfileRef.update("location", address);

                            locationOutput.setText(address.get("City"));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // aktualni lokace nebyla pristupna -> nacteme z firebase
                        firestore.collection("Profiles")
                                .document(signedUser.getUid())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                address = documentSnapshot.toObject(User.class).getLocation();
                                locationOutput.setText(address.get("City"));
                            }
                        });

                        // pokud nemame adresu ve firebase
                        if(locationOutput.getText().toString().equals("")) {
                            locationOutput.setText("Couldn\'t get your location");
                        }
                    }
                }
            });
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
                        queryProfiles("location.City");
                        break;
                    case 1:
                        queryProfiles("location.Region");
                        break;
                    case 2:
                        queryProfiles("location.Country");
                        break;
                }
            }
        });
    }

    private void queryProfiles(final String locationField) {

        final List<User> profiles = new ArrayList<>();
        //nacteni uzivatele
        currentProfileRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final User user = documentSnapshot.toObject(User.class);
                if(user != null && user.getTags() != null) {
                    // nacteni profilu kde je stejna vybrana lokace (city=city / region=region...)
                    FirebaseFirestore.getInstance()
                            .collection("Profiles")
                            .whereEqualTo(locationField, locationOutput.getText().toString())
                            .whereArrayContainsAny("tags", user.getTags())
                            .limit(50)
                            .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(DocumentSnapshot profile : queryDocumentSnapshots.getDocuments()) {
                                    User u = profile.toObject(User.class);
                                    // uzivatel je pridan pokud != prihlasenemu a pokud jiz neni jeho friendslistu
                                    if(!profile.getId().equals(signedUser.getUid()) &&
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
