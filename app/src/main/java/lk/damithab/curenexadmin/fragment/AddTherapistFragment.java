package lk.damithab.curenexadmin.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.ProductSliderAdapter;
import lk.damithab.curenexadmin.adapter.TherapistScheduleAdapter;
import lk.damithab.curenexadmin.databinding.FragmentAddTherapistBinding;
import lk.damithab.curenexadmin.dialog.AddScheduleBottomSheet;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Category;
import lk.damithab.curenexadmin.model.Gender;
import lk.damithab.curenexadmin.model.Product;
import lk.damithab.curenexadmin.model.Service;
import lk.damithab.curenexadmin.model.Therapist;
import lk.damithab.curenexadmin.model.TherapistSchedule;
import lk.damithab.curenexadmin.model.User;
import lk.damithab.curenexadmin.util.AnimationUtil;

public class AddTherapistFragment extends Fragment {

    private FirebaseFirestore db;

    private String serviceId;
    private String genderId;

    private String titleName;

    private List<Service> serviceList;
    private List<Gender> genderList;

    private Uri selectedImage;

    private static final int PICK_IMAGE = 100;

    private FirebaseStorage storage;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 5;

    private SpinnerDialog spinnerDialog;

    private FragmentAddTherapistBinding binding;

    private List<TherapistSchedule> therapistSchedules = new ArrayList<>();

    private TherapistScheduleAdapter scheduleAdapter;

    DocumentReference therapistRef;


    private Spinner titleSpinner;
    private static final String TAG = AddTherapistFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTherapistBinding.inflate(getLayoutInflater());
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

        spinnerDialog = new SpinnerDialog();

