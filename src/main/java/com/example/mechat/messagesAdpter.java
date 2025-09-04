package com.example.mechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class messagesAdpter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<msgModelclass> messagesList;
    private static final int MSG_TYPE_SENDER = 1;
    private static final int MSG_TYPE_RECEIVER = 2;

    public messagesAdpter(Context context, ArrayList<msgModelclass> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            return MSG_TYPE_SENDER;
        } else {
            return MSG_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciever_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgModelclass message = messagesList.get(position);

        if (holder.getItemViewType() == MSG_TYPE_SENDER) {
            ((SenderViewHolder) holder).msgsendertyp.setText(message.getMessage());
        } else {
            ((ReceiverViewHolder) holder).recivertextset.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView msgsendertyp;
        AppCompatImageView profilerggg;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            msgsendertyp = itemView.findViewById(R.id.msgsendertyp);
            profilerggg = itemView.findViewById(R.id.profilerggg);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView recivertextset;
        AppCompatImageView pro;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            recivertextset = itemView.findViewById(R.id.recivertextset);
            pro = itemView.findViewById(R.id.pro);
        }
    }
}
