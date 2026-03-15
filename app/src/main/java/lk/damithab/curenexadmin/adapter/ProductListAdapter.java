package lk.damithab.curenexadmin.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import lk.damithab.curenexadmin.module.GlideApp;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
    private List<Product> products;


    private FirebaseStorage storage;

    private OnEditClickListener editListener;

    private OnRemoveClick removeListener;

    public void setOnRemoveListener(OnRemoveClick listener) {
        this.removeListener = listener;
    }

    public ProductListAdapter(List<Product> products, OnEditClickListener listener) {
        this.products = products;
        this.editListener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }

        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());

        if (product.getStockCount() > 0) {
            holder.productStock.setText("In Stock");
        } else {
            holder.productStock.setText("Out of Stock");
            holder.productStock.setTextColor(Color.RED);
        }
        StorageReference ref = storage.getReference(product.getImages().get(0));

        GlideApp.with(holder.itemView.getContext())
                .load(ref)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder2)
                .into(holder.productImage);

        holder.editButton.setOnClickListener(v->{
            if(editListener != null){
                editListener.onEditItemClick(product);
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
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice, productStock;

        MaterialButton editButton, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.productImage = itemView.findViewById(R.id.item_product_image);
            this.productTitle = itemView.findViewById(R.id.item_product_title);
            this.productPrice = itemView.findViewById(R.id.item_product_price);
            this.productStock = itemView.findViewById(R.id.list_item_stock);
            this.editButton = itemView.findViewById(R.id.item_product_edit);
            this.btnRemove = itemView.findViewById(R.id.item_product_remove);
        }
    }

    public interface OnEditClickListener {
        void onEditItemClick(Product product);
    }

    public interface OnRemoveClick {
        void onRemove(int position);
    }
}
