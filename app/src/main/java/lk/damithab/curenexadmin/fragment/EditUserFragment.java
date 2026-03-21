package lk.damithab.curenexadmin.fragment;

import static lk.damithab.curenexadmin.util.RegexUtil.isCharacterValid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.FragmentEditUserBinding;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.listener.FirebaseCallback;
import lk.damithab.curenexadmin.model.Gender;
import lk.damithab.curenexadmin.model.User;
import lk.damithab.curenexadmin.module.GlideApp;
import lk.damithab.curenexadmin.util.AnimationUtil;
import lk.damithab.curenexadmin.util.RegexUtil;

public class EditUserFragment extends Fragment {

    private FragmentEditUserBinding binding;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private String userId;

    private Spinner statusSpinner;

    private Boolean setUserStatus;

    private Uri imageUri;

    String firstName, lastName;

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

        AnimationUtil.bottomSlideDown(getActivity().findViewById(R.id.bottomNavigationView));
        AnimationUtil.topSlideUp(getActivity().findViewById(R.id.main_toolbar));

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

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

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot ds) {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user.getProfileUrl().startsWith("https")) {
                                GlideApp.with(binding.getRoot())
                                        .load(user.getProfileUrl())
                                        .centerCrop()
                                        .into(binding.mainImageView);
                            } else {
                                StorageReference ref = storage.getReference(user.getProfileUrl());

                                GlideApp.with(binding.getRoot())
                                        .load(ref)
                                        .centerCrop()
                                        .placeholder(R.drawable.imageplaceholder2)
                                        .into(binding.mainImageView);
                            }
                            binding.firstNameProfileInput.setText(user.getFirstName());
                            binding.lastNameProfileInput.setText(user.getLastName());

                            if(user.isUserStatus()){
                                statusSpinner.setSelection(statusAdapter.getPosition("true"));
                            }else{
                                statusSpinner.setSelection(statusAdapter.getPosition("false"));
                            }
                        }

                    }
                });

        binding.profileEditImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            activityResultLauncher.launch(intent);
        });


        binding.saveProfileBtn.setOnClickListener(v -> {
            String firstName = binding.firstNameProfileInput.getText().toString().trim();
            String lastName = binding.lastNameProfileInput.getText().toString().trim();

            if (firstName.isEmpty()) {
                binding.profileFirstNameLayout.setErrorEnabled(true);
                binding.profileFirstNameLayout.setError("Firstname is required!");
                binding.firstNameProfileInput.requestFocus();
                return;
            }
            if (lastName.isEmpty()) {
                binding.profileLastNameLayout.setErrorEnabled(true);
                binding.profileLastNameLayout.setError("Lastname is required!");
                binding.lastNameProfileInput.requestFocus();
                return;
            }

            if (!isCharacterValid(firstName)) {
                return;
            }

            if (!isCharacterValid(lastName)) {
                return;
            }

            db.collection("users").document(userId)
                    .update("firstName", firstName,
                            "lastName", lastName, "userStatus", setUserStatus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            new ToastDialog(getActivity().getSupportFragmentManager(), "Profile updated successfully!");
                            if (isAdded()) {
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    Log.i("Image uri", uri.getPath());

                    Glide.with(requireActivity())
                            .load(uri)
                            .circleCrop()
                            .into(binding.mainImageView);

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference imageReference = storage.getReference("profile-images").child(userId);
                    imageReference.putFile(uri)
                            .addOnSuccessListener(takeSnapshot -> {

                                db.collection("users")
                                        .document(userId)
                                        .update("profileUrl", "profile-images/" + userId)
                                        .addOnSuccessListener(aVoid -> {
                                            imageUri = uri;
                                        });

                            });
                }
            }
    );

    private void loadListeners() {

    }


    @Override
    public void onResume() {
        super.onResume();
        AnimationUtil.bottomSlideDown(getActivity().findViewById(R.id.bottomNavigationView));
        AnimationUtil.topSlideUp(getActivity().findViewById(R.id.main_toolbar));
    }

    @Override
    public void onStop() {
        super.onStop();
        AnimationUtil.bottomSlideUp(getActivity().findViewById(R.id.bottomNavigationView));
        AnimationUtil.topSlideDown(getActivity().findViewById(R.id.main_toolbar));
    }
}