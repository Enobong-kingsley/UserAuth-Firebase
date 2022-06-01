package com.example.loginandout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticate;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG = "DeleteProfileactivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getSupportActionBar().setTitle("Delete your Profile");

        progressBar = findViewById(R.id.progress_bar);
        editTextUserPwd = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticate = findViewById(R.id.textView_delete_user_authentication);
        buttonDeleteUser = findViewById(R.id.button_delete_user);
        buttonReAuthenticate = findViewById(R.id.button_delete_user_authenticate);

        // disable delete button until user is authenticated

        buttonDeleteUser.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong!"
                    + "User's details is not available at the moment.", Toast.LENGTH_SHORT)
                    .show();
            Intent intent = new Intent(DeleteProfileActivity.this,UserProfileActivity.class);
            startActivity(intent);
            finish();
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {

        //ReAuthenticate User bfr changing password
            buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userPwd = editTextUserPwd.getText().toString();

                    if (TextUtils.isEmpty(userPwd)){
                        Toast.makeText(DeleteProfileActivity.this, "Password is needed",
                                Toast.LENGTH_SHORT).show();
                        editTextUserPwd.setError("Please enter your current password to authenticate");
                        editTextUserPwd.requestFocus();
                    }else {
                        progressBar.setVisibility(View.VISIBLE);

                        // ReAuthenticate User now
                        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwd);

                        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressBar.setVisibility(View.GONE);

                                    // Disable editText for current password.
                                    editTextUserPwd.setEnabled(false);

                                    // Enable delete bttn. Disable Authentication Bttn
                                    buttonReAuthenticate.setEnabled(false);
                                    buttonDeleteUser.setEnabled(true);

                                    //set Textview to show User is authenticated/verified
                                    textViewAuthenticate.setText("You are authenticated/ verified." +
                                            "you can changdelete your profile now!");

                                    Toast.makeText(DeleteProfileActivity.this, "Password has been verified." +
                                            "You can delete your profile now. Be careful, this might be irreversible ", Toast.LENGTH_SHORT).show();

                                    //update or change bg color of button
                                    buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(
                                            DeleteProfileActivity.this,R.color.dark_green
                                    ));
                                    buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                           showAlertDialog();
                                        }
                                    });
                                }else {
                                    try {
                                        throw task.getException();
                                    }catch (Exception e){
                                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }

    private void showAlertDialog() {

        // setUp alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("delete User and Related Data?");
        builder.setMessage("Do you  really want to delete your user and related data? this action is irreversible!");

        // Open Email Apps if user clicks continue

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              deleteUser(firebaseUser);
            }
        });

        //Cancel bttn to return back to the user profile activity
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(DeleteProfileActivity.this,UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // change color of continue button
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface){
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        // show alertDialog
        alertDialog.show();
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    deleteUserData(); // delete user data
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this, "User has been deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    try {
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //delete all user data
    private void deleteUserData() {
        // delete display pic
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: Phone Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Delete data from realtime database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(DeleteProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(DeleteProfileActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_settings){
            Toast.makeText(DeleteProfileActivity.this, "Menu Settings", Toast.LENGTH_SHORT).show();
        }else if (id == R.id.menu_change_password){
            Intent intent = new Intent(DeleteProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(DeleteProfileActivity.this,DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this,MainActivity.class);

            // clear stack to prevent user coming back to userProfileActivity by pressing back bttn
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close Profile Activity
        }
        else {
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();

        }


        return super.onOptionsItemSelected(item);
    }
}