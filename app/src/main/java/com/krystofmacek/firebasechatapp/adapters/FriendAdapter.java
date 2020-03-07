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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.activity.MainActivity;
import com.krystofmacek.firebasechatapp.activity.MessagingActivity;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{

    private Context context;
    private List<User> profiles;
    private FirebaseUser signedUser;

    public FriendAdapter(Context context, List<User> profiles) {
        this.context = context;
        this.profiles = profiles;
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {

        final User profile = profiles.get(position);
        String username = profile.getDisplayName();
        holder.username.setText(username);

        getLastMessage(holder.lastMessage, profile.getUid());

        holder.itemView.findViewById(R.id.item_friend_btnStartChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagingActivity.class);
                intent.putExtra("userid", profile.getUid());
                context.startActivity(intent);
            }
        });

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
                        FirebaseFirestore.getInstance()
                                .collection("Profiles")
                                .document(signedUser.getUid())
                                .update("friends", FieldValue.arrayRemove(profile.getUid()));
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

    private void getLastMessage(final TextView lastMessageView, final String friendId) {
        // Najdeme odpovidajici Chat
        FirebaseFirestore.getInstance()
                .collection("Chats")
                .whereArrayContains("members", friendId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Chat chat = doc.toObject(Chat.class);
                    if(chat != null) {
                        if(chat.getMembers().get(0).equals(signedUser.getUid()) ||
                                chat.getMembers().get(1).equals(signedUser.getUid())){

                            // Z chatu dostaneme posledni zpravu
                            FirebaseFirestore.getInstance()
                                    .collection("Chats")
                                    .document(chat.getUid())
                                    .collection("Messages").orderBy("timestamp")
                                    .limit(1)
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if(queryDocumentSnapshots.size() > 0){
                                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                        Message lastMessage = doc.toObject(Message.class);
                                        lastMessageView.setText(lastMessage.getMessageText());
                                        if(!lastMessage.getAuthor().equals(signedUser.getUid())) {
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
