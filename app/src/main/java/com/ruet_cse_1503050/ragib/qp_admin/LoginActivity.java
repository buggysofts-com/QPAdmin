package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login_btn;
    private Button create_new_account_btn;
    private ProgressBar login_progress;
    private ImageButton show_password;

    private boolean login_running=false;
    private boolean show_password_indicator=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        Initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case AppDataStorage.ACCOUNT_TASK_CODE:{
                    email.setText(data.getStringExtra("passed_email"));
                    password.setText(data.getStringExtra("passed_password"));
                    /*getIntent().putExtra("return_mode",1);
                    setResult(RESULT_OK,getIntent());
                    finish();*/
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!login_running){
            int back_mode=getIntent().getIntExtra("back_mode",0);
            if(back_mode==0){
                getIntent().putExtra("return_mode",0);
            } else {
                getIntent().putExtra("return_mode",1);
            }
            setResult(RESULT_OK,getIntent());
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Please wait while login is complete...", Toast.LENGTH_SHORT).show();
        }
    }

    private void Initialize() {
        InitiazeUIComponents();
        InitializeData();
    }

    private void InitiazeUIComponents() {

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        show_password=findViewById(R.id.show_password);
        login_btn=findViewById(R.id.login_btn);
        create_new_account_btn=findViewById(R.id.create_new_account_btn);
        login_progress=findViewById(R.id.login_progress);
        login_progress.setVisibility(View.INVISIBLE);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email_str=email.getText().toString().trim();
                final String password_str=password.getText().toString().trim();
                boolean all_ok=true;
                if(email_str.isEmpty()){
                    all_ok=false;
                    email.setText("");
                }
                if(password_str.isEmpty()){
                    all_ok=false;
                    password.setText("");
                }
                if(all_ok){
                    login_running=true;
                    login_btn.setEnabled(false);
                    create_new_account_btn.setEnabled(false);
                    login_btn.setText("Logging In...");
                    login_progress.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(UtilCollections.isConnectedToInternet(getApplicationContext())){
                                AppDataStorage.auth.signInWithEmailAndPassword(email_str,password_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            if(AppDataStorage.auth.getCurrentUser().isEmailVerified()){
                                                Toast.makeText(LoginActivity.this, "Logged in as "+email_str, Toast.LENGTH_SHORT).show();
                                                getIntent().putExtra("return_mode",1);
                                                setResult(RESULT_OK,getIntent());
                                                finish();
                                            } else {
                                                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                                                builder.setMessage(
                                                        "The email you've entered is not verified.\n\n" +
                                                        "If you haven't received the verification link, " +
                                                        "click the button bellow to resend a verification link at "+email_str
                                                );
                                                builder.setPositiveButton("Resend Verification Link", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(LoginActivity.this, "Sending Verification...", Toast.LENGTH_SHORT).show();
                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if(UtilCollections.isConnectedToInternet(LoginActivity.this)){
                                                                    AppDataStorage.auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Toast.makeText(LoginActivity.this, "Email sent...", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            } else {
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Toast.makeText(LoginActivity.this, "Error while sending email. Please try again after sometime.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(LoginActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }).start();
                                                    }
                                                });
                                                builder.setNegativeButton("Cancel",null);
                                                builder.show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Log In Failed. Check your Email and Password.", Toast.LENGTH_SHORT).show();
                                        }
                                        login_running=false;
                                        login_btn.setText("Log In");
                                        login_btn.setEnabled(true);
                                        create_new_account_btn.setEnabled(true);
                                        login_progress.setVisibility(View.INVISIBLE);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "Internet connection required!", Toast.LENGTH_SHORT).show();
                                        login_running=false;
                                        login_btn.setText("Log In");
                                        login_btn.setEnabled(true);
                                        create_new_account_btn.setEnabled(true);
                                        login_progress.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }
                    }).start();

                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all the fields...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        create_new_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,AccountCreationActivity.class);
                startActivityForResult(intent,AppDataStorage.ACCOUNT_TASK_CODE);
            }
        });

        show_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursor_pos=password.getSelectionStart();
                if(!show_password_indicator){
                    password.setTransformationMethod(null);
                    show_password.setImageDrawable(getResources().getDrawable(R.drawable.visible_icon));
                    show_password_indicator=true;
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    show_password.setImageDrawable(getResources().getDrawable(R.drawable.invisible_icon));
                    show_password_indicator=false;
                }
                password.setSelection(cursor_pos);
            }
        });
    }

    private void InitializeData() {
        //todo if any data needs to be initialized
    }
}
