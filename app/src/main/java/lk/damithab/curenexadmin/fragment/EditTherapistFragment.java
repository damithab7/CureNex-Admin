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

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.TherapistScheduleAdapter;
import lk.damithab.curenexadmin.databinding.FragmentEditTherapistBinding;
import lk.damithab.curenexadmin.dialog.AddScheduleBottomSheet;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.listener.FirebaseCallback;
import lk.damithab.curenexadmin.model.Gender;
import lk.damithab.curenexadmin.model.Service;
import lk.damithab.curenexadmin.model.Therapist;
import lk.damithab.curenexadmin.model.TherapistSchedule;
import lk.damithab.curenexadmin.module.GlideApp;
import lk.damithab.curenexadmin.util.AnimationUtil;

public class EditTherapistFragment extends Fragment {

    private FragmentEditTherapistBinding binding;
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
    private final int TOTAL_TASKS = 3;

    private SpinnerDialog spinnerDialog;

    private String therapistId;

    private Therapist therapist;

    private Spinner titleSpinner, genderSpinner, serviceSpinner;

    private TherapistScheduleAdapter scheduleAdapter;

    private List<TherapistSchedule> therapistSchedules = new ArrayList<>();

    private Boolean setStatus;

    Set<String> localIds = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        if (getArguments() != null) {
            this.therapistId = getArguments().getString("therapistId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditTherapistBinding.inflate(inflater, container, false);
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

        binding.therapistEditImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            activityResultLauncher.launch(intent);
        });

        serviceSpinner = binding.editTherapistServiceSpinner;

        genderSpinner = binding.editTherapistGenderSpinner;


