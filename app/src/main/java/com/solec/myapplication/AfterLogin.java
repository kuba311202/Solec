package com.solec.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import kotlin.text.Charsets;

public class AfterLogin extends AppCompatActivity {
    String wholeLog=" ";
    private String username;
    private String password;
    private String serverAddress;
    private EditText message;
    private TextView messageLog;
    Protocols p = new Protocols();
    String userToSend;
    MyThread myThread;
    Button AddChUs;
    int buttonCount = 0;
    String lastLine;
    String portion;
    long TimestampLong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.username = extras.getString("login");
            this.password = extras.getString("pass");
            this.serverAddress = extras.getString("server");
        }

        Log.i("serverAdress-2", serverAddress);
        myThread = new MyThread();
        new Thread(myThread).start();
        message = findViewById(R.id.Message);
        messageLog = findViewById(R.id.MessageLog);
        AddChUs = findViewById(R.id.Add);
    }

private class MyThread implements Runnable {
    ByteBuffer code;
    Protocols p = new Protocols();
    SSLSocket socket;
    byte[] packetType = new byte[1];
    byte[] readPayloadLength = new byte[3];
    ByteBuffer handshakeBuffer;
    InputStream dis;
    DataOutputStream dos;
    byte[] readMessage;
    @Override
    public void run() {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            socket = (SSLSocket) factory.createSocket(serverAddress, 9999);
            if(!serverAddress.equals("rctt.net")){
                serverAddress = "localhost";
            }

            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            dis.readNBytes(readPayloadLength, 0, 3);
            int payloadLengthInt = 3;
            byte[] readPayload = new byte[payloadLengthInt];
            dis.readNBytes(readPayload, 0, payloadLengthInt);

            handshakeBuffer = p.getHandshake();
            dos.write(handshakeBuffer.array());


            dos.write(sendAuth(username+"@"+serverAddress,password).array());

            dos.write(p.getList().array());

            while(true){
                if (!socket.isClosed()) {
                    dis.readNBytes(packetType,0,1);
                    if (packetType[0] == 0x05) {
                        byte[] readAllLength = new byte[2];
                        dis.readNBytes(readAllLength, 0, 2);
                        byte[] readSenderLength = new byte[2];
                        dis.readNBytes(readSenderLength, 0, 2);
                        int readSenderLengthInt = p.decodeBytesToInt(readSenderLength);
                        byte[] readSender = new byte[readSenderLengthInt];
                        dis.readNBytes(readSender, 0, readSenderLengthInt);
                        String Sender = p.decodeBytesToString(readSender);

                        byte[] readTargetLength = new byte[2];
                        dis.readNBytes(readTargetLength, 0, 2);
                        int readTargetLengthInt = p.decodeBytesToInt(readTargetLength);
                        byte[] readTarget = new byte[readTargetLengthInt];
                        dis.readNBytes(readTarget, 0, readTargetLengthInt);
                        String Target = p.decodeBytesToString(readTarget);


                        byte[] readTimestamp = new byte[8];
                        dis.readNBytes(readTimestamp, 0, 8);
                        Long Timestamp = ByteBuffer.wrap(readTimestamp).getLong();
                        Instant insTimestamp = Instant.ofEpochMilli(Timestamp);

                        byte[] readMessageLength = new byte[2];
                        dis.readNBytes(readMessageLength, 0, 2);
                        int readMessageLengthInt = p.decodeBytesToInt(readMessageLength);
                        readMessage = new byte[readMessageLengthInt];
                        dis.readNBytes(readMessage, 0, readMessageLengthInt);
                        String Message = p.decodeBytesToString(readMessage);
                        iterateLog(Sender + ":   " + Message + "\n\n",Sender,Target,insTimestamp);

                    } else if (packetType[0] == 0x01) {
                        byte[] successRead = new byte[2];
                        dis.readNBytes(successRead, 0, 2);
                    }
                    else if(packetType[0]==0x02){
                        byte[] error = new byte[4];
                        dis.readNBytes(error,0,4);
                    }
                    else if(packetType[0]==0x10){
                        byte[] readLength = new byte[2];
                        dis.readNBytes(readLength, 0, 2);
                        int readLengthInt = p.decodeBytesToInt(readLength);
                        byte[] readAddress = new byte[readLengthInt];
                        dis.readNBytes(readAddress, 0, readLengthInt);
                        String Address = p.decodeBytesToString(readAddress);
                        if(!Address.isEmpty()){
                            try {
                            Address = Address.substring(2);
                            File file = new File(getApplicationContext().getFilesDir(), Address);
                            FileOutputStream writer;
                            File path = getApplicationContext().getFilesDir();
                            if (file.exists()) {
                                    writer = new FileOutputStream(new File(path, Address));
                                    writer.write("".getBytes());
                                    writer.close();
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            if(Objects.equals(getLogin()+"@"+serverAddress, Address)){
                            }
                            else if(Address.charAt(0) == '#'){
                                Address = Address.substring(1,Address.length()-9);
                                joinChannel(Address);
                            }else{
                                Address = Address.substring(0,Address.length()-9);
                                joinUser(Address);
                            }
                        }
                    }
                }
            }


        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }


    public String getLogin(){
        return username;
    }

    public void sendMessage(ByteBuffer code) {
        this.code = code;
        try {
            dos.write(code.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public ByteBuffer sendAuth(String login, String pass){
        ByteBuffer loginBuffer = p.encodeString(login);
        ByteBuffer passBuffer = p.encodeString(pass);
        loginBuffer.rewind();
        passBuffer.rewind();
        ByteBuffer AuthBuffer = p.getAuth(loginBuffer,passBuffer);
        AuthBuffer.rewind();
        return AuthBuffer;
    }
}


public void UserButton(View v){
    Button b = (Button)v;
    this.userToSend = b.getText().toString()+"@"+serverAddress;
    ByteBuffer Address = p.encodeString(userToSend);

    try{
        File path = getApplicationContext().getFilesDir();
        File readFrom = new File(path,userToSend);
        BufferedReader br = new BufferedReader(new FileReader(readFrom));
        String line;
        while((line = br.readLine())!= null){
            this.lastLine = line;
        }

    } catch (Exception e) {
        throw new RuntimeException(e);
    }


    messageLog.setText("");
    myThread.sendMessage(p.getHistory(Address,TimestampLong,100));
    File path = getApplicationContext().getFilesDir();
    File readFrom = new File(path,userToSend);
    byte[] content = new byte[(int) readFrom.length()];
    //FileOutputStream writer = null;
    try{
        //writer = new FileOutputStream(new File(path, userToSend));
        //writer.write("".getBytes());
        FileInputStream fis = new FileInputStream(readFrom);
        fis.read(content);
        messageLog.setText(p.decodeBytesToString(content));
    }
    catch (Exception e) {
        message.setText("");
    }
}

public void ChannelButton(View v){
    Button b = (Button)v;
    this.userToSend = "#"+b.getText().toString()+"@"+serverAddress;
    ByteBuffer Address = p.encodeString(userToSend);
    try{
        File path = getApplicationContext().getFilesDir();
        File readFrom = new File(path,userToSend);
        BufferedReader br = new BufferedReader(new FileReader(readFrom));
        String line;
        while((line = br.readLine())!= null){
            this.lastLine = line;
        }

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    if(lastLine != null && lastLine.length()<=24){
        this.portion = lastLine.substring(lastLine.length()-24);
        Log.i("p", portion);
        this.TimestampLong = Instant.parse(portion).toEpochMilli();
    }else{
        this.TimestampLong = 0;
    }
    Log.i("time", String.valueOf(TimestampLong));
    messageLog.setText("");
    myThread.sendMessage(p.getHistory(Address,TimestampLong,100));
    File path = getApplicationContext().getFilesDir();
    File readFrom = new File(path,userToSend);
    byte[] content = new byte[(int) readFrom.length()];
    //FileOutputStream writer = null;
    try{
        //writer = new FileOutputStream(new File(path, userToSend));
        //writer.write("".getBytes());
        FileInputStream fis = new FileInputStream(readFrom);
        fis.read(content);
        messageLog.setText(p.decodeBytesToString(content));
    }
    catch (Exception e) {
        message.setText("");
    }
}


public void sendMessageButton(View v) {
    String content = message.getText().toString();
    Log.i("cont",content);
    String myUser = username;
    ByteBuffer contentBuffer = p.encodeString(content);
    ByteBuffer myUserBuffer = p.encodeString(username+"@"+serverAddress);
    ByteBuffer userToSendBuffer = p.encodeString(userToSend);
    contentBuffer.rewind();
    myUserBuffer.rewind();
    userToSendBuffer.rewind();
    ByteBuffer messageBuffer = p.getMessage(myUserBuffer,userToSendBuffer,contentBuffer);
    messageBuffer.rewind();
    iterateLog(myUser +":   " + content + "\n\n",myThread.getLogin(),userToSend,p.getTimestamp());
    Log.i("buffer2", String.valueOf(messageBuffer));
    message.setText("");
    myThread.sendMessage(messageBuffer);
    for(int i=0;i<messageBuffer.limit();i++){
        Log.i("mess", String.valueOf(messageBuffer.get(i)));
    }
}

public void addChannel(View v){
    AlertDialog.Builder PopUp = new AlertDialog.Builder(this);
    PopUp.setTitle("Add");
    EditText name = new EditText(this);
    PopUp.setView(name);

    PopUp.setPositiveButton("Add channel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Log.i("channname", String.valueOf(name.getText()));
          //  if(String.valueOf(name.getText()).equals("test")) {
                joinChannel(String.valueOf(name.getText()), v);
           // }else{
                //messageLog.setText("This channel doesn't exist!");
           // }
        }
    });
    PopUp.setNeutralButton("Add User", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
           // if(String.valueOf(name.getText()).equals("user1") || String.valueOf(name.getText()).equals("user2") || String.valueOf(name.getText()).equals("user3")) {
                joinUser(String.valueOf(name.getText()), v);
           // }else{
                //messageLog.setText("This user doesn't exist!");
           // }
        }
    });

    PopUp.show();
}

public void joinUser(String userName, View v){
    buttonCount++;
    userToSend = userName+"@"+serverAddress;
    File file = new File(getApplicationContext().getFilesDir(),userToSend);
    if(!file.exists()){
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    Button b = (Button)v;
    Button newButton = new AppCompatButton(this);
    newButton.setText(userName);
    newButton.setId(View.generateViewId());
    newButton.setBackgroundResource(R.drawable.square_button);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    newButton.setLayoutParams(params);

    newButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserButton(v);
        }
    });
    ConstraintLayout mainLayout = findViewById(R.id.after_log);
    mainLayout.addView(newButton);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(mainLayout);

