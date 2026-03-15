package lk.damithab.curenexadmin.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.ProductListAdapter;
import lk.damithab.curenexadmin.databinding.FragmentProductBinding;
import lk.damithab.curenexadmin.dialog.CustomAlertDialog;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Product;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;
    private SpinnerDialog spinnerDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.allProductsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!qds.isEmpty()) {
                            List<Product> productList = qds.toObjects(Product.class);
                            /// On edit handled
                            ProductListAdapter adapter = new ProductListAdapter(productList, product -> {
                                EditProductFragment fragment = new EditProductFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, fragment)
                                        .addToBackStack(null)
                                        .commit();

                            });
                            /// remove listener
                            adapter.setOnRemoveListener(position -> {
                                String documentId = productList.get(position).getProductId();
                                int imagesArraySize = productList.get(position).getImages().size();

                                new CustomAlertDialog(getContext())
                                        .setTitle("Confirmation Message")
                                        .setMessage("Are you sure you want to delete this product?")
                                        .setPositiveButton("Remove", v -> {
                                            spinnerDialog = SpinnerDialog.show(getParentFragmentManager());
                                            db.collection("products").document(documentId)
                                                    .delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        deleteProductImages(documentId, imagesArraySize);
                                                        productList.remove(position);
                                                        adapter.notifyItemRemoved(position);
                                                        adapter.notifyItemRangeChanged(position, productList.size());
                                                    })
                                                    .addOnFailureListener(exception -> {
                                                        spinnerDialog.dismiss();
                                                    });
                                        })
                                        .setNegativeButton()
                                        .show();

                            });
                            binding.allProductsRecycler.setAdapter(adapter);
                        }
                    }
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });

        binding.addProductBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AddProductFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            if (spinnerDialog.isAdded()) {
                spinnerDialog.dismissAllowingStateLoss();
            }
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void deleteProductImages(String documentId, int imagesArraySize) {
        storage = FirebaseStorage.getInstance();

        for (int i = 0; i < imagesArraySize; i++) {
            String imagePath = "product-images/" + documentId + "/image" + i;
            StorageReference imageRef = storage.getReference().child(imagePath);

            new ToastDialog(getActivity().getSupportFragmentManager(), "Product removed successfully!");

            imageRef.delete().addOnSuccessListener(aVoid -> {
                if (spinnerDialog.isAdded()) {
                    spinnerDialog.dismiss();
                }
            }).addOnFailureListener(exception -> {
                if (spinnerDialog.isAdded()) {
                    spinnerDialog.dismiss();
                }
                Log.e("StorageDelete", "Failed to delete: " + imagePath);
            });
        }
    }
}