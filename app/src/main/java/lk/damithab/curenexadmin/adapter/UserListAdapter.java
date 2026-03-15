package lk.damithab.curenexadmin.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.model.Product;
import lk.damithab.curenexadmin.model.User;
import lk.damithab.curenexadmin.module.GlideApp;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private List<User> userList;


    private FirebaseStorage storage;

    private OnEditClickListener editListener;

    private OnRemoveClick removeListener;

    public void setOnRemoveListener(OnRemoveClick listener) {
        this.removeListener = listener;
    }

    public UserListAdapter(List<User> userList, OnEditClickListener listener) {
        this.userList = userList;
        this.editListener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }

        User user = userList.get(position);
        holder.userName.setText(user.getFirstName() + " " + user.getLastName());
        holder.userEmail.setText(user.getEmail());

        if (user.isUserStatus()) {
            holder.userStatus.setText("Active");
        } else {
            holder.userStatus.setText("Deactive");
            holder.userStatus.setTextColor(Color.RED);
        }
        StorageReference ref = storage.getReference(user.getProfileUrl());

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.userImage);

        holder.editButton.setOnClickListener(v->{
            if(editListener != null){
                editListener.onEditItemClick(user);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            Log.i("Position", String.valueOf(pos));
            if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                removeListener.onRemove(currentPosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName, userEmail, userStatus;

        MaterialButton editButton, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userImage = itemView.findViewById(R.id.item_user_image);
            this.userName = itemView.findViewById(R.id.item_user_name);
            this.userEmail = itemView.findViewById(R.id.item_user_email);
            this.userStatus = itemView.findViewById(R.id.item_user_status);
            this.editButton = itemView.findViewById(R.id.item_product_edit);
            this.btnRemove = itemView.findViewById(R.id.item_product_remove);
        }
    }

    public interface OnEditClickListener {
        void onEditItemClick(User user);
    }

    public interface OnRemoveClick {
        void onRemove(int position);
    }
}