        binding.therapistAddImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            activityResultLauncher.launch(intent);
        });

        Spinner serviceSpinner = binding.addTherapistServiceSpinner;

        db.collection("services").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    serviceList = qds.toObjects(Service.class);

                    List<String> services = new ArrayList<>();

                    for (Service service : serviceList) {
                        services.add(service.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, services);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    serviceSpinner.setAdapter(adapter);
                    serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            serviceId = serviceList.get(i).getServiceId();
                            Log.d("AddTherapistFragment", "ServiceId: " + serviceId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });

        Spinner genderSpinner = binding.addTherapistGenderSpinner;

        db.collection("gender").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    genderList = qds.toObjects(Gender.class);

                    List<String> genders = new ArrayList<>();

                    for (Gender gender : genderList) {
                        genders.add(gender.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, genders);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    genderSpinner.setAdapter(adapter);
                    genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            genderId = genderList.get(i).getGenderId();
                            Log.d("AddTherapistFragment", "GenderId: " + genderId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });

        titleSpinner = binding.addTherapistTitleSpinner;
        List<String> therapistTitleList = new ArrayList<>();
        therapistTitleList.add("Dr.");
        therapistTitleList.add("Mr.");
        therapistTitleList.add("Mrs.");
        therapistTitleList.add("Ms.");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, therapistTitleList);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        titleSpinner.setAdapter(statusAdapter);

        binding.addTherapistImageRemoveBtn.setOnClickListener(v -> {
            if (selectedImage != null) {
                selectedImage = null;
                binding.addTherapistImage.setVisibility(View.GONE);
                binding.addTherapistImagePlaceholder.setVisibility(View.VISIBLE);
                binding.addTherapistImageRemoveBtn.setVisibility(View.GONE);
            }
        });

        scheduleAdapter = new TherapistScheduleAdapter();

        binding.addTherapistScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        scheduleAdapter.setOnRemoveListener(position -> {
            if (therapistSchedules != null) {
                therapistSchedules.remove(position);
                scheduleAdapter.setScheduleList(therapistSchedules);
            }
        });

        binding.addTherapistScheduleBtn.setOnClickListener(v -> {
            AddScheduleBottomSheet sheet = new AddScheduleBottomSheet((schedule) -> {
                therapistSchedules.add(schedule);
                Log.d(TAG, "onViewCreated: Schedule Time" + schedule.getStartTime());
                scheduleAdapter.setScheduleList(therapistSchedules);
                binding.addTherapistScheduleRecycler.setAdapter(scheduleAdapter);
            });

            sheet.show(getChildFragmentManager(), "ScheduleBottomSheet");

        });

        loadListeners();

        binding.saveTherapistBtn.setOnClickListener(view1 -> {

            String firstName = binding.addTherapistFirstName.getText().toString().trim();
            String lastName = binding.addTherapistLastName.getText().toString().trim();
            String rate = binding.addTherapistRate.getText().toString();
            String email = binding.addTherapistEmail.getText().toString();
            String password = binding.addTherapistPassword.getText().toString();
            String bio = binding.addTherapistBio.getText().toString();
            String workEmail = binding.addTherapistWorkEmail.getText().toString();
            String workMobileNo = binding.addTherapistWorkMobile.getText().toString();

            /// validation
            if (selectedImage == null) {
                new ToastDialog(getParentFragmentManager(), "Please select therapist image");
                return;
            }

            if (firstName.isEmpty()) {
                binding.addTherapistFirstNameLayout.setErrorEnabled(true);
                binding.addTherapistFirstNameLayout.setError("Firstname is required!");
                binding.addTherapistFirstName.requestFocus();
                return;
            }
            if (lastName.isEmpty()) {
                binding.addTherapistLastNameLayout.setErrorEnabled(true);
                binding.addTherapistLastNameLayout.setError("Lastname is required!");
                binding.addTherapistLastName.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                binding.addTherapistEmailLayout.setErrorEnabled(true);
                binding.addTherapistEmailLayout.setError("Email is required!");
                binding.addTherapistEmail.requestFocus();
                return;
            }
            if (bio.isEmpty()) {
                binding.addTherapistBioLayout.setErrorEnabled(true);
                binding.addTherapistBioLayout.setError("Bio is required!");
                binding.addTherapistBio.requestFocus();
                return;
            }
            if (workEmail.isEmpty()) {
                binding.addTherapistWorkEmailLayout.setErrorEnabled(true);
                binding.addTherapistWorkEmailLayout.setError("WorkEmail is required!");
                binding.addTherapistWorkEmail.requestFocus();
                return;
            }
            if (workMobileNo.isEmpty()) {
                binding.addTherapistWorkMobileLayout.setErrorEnabled(true);
                binding.addTherapistWorkMobileLayout.setError("WorkMobileNo is required!");
                binding.addTherapistWorkMobile.requestFocus();
                return;
            }
            if (rate.isEmpty()) {
                binding.addTherapistRateLayout.setErrorEnabled(true);
                binding.addTherapistRateLayout.setError("Rate is required!");
                binding.addTherapistRate.requestFocus();
                return;
            }

            requireActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );

            spinnerDialog.show(getParentFragmentManager(), AddTherapistFragment.class.getSimpleName());
            therapistRef = db.collection("therapist").document();

            checkTherapistStatus();

        });

    }

    private void loadListeners() {
        binding.addTherapistFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistFirstNameLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistLastNameLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistEmailLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistBioLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistWorkEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistWorkEmailLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistWorkMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistWorkMobileLayout.setErrorEnabled(false);
            }
        });
        binding.addTherapistRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addTherapistRateLayout.setErrorEnabled(false);
            }
        });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void checkTherapistStatus() {
        String email = binding.addTherapistEmail.getText().toString().trim();
        final String[] uid = new String[1];
        if (email != null) {
            db.collection("users").whereEqualTo("email", email).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {
                            checkAllTasksFinished(); /// 1
                            if (!qds.isEmpty()) {
                                User user = qds.toObjects(User.class).get(0);
                                uid[0] = user.getUid();
                                db.collection("therapist").whereEqualTo("userId", user.getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot qds) {
                                                checkAllTasksFinished(); /// 2
                                                if (!qds.isEmpty()) {
                                                    ///  this user id already has a therapist
                                                    new ToastDialog(getParentFragmentManager(), "This therapist already exist!");
                                                } else {
                                                    /// save therapist
                                                    saveUserTherapist(user.getUid());
                                                }
                                            }
                                        }).addOnFailureListener(error -> {
                                            checkAllTasksFinished(); /// 2
                                        });

                            } else {
                                checkAllTasksFinished(); /// 2
                                /// save user & therapist
                                saveTherapist();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            checkAllTasksFinished();
                        }
                    });
        }

    }

    public void saveTherapist() {
        String therapistId = therapistRef.getId();

        String firstName = binding.addTherapistFirstName.getText().toString().trim();
        String lastName = binding.addTherapistLastName.getText().toString().trim();
        String therapistName = firstName + " " + lastName;
        String rate = binding.addTherapistRate.getText().toString();
        String email = binding.addTherapistEmail.getText().toString();
        String password = binding.addTherapistPassword.getText().toString();
        String bio = binding.addTherapistBio.getText().toString();
        String workEmail = binding.addTherapistWorkEmail.getText().toString();
        String workMobileNo = binding.addTherapistWorkMobile.getText().toString();

        String therapistImagePath = "therapist-images/" + therapistId;

        double finalRate = Double.parseDouble(rate);

        FirebaseAuth secondaryAuth = getSecondaryAuth();

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        checkAllTasksFinished(); /// 3
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();

                            secondaryAuth.signOut();

                            User user = User.builder().uid(uid).firstName(firstName)
                                    .lastName(lastName)
                                    .profileUrl("https://ui-avatars.com/api/" + firstName + "+" + lastName)
                                    .email(email)
                                    .userStatus(true).build();

                            db.collection("users").document(uid)
                                    .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Therapist therapistItem = Therapist.builder().therapistId(therapistId).bio(bio)
                                                    .therapistImage(therapistImagePath).documentId(therapistId).name(therapistName)
                                                    .title(titleSpinner.getSelectedItem().toString()).genderId(genderId).serviceId(serviceId).rate(finalRate)
                                                    .workEmail(workEmail).workMobileNo(workMobileNo).uid(uid).build();

                                            therapistRef.set(therapistItem)
                                                    .addOnSuccessListener(aVoid -> {

                                                        saveTherapistImage(therapistId);
                                                        saveTherapistSchedule();
                                                        checkAllTasksFinished();

                                                    }).addOnFailureListener(aVoid -> {
                                                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        checkAllTasksFinished();
                                                    });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                binding.addTherapistEmailLayout.setErrorEnabled(true);
                                binding.addTherapistEmailLayout.setError("This email is already registered. Please login.");
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

    public void saveUserTherapist(String userId) {

        String therapistId = therapistRef.getId();

        String firstName = binding.addTherapistFirstName.getText().toString().trim();
        String lastName = binding.addTherapistLastName.getText().toString().trim();
        String therapistName = firstName + " " + lastName;
        String rate = binding.addTherapistRate.getText().toString();
        String bio = binding.addTherapistBio.getText().toString();
        String workEmail = binding.addTherapistWorkEmail.getText().toString();
        String workMobileNo = binding.addTherapistWorkMobile.getText().toString();

        double finalRate = Double.parseDouble(rate);

        String therapistImagePath = "therapist-images/" + therapistId;

        Therapist therapistItem = Therapist.builder().therapistId(therapistId).bio(bio)
                .therapistImage(therapistImagePath).documentId(therapistId).name(therapistName)
                .title(titleSpinner.getSelectedItem().toString()).genderId(genderId).serviceId(serviceId).rate(finalRate)
                .workEmail(workEmail).workMobileNo(workMobileNo).uid(userId).build();

        therapistRef.set(therapistItem)
                .addOnSuccessListener(aVoid -> {

                    checkAllTasksFinished(); /// 3
                    saveTherapistImage(therapistId);
                    saveTherapistSchedule();

                }).addOnFailureListener(aVoid -> {

                    checkAllTasksFinished(); /// 3
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                });
    }

    public void saveTherapistImage(String therapistId) {
        StorageReference fileRef = storage.getReference("therapist-images")
                .child(therapistId);

        fileRef.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                checkAllTasksFinished(); /// 4
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                new ToastDialog(getParentFragmentManager(), "Therapist Added successfully!");

                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                checkAllTasksFinished(); /// 4
            }
        });

    }

    private void saveTherapistSchedule() {
        WriteBatch batch = db.batch();
        CollectionReference scheduleRef = db.collection("therapist").document(therapistRef.getId()).collection("schedule");

        for (TherapistSchedule slot : therapistSchedules) {
            DocumentReference newDoc = scheduleRef.document();
            slot.setScheduleId(newDoc.getId());
            batch.set(newDoc, slot);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            checkAllTasksFinished(); /// 5
            Log.d("Firestore", "All schedules added successfully!");
        }).addOnFailureListener(e -> {
            checkAllTasksFinished(); /// 5
            Log.e("Firestore", "Error adding schedules", e);
        });
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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    binding.addTherapistImageRemoveBtn.setVisibility(View.VISIBLE);
                    binding.addTherapistImagePlaceholder.setVisibility(View.GONE);
                    binding.addTherapistImage.setVisibility(View.VISIBLE);

                    Uri imageUri = result.getData().getData();
                    Log.i("Image uri", imageUri.toString());
                    selectedImage = imageUri;

                    Glide.with(requireActivity())
                            .load(imageUri)
                            .centerCrop()
                            .into(binding.addTherapistImage);

                }
            }
    );

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