package com.example.gupsup.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gupsup.MainActivity;
import com.example.gupsup.R;
import com.example.gupsup.Register.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail,etPassword;
    private String email,password;
    private TextView RecoverPassword;
     private FirebaseAuth firebaseAuth;
     private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail=findViewById(R.id.tEmail);
        etPassword=findViewById(R.id.tPassword);
        firebaseAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progressBar);

    }
    public void btn_Login(View view){
        email=etEmail.getText().toString().trim();
        password=etPassword.getText().toString().trim();

        if (email.equals("")){
            etEmail.setError(getString(R.string.enter_email));

        }
        else if (password.equals("")){
            etPassword.setError(getString(R.string.enter_password));
        }
        else {


            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful())
                    {

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Login Failed:"+task.getException(), Toast.LENGTH_SHORT).show();
                    }

                }
            });


        }
    }

    public void not_account(View  view){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
    public void tvResetPassword(View  view){
        recoveredpass();
    }

    private void recoveredpass() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        builder.setTitle("Recover Password");
        //set linear Layout

        LinearLayout linearLayout=new LinearLayout(this);
        //views
        final EditText emailET=new EditText(this);
        emailET.setHint("Email");
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailET.setMinEms(10);
        linearLayout.addView(emailET);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);
        //button
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email=emailET.getText().toString().trim();
                beginRecovery(email);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //
                dialog.dismiss();

            }
        });
        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {


     firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
//this method check a user log in or not
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        //do not login in every time
        if (firebaseUser!=null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }

    }
}