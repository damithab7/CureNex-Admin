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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.ProductListAdapter;
import lk.damithab.curenexadmin.adapter.TherapistListAdapter;
import lk.damithab.curenexadmin.databinding.FragmentProductBinding;
import lk.damithab.curenexadmin.databinding.FragmentTherapistBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Product;
import lk.damithab.curenexadmin.model.Service;
import lk.damithab.curenexadmin.model.Therapist;

public class TherapistFragment extends Fragment {

    private FragmentTherapistBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private SpinnerDialog spinnerDialog;

    private Map<String, Service> serviceMap;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTherapistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        serviceMap = new HashMap<>();

        db.collection("services")
                .get()
                .addOnSuccessListener(serviceSnaps -> {
                    serviceSnaps.forEach(ds -> {
                        Service service = ds.toObject(Service.class);
                        if (service != null) {
                            serviceMap.put(service.getServiceId(), service);
                        }
                    });
                });

        binding.allTherapistsRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

        db.collection("therapist").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                checkAllTasksFinished();
                if (!qds.isEmpty()) {
                    List<Therapist> therapistList = qds.toObjects(Therapist.class);
                    for (Therapist therapist : therapistList) {
                        Service service = serviceMap.get(therapist.getServiceId());
                        if(service != null){
                            therapist.setServiceName(service.getName());
                        }
                    }
                    /// On edit handled
                    TherapistListAdapter adapter = new TherapistListAdapter(therapistList, therapist -> {
                        EditTherapistFragment fragment = new EditTherapistFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("therapistId", therapist.getTherapistId());
                        fragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.navContainerView, fragment)
                                .addToBackStack(null)
                                .commit();

                    });
                    /// remove listener
                    adapter.setOnRemoveListener(position -> {
                        String documentId = therapistList.get(position).getTherapistId();
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Confirmation Message")
                                .setMessage("Are you sure you want to delete this therapist?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        spinnerDialog = SpinnerDialog.show(getParentFragmentManager());
                                        db.collection("therapist").document(documentId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    deleteTherapistImage(documentId);
                                                    therapistList.remove(position);
                                                    adapter.notifyItemRemoved(position);
                                                    adapter.notifyItemRangeChanged(position, therapistList.size());
                                                })
                                                .addOnFailureListener(exception -> {

                                                });
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                    });
                    binding.allTherapistsRecycler.setAdapter(adapter);
                }
            }
        }).addOnFailureListener(error->{
            checkAllTasksFinished();
        });

        binding.addTherapistBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new AddTherapistFragment())
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

    private void deleteTherapistImage(String documentId) {
        storage = FirebaseStorage.getInstance();

        String imagePath = "therapist-images/" + documentId;
        StorageReference imageRef = storage.getReference().child(imagePath);

        imageRef.delete().addOnSuccessListener(aVoid -> {
            spinnerDialog.dismiss();
            new ToastDialog(getActivity().getSupportFragmentManager(), "Therapist removed successfully!");
        }).addOnFailureListener(exception -> {
            spinnerDialog.dismiss();
            Log.e("StorageDelete", "Failed to delete: " + imagePath);
        });


    }
}