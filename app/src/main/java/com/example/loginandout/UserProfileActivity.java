package com.example.loginandout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome, textViewFullName, textViewEmail, textViewDob, textViewGender, textViewMobile;
    private ProgressBar progressBar;
    private String fullName, email, doB, gender, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("Home");

        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewDob = findViewById(R.id.textView_show_dob);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewMobile = findViewById(R.id.textView_show_mobile);
        progressBar = findViewById(R.id.progress_bar);

        //set nClickListener on ImageClick
        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePiActiviity.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null){
            Toast.makeText(UserProfileActivity.this, "Something went wrong! User's details are not available at the moment", Toast.LENGTH_SHORT).show();

        }else {

            chechIfEmailVerified(firebaseUser);

            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);

        }
    }

    private void chechIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    // Users coming to UserProfileActivity after successful registration
    private void showAlertDialog() {
        // setUp alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. you can not login without verification next time.");

        // Open Email Apps if user clicks continue

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // TOO EMAIL APP IN NEW WINDOW NOT WITHIN THE APP
                startActivity(intent);
            }
        });

        // create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // show alertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        //Extracting user reference from database for registered users
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB = readUserDetails.doB;
                    gender = readUserDetails.gender;
                    mobile = readUserDetails.mobile;

                    textViewWelcome.setText("Welcome, "+ fullName + "!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDob.setText(doB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
//        }else if (id == R.id.menu_update_profile){
//            Intent intent = new Intent(UserProfileActivity.this,UpdateProfileActivity.class);
//            startActivity(intent);
//        }else if (id == R.id.menu_update_email){
//            Intent intent = new Intent(UserProfileActivity.this,UpdateProfileEmailActivity.class);
//            startActivity(intent);
//        }else if (id == R.id.menu_settings){
//            Toast.makeText(UserProfileActivity.this, "Menu Settings", Toast.LENGTH_SHORT).show();
//        }else if (id == R.id.menu_change_password){
//            Intent intent = new Intent(UserProfileActivity.this,ChangePasswordActivity.class);
//            startActivity(intent);
//        }else if (id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UserProfileActivity.this,MenuDeleteProfile.class);
//            startActivity(intent);
        }else if (id == R.id.menu_logout){
           authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this,MainActivity.class);

            // clear stack to prevent user coming back to userProfileActivity by pressing back bttn
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close Profile Activity
        }
        else {
            Toast.makeText(UserProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }


        return super.onOptionsItemSelected(item);
    }
}