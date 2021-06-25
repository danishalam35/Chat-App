package com.example.gupsup.Profile;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gupsup.Common.NodeNames;
import com.example.gupsup.Login.LoginActivity;
import com.example.gupsup.Password.ChangePasswordActivity;
import com.example.gupsup.R;
import com.example.gupsup.Register.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail;
    private String name, email;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private Uri localFileUri, serverFileUri;
    private ImageView profile;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private View pg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        etName = findViewById(R.id.tName);
        etEmail = findViewById(R.id.tEmail);
        profile=findViewById(R.id.profilimg);
        pg=findViewById(R.id.progressBar);


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        if (firebaseUser != null) {
            etName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            serverFileUri = firebaseUser.getPhotoUrl();

            if (serverFileUri!=null){

                Glide.with(this)
                        .load(serverFileUri)
                        .placeholder(R.drawable.defaultimg)
                        .error(R.drawable.defaultimg)
                        .into(profile);
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
                pg.setVisibility(View.GONE);
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
                                    //update these value
                                    String userID = firebaseUser.getUid();
                                    databaseReference= FirebaseDatabase.getInstance().getReference(NodeNames.USERS);
                                    HashMap<String,String> hashMap = new HashMap<>();

                                    hashMap.put(NodeNames.NAME,etName.getText().toString().trim());
                                    hashMap.put(NodeNames.PHOTO,serverFileUri.getPath());

                                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                         finish();


                                        }
                                    });
                                }
                                else {

                                    Toast.makeText(ProfileActivity.this,getString( R.string.failed_to_update_profile,task.getException()) ,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });



                    }
                });

            }
        });

    }
    public void changeImage(View view){

        if (serverFileUri==null){
            pickImage();
        }
        else {
            PopupMenu popupMenu=new PopupMenu(this,view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_item,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id =item.getItemId();
                    if (id==R.id.menuChangedPic){
                        pickImage();

                    }
                    else if (id == R.id.menuRemovePic){
                        removePhoto();
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
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

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            finish();
                        }
                    });
                }
                else {

                    Toast.makeText(ProfileActivity.this,getString( R.string.failed_to_update_profile,task.getException()) ,Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void removePhoto()
    {
        pg.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setDisplayName(etName.getText().toString().trim())
                .setPhotoUri(null).build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pg.setVisibility(View.GONE);

                if (task.isSuccessful()){
                    //update these value
                    String userID = firebaseUser.getUid();
                    databaseReference= FirebaseDatabase.getInstance().getReference(NodeNames.USERS);
                    HashMap<String,String> hashMap = new HashMap<>();


                    hashMap.put(NodeNames.PHOTO,"");

                    databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Toast.makeText(ProfileActivity.this,R.string.photo_removed_successfully , Toast.LENGTH_SHORT).show();



                        }
                    });
                }
                else {

                    Toast.makeText(ProfileActivity.this,getString( R.string.failed_to_update_profile,task.getException()) ,Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    public void btn_save_click(View view){
        if (etName.getText().toString().trim().equals(""))
        {
            etName.setError(getString(R.string.enter_Name));

        }
        else{

               if (localFileUri!=null)
                   updateNamePhoto();
               else
                   updateOnlyName();

        }
    }
    public void btnLogoutClick(View view){
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        startActivity(new Intent(ProfileActivity.this,LoginActivity.class));
        finish();
    }

    public void setChangePassword(View view){
        startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
    }
}