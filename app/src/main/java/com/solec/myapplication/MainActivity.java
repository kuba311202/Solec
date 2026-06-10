package com.solec.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Arrays;

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
        Log.i("serverAdress1", serverAddress);
        Intent afterLogin = new Intent(getApplicationContext(),AfterLogin.class);
        afterLogin.putExtra("login",login);
        afterLogin.putExtra("pass",pass);
        afterLogin.putExtra("server", serverAddress);
        startActivity(afterLogin);
    }
}


