package com.ruet_cse_1503050.ragib.qp_admin;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccountCreationActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText confirm_password;
    private Button create_new_account_btn;
    private Button login_btn;
    private ProgressBar account_creation_progress;
    private ImageButton show_password;

    private boolean account_creation_running;
    private boolean show_password_indicator=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_create_account);

        Initialize();
    }

    private void Initialize() {
        InitializeUIComponents();
        InitializeData();
    }
    private void InitializeUIComponents() {
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        confirm_password=findViewById(R.id.confirm_password);
        show_password=findViewById(R.id.show_password);
        create_new_account_btn=findViewById(R.id.create_new_account_btn);
        login_btn=findViewById(R.id.login_btn);
        account_creation_progress=findViewById(R.id.account_creation_progress);
        account_creation_progress.setVisibility(View.INVISIBLE);

        create_new_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email_str=email.getText().toString().trim();
                final String password_str=password.getText().toString().trim();
                final String confirm_password_str=confirm_password.getText().toString().trim();
                boolean all_ok=true;
                if(email_str.isEmpty()){
                    all_ok=false;
                    email.setText("");
                }
                if(password_str.isEmpty()){
                    all_ok=false;
                    password.setText("");
                }
                if(confirm_password_str.isEmpty()){
                    all_ok=false;
                    confirm_password.setText("");
                }
                if(all_ok){

                    if(password_str.equals(confirm_password_str)){

                        if(password_str.length()<6){
                            Toast.makeText(AccountCreationActivity.this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
                        } else {
                            account_creation_running=true;
                            create_new_account_btn.setEnabled(false);
                            login_btn.setEnabled(false);
                            create_new_account_btn.setText("Creating new account...");
                            account_creation_progress.setVisibility(View.VISIBLE);
                            Toast.makeText(AccountCreationActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();


                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    if(UtilCollections.isConnectedToInternet(getApplicationContext())){

                                        AppDataStorage.auth.createUserWithEmailAndPassword(email_str,password_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<AuthResult> task) {

                                                if(task.isSuccessful()){
                                                    AppDataStorage.auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(AccountCreationActivity.this, "Account created successfully. Please verify your email to login.", Toast.LENGTH_LONG).show();
                                                                getIntent().putExtra("passed_email",email_str);
                                                                getIntent().putExtra("passed_password",password_str);
                                                                setResult(RESULT_OK,getIntent());
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(AccountCreationActivity.this, "Account creation failed! May be this email is already being used by another account...", Toast.LENGTH_SHORT).show();
                                                    account_creation_running=false;
                                                    create_new_account_btn.setText("Create New Account");
                                                    login_btn.setEnabled(true);
                                                    create_new_account_btn.setEnabled(true);
                                                    account_creation_progress.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(AccountCreationActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                                account_creation_running=false;
                                                create_new_account_btn.setText("Create New Account");
                                                login_btn.setEnabled(true);
                                                create_new_account_btn.setEnabled(true);
                                                account_creation_progress.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    } else {
                        Toast.makeText(AccountCreationActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AccountCreationActivity.this, "Please fill all the fields...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        show_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursor1_pos=password.getSelectionStart();
                int cursor2_pos=confirm_password.getSelectionStart();
                if(!show_password_indicator){
                    password.setTransformationMethod(null);
                    confirm_password.setTransformationMethod(null);
                    show_password.setImageDrawable(getResources().getDrawable(R.drawable.visible_icon));
                    show_password_indicator=true;
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    confirm_password.setTransformationMethod(new PasswordTransformationMethod());
                    show_password.setImageDrawable(getResources().getDrawable(R.drawable.invisible_icon));
                    show_password_indicator=false;
                }
                password.setSelection(cursor1_pos);
                confirm_password.setSelection(cursor2_pos);
            }
        });
    }
    private void InitializeData() {
        //todo any task if needed
    }

    @Override
    public void onBackPressed() {
        if(!account_creation_running){
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Please wait while the account is created...", Toast.LENGTH_SHORT).show();
        }
    }

}