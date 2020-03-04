package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.FriendAdapter;
import com.krystofmacek.firebasechatapp.adapters.ProfileAdapter;
import com.krystofmacek.firebasechatapp.model.User;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FriendsFragment extends Fragment {

    private RecyclerView recyclerFriends;
    private CollectionReference profilesCollectionRef;
    private DocumentReference currentUserProfileRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerFriends = view.findViewById(R.id.fFriends_recycler);

        profilesCollectionRef =
                FirebaseFirestore
                        .getInstance()
                        .collection("Profiles");

        currentUserProfileRef =
                FirebaseFirestore
                        .getInstance()
                        .collection("Profiles")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        loadFriendsList();

        return view;
    }

    private void loadFriendsList() {

        final List<User> friendsList = new ArrayList<>();

        currentUserProfileRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if(user != null) {
                            List<String> ids = user.getFriends();
                            for(String id : ids) {
                                profilesCollectionRef
                                        .document(id).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        friendsList.add(documentSnapshot.toObject(User.class));
                                    }
                                });
                            }

                            FriendAdapter adapter = new FriendAdapter(getContext(), friendsList);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            recyclerFriends.setLayoutManager(layoutManager);
                            recyclerFriends.setAdapter(adapter);
                        }
                    }
                });
    }

}
