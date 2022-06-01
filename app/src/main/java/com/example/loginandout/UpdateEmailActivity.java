package com.example.loginandout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthentication;
    private String userOldEmail, userNewEmail,userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail, editTextPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");

        progressBar = findViewById(R.id.progress_bar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthentication = findViewById(R.id.textView_update_email_authentication);
        buttonUpdateEmail = findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false); // make button disabled in the beginning auth
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // set old email id on tv
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_old_email);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong! User's details details not available.", Toast.LENGTH_SHORT).show();

        }else {
            reAuthenticate(firebaseUser);
        }
    }

    // ReAuthenticate/Verify User before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtain password for authentication
                userPwd = editTextPwd.getText().toString();

                if (TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Please enter your password for authentication");
                    editTextPwd.requestFocus();

                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified." + "You can update email now.", Toast.LENGTH_SHORT).show();

                                // set TextView to show that is authenticated
                                textViewAuthentication.setText("You are authenticated. You can update your email now.");
                                // Disable editText for password, button to verify user and enable editText for new Email and Update Email button
                                editTextNewEmail.setEnabled(true);
                                editTextPwd.setEnabled(false);
                                buttonVerifyUser.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);

                                // change color of updates
                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
                                        R.color.dark_green));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New Email is registered", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter new email");
                                            editTextNewEmail.requestFocus();
                                        }else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide valid Email");
                                            editTextNewEmail.requestFocus();
                                        }else if (userOldEmail.matches(userNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New Email cannot be same as old email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide valid Email");
                                            editTextNewEmail.requestFocus();
                                        }else{
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }

            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){
                    // verify email
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmailActivity.this,"Email has been updated. Please verify your new Email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try {
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(UpdateEmailActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    //when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh){
            //Refresh the page
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UpdateEmailActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateEmailActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
              }else if (id == R.id.menu_settings){
                  Toast.makeText(UpdateEmailActivity.this, "Menu Settings", Toast.LENGTH_SHORT).show();
             }else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UpdateEmailActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UpdateEmailActivity.this,DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateEmailActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmailActivity.this,MainActivity.class);

            // clear stack to prevent user coming back to userProfileActivity by pressing back bttn
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close Profile Activity
        }
        else {
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}