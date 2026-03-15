package lk.damithab.curenexadmin.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreRegistrar;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.ProductListAdapter;
import lk.damithab.curenexadmin.adapter.UserListAdapter;
import lk.damithab.curenexadmin.databinding.FragmentUserBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Product;
import lk.damithab.curenexadmin.model.User;

public class UserFragment extends Fragment {

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;
    private SpinnerDialog spinnerDialog;

    private FragmentUserBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.allUsersRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

        db.collection("users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!qds.isEmpty()) {
                            List<User> userList = qds.toObjects(User.class);
                            /// On edit handled
                            UserListAdapter adapter = new UserListAdapter(userList, user -> {
                                EditUserFragment fragment = new EditUserFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("userId", user.getUid());
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.navContainerView, fragment)
                                        .addToBackStack(null)
                                        .commit();

                            });
                            /// remove listener
                            adapter.setOnRemoveListener(position -> {
                                String documentId = userList.get(position).getUid();

                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Confirmation Message")
                                        .setMessage("Are you sure you want to delete this user?")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

                                                /// Also need to remove the auth from firebase
                                                db.collection("users").document(documentId)
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            deleteUserImage(documentId);
                                                            userList.remove(position);
                                                            adapter.notifyItemRemoved(position);
                                                            adapter.notifyItemRangeChanged(position, userList.size());

                                                        })
                                                        .addOnFailureListener(exception -> {
                                                            spinnerDialog.dismiss();
                                                        });
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .show();

                            });
                            binding.allUsersRecycler.setAdapter(adapter);
                        }
                    }
                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });

        binding.addUserBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AddUserFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void deleteUserImage(String documentId) {
        storage = FirebaseStorage.getInstance();

        String imagePath = "profile-images/" + documentId;
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

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            if (spinnerDialog.isAdded()) {
                spinnerDialog.dismissAllowingStateLoss();
            }
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

}