    constraintSet.connect(newButton.getId(), ConstraintSet.TOP, b.getId(), ConstraintSet.BOTTOM, 150*buttonCount);

    constraintSet.connect(newButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

    constraintSet.applyTo(mainLayout);
}

public void joinUser(String userName){

        runOnUiThread(() -> {
            buttonCount++;
            Log.i("username",userName);
            userToSend = userName + "@" + serverAddress;

            File file = new File(getApplicationContext().getFilesDir(), userToSend);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ignored) {

                }
            }

            Button newButton = new AppCompatButton(this);
            newButton.setText(userName);
            newButton.setId(View.generateViewId());
            newButton.setBackgroundResource(R.drawable.square_button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            newButton.setLayoutParams(params);

            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserButton(v);
                }
            });
            ConstraintLayout mainLayout = findViewById(R.id.after_log);
            mainLayout.addView(newButton);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainLayout);

            constraintSet.connect(newButton.getId(), ConstraintSet.TOP, AddChUs.getId(), ConstraintSet.BOTTOM, 150 * buttonCount);

            constraintSet.connect(newButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

            constraintSet.applyTo(mainLayout);
        });
    }

public void joinChannel(String channelName, View v){

    buttonCount++;
    String channel = "#"+channelName+"@"+serverAddress;
    ByteBuffer myUser = p.encodeString(username+"@"+serverAddress);
    ByteBuffer chName = p.encodeString(channel);
    char mode = 1;

    myUser.rewind();
    chName.rewind();
    ByteBuffer channelJoinBuffer = p.getJoinChannel(myUser,chName,mode);
    channelJoinBuffer.rewind();
    userToSend = channel;
    File file = new File(getApplicationContext().getFilesDir(),userToSend);
    if(!file.exists()){
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    Button b = (Button)v;
    Button newButton = new AppCompatButton(this);
    newButton.setText(channelName);
    newButton.setId(View.generateViewId());
    newButton.setBackgroundResource(R.drawable.square_button);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    newButton.setLayoutParams(params);

    newButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ChannelButton(v);
        }
    });
    ConstraintLayout mainLayout = findViewById(R.id.after_log);
    mainLayout.addView(newButton);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(mainLayout);

    constraintSet.connect(newButton.getId(), ConstraintSet.TOP, b.getId(), ConstraintSet.BOTTOM, 150*buttonCount);

    constraintSet.connect(newButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

    constraintSet.applyTo(mainLayout);
}
    public void joinChannel(String channelName){
        runOnUiThread(() -> {

            buttonCount++;
            String channel = "#" + channelName + "@" + serverAddress;
            ByteBuffer myUser = p.encodeString(username + "@" + serverAddress);
            ByteBuffer chName = p.encodeString(channel);
            char mode = 1;

            myUser.rewind();
            chName.rewind();
            ByteBuffer channelJoinBuffer = p.getJoinChannel(myUser, chName, mode);
            channelJoinBuffer.rewind();
            userToSend = channel;
            File file = new File(getApplicationContext().getFilesDir(), userToSend);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ignored) {
                }
            }
            Button newButton = new AppCompatButton(this);
            newButton.setText(channelName);
            newButton.setId(View.generateViewId());
            newButton.setBackgroundResource(R.drawable.square_button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            newButton.setLayoutParams(params);

            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChannelButton(v);
                }
            });
            ConstraintLayout mainLayout = findViewById(R.id.after_log);
            mainLayout.addView(newButton);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainLayout);

            constraintSet.connect(newButton.getId(), ConstraintSet.TOP, AddChUs.getId(), ConstraintSet.BOTTOM, 150 * buttonCount);

            constraintSet.connect(newButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

            constraintSet.applyTo(mainLayout);
        });
    }
