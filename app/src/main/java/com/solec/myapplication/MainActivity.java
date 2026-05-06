package com.solec.myapplication;

import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


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
private Button login;

private EditText username;
private EditText password;
private EditText message;
private TextView messageLog;

Protocols p = new Protocols();
String[] log = new String[10];
int logCounter = 0;

private Button user1;
private Button user2;
private Button user3;

String userToSend;


MyThread myThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        username = findViewById(R.id.Username);
        password = findViewById(R.id.Password);
        }

    public void changeLayoutAfterLogin(View view){

        myThread = new MyThread();
        new Thread(myThread).start();
        setContentView(R.layout.after_login);
        message = findViewById(R.id.Message);
        messageLog = findViewById(R.id.MessageLog);
        user1 = findViewById(R.id.User1Button);
        user2 = findViewById(R.id.User2Button);
        user3 = findViewById(R.id.User3Button);
    }

    private class MyThread implements Runnable {
        ByteBuffer code;
        Protocols p = new Protocols();

        Socket socket;
        String pass;
        String login;
        byte packetType;
        byte[] payloadLength = new byte[2];
        byte[] readPayloadLength = new byte[3];
        ByteBuffer handshakeBuffer;
        int messageLength;

        @Override
        public void run() {
            try {
                socket = new Socket("172.19.136.210", 9999);
                Log.i("Connection", ("Connected to server"));
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                Log.i("Avaible after connection", String.valueOf(dis.available()));

                dis.readFully(readPayloadLength, 0, 3);
                int payloadLengthInt = 3;
                byte[] readPayload = new byte[payloadLengthInt];
                dis.readFully(readPayload, 0, payloadLengthInt);
                Log.i("Handshake Payload",(Arrays.toString(readPayload)));

                handshakeBuffer = p.getHandshake();
                dos.write(handshakeBuffer.array());

                login = String.valueOf(username.getText());
                pass = String.valueOf(password.getText());
                dos.write(sendAuth(login,pass).array());
                Log.i("Authentication",("Authentication send"));
                Log.i("After Authentication", String.valueOf(dis.available()));

                while(true){
                      if (dis.available() != 0) {
                          Log.i("Avaible", String.valueOf(dis.available()));
                          packetType = dis.readByte();
                          if (packetType == 0x05) {
                              Log.i("Avaible", String.valueOf(dis.available()));
                              byte[] readAllLength = new byte[2];
                              dis.readFully(readAllLength, 0, 2);
                              byte[] readSenderLength = new byte[2];
                              dis.readFully(readSenderLength, 0, 2);
                              int readSenderLengthInt = p.decodeBytesToInt(readSenderLength);
                              byte[] readSender = new byte[readSenderLengthInt];
                              dis.readFully(readSender, 0, readSenderLengthInt);
                              String Sender = p.decodeBytesToString(readSender);
                              Sender = Sender.substring(0,Sender.length()-5);

                              byte[] readTargetLength = new byte[2];
                              dis.readFully(readTargetLength, 0, 2);
                              int readTargetLengthInt = p.decodeBytesToInt(readTargetLength);
                              byte[] readTarget = new byte[readTargetLengthInt];
                              dis.readFully(readTarget, 0, readTargetLengthInt);
                              String Target = p.decodeBytesToString(readTarget);
                              Log.i("Target", Target);
                              Log.i("ava", String.valueOf(dis.available()));

                              byte[] readTimestampLength = new byte[8];
                              dis.readFully(readTimestampLength, 0, 8);
                              //byte[] readTimestamp = new byte[10];
                              //dis.readFully(readTimestamp, 0, 10);
                              //String Timestamp = p.decodeBytesToString(readTimestamp);
                              //Log.i("Timestamp", Timestamp);

                              byte[] readMessageLength = new byte[2];
                              dis.readFully(readMessageLength, 0, 2);
                              int readMessageLengthInt = p.decodeBytesToInt(readMessageLength);
                              Log.i("len", String.valueOf(readMessageLengthInt));
                              byte[] readMessage = new byte[readMessageLengthInt];
                              dis.readFully(readMessage, 0, readMessageLengthInt);
                              String Message = p.decodeBytesToString(readMessage);
                              iterateLog(Sender + ":   " + Message + "\n\n",Sender);

                          } else if (packetType == 0x01) {
                              byte[] successRead = new byte[2];
                              dis.readFully(successRead, 0, 2);
                              Log.i("Success", Arrays.toString(successRead));
                          }
                          else if(packetType==0x00){

                          }
                      }
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


        public String getLogin(){
            return login;
        }

        public void sendMessage(ByteBuffer code) {
            this.code = code;
            try {
                OutputStream dos = socket.getOutputStream();
                Log.i("buffer", String.valueOf(code));
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
        this.userToSend = b.getText().toString()+"@localhost";
        messageLog.setText("");
        File path = getApplicationContext().getFilesDir();
        File readFrom = new File(path,userToSend);
        byte[] content = new byte[(int) readFrom.length()];
        try{
            FileInputStream fis = new FileInputStream(readFrom);
            fis.read(content);
            messageLog.setText(p.decodeBytesToString(content));
        }
        catch (Exception e) {
            message.setText("");
        }
    }

    public void clear(View v) {
        try {
            File path = getApplicationContext().getFilesDir();
            FileOutputStream writer = new FileOutputStream(new File(path, "user1@localhost"));
            writer.write("".getBytes());
            writer.close();
            writer = new FileOutputStream(new File(path, "user2@localhost"));
            writer.write("".getBytes());
            writer.close();
            writer = new FileOutputStream(new File(path, "user3@localhost"));
            writer.write("".getBytes());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
   public void sendMessageButton(View v) {
        String content = String.valueOf(message.getText());;
        String myUser = myThread.login;
        ByteBuffer contentBuffer = p.encodeString(content);
        ByteBuffer myUserBuffer = p.encodeString(myUser);
        ByteBuffer userToSendBuffer = p.encodeString(userToSend);
        contentBuffer.rewind();
        myUserBuffer.rewind();
        userToSendBuffer.rewind();
        ByteBuffer messageBuffer = p.getMessage(myUserBuffer,userToSendBuffer,contentBuffer);
        messageBuffer.rewind();
        iterateLog(myUser +":   " + content + "\n\n",myThread.getLogin());
        Log.i("buffer", String.valueOf(messageBuffer));
        message.setText("");
        myThread.sendMessage(messageBuffer);

   }

   public void iterateLog(String addToLog, String Sender){
        String wholeLog=" ";
        try {
           File path = getApplicationContext().getFilesDir();
           FileOutputStream writer = null;
           Log.i("Ścieżka i nazwa", userToSend + " " + Sender);

           if(Sender == myThread.getLogin() || Sender.contentEquals(userToSend)) {
               File readFrom = new File(path,userToSend);
               byte[] content = new byte[(int) readFrom.length()];
               FileInputStream fis = new FileInputStream(readFrom);
               fis.read(content);
               wholeLog = p.decodeBytesToString(content) + addToLog;
               messageLog.setText(wholeLog);
               writer = new FileOutputStream(new File(path, userToSend));
               Log.i("Ścieżka i nazwa2", userToSend + " " + Sender);
           }

           else{
               if(!Sender.contentEquals(userToSend)){
                   File readFrom = new File(path,Sender);
                   byte[] content = new byte[(int) readFrom.length()];
                   FileInputStream fis = new FileInputStream(readFrom);
                   fis.read(content);
                   wholeLog = p.decodeBytesToString(content) + addToLog;
                   writer = new FileOutputStream(new File(path, Sender));
                   Log.i("Ścieżka i nazwa3", userToSend + " " + Sender);
               }
           }
           assert writer != null;
           writer.write(wholeLog.getBytes());
           writer.close();

       }
       catch (Exception e) {
           throw new RuntimeException(e);
       }

   }



}


