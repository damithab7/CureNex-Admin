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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.TherapistScheduleAdapter;
import lk.damithab.curenexadmin.databinding.FragmentEditTherapistBinding;
import lk.damithab.curenexadmin.dialog.AddScheduleBottomSheet;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
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
    private final int TOTAL_TASKS = 5;

    private SpinnerDialog spinnerDialog;

    private String therapistId;

    private Therapist therapist;

    private Spinner titleSpinner,genderSpinner, serviceSpinner;

    private TherapistScheduleAdapter scheduleAdapter;

    private List<TherapistSchedule> therapistSchedules;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        if(getArguments() != null){
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

        getTherapist(therapist->{
            this.therapist = therapist;

            StorageReference ref = storage.getReference(therapist.getTherapistImage());
            titleSpinner.setSelection(titleAdapter.getPosition(therapist.getTitle()));


            GlideApp.with(getContext())
                    .load(ref)
                    .centerCrop()
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
                            if(gender.getGenderId().equals(therapist.getGenderId())){
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
                        serviceSpinner.setSelection(adapter.getPosition(selectedGender));
                    }
                }
            });

            db.collection("services").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot qds) {
                    if (!qds.isEmpty()) {
                        serviceList = qds.toObjects(Service.class);

                        List<String> services = new ArrayList<>();

                        String selectedService = "";

                        for (Service service : serviceList) {
                            services.add(service.getName());
                            if(service.getServiceId().equals(therapist.getServiceId())){
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

        binding.saveTherapistBtn.setOnClickListener(v->{

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

    private void getTherapist(FirebaseCallback<Therapist> callback){
        db.collection("therapist").whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                            callback.onCallback(therapist);
                        }
                    }
                });
    }

    private void loadListeners(){

    }
}