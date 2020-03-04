package com.krystofmacek.firebasechatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.io.Resources;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private Context context;
    private List<Chat> chats;
    private String lastMsg;
    FirebaseUser signedUser;

    public ChatAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        final Chat chat = chats.get(position);
        String username = chat.getOtherMember(signedUser.getUid());
        holder.username.setText(username);

        getLastMessage(holder.lastMessage, chat);

    }

    private void getLastMessage(final TextView lastMessageView, final Chat chat) {

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

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

         TextView lastMessage;
         TextView username;
         ImageButton item_chat_btnStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            lastMessage = itemView.findViewById(R.id.item_chat_lastMessage);
            username = itemView.findViewById(R.id.item_chat_username);
            item_chat_btnStartChat = itemView.findViewById(R.id.item_chat_btnStartChat);
        }
    }
}
