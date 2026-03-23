package lk.damithab.curenexadmin.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.ActivityAddBinding;
import lk.damithab.curenexadmin.fragment.AddItemsHome;

public class AddActivity extends AppCompatActivity {

    private ActivityAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = findViewById(R.id.add_item_toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.add_item_fragment_container, new AddItemsHome())
                    .commit();
        }
    }
}