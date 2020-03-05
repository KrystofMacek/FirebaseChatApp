package com.krystofmacek.firebasechatapp.fragments;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ChatAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.User;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class HomeFragment extends Fragment {

    private FirebaseFirestore firestore;
    private String signedUserUid;
    private DocumentReference signedUserProfileRef;
    private User signedUser;

    private TextView viewTxtUsername;
    private TextView viewTxtTags;
    private ImageButton viewBtnEditProfile;

    private RecyclerView recyclerRecentChats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // ui elements
        viewTxtUsername = view.findViewById(R.id.fHome_username);
        viewTxtTags = view.findViewById(R.id.fHome_tags);
        viewBtnEditProfile = view.findViewById(R.id.fHome_btnEditProfile);
        recyclerRecentChats = view.findViewById(R.id.fHome_recycler);

        //firebase objects
        firestore = FirebaseFirestore.getInstance();
        signedUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUser();
        setupDialog();
        loadRecentChats();

        return view;
    }

    private void loadRecentChats() {
        final List<Chat> chats = new ArrayList<>();
        firestore.collection("Chats")
                .whereArrayContains("members", signedUserUid)
                .limit(5)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot chat: docs) {
                            chats.add(chat.toObject(Chat.class));
                        }
                        ChatAdapter adapter = new ChatAdapter(getContext(), chats);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerRecentChats.setLayoutManager(layoutManager);
                        recyclerRecentChats.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentChats();
    }

    // Nacteni dat o uzivateli
    private void loadUser() {

        signedUserProfileRef = firestore.collection("Profiles").document(signedUserUid);
        signedUserProfileRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    signedUser = documentSnapshot.toObject(User.class);
                    if(signedUser.getDisplayName().equals("") || signedUser.getDisplayName() == null) {
                        viewTxtUsername.setText("Please setup your profile");
                    } else {
                        viewTxtUsername.setText(signedUser.getDisplayName());
                    }
                    createTagsString(viewTxtTags);
                }
            }
        });
    }

    // nastaveni dialogu pro upravu profilu
    private void setupDialog() {
        viewBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog editProfileDialog = new Dialog(getContext());
                editProfileDialog.setTitle("Edit your profile");
                editProfileDialog.setContentView(R.layout.dialog_edit_profile);

                //Nastaveni velikosti dialogu
                Window window = editProfileDialog.getWindow();
                Point size = new Point();
                Display display = window.getWindowManager().getDefaultDisplay();
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
                if(signedUser == null) {
                    signedUser = new User();
                }
                if(signedUser.getDisplayName() != null){
                    editUsername.setText(signedUser.getDisplayName());
                }
                if(signedUser.getTags() != null) {
                    createTagsString(tagOutput);
                }

                //Clear tag button listener
                clearTags.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signedUser.getTags().clear();
                        tagOutput.setText("");
                    }
                });

                //Add Tag button listeners
                addTagBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(addTagInput.getText().equals("")) {
                        } else {
                            signedUser.getTags().add(addTagInput.getText().toString().toLowerCase());
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
                saveProfileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signedUser.setDisplayName(editUsername.getText().toString());
                        signedUser.setUid(signedUserUid);
                        firestore.collection("Profiles")
                                .document(signedUserUid)
                                .set(signedUser);
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_LONG);
                        editProfileDialog.cancel();
                        viewTxtUsername.setText(signedUser.getDisplayName());
                        createTagsString(viewTxtTags);
                    }
                });
            }
        });

    }
    private void createTagsString(TextView output) {
        output.setText("");
        StringBuilder tagList = new StringBuilder();
        for (String tag : signedUser.getTags()) {
            tagList.append("#").append(tag).append(" ");
        }
        if(tagList.equals("")){
            output.setText("No tags specified");
        } else {
            output.setText(tagList);
        }
    }

}
