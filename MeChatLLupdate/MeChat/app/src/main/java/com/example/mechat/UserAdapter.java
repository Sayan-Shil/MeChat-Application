package com.example.mechat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context; // 🔹 Added context
    private List<Users> usersList;

    public UserAdapter(Context context, List<Users> usersList) { // 🔹 Fixed constructor
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = usersList.get(position);
        holder.username.setText(user.getUsername());
        holder.userstatus.setText(user.getStatus());

        // Handle image loading with a placeholder & error image
        Picasso.get()
                .load(user.getProfilePic())
                .placeholder(R.drawable.man)  // Set a default image
                .error(R.drawable.man)        // Set an error image
                .into(holder.userimg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, chatWin.class); // 🔹 Fixed
                intent.putExtra("nameee", user.getUsername());
                intent.putExtra("recieverImg", user.getProfilePic());
                intent.putExtra("uid", user.getUserId());
                context.startActivity(intent); // 🔹 Fixed
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username, userstatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}
