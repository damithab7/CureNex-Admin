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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.CategoryAdapter;
import lk.damithab.curenexadmin.adapter.ProductSliderAdapter;
import lk.damithab.curenexadmin.databinding.FragmentAddProductBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Category;
import lk.damithab.curenexadmin.model.Product;
import lk.damithab.curenexadmin.util.AnimationUtil;

public class AddProductFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentAddProductBinding binding;

    private FirebaseFirestore db;

    private String categoryId;

    private List<Category> categoryList;

    private List<Uri> selectedImages;

    private static final int PICK_IMAGE = 100;

    private FirebaseStorage storage;

    private ProductSliderAdapter adapter;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;

    private SpinnerDialog spinnerDialog;

    private ArrayList<String> imagesArray;

    private DocumentReference newProductRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);
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

        adapter = new ProductSliderAdapter();
        binding.addProductImageSlider.setAdapter(adapter);

        adapter.setOnRemoveListener(position -> {
            if (position != -1 && selectedImages.size() > position) {
                selectedImages.remove(position);

                if (selectedImages.isEmpty()) {
                    adapter.setImages(selectedImages);
                } else {
                    adapter.handleItemRemoved(position);
                }
            }
        });

        Spinner categorySpinner = binding.addProductCategorySpinner;

        db.collection("categories").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    categoryList = qds.toObjects(Category.class);

                    List<String> categories = new ArrayList<>();

                    for (Category category : categoryList) {
                        categories.add(category.getCategoryName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, categories);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);
                    categorySpinner.setOnItemSelectedListener(AddProductFragment.this);
                }
            }
        });

        binding.productAddImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            activityResultLauncher.launch(intent);
        });

        loadListeners();

        binding.saveProductBtn.setOnClickListener(v -> {

            if(selectedImages == null){
                new ToastDialog(getParentFragmentManager(), "Please at least select minimum of 1 image. (Maximum of 5 images)");
                return;
            }

            String title = binding.addProductTitle.getText().toString();
            String description = binding.addProductTitle.getText().toString();

            if(title.isEmpty()){
                binding.addProductTitleLayout.setErrorEnabled(true);
                binding.addProductTitleLayout.setError("Title is required!");
                binding.addProductTitle.requestFocus();
                return;
            }

            if(description.isEmpty()){
                binding.addProductDescriptionLayout.setErrorEnabled(true);
                binding.addProductDescriptionLayout.setError("Description is required!");
                binding.addProductDescription.requestFocus();
                return;
            }

            try {
                String qty = binding.addProductTitle.getText().toString();
                if(qty.isEmpty()){
                    binding.addProductQtyLayout.setErrorEnabled(true);
                    binding.addProductQtyLayout.setError("Description is required!");
                    binding.addProductQty.requestFocus();
                    return;
                }

                String price = binding.addProductTitle.getText().toString();
                if(price.isEmpty()){
                    binding.addProductPriceLayout.setErrorEnabled(true);
                    binding.addProductPriceLayout.setError("Description is required!");
                    binding.addProductPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                new ToastDialog(getParentFragmentManager(), "Invalid number format ( use format 0.00 )");
            }

            if(categoryId == null){
                new ToastDialog(getParentFragmentManager(), "Please select a category");
                return;
            }


            requireActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );

            spinnerDialog.show(getParentFragmentManager(), AddProductFragment.class.getSimpleName());
            newProductRef = db.collection("products").document();
            String productId = newProductRef.getId();

            saveProduct(productId, selectedImages);

        });

    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void addDataToFirebase(){
        String title = binding.addProductTitle.getText().toString();
        String description = binding.addProductDescription.getText().toString();

        String qty = binding.addProductQty.getText().toString();
        String price = binding.addProductPrice.getText().toString();

        int finalQty = Integer.parseInt(qty);
        double finalPrice = Double.parseDouble(price);

        String productId = newProductRef.getId();
        Product productItem = Product.builder().productId(productId).title(title).description(description).price(finalPrice).stockCount(finalQty).status(true).categoryId(categoryId).images(imagesArray).build();
        newProductRef.set(productItem)
                .addOnSuccessListener(aVoid -> {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    checkAllTasksFinished();
                    new ToastDialog(getParentFragmentManager(), "Product Added successfully!");
                    if (isAdded()) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }).addOnFailureListener(aVoid -> {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    checkAllTasksFinished();
                });
    }

    public void saveProduct(String productId, List<Uri> selectedImages) {

        imagesArray = new ArrayList<>();

        List<Task<UploadTask.TaskSnapshot>> uploadTasks = new ArrayList<>();

        for (int i = 0; i < selectedImages.size(); i++) {
            StorageReference fileRef = storage.getReference("product-images")
                    .child(productId)
                    .child("image" + i);
            imagesArray.add("product-images/"+ productId + "/image" + i);
            uploadTasks.add(fileRef.putFile(selectedImages.get(i)));
        }

        Tasks.whenAll(uploadTasks)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Upload", "All " + selectedImages.size() + " images uploaded!");
                    checkAllTasksFinished();
                    addDataToFirebase();
                })
                .addOnFailureListener(e -> {
                    Log.e("Upload", "At least one upload failed: " + e.getMessage());
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    checkAllTasksFinished();
                });
    }

    private void loadListeners() {
        binding.addProductTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addProductTitleLayout.setErrorEnabled(false);
            }
        });
        binding.addProductDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addProductDescriptionLayout.setErrorEnabled(false);
            }
        });
        binding.addProductQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addProductQtyLayout.setErrorEnabled(false);
            }
        });
        binding.addProductPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.addProductPriceLayout.setErrorEnabled(false);
            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    if (selectedImages == null) selectedImages = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            if (selectedImages.size() < 5) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                Log.i("Image uri", imageUri.toString());
                                selectedImages.add(imageUri);
                            } else {
                                new ToastDialog(getParentFragmentManager(), "Only allowed maximum of 5 Images");
                                break;
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        if (selectedImages.size() < 4) {
                            selectedImages.add(result.getData().getData());
                        }
                    }

                    adapter.setImages(selectedImages);

                }
            }
    );

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        categoryId = categoryList.get(i).getCategoryId();
        Log.d("AddProductFragment", "CategoryId: " + categoryId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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