public void iterateLog(String addToLog, String Sender, String Target, Instant Timestamp){

    runOnUiThread(() -> {
        try {
            Date date = Date.from(Timestamp);
            File path = getApplicationContext().getFilesDir();
            FileOutputStream writer = null;
            File readFrom = null;
            Log.i("Ścieżka i nazwa", userToSend + " " + Sender + Target);
            Log.i("nick", myThread.getLogin() + "@" + serverAddress);
            if(Objects.equals(Target, myThread.getLogin())){
                readFrom = new File(path, Sender);
                byte[] content = new byte[(int) readFrom.length()];
                FileInputStream fis = new FileInputStream(readFrom);
                fis.read(content);
                wholeLog = p.decodeBytesToString(content) + addToLog + android.text.format.DateFormat.getMediumDateFormat(this)
                        .format(date)
                        + " "
                        + android.text.format.DateFormat.getTimeFormat(this)
                        .format(date);
                messageLog.setText(wholeLog);
                writer = new FileOutputStream(new File(path, Target));
                Log.i("Ścieżka i nazwa2", userToSend + " " + Sender);
            }
            else if (!Objects.equals(Target, myThread.getLogin() + "@" + serverAddress) && !Objects.equals(Sender, myThread.getLogin() + "@" + serverAddress)) {
                readFrom = new File(path, Target);
                byte[] content = new byte[(int) readFrom.length()];
                FileInputStream fis = new FileInputStream(readFrom);
                fis.read(content);
                wholeLog = p.decodeBytesToString(content) + addToLog + android.text.format.DateFormat.getMediumDateFormat(this)
                        .format(date)
                        + " "
                        + android.text.format.DateFormat.getTimeFormat(this)
                        .format(date);
                messageLog.setText(wholeLog);
                writer = new FileOutputStream(new File(path, Target));
                Log.i("Ścieżka i nazwa2", userToSend + " " + Sender);
            } else if (Objects.equals(Sender, myThread.getLogin() + "@" + serverAddress) || Sender.contentEquals(userToSend)) {
                readFrom = new File(path, userToSend);
                byte[] content = new byte[(int) readFrom.length()];
                FileInputStream fis = new FileInputStream(readFrom);
                fis.read(content);
                wholeLog = p.decodeBytesToString(content) + addToLog + android.text.format.DateFormat.getMediumDateFormat(this)
                        .format(date)
                        + " "
                        + android.text.format.DateFormat.getTimeFormat(this)
                        .format(date);
                messageLog.setText(wholeLog);
                writer = new FileOutputStream(new File(path, userToSend));
                Log.i("Ścieżka i nazwa2", userToSend + " " + Sender);
            } else if (!Sender.contentEquals(userToSend)) {
                readFrom = new File(path, Sender);
                byte[] content = new byte[(int) readFrom.length()];
                FileInputStream fis = new FileInputStream(readFrom);
                fis.read(content);
                wholeLog = p.decodeBytesToString(content) + addToLog + android.text.format.DateFormat.getMediumDateFormat(this)
                        .format(date)
                        + " "
                        + android.text.format.DateFormat.getTimeFormat(this)
                        .format(date);
                writer = new FileOutputStream(new File(path, Sender));
                Log.i("Ścieżka i nazwa3", userToSend + " " + Sender);
            }

            assert writer != null;
            writer.write((wholeLog + "\n").getBytes(Charsets.UTF_8));
            writer.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });

}



}
