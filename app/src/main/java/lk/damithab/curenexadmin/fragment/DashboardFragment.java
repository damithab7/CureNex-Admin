package lk.damithab.curenexadmin.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private FirebaseFirestore db;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.dashboardProductBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new ProductFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.dashboardUserBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new UserFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.dashboardTherapistBtn.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.navContainerView, new TherapistFragment())
                    .addToBackStack(null)
                    .commit();
        });

        startDataLoading(true);

    }

    private void checkAllTasksFinished() {
        completedTasks++;
        if (completedTasks >= TOTAL_TASKS) {
            onDataLoad(false);
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void startDataLoading(boolean isShimmer) {
        onDataLoad(isShimmer);
        loadData();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerListingViewContainer.startShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.VISIBLE);
            binding.dashboardMain.setVisibility(View.GONE);
        } else {
            binding.shimmerListingViewContainer.stopShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.GONE);
            binding.dashboardMain.setVisibility(View.VISIBLE);
        }
    }

    private void loadData(){

        db.collection("users").whereEqualTo("userStatus", Boolean.TRUE).count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getCount();
                binding.dashboardUserCount.setText(String.valueOf(count));
                checkAllTasksFinished();
            }
        });

        db.collection("therapist").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getCount();
                binding.dashboardTherapistCount.setText(String.valueOf(count));
                checkAllTasksFinished();
            }
        });

        db.collection("orders").whereEqualTo("status", "PAID").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getCount();
                binding.dashboardProductCount.setText(String.valueOf(count));
                checkAllTasksFinished();
            }
        });
    }
}