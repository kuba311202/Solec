package com.solec.myapplication;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
private Button login;

private EditText username;
private EditText password;

Protocols p = new Protocols();

MyThread myThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        }

    public void changeLayoutAfterLogin(View view){
        myThread = new MyThread();
        new Thread(myThread).start();
        setContentView(R.layout.after_login);
    }

    private static class MyThread implements Runnable {
        ByteBuffer code;
        Protocols p = new Protocols();

        Socket socket;

        byte packetType;
        byte[] timestamp = new byte[8];
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
                Log.i("Connection", String.valueOf(dis.available()));

                dis.readFully(readPayloadLength, 0, 3);
                Log.i("Payload Length", ((Arrays.toString(readPayloadLength))));
                packetType = readPayloadLength[0];
                payloadLength[0] = readPayloadLength[1];
                payloadLength[1] = readPayloadLength[2];
                Log.i("Packet Type", String.valueOf((packetType)));
                int payloadLengthInt = 2;
                byte[] readPayload = new byte[payloadLengthInt];
                dis.readFully(readPayload, 0, payloadLengthInt);
                Log.i("Handshake Payload",(Arrays.toString(readPayload)));

                handshakeBuffer = p.getHandshake();
                dos.write(handshakeBuffer.array());
                String login = "Jakub123";
                String pass = "valid";
                ByteBuffer loginBuffer = p.encodeString(login);
                ByteBuffer passBuffer = p.encodeString(pass);
                loginBuffer.rewind();
                passBuffer.rewind();
                ByteBuffer AuthBuffer = p.getAuth(loginBuffer,passBuffer);
                AuthBuffer.rewind();
                Log.i("login Authentication", String.valueOf(loginBuffer));
                Log.i("pass Authentication", String.valueOf(passBuffer ));
                Log.i("auth Authentication", String.valueOf(AuthBuffer));
                for(int i=0; i<AuthBuffer.limit();i++){
                    Log.i("auth Authentication" + i, String.valueOf(AuthBuffer.get()));
                }
                dos.write(AuthBuffer.array());
                Log.i("After Authentication", String.valueOf(dis.available()));
                Log.i("Authentication",("Authentication send"));

                while(true){
                      if (dis.available() != 0) {
                          packetType = dis.readByte();
                          if (packetType == 0x05) {
                              Log.i("Avaible", String.valueOf(dis.available()));
                              byte[] readAllLength = new byte[2];
                              dis.readFully(readAllLength, 0, 2);
                              Log.i("Avaible", Arrays.toString(readAllLength));
                              byte[] readSenderLength = new byte[2];
                              dis.readFully(readSenderLength, 0, 2);
                              int readSenderLengthInt = p.decodeBytesToInt(readSenderLength);
                              byte[] readSender = new byte[readSenderLengthInt];
                              dis.readFully(readSender, 0, readSenderLengthInt);
                              String Sender = p.decodeBytesToString(readSender);
                              Log.i("Sender", Sender);

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
                              Log.i("ava", String.valueOf(dis.available()));
                              Log.i("ava", Arrays.toString(readTimestampLength));
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
                              Log.i("Message", Message);

                          } else if (packetType == 0x01) {
                              byte[] successRead = new byte[2];
                              dis.readFully(successRead, 0, 2);
                              Log.i("Success", Arrays.toString(successRead));
                          }
                      }
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
    }



   public void sendMessageButton(View v) {
       String content = "SolecKujawskijesttop";
       String myUser = "Jakub123@localhost";
       String userToSend = "user2@localhost:9999";
       ByteBuffer contentBuffer = p.encodeString(content);
       ByteBuffer myUserBuffer = p.encodeString(myUser);
       ByteBuffer userToSendBuffer = p.encodeString(userToSend);
       contentBuffer.rewind();
       myUserBuffer.rewind();
       userToSendBuffer.rewind();
       ByteBuffer messageBuffer = p.getMessage(myUserBuffer,userToSendBuffer,contentBuffer);
       messageBuffer.rewind();
       Log.i("buffer", String.valueOf(messageBuffer));
       myThread.sendMessage(messageBuffer);
   }



}


