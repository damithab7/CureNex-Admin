package lk.damithab.curenexadmin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.ActivityMainBinding;
import lk.damithab.curenexadmin.databinding.SideNavHeaderBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.fragment.AccountFragment;
import lk.damithab.curenexadmin.fragment.DashboardFragment;
import lk.damithab.curenexadmin.fragment.ProductFragment;
import lk.damithab.curenexadmin.fragment.TherapistFragment;
import lk.damithab.curenexadmin.fragment.UserFragment;
import lk.damithab.curenexadmin.model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        storage = FirebaseStorage.getInstance();

        setContentView(binding.getRoot());

        /// Initializing SideNavHeaderBinding layout by getting headerView from sideNavigation
        View headerView = binding.sideNavigationView.getHeaderView(0);

        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.mainDrawerlayout;
        toolbar = binding.mainToolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navigationView.getMenu().findItem(R.id.side_nav_dashboard).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());

        if (currentUser != null) {

            currentUser.getIdToken(false).addOnSuccessListener(result -> {
                Map<String, Object> claims = result.getClaims();

                if (Boolean.TRUE.equals(claims.get("admin"))) {
                    firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                            .addOnSuccessListener(ds -> {
                                if (ds.exists()) {
                                    User user = ds.toObject(User.class);
                                    sideNavHeaderBinding.drawerHeaderName.setText(user.getFirstName() + " " + user.getLastName());
                                    sideNavHeaderBinding.drawerHeaderEmail.setText(user.getEmail());

                                    if (user.getProfileUrl().startsWith("https")) {
                                        Glide.with(binding.getRoot())
                                                .load(user.getProfileUrl())
                                                .centerCrop()
                                                .into(sideNavHeaderBinding.drawerHeaderImage);
                                    } else {
                                        storage.getReference(user.getProfileUrl())
                                                .getDownloadUrl()
                                                .addOnSuccessListener(uri -> {
                                                    Glide.with(binding.getRoot())
                                                            .load(uri)
                                                            .centerCrop()
                                                            .into(sideNavHeaderBinding.drawerHeaderImage);
                                                });
                                    }
                                }
                                if (spinner.isAdded()) {
                                    spinner.dismissAllowingStateLoss();
                                }
                            }).addOnFailureListener(e -> {

                            });

                }else{
                    FirebaseAuth.getInstance().signOut();
                    redirectToLogin();
                }

                if (spinner.isAdded()) {
                    spinner.dismissAllowingStateLoss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("FireStore", "Error: " + e.getMessage());
                }
            });



        } else {
            if (spinner.isAdded()) {
                spinner.dismissAllowingStateLoss();
            }
        }

    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.navContainerView, fragment);
        transaction.commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.navContainerView, fragment).commit();

    }

    public void redirectToLogin(){
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        navigationView.setCheckedItem(-1);

        Menu navMenu = navigationView.getMenu();
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int g = 0; g < navMenu.size(); g++) {
            navMenu.getItem(g).setChecked(false);
        }

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }

        if (itemId == R.id.nav_dashboard || itemId == R.id.side_nav_dashboard) {
            loadFragment(new DashboardFragment());
            navigationView.getMenu().findItem(R.id.side_nav_dashboard).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);

        } else if (itemId == R.id.nav_therapists || itemId == R.id.side_nav_therapists) {
            loadFragment(new TherapistFragment());
            navigationView.getMenu().findItem(R.id.side_nav_therapists).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_therapists).setChecked(true);

        } else if (itemId == R.id.nav_users || itemId == R.id.side_nav_users) {
            loadFragment(new UserFragment());
            navigationView.getMenu().findItem(R.id.side_nav_users).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_users).setChecked(true);

        } else if (itemId == R.id.nav_products || itemId == R.id.side_nav_products) {
            loadFragment(new ProductFragment());
            navigationView.getMenu().findItem(R.id.side_nav_products).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_products).setChecked(true);

        } else if (itemId == R.id.nav_account|| itemId == R.id.side_nav_account) {
            loadFragment(new AccountFragment());
            navigationView.getMenu().findItem(R.id.side_nav_account).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
        } else if (itemId == R.id.side_nav_logout) {

            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);

            firebaseAuth.signOut();
            redirectToLogin();

            clearNavigationHeader();
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return false;
    }

    public void clearNavigationHeader() {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.side_nav_menu);

        navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
        navigationView.inflateHeaderView(R.layout.side_nav_header);
    }


}