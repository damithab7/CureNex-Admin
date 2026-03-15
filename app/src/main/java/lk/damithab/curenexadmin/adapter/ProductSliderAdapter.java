package lk.damithab.curenexadmin.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.module.GlideApp;
import lombok.Setter;

public class ProductSliderAdapter extends RecyclerView.Adapter<ProductSliderAdapter.ProductSliderViewHolder> {
    private List<Uri> images;

    private FirebaseStorage storage;

    private OnRemoveListener removeListener;
    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setImages(List<Uri> images){
        this.images = images;
        notifyDataSetChanged();
    }

    public void handleItemRemoved(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_slider_item, parent, false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSliderViewHolder holder, int position) {
        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }
        if (images == null || images.isEmpty()) {
            holder.removeBtn.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.addBtn.setVisibility(View.VISIBLE);
        }else {
            holder.addBtn.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.removeBtn.setVisibility(View.VISIBLE);

            Uri imageUri = images.get(position);
            GlideApp.with(holder.itemView.getContext())
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder2)
                    .into(holder.imageView);


            holder.removeBtn.setOnClickListener(v->{
                int pos = holder.getAbsoluteAdapterPosition();
                Log.i("Position", String.valueOf(pos));
                if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoved(currentPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (images == null || images.isEmpty()) ? 1 : images.size();
    }

    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        MaterialButton removeBtn, addBtn;
        public ProductSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.product_slider_item_image);
            this.removeBtn = itemView.findViewById(R.id.product_slider_item_remove);
            this.addBtn = itemView.findViewById(R.id.add_product_placeholder);
        }
    }


    public interface OnRemoveListener {
        void onRemoved(int position);
    }

}
