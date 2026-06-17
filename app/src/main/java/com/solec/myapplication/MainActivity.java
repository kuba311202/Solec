package com.solec.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText chooseSerwer;
    String login;
    String pass;
    String serverAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        username = findViewById(R.id.Username);
        password = findViewById(R.id.Password);
        chooseSerwer = findViewById(R.id.chooseServer);
    }

    public void changeLayoutAfterLogin(View view) {
        this.login = String.valueOf(username.getText());
        this.pass = String.valueOf(password.getText());
        this.serverAddress = String.valueOf(chooseSerwer.getText());
        Intent afterLogin = new Intent(getApplicationContext(),AfterLogin.class);
        afterLogin.putExtra("login",login);
        afterLogin.putExtra("pass",pass);
        afterLogin.putExtra("server", serverAddress);
        startActivity(afterLogin);
    }
}


