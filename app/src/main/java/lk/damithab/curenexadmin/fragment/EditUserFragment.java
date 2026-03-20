package lk.damithab.curenexadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.FragmentEditUserBinding;
import lk.damithab.curenexadmin.listener.FirebaseCallback;
import lk.damithab.curenexadmin.model.Gender;

public class EditUserFragment extends Fragment {

    private FragmentEditUserBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private String userId;

    private Spinner statusSpinner;

    private Boolean setUserStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        loadListeners();


        Spinner statusSpinner = binding.editUserStatusSpinner;
        final String[][] userStatus = {{"true", "false"}};

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, userStatus[0]);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setUserStatus = userStatus[0][i].equals("true");
                Log.d("EditUserFragment", "ProductStauts: " + setUserStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        binding.saveProfileBtn.setOnClickListener(v -> {
            String firstName = binding.firstNameProfileInput.getText().toString().trim();
            String lastName = binding.lastNameProfileInput.getText().toString().trim();

            db.collection("users").document(userId)
                    .update("firstName", firstName,
                            "lastName", lastName, "userStatus", setUserStatus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        });
    }

    private void loadListeners() {

    }

    private void getGenders(FirebaseCallback<List<Gender>> callback) {
        db.collection("gender").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        List<Gender> genderList = qds.toObjects(Gender.class);
                        callback.onCallback(genderList);
                    }
                });
    }
}