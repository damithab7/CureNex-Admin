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
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.FragmentAddUserBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.listener.FirebaseCallback;
import lk.damithab.curenexadmin.model.Gender;
import lk.damithab.curenexadmin.model.Therapist;
import lk.damithab.curenexadmin.model.User;
import lk.damithab.curenexadmin.util.AnimationUtil;

public class AddUserFragment extends Fragment {

    private FragmentAddUserBinding binding;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri selectedImage;
    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;

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
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
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

        binding.addUserAddImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            activityResultLauncher.launch(intent);
        });


        binding.addNewUserBtn.setOnClickListener(v -> {
            String firstName = binding.addUserFirstname.getText().toString().trim();
            String lastName = binding.addUserLastname.getText().toString().trim();
            String email = binding.addUserEmail.getText().toString().trim();
            String password = binding.addUserPassword.getText().toString().trim();



            if (firstName.isEmpty()) {
                binding.addUserFirstnameLayout.setErrorEnabled(true);
                binding.addUserFirstnameLayout.setError("Firstname is required!");
                binding.addUserFirstname.requestFocus();
                return;
            }
            if (lastName.isEmpty()) {
                binding.addUserLastnameLayout.setErrorEnabled(true);
                binding.addUserLastnameLayout.setError("Lastname is required!");
                binding.addUserLastname.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                binding.addUserEmailLayout.setErrorEnabled(true);
                binding.addUserLastnameLayout.setError("Lastname is required!");
                binding.addUserEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                binding.addUserPasswordLayout.setErrorEnabled(true);
                binding.addUserPasswordLayout.setError("Lastname is required!");
                binding.addUserPassword.requestFocus();
                return;
            }

            if (!isCharacterValid(firstName)) {
                return;
            }

            if (!isCharacterValid(lastName)) {
                return;
            }

            spinnerDialog.show(getParentFragmentManager(), AddTherapistFragment.class.getSimpleName());

            db.collection("users").whereEqualTo("email", email).get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot qds) {
                                    if(!qds.isEmpty()){
                                        new ToastDialog(getParentFragmentManager(), "This email is already registered.");
                                        return;
                                    }else{
                                        saveUser(email, password, firstName, lastName);
                                    }
                                }
                            });


            /// create auth
        });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void saveUser(String email, String password, String firstName, String lastName){
        final String[] imagePath = new String[1];
        FirebaseAuth secondaryAuth = getSecondaryAuth();
        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        checkAllTasksFinished(); /// 3
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();

                            secondaryAuth.signOut();

                            if(selectedImage == null){
                                imagePath[0] = "https://ui-avatars.com/api/" + firstName + "+" + lastName;
                            }else{
                                imagePath[0] = "profile-images/" + uid;
                            }

                            User user = User.builder().uid(uid).firstName(firstName)
                                    .lastName(lastName)
                                    .profileUrl(imagePath[0])
                                    .email(email)
                                    .userStatus(true).build();

                            db.collection("users").document(uid)
                                    .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            if(selectedImage != null) {
                                                saveUserImage(uid);
                                            }else{
                                                spinnerDialog.dismiss();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                binding.addUserEmailLayout.setErrorEnabled(true);
                                binding.addUserEmailLayout.setError("This email is already registered. Please login.");
                            } else {
                                Log.e("SignUp", "Error: " + task.getException().getMessage());
                            }

                        }
                    }
                })
                .addOnFailureListener(e -> {
                    checkAllTasksFinished(); /// 3
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        new ToastDialog(getParentFragmentManager(), "This email is already registered.");
                    }
                });
    }

    private void saveUserImage(String uid){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageReference = storage.getReference("profile-images").child(uid);
        imageReference.putFile(selectedImage)
                .addOnSuccessListener(takeSnapshot -> {
                    new ToastDialog(getParentFragmentManager(), "User imaged updated");
                    checkAllTasksFinished();
                    if(isAdded()){
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }).addOnFailureListener(error->{
                    checkAllTasksFinished();
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
                            .into(binding.addUserImg);

                    selectedImage = uri;

                }
            }
    );

    private void loadListeners() {

    }

    private FirebaseAuth getSecondaryAuth() {
        FirebaseOptions defaultOptions = FirebaseApp.getInstance().getOptions();

        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.initializeApp(requireContext(), defaultOptions, "SecondaryContext");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.getInstance("SecondaryContext");
        }
        return FirebaseAuth.getInstance(secondaryApp);
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