        titleSpinner = binding.editTherapistTitleSpinner;
        List<String> therapistTitleList = new ArrayList<>();
        therapistTitleList.add("Dr.");
        therapistTitleList.add("Mr.");
        therapistTitleList.add("Mrs.");
        therapistTitleList.add("Ms.");

        ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, therapistTitleList);
        titleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        titleSpinner.setAdapter(titleAdapter);

        binding.editTherapistImageRemoveBtn.setOnClickListener(v -> {
            if (selectedImage != null) {
                selectedImage = null;
                binding.editTherapistImage.setVisibility(View.GONE);
                binding.editTherapistImage.setVisibility(View.VISIBLE);
                binding.editTherapistImageRemoveBtn.setVisibility(View.GONE);
            }
        });

        getTherapist(therapist -> {
            this.therapist = therapist;

            StorageReference ref = storage.getReference(therapist.getTherapistImage());
            titleSpinner.setSelection(titleAdapter.getPosition(therapist.getTitle()));
            binding.editTherapistBio.setText(therapist.getBio());

            String[] name = therapist.getName().split(" ");
            binding.editTherapistFirstName.setText(name[0]);
            binding.editTherapistLastName.setText(name[1]);
            binding.editTherapistRate.setText(String.valueOf(therapist.getRate()));
            binding.editTherapistWorkEmail.setText(therapist.getWorkEmail());
            binding.editTherapistWorkMobile.setText(therapist.getWorkMobileNo());


            StorageReference sref = storage.getReference(therapist.getTherapistImage());

            sref.getDownloadUrl().addOnSuccessListener(imageUri -> {
                selectedImage = imageUri;
            });

            db.collection("therapist").document(therapist.getTherapistId()).collection("schedule")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {
                            if (!qds.isEmpty()) {
                                therapistSchedules = qds.toObjects(TherapistSchedule.class);
                                TherapistScheduleAdapter therapistScheduleAdapter = new TherapistScheduleAdapter();
                                therapistScheduleAdapter.setScheduleList(therapistSchedules);

                                binding.editTherapistScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                binding.editTherapistScheduleRecycler.setAdapter(therapistScheduleAdapter);

                            }
                        }
                    }).addOnFailureListener(error -> {

                    });

            ObjectKey signature = new ObjectKey(therapist.getLastUpdate());

            GlideApp.with(getContext())
                    .load(ref)
                    .centerCrop()
                    .signature(signature)
                    .placeholder(R.drawable.imageplaceholder2)
                    .into(binding.editTherapistImage);

            db.collection("gender").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot qds) {
                    if (!qds.isEmpty()) {
                        genderList = qds.toObjects(Gender.class);

                        List<String> genders = new ArrayList<>();

                        String selectedGender = "";

                        for (Gender gender : genderList) {
                            genders.add(gender.getName());
                            if (gender.getGenderId().equals(therapist.getGenderId())) {
                                selectedGender = gender.getName();
                            }
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
                        genderSpinner.setSelection(adapter.getPosition(selectedGender));
                    }
                }
            });

            Spinner statusSpinner = binding.editTherapistStatusSpinner;
            List<String> productStatus = new ArrayList<>();
            productStatus.add("true");
            productStatus.add("false");

            ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, productStatus);
            statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            statusSpinner.setAdapter(statusAdapter);
            statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (productStatus.get(i).equals("true")) {
                        setStatus = true;
                    } else {
                        setStatus = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            statusSpinner.setSelection(statusAdapter.getPosition(String.valueOf(therapist.isStatus())));


            db.collection("services").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot qds) {
                    if (!qds.isEmpty()) {
                        serviceList = qds.toObjects(Service.class);

                        List<String> services = new ArrayList<>();

                        String selectedService = "";

                        for (Service service : serviceList) {
                            services.add(service.getName());
                            if (service.getServiceId().equals(therapist.getServiceId())) {
                                selectedService = service.getName();
                            }
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
                        serviceSpinner.setSelection(adapter.getPosition(selectedService));
                    }
                }
            });

        });

        scheduleAdapter = new TherapistScheduleAdapter();

        binding.editTherapistScheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        scheduleAdapter.setOnRemoveListener(position -> {
            if (therapistSchedules != null) {
                therapistSchedules.remove(position);
                scheduleAdapter.setScheduleList(therapistSchedules);
            }
        });

        binding.editTherapistScheduleBtn.setOnClickListener(v -> {
            AddScheduleBottomSheet sheet = new AddScheduleBottomSheet((schedule) -> {
                therapistSchedules.add(schedule);
                Log.d("Edit Therapist", "onViewCreated: Schedule Time" + schedule.getStartTime());
                scheduleAdapter.setScheduleList(therapistSchedules);
                binding.editTherapistScheduleRecycler.setAdapter(scheduleAdapter);
            });

            sheet.show(getChildFragmentManager(), "ScheduleBottomSheet");

        });

        loadListeners();

        binding.saveTherapistBtn.setOnClickListener(v -> {

            String firstName = binding.editTherapistFirstName.getText().toString().trim();
            String lastName = binding.editTherapistLastName.getText().toString().trim();
            String rate = binding.editTherapistRate.getText().toString();
            String bio = binding.editTherapistBio.getText().toString();
            String workEmail = binding.editTherapistWorkEmail.getText().toString();
            String workMobileNo = binding.editTherapistWorkMobile.getText().toString();

            /// validation
            if (selectedImage == null) {
                new ToastDialog(getParentFragmentManager(), "Please select therapist image");
                return;
            }

            if (therapistSchedules.isEmpty()) {
                new ToastDialog(getParentFragmentManager(), "Please add at least 1 schedule");
                return;
            }

            if (firstName.isEmpty()) {
                binding.editTherapistFirstNameLayout.setErrorEnabled(true);
                binding.editTherapistFirstNameLayout.setError("Firstname is required!");
                binding.editTherapistFirstName.requestFocus();
                return;
            }
            if (lastName.isEmpty()) {
                binding.editTherapistLastNameLayout.setErrorEnabled(true);
                binding.editTherapistLastNameLayout.setError("Lastname is required!");
                binding.editTherapistLastName.requestFocus();
                return;
            }

            if (bio.isEmpty()) {
                binding.editTherapistBioLayout.setErrorEnabled(true);
                binding.editTherapistBioLayout.setError("Bio is required!");
                binding.editTherapistBio.requestFocus();
                return;
            }
            if (workEmail.isEmpty()) {
                binding.editTherapistWorkEmailLayout.setErrorEnabled(true);
                binding.editTherapistWorkEmailLayout.setError("WorkEmail is required!");
                binding.editTherapistWorkEmail.requestFocus();
                return;
            }
            if (workMobileNo.isEmpty()) {
                binding.editTherapistWorkMobileLayout.setErrorEnabled(true);
                binding.editTherapistWorkMobileLayout.setError("WorkMobileNo is required!");
                binding.editTherapistWorkMobile.requestFocus();
                return;
            }
            if (rate.isEmpty()) {
                binding.editTherapistRateLayout.setErrorEnabled(true);
                binding.editTherapistRateLayout.setError("Rate is required!");
                binding.editTherapistRate.requestFocus();
                return;
            }

            requireActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );

            spinnerDialog.show(getParentFragmentManager(), AddTherapistFragment.class.getSimpleName());

            updateTherapist();

        });


    }

    private void updateTherapist() {

        String firstName = binding.editTherapistFirstName.getText().toString().trim();
        String lastName = binding.editTherapistLastName.getText().toString().trim();
        String therapistName = firstName + " " + lastName;
        String rate = binding.editTherapistRate.getText().toString();
        String bio = binding.editTherapistBio.getText().toString();
        String workEmail = binding.editTherapistWorkEmail.getText().toString();
        String workMobileNo = binding.editTherapistWorkMobile.getText().toString();
        titleName = titleSpinner.getSelectedItem().toString();

        long lastUpdate = System.currentTimeMillis();

        double finalRate = Double.parseDouble(rate);

        db.collection("therapist").document(therapistId).update("name",
                        therapistName, "serviceId", serviceId,
                        "genderId", genderId,
                        "title", titleName,
                        "bio", bio,
                        "rate", finalRate,
                        "workEmail", workEmail,
                        "workMobileNo", workMobileNo,
                        "status", setStatus,
                        "lastUpdate", lastUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        checkAllTasksFinished(); /// 1
                        saveTherapistImage();
                        saveTherapistSchedule();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkAllTasksFinished(); /// 1
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });

    }

    public void saveTherapistImage() {

        StorageReference fileRef = storage.getReference(therapist.getTherapistImage());

        if (selectedImage.toString().startsWith("content://") || selectedImage.toString().startsWith("file://")) {
            storage.getReference(therapist.getTherapistImage()).delete().addOnCompleteListener(task -> {
                fileRef.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        checkAllTasksFinished(); /// 2
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        checkAllTasksFinished(); /// 2

                    }
                });
            }).addOnFailureListener(error -> {
                checkAllTasksFinished(); /// 2
            });
        } else {
            checkAllTasksFinished(); /// 2
        }

    }


    private void saveTherapistSchedule() {

        Set<String> localIds = new HashSet<>();
        for (TherapistSchedule sch : therapistSchedules) {
            localIds.add(sch.getScheduleId());
        }

        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        db.collection("therapist").document(therapistId).collection("schedule").get()
                .addOnSuccessListener(qds -> {
                    WriteBatch batch = db.batch();
                    if (!qds.isEmpty()) {

                        Set<String> dbIds = new HashSet<>();

                        for (QueryDocumentSnapshot doc : qds) {
                            String docId = doc.getId();
                            dbIds.add(docId);

                            if (!localIds.contains(docId)) {
                                batch.delete(doc.getReference());
                            }
                        }

                        for (TherapistSchedule sch : therapistSchedules) {
                            if (!dbIds.contains(sch.getScheduleId())) {
                                DocumentReference newDoc = db.collection("therapist")
                                        .document(therapistId)
                                        .collection("schedule")
                                        .document(sch.getScheduleId());
                                batch.set(newDoc, sch);
                            }
                        }

                        batch.commit().addOnSuccessListener(aVoid -> {
                            checkAllTasksFinished(); /// 3
                            Log.d("Firestore", "All schedules added successfully!");
                        }).addOnFailureListener(e -> {
                            checkAllTasksFinished(); /// 3
                            Log.e("Firestore", "Error adding schedules", e);
                        });
                    } else {
                        for (TherapistSchedule sch : therapistSchedules) {
                            DocumentReference newDoc = db.collection("therapist")
                                    .document(therapistId)
                                    .collection("schedule")
                                    .document(sch.getScheduleId());
                            batch.set(newDoc, sch);
                            batch.commit().addOnSuccessListener(aVoid -> {
                                checkAllTasksFinished(); /// 3
                                Log.d("Firestore", "All schedules added successfully!");
                            }).addOnFailureListener(e -> {
                                checkAllTasksFinished(); /// 3
                                Log.e("Firestore", "Error adding schedules", e);
                            });
                        }
                    }
                }).addOnFailureListener(error -> {
                    checkAllTasksFinished(); /// 3
                });


    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    binding.editTherapistImageRemoveBtn.setVisibility(View.VISIBLE);
                    binding.editTherapistImagePlaceholder.setVisibility(View.GONE);
                    binding.editTherapistImage.setVisibility(View.VISIBLE);

                    Uri imageUri = result.getData().getData();
                    Log.i("Image uri", imageUri.toString());
                    selectedImage = imageUri;

                    Glide.with(requireActivity())
                            .load(imageUri)
                            .centerCrop()
                            .into(binding.editTherapistImage);

                }
            }
    );

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            new ToastDialog(getParentFragmentManager(), "Therapist Added successfully!");
            if (isAdded()) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }


    private void getTherapist(FirebaseCallback<Therapist> callback) {
        db.collection("therapist").whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                            callback.onCallback(therapist);
                        }
                    }
                });
    }

    private void loadListeners() {

        binding.editTherapistFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistFirstNameLayout.setErrorEnabled(false);
            }
        });
        binding.editTherapistLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistLastNameLayout.setErrorEnabled(false);
            }
        });

        binding.editTherapistBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistBioLayout.setErrorEnabled(false);
            }
        });
        binding.editTherapistWorkEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistWorkEmailLayout.setErrorEnabled(false);
            }
        });
        binding.editTherapistWorkMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistWorkMobileLayout.setErrorEnabled(false);
            }
        });
        binding.editTherapistRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editTherapistRateLayout.setErrorEnabled(false);
            }
        });
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