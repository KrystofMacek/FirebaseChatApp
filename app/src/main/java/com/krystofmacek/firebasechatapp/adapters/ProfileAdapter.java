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
import com.krystofmacek.firebasechatapp.model.User;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder>{

    private Context context;
    private List<User> profiles;

    public ProfileAdapter(Context context, List<User> profiles) {
        this.context = context;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        final User profile = profiles.get(position);
        String username = profile.getDisplayName();
        holder.username.setText(username);
        createTagsString(holder.tags, profile);
    }

    private void createTagsString(TextView output, User profile) {
        output.setText("");
        StringBuilder tagList = new StringBuilder();
        for (String tag : profile.getTags()) {
            tagList.append("#").append(tag).append(" ");
        }
        output.setText(tagList);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tags;
        TextView username;
        ImageButton item_chat_btnStartChat;

        public ViewHolder(View itemView) {
            super(itemView);

            tags = itemView.findViewById(R.id.item_profile_tags);
            username = itemView.findViewById(R.id.item_profile_username);
            item_chat_btnStartChat = itemView.findViewById(R.id.item_profile_btnStartChat);
        }
    }
}
