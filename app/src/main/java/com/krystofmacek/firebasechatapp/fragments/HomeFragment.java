package com.krystofmacek.firebasechatapp.fragments;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ChatAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private FirebaseUser signedUser;
    private User signedUserObject;
    private FirestoreService firestoreService;

    private TextView viewTxtUsername;
    private TextView viewTxtTags;
    private ImageButton viewBtnEditProfile;

    private RecyclerView recyclerRecentChats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // inicializace ui elemntu
        viewTxtUsername = view.findViewById(R.id.fHome_username);
        viewTxtTags = view.findViewById(R.id.fHome_tags);
        viewBtnEditProfile = view.findViewById(R.id.fHome_btnEditProfile);
        recyclerRecentChats = view.findViewById(R.id.fHome_recycler);

        //firebase objekty
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreService = new FirestoreService();

        loadUser();
        setupDialog();
        loadRecentChats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentChats();
    }

    // Nacteni dat o uzivateli
    private void loadUser() {
        firestoreService.getSignedUserDocumentRef()
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Log.i("First login", "Exists not first login");
                    signedUserObject = documentSnapshot.toObject(User.class);
                    // pokud nema uzivatel nastaveny profil
                    if(signedUserObject.getDisplayName() == null || signedUserObject.getDisplayName().equals("")) {
                        viewTxtUsername.setText("Please setup your profile");
                    } else {
                        viewTxtUsername.setText(signedUserObject.getDisplayName());
                    }
                    createTagsString(viewTxtTags);
                } else {
                    firestoreService.getSignedUserDocumentRef().set(new User());
                    viewTxtUsername.setText("Please setup your profile");
                }
            }
        });
    }

    private void loadRecentChats() {
        final List<Chat> chats = new ArrayList<>();
        firestoreService.queryByArrayContains("Chats", "members", signedUser.getUid())
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        chats.clear();
                        if(queryDocumentSnapshots != null) {
                            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot c: docs) {
                                Chat chat = c.toObject(Chat.class);
                                if(chat != null && chat.getLastMessageTime() != null) {
                                    chats.add(chat);
                                }
                            }
                            ChatAdapter adapter = new ChatAdapter(getContext(), chats);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            recyclerRecentChats.setLayoutManager(layoutManager);
                            recyclerRecentChats.setAdapter(adapter);
                        }
                    }
                });
    }

    // nastaveni dialogu pro upravu profilu
    private void setupDialog() {
        viewBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // vytvoreni dialogu
                final Dialog editProfileDialog = new Dialog(Objects.requireNonNull(getContext()));
                editProfileDialog.setTitle("Edit your profile");
                editProfileDialog.setContentView(R.layout.dialog_edit_profile);

                //Nastaveni velikosti dialogu
                Window window = editProfileDialog.getWindow();
                Point size = new Point();
                Display display = Objects.requireNonNull(window).getWindowManager().getDefaultDisplay();
                display.getSize(size);
                int width = size.x;
                window.setLayout((int) (width * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);

                editProfileDialog.show();

                //dialog view elementy
                final Button cancelBtn = editProfileDialog.findViewById(R.id.dialog_btnCancel);
                final Button addTagBtn = editProfileDialog.findViewById(R.id.dialog_btnAddTag);
                final Button saveProfileBtn = editProfileDialog.findViewById(R.id.dialog_btnSave);
                final Button clearTags = editProfileDialog.findViewById(R.id.dialog_btnClearTag);

                final EditText editUsername = editProfileDialog.findViewById(R.id.dialog_username);
                final EditText addTagInput = editProfileDialog.findViewById(R.id.dialog_taginput);
                final TextView tagOutput = editProfileDialog.findViewById(R.id.dialog_tagList);

                //naplneni view elementu
                if(signedUserObject == null) {
                    signedUserObject = new User();
                }
                if(signedUserObject.getDisplayName() != null){
                    editUsername.setText(signedUserObject.getDisplayName());
                }
                if(signedUserObject.getTags() != null) {
                    createTagsString(tagOutput);
                }

                //Clear tag button listener - smazani tagu
                clearTags.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signedUserObject.getTags().clear();
                        tagOutput.setText("");
                    }
                });

                //Add Tag button listeners - pridani tagu
                addTagBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!addTagInput.getText().toString().equals("")) {
                            signedUserObject.getTags().add(addTagInput.getText().toString().toLowerCase().replaceAll("\\s",""));
                            addTagInput.setText("");
                            createTagsString(tagOutput);
                        }
                    }
                });

                //Cancel button listener
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editProfileDialog.cancel();
                    }
                });

                //Save button listener
                // aktualizace dokumentu profilu ve firestore
                saveProfileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signedUserObject.setDisplayName(editUsername.getText().toString());
                        signedUserObject.setUid(signedUser.getUid());
                        firestoreService.getSignedUserDocumentRef().set(signedUserObject);
                        setRegistrationToken();

                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_LONG).show();
                        editProfileDialog.cancel();
                        viewTxtUsername.setText(signedUserObject.getDisplayName());
                        createTagsString(viewTxtTags);
                    }
                });
            }
        });
    }
    private void setRegistrationToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Token", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();
                        firestoreService
                                .updateField("Profiles", signedUser.getUid(),"registrationToken", token);

                        Log.w("Token", "getToken Success : " + token);

                    }
                });
    }

    // metoda pro vypsani jednotlivych tagu do textView elementu
    private void createTagsString(TextView output) {
        output.setText("");
        StringBuilder tagList = new StringBuilder();
        for (String tag : signedUserObject.getTags()) {
            tagList.append("#").append(tag).append(" ");
        }
        if(tagList.toString().equals("")){
            output.setText("No tags specified");
        } else {
            output.setText(tagList);
        }
    }

}
