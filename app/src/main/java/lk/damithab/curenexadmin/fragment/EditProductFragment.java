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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.adapter.ProductSliderAdapter;
import lk.damithab.curenexadmin.databinding.FragmentEditProductBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;
import lk.damithab.curenexadmin.model.Category;
import lk.damithab.curenexadmin.model.Product;

public class EditProductFragment extends Fragment {

    private FragmentEditProductBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String productId;

    private List<Uri> selectedImages;
    private static final int PICK_IMAGE = 100;
    private ProductSliderAdapter adapter;
    private ArrayList<String> imagesArray;
    private List<Uri> getImagesArray;

    private String categoryId;

    private boolean setProductStatus;
    private List<Category> categoryList;

    private DocumentReference newProductRef;
    private int completedTasks = 0;
    private final int TOTAL_TASKS = 3;
    private SpinnerDialog spinnerDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            Log.d("EditProductFragment", "ProductId : " + productId);
        }
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);


        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        adapter = new ProductSliderAdapter();
        binding.editProductImageSlider.setAdapter(adapter);

        if (selectedImages == null) selectedImages = new ArrayList<>();

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

        binding.productEditImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            activityResultLauncher.launch(intent);
        });

        Spinner categorySpinner = binding.editProductCategorySpinner;

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
                    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            categoryId = categoryList.get(i).getCategoryId();
                            Log.d("EditProductFragment", "CategoryId: " + categoryId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });

        Spinner statusSpinner = binding.editProductStatusSpinner;
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
                    setProductStatus = true;
                } else {
                    setProductStatus = false;
                }
                Log.d("EditProductFragment", "ProductStauts: " + setProductStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinnerDialog = SpinnerDialog.show(getParentFragmentManager());

        db.collection("products").whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        getImagesArray = new ArrayList<>();
                        Product product = qs.toObjects(Product.class).get(0);
                        if (product != null && product.getImages() != null) {

                            binding.editProductTitle.setText(product.getTitle());
                            binding.editProductDescription.setText(product.getDescription());
                            binding.editProductQty.setText(String.valueOf(product.getStockCount()));
                            binding.editProductPrice.setText(String.valueOf(product.getPrice()));
                            int selectedPosition = 0;
                            if (categoryList != null) {
                                for (int i = 0; i < categoryList.size(); i++) {
                                    Category category = categoryList.get(i);
                                    if (Objects.equals(category.getCategoryId(), product.getCategoryId())) {
                                        selectedPosition = i;
                                    }
                                }
                                categorySpinner.setSelection(selectedPosition);
                            }
                            if (product.isStatus()) {
                                statusSpinner.setSelection(0);
                            } else {
                                statusSpinner.setSelection(1);
                            }

                            int totalImages = product.getImages().size();
                            final int[] loadedCount = {0};

                            if(totalImages != 0) {
                                for (int i = 0; i < totalImages; i++) {
                                    StorageReference ref = storage.getReference(product.getImages().get(i));

                                    ref.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                        Log.d("EditProductFragment", "ProductImages URI : " + imageUri);
                                        selectedImages.add(imageUri);
                                        getImagesArray.add(imageUri);

                                        loadedCount[0]++;

                                        if (loadedCount[0] == totalImages) {
                                            adapter.setImages(selectedImages);
                                            if (spinnerDialog.isAdded()) {
                                                spinnerDialog.dismiss();
                                            }
                                        }
                                    }).addOnFailureListener(e -> {
                                        Log.e("EditProductFragment", "Failed to get URL", e);
                                    });
                                }
                            }
                        }
                    }
                });


        loadListeners();

        binding.editProductBtn.setOnClickListener(v -> {
            if (selectedImages == null) {
                new ToastDialog(getParentFragmentManager(), "Please at least select minimum of 1 image. (Maximum of 5 images)");
                return;
            }

            String title = binding.editProductTitle.getText().toString();
            String description = binding.editProductTitle.getText().toString();

            if (title.isEmpty()) {
                binding.editProductTitleLayout.setErrorEnabled(true);
                binding.editProductTitleLayout.setError("Title is required!");
                binding.editProductTitle.requestFocus();
                return;
            }

            if (description.isEmpty()) {
                binding.editProductDescriptionLayout.setErrorEnabled(true);
                binding.editProductDescriptionLayout.setError("Description is required!");
                binding.editProductDescription.requestFocus();
                return;
            }

            try {
                String qty = binding.editProductTitle.getText().toString();
                if (qty.isEmpty()) {
                    binding.editProductQtyLayout.setErrorEnabled(true);
                    binding.editProductQtyLayout.setError("Description is required!");
                    binding.editProductQty.requestFocus();
                    return;
                }

                String price = binding.editProductTitle.getText().toString();
                if (price.isEmpty()) {
                    binding.editProductPriceLayout.setErrorEnabled(true);
                    binding.editProductPriceLayout.setError("Description is required!");
                    binding.editProductPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                new ToastDialog(getParentFragmentManager(), "Invalid number format ( use format 0.00 )");
            }

            if (categoryId == null) {
                new ToastDialog(getParentFragmentManager(), "Please select a category");
                return;
            }

            requireActivity().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );

            spinnerDialog.show(getParentFragmentManager(), AddProductFragment.class.getSimpleName());

            updateProduct();
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

    private void updateDataToFirebase() {
        String title = binding.editProductTitle.getText().toString();
        String description = binding.editProductDescription.getText().toString();

        String qty = binding.editProductQty.getText().toString();
        String price = binding.editProductPrice.getText().toString();

        int finalQty = Integer.parseInt(qty);
        double finalPrice = Double.parseDouble(price);

        DocumentReference productRef = db.collection("products").document(productId);

        productRef.update(
                        "title", title,
                        "description", description,
                        "stockCount", finalQty,
                        "price", finalPrice,
                        "status", setProductStatus,
                        "categoryId", categoryId,
                        "images", imagesArray
                ).addOnSuccessListener(aVoid -> {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    checkAllTasksFinished();
                    new ToastDialog(getParentFragmentManager(), "Product updated successfully!");
                    if (isAdded()) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }).addOnFailureListener(aVoid -> {
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    checkAllTasksFinished();
                });
    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            spinnerDialog.dismiss();
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void updateProduct() {
        /// Delete currently available images first
        List<Task<Void>> deleteTasks = new ArrayList<>();
        for (Uri oldUri : getImagesArray) {
            if (!selectedImages.contains(oldUri)) {
                deleteTasks.add(storage.getReferenceFromUrl(oldUri.toString()).delete());
            }
        }
        if (deleteTasks.isEmpty()) {
            saveProduct();
        } else {
            Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(task -> {
                checkAllTasksFinished();
                saveProduct();
            });
        }
    }


    public void saveProduct() {
        imagesArray = new ArrayList<>();

        List<Task<UploadTask.TaskSnapshot>> uploadTasks = new ArrayList<>();

        for (int i = 0; i < selectedImages.size(); i++) {
            Uri currentUri = selectedImages.get(i);
            String imagePath = "product-images/" + productId + "/image" + i;

            imagesArray.add(imagePath);

            if (currentUri.toString().startsWith("content://") || currentUri.toString().startsWith("file://")) {
                StorageReference fileRef = storage.getReference(imagePath);
                uploadTasks.add(fileRef.putFile(currentUri));
            }
        }
        if (uploadTasks.isEmpty()) {
            updateDataToFirebase();
        } else {
            Tasks.whenAll(uploadTasks)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Upload", "All " + selectedImages.size() + " images uploaded!");
                        checkAllTasksFinished();
                        updateDataToFirebase();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Upload", "At least one upload failed: " + e.getMessage());
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        checkAllTasksFinished();
                    });
        }
    }

    private void loadListeners() {
        binding.editProductTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editProductTitleLayout.setErrorEnabled(false);
            }
        });
        binding.editProductDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editProductDescriptionLayout.setErrorEnabled(false);
            }
        });
        binding.editProductQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editProductQtyLayout.setErrorEnabled(false);
            }
        });
        binding.editProductPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.editProductPriceLayout.setErrorEnabled(false);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.main_toolbar).setVisibility(View.VISIBLE);
    }
}