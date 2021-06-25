package com.example.gupsup.Password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.gupsup.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText eTPassword,eTConfirmPassword;

    private View pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        eTPassword=findViewById(R.id.tPassword);
        eTConfirmPassword=findViewById(R.id.cPassword);
        pg=findViewById(R.id.progressBar);
    }

    public void btnChange_Password(View view){
        String password=eTPassword.getText().toString().trim();
        String confirmPassword=eTConfirmPassword.getText().toString().trim();


        if (password.equals("")){
            eTPassword.setError(getString(R.string.enter_password));

        }
        else if (confirmPassword.equals("")){
            eTConfirmPassword.setError(getString(R.string.enter_confirm_password));

        }
        else if (!password.equals(confirmPassword)){
            eTConfirmPassword.setError(getString(R.string.password_mismatch));

        }
        else {
            pg.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
            FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
            if (firebaseUser!=null){
                firebaseUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pg.setVisibility(View.GONE);
                        if (task.isSuccessful()){
                            Toast.makeText(ChangePasswordActivity.this, "Password changed Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else {
                            Toast.makeText(ChangePasswordActivity.this, "Something Wrong", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }


    }
}