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
import lk.damithab.curenexadmin.model.Service;
import lk.damithab.curenexadmin.model.TherapistSchedule;
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

    private ProductSliderAdapter adapter;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;

    private SpinnerDialog spinnerDialog;

    private String imageArray;

    private FragmentAddTherapistBinding binding;

    private List<TherapistSchedule> therapistSchedules = new ArrayList<>();

    private TherapistScheduleAdapter scheduleAdapter;

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

        Spinner titleSpinner = binding.addTherapistTitleSpinner;
        List<String> therapistTitleList = new ArrayList<>();
        therapistTitleList.add("Dr.");
        therapistTitleList.add("Mr.");
        therapistTitleList.add("Mrs.");
        therapistTitleList.add("Ms.");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, therapistTitleList);
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        titleSpinner.setAdapter(statusAdapter);
//        titleSpinner.getSelectedItem().toString(); get the therapist title

        binding.addTherapistImageRemoveBtn.setOnClickListener(v -> {
            if (selectedImage != null) {
                selectedImage = null;
                binding.addTherapistImage.setVisibility(View.GONE);
                binding.addTherapistImagePlaceholder.setVisibility(View.VISIBLE);
                binding.addTherapistImageRemoveBtn.setVisibility(View.GONE);
            }
        });

        scheduleAdapter = new TherapistScheduleAdapter();
        scheduleAdapter.setOnRemoveListener(position -> {
            if(therapistSchedules != null){
                therapistSchedules.remove(position);
            }
        });

        binding.addTherapistScheduleBtn.setOnClickListener(v -> {
            AddScheduleBottomSheet sheet = new AddScheduleBottomSheet((schedule) -> {
                therapistSchedules.add(schedule);
                scheduleAdapter.setScheduleList(therapistSchedules);
            });

            sheet.show(getChildFragmentManager(), "ScheduleBottomSheet");
        });


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