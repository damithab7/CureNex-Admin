package lk.damithab.curenexadmin.activity;

import static lk.damithab.curenexadmin.util.RegexUtil.isEmailValid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import lk.damithab.curenexadmin.R;
import lk.damithab.curenexadmin.databinding.ActivitySignInBinding;
import lk.damithab.curenexadmin.dialog.SpinnerDialog;
import lk.damithab.curenexadmin.dialog.ToastDialog;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if(currentUser != null){
            currentUser.getIdToken(false).addOnSuccessListener(result -> {
                Map<String, Object> claims = result.getClaims();

                if (Boolean.TRUE.equals(claims.get("admin"))) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }

        loadListeners();

        binding.signinBtn.setOnClickListener(v->{
            String email = binding.signInEmail.getText().toString();
            String password = binding.signInPassword.getText().toString();
            login(email, password);
        });
    }

    public void login(String email, String password) {

        Log.d("SignInActivity", "login: email" +email);
        if (email.isEmpty()) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Email address is required.");
            binding.signInEmail.requestFocus();
            return;
        }
        if (!isEmailValid(email)) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Invalid email format. Please use the format: name@example.com.");
            binding.signInEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.signInPasswordLayout.setErrorEnabled(true);
            binding.signInPasswordLayout.setError("Password is required.");
            binding.signInPassword.requestFocus();
            return;
        }

        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    authResult.getUser().getIdToken(true).addOnSuccessListener(result -> {
                        Map<String, Object> claims = result.getClaims();

                        if (Boolean.TRUE.equals(claims.get("admin"))) {
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            firebaseAuth.signOut();
                            new ToastDialog(getSupportFragmentManager(), "Invalid Credentials. Please try again!");
                        }
                        spinner.dismiss();
                    });
                })
                .addOnFailureListener(e -> {
                    spinner.dismiss();
                    Log.e("AuthError", e.getMessage());
                    new ToastDialog(getSupportFragmentManager(), "Invalid Credentials. Please try again!");
                });
    }

    private void loadListeners(){
        binding.signInEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.signInEmailLayout.setErrorEnabled(false);
            }
        });

        binding.signInPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.signInPasswordLayout.setErrorEnabled(false);
            }
        });
    }

    private void updateUI(FirebaseUser user){
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}