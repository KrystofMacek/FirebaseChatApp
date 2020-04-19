package com.krystofmacek.firebasechatapp.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.activity.MessagingActivity;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.List;

// Vytvoreni polozky seznamu pratel
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{

    private Context context;
    private List<User> profiles;
    private DocumentReference signedUser;
    private FirestoreService firestoreService;

    public FriendAdapter(Context context, List<User> profiles) {
        this.context = context;
        this.profiles = profiles;
        firestoreService = new FirestoreService();
        signedUser = firestoreService.getSignedUserDocumentRef();
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView lastMessage;
        TextView username;
        ImageButton item_chat_btnStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            lastMessage = itemView.findViewById(R.id.item_friend_lastMessage);
            username = itemView.findViewById(R.id.item_friend_username);
            item_chat_btnStartChat = itemView.findViewById(R.id.item_friend_btnStartChat);

        }
    }

    // naplneni elementu ui
    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {

        final User profile = profiles.get(position);
        String username = profile.getDisplayName();
        holder.username.setText(username);

        getLastMessage(holder.lastMessage, profile.getUid());

        //tlacitko spusteni chatu
        holder.itemView.findViewById(R.id.item_friend_btnStartChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra("userid", profile.getUid());
                context.startActivity(intent);
            }
        });

        // odstraneni uzivatele z pratel
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog removeFriendDialog = new Dialog(context);
                removeFriendDialog.setContentView(R.layout.dialog_remove_friend);
                removeFriendDialog.show();

                Button remove = removeFriendDialog.findViewById(R.id.dialog_friend_remove);
                Button cancel = removeFriendDialog.findViewById(R.id.dialog_friend_remove_cancel);

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        firestoreService.updateField("Profiles",signedUser.getId(), "friends", FieldValue.arrayRemove(profile.getUid()));
                        removeFriendDialog.cancel();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeFriendDialog.cancel();
                    }
                });

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


    // nacteni poslední zpravy
    private void getLastMessage(final TextView lastMessageView, final String friendId) {
        // Najdeme odpovidajici Chat
        firestoreService.queryByArrayContains("Chats", "members", friendId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Chat chat = doc.toObject(Chat.class);
                    if(chat != null) {
                        if(chat.getMembers().get(0).equals(signedUser.getId()) ||
                                chat.getMembers().get(1).equals(signedUser.getId())){
                            // Z chatu dostaneme posledni zpravu
                            firestoreService.queryForLastMessage(chat.getUid())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if(queryDocumentSnapshots.size() > 0){
                                                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                                Message lastMessage = doc.toObject(Message.class);
                                                lastMessageView.setText(lastMessage.getMessageText());
                                                if(!lastMessage.getAuthorId().equals(signedUser.getId())) {
                                                    lastMessageView
                                                            .setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                                                }
                                            } else {
                                                lastMessageView.setText("No Messages");
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }
}
