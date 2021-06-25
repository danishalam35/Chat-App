package com.example.gupsup.Register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gupsup.Common.NodeNames;
import com.example.gupsup.Login.LoginActivity;
import com.example.gupsup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

     private TextInputEditText etName,etEmail,etPassword,etConfirmP;
     Button signUp;
    private String name,email,password,confirmPassword;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private Uri localFileUri,serverFileUri;
    private ImageView profile;
    private View pg;

    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName=findViewById(R.id.tName);
        etEmail=findViewById(R.id.tEmail);
        etPassword=findViewById(R.id.tPassword);
        etConfirmP=findViewById(R.id.cPassword);
        signUp=findViewById(R.id.btn_SignUp);
        profile=findViewById(R.id.prof_img);
        pg=findViewById(R.id.progressBar);


        storageReference= FirebaseStorage.getInstance().getReference();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });


    }

    private void createAccount() {

        email=etEmail.getText().toString().trim();
        name=etName.getText().toString().trim();
        password=etPassword.getText().toString().trim();
        confirmPassword=etConfirmP.getText().toString().trim();

        if (email.equals("")){
            etEmail.setError(getString(R.string.enter_email));

        }

       else if (name.equals("")){
            etName.setError(getString(R.string.enter_Name));
        }
        else if (password.equals("")){
            etPassword.setError(getString(R.string.enter_password));
        }
        else if (confirmPassword.equals("")){
            etConfirmP.setError(getString(R.string.enter_confirm_password));
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError(getString(R.string.enter_correct_email));
        }
        else if (!password.equals(confirmPassword)){
            etConfirmP.setError(getString(R.string.password_mismatch));
        }
        else {
            pg.setVisibility(View.VISIBLE);

            final FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    pg.setVisibility(View.GONE);
                    if (task.isSuccessful()){

                        firebaseUser =firebaseAuth.getCurrentUser();
                        if (localFileUri!=null){
                            updateNamePhoto();
                        }
                        else {
                            updateOnlyName();
                        }


                    }

                    else {
                        Toast.makeText(RegisterActivity.this, R.string.signup_fails, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void updateOnlyName()
    {
        pg.setVisibility(View.VISIBLE);

        //it create a request
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()

                .setDisplayName(etName.getText().toString().trim()).build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                pg.setVisibility(View.GONE);
                if (task.isSuccessful()){


                    //update these value
                    String userID = firebaseUser.getUid();
                    databaseReference= FirebaseDatabase.getInstance().getReference(NodeNames.USERS);
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME,etName.getText().toString().trim());
                    hashMap.put(NodeNames.EMAIL,etEmail.getText().toString().trim());
                    hashMap.put(NodeNames.ONLINE,"true");
                    hashMap.put(NodeNames.PHOTO,"");
                    pg.setVisibility(View.VISIBLE);
                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pg.setVisibility(View.GONE);

                            Toast.makeText(RegisterActivity.this,R.string.user_created_successfully , Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                        }
                    });
                }
                else {

                    Toast.makeText(RegisterActivity.this,getString( R.string.failed_to_update_profile,task.getException()) ,Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);

        }
        else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101){
            if (resultCode==RESULT_OK) {
              localFileUri=  data.getData();
              profile.setImageURI(localFileUri);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==102){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            }
            else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateNamePhoto(){

        String strFile = firebaseUser.getUid()+".jpg";
        final StorageReference fileref=storageReference.child("images/"+strFile);
        pg.setVisibility(View.VISIBLE);
        fileref.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //server uri
                        serverFileUri=uri;
                        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setDisplayName(etName.getText().toString().trim())
                                .setPhotoUri(serverFileUri).build();

                        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    pg.setVisibility(View.GONE);
                                    //update these value
                                    String userID = firebaseUser.getUid();
                                    databaseReference= FirebaseDatabase.getInstance().getReference(NodeNames.USERS);
                                    HashMap<String,String> hashMap = new HashMap<>();
                                    hashMap.put(NodeNames.NAME,etName.getText().toString().trim());
                                    hashMap.put(NodeNames.EMAIL,etEmail.getText().toString().trim());
                                    hashMap.put(NodeNames.ONLINE,"true");
                                    hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());
                                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            Toast.makeText(RegisterActivity.this,R.string.user_created_successfully , Toast.LENGTH_SHORT).show();

                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                                        }
                                    });
                                }
                                else {

                                    Toast.makeText(RegisterActivity.this,getString( R.string.failed_to_update_profile,task.getException()) ,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });



                    }
                });

            }
        });

    }
}
