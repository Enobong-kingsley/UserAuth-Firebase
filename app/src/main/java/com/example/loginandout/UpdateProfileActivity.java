package com.example.loginandout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBar = findViewById(R.id.progress_bar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // show profile data
        showProfile(firebaseUser);

        //upload profile pic
        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateProfileActivity.this,UploadProfilePiActiviity.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonUpdateEmail = findViewById(R.id.button_update_profile_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateProfileActivity.this,UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });
   // }
        //setting up datepicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Extracting saved dd, mm, yyyy int diff variables by creating an array dilimited by "/"
                String textSADoB[] = textDoB.split("/");

                int day = Integer.parseInt(textSADoB[0]);
                int month =Integer.parseInt(textSADoB[1]) - 1; // to take care of index starting from 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;


                //date picker dialog
                picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int day, int month, int dayOfMonth) {

                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year,month,day);
                picker.show();
            }
        });

        // Update Profile
        Button buttonUpdateProofile = findViewById(R.id.button_update_profile);
        buttonUpdateProofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
                
            }
        });

    }
    // Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {
        int  selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        // validating user
        String mobileRegex ="[6-9][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobileParttern = Pattern.compile(mobileRegex);
        mobileMatcher = mobileParttern.matcher(textMobile);

        if (TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this, "please enter full name", Toast.LENGTH_SHORT).show();
            editTextUpdateName.setError("Full Name is required");
            editTextUpdateName.requestFocus();
        }else if (TextUtils.isEmpty(textDoB)){
            Toast.makeText(UpdateProfileActivity.this, "please re-enter date of birth", Toast.LENGTH_SHORT).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        }else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())){
            Toast.makeText(UpdateProfileActivity.this, "please select a gender", Toast.LENGTH_SHORT).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        }else if (TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfileActivity.this, "please enter your mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile No. is required");
            editTextUpdateMobile.requestFocus();
        }else if (textMobile.length() != 10){
            Toast.makeText(UpdateProfileActivity.this, "please re-enter your mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile No. should be 10 digits");
            editTextUpdateMobile.requestFocus();
        }else if (!mobileMatcher.find()){
            Toast.makeText(UpdateProfileActivity.this, "please re-enter your mobile no.", Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Mobile No. is not valid");
            editTextUpdateMobile.requestFocus();

        }else{

            // Obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            // Enter User data into the firebase realtime database. set up dependencies
            ReadWriteUserDetails writeUserDetails   = new ReadWriteUserDetails(textDoB, textGender, textMobile);

            //Extract User reference from DB for "Registered Users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        // set new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();

                        // stop back stack

                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        try {
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

        }
    }

    // fetch data from firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        // extracting User Reference from Database for "regstered users"

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    textFullName = firebaseUser.getDisplayName();
                    textDoB = readUserDetails.doB;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    // show gender through radio button
                    if (textGender.equals("Male")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    }else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                }else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
      //  }else if (id == R.id.menu_settings){
      //      Toast.makeText(UserProfileActivity.this, "Menu Settings", Toast.LENGTH_SHORT).show();
       // }else if (id == R.id.menu_change_password){
//            Intent intent = new Intent(UserProfileActivity.this,ChangePasswordActivity.class);
//            startActivity(intent);
//        }else if (id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UserProfileActivity.this,MenuDeleteProfile.class);
//            startActivity(intent);
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this,MainActivity.class);

            // clear stack to prevent user coming back to userProfileActivity by pressing back bttn
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close Profile Activity
        }
        else {
            Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }


        return super.onOptionsItemSelected(item);
    }
}