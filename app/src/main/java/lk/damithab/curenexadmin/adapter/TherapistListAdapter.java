package lk.damithab.curenexadmin.adapter;

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
import lk.damithab.curenexadmin.model.Therapist;
import lk.damithab.curenexadmin.module.GlideApp;

public class TherapistListAdapter extends RecyclerView.Adapter<TherapistListAdapter.ViewHolder> {
    private List<Therapist> therapistList;


    private FirebaseStorage storage;

    private OnEditClickListener editListener;

    private OnRemoveClick removeListener;

    public void setOnRemoveListener(OnRemoveClick listener) {
        this.removeListener = listener;
    }

    public TherapistListAdapter(List<Therapist> therapistList, OnEditClickListener listener) {
        this.therapistList = therapistList;
        this.editListener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_therapists, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }

        Therapist therapist = therapistList.get(position);
        holder.therapistName.setText(therapist.getTitle() + " " +therapist.getName());
        holder.therapistPrice.setText("LKR " + therapist.getRate() + "/h");
        holder.therapistService.setText(therapist.getServiceName());

        StorageReference ref = storage.getReference(therapist.getTherapistImage());

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.therapistImage);

        holder.editButton.setOnClickListener(v->{
            if(editListener != null){
                editListener.onEditItemClick(therapist);
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
        return therapistList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView therapistImage;
        TextView therapistName, therapistPrice, therapistService;

        MaterialButton editButton, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.therapistImage = itemView.findViewById(R.id.item_therapist_image);
            this.therapistName = itemView.findViewById(R.id.item_therapist_title);
            this.therapistPrice = itemView.findViewById(R.id.item_therapist_price);
            this.therapistService = itemView.findViewById(R.id.item_therapist_service);
            this.editButton = itemView.findViewById(R.id.item_therapist_edit);
            this.btnRemove = itemView.findViewById(R.id.item_therapist_remove);
        }
    }

    public interface OnEditClickListener {
        void onEditItemClick(Therapist Therapist);
    }

    public interface OnRemoveClick {
        void onRemove(int position);
    }
}
