package lk.damithab.curenexadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.FragmentAddItemsHomeBinding;


public class AddItemsHome extends Fragment {

    private FragmentAddItemsHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddItemsHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addProductItemCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.add_item_fragment_container, new AddProductFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.addUserItemCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.add_item_fragment_container, new AddUserFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.addTherapistItemCard.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.add_item_fragment_container, new AddTherapistFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}