package com.ruet_cse_1503050.ragib.qp_admin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AccountInfoActivity extends AppCompatActivity {

    private TextView email;
    private Button sign_in_another;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_account_info);

        Initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        email.setText("Email: "+AppDataStorage.current_user.getEmail());
    }

    private void Initialize() {
        InitializeUI();
    }

    private void InitializeUI() {
        email=findViewById(R.id.email);
        sign_in_another=findViewById(R.id.sign_in_another);
        sign_in_another.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AccountInfoActivity.this,LoginActivity.class);
                intent.putExtra("back_mode",1);
                startActivityForResult(intent, AppDataStorage.ACCOUNT_TASK_CODE);
            }
        });
    }
}
