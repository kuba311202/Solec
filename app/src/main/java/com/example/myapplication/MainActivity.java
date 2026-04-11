package com.example.myapplication;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
private Button login;

private EditText username;
private EditText password;


    byte[] ping = {0x02};
    byte pong = 0x03;
    byte[] message = new byte[] {
            (byte)0x04,

    };



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

        Socket socket;

        byte packetType;
        byte[] timestamp = new byte[8];
        byte[] payloadLength = new byte[2];
        byte[] readPayloadLength = new byte[3];
        ByteBuffer handshakeBuffer = ByteBuffer.allocate(1+2+1+1);
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
                int payloadLengthInt = ((int)readPayloadLength[0])-1 + (256*(int)readPayloadLength[1]);
                byte[] readPayload = new byte[payloadLengthInt];
                dis.readFully(readPayload, 0, payloadLengthInt);
                Log.i("Handshake Payload",(Arrays.toString(readPayload)));
                handshakeBuffer.put((byte)0x03);
                handshakeBuffer.putShort((short)2);
                handshakeBuffer.put((byte)0x00);
                handshakeBuffer.put((byte)0x01);
                dos.write(handshakeBuffer.array());
                String login = "Jakub";
                String pass = "valid";
                String auth = String.join("",login,pass);
                ByteBuffer AuthBuffer = ByteBuffer.allocate(1+2+auth.length());
                AuthBuffer.put((byte)0x04);
                AuthBuffer.putShort((short)auth.length());
                AuthBuffer.put(auth.getBytes());
                dos.write(AuthBuffer.array());
                Log.i("After Authentication", String.valueOf(dis.available()));
                Log.i("Authentication",("Authentication send"));



                while(true){
                    synchronized (this){
                        try {
                            this.wait(1000);
                            Log.i("Ava", String.valueOf(dis.available()));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(dis.available()!=0) {
                        packetType = dis.readByte();
                        Log.i("Avaible", String.valueOf(dis.available()));
                            byte[] readUsers = new byte[2];
                            byte[] readMessageLength = new byte[4];
                            dis.readFully(readUsers, 0, 2);
                            dis.readFully(readMessageLength, 0, 4);
                            for (byte b : readMessageLength) {
                                messageLength = (messageLength << 8) + (b & 0xFF);
                            }
                            byte[] readMessage = new byte[messageLength];
                            dis.readFully(readMessage, 0, messageLength);
                            Log.i("Message", Arrays.toString(readMessage));
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
                dos.write(code.array());
                dos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



   public void sendMessageButton(View v) {
       String message = "Solec Kujawski jest top";
       String myUser = "Jakub";
       String userToSend = "user3";
       String users = String.join(myUser,userToSend);
       ByteBuffer messageBuffer = ByteBuffer.allocate(1 + 2 + 8 + message.length());
       messageBuffer.put((byte)0x05);
      // messageBuffer.putChar((char)users);
       messageBuffer.putDouble((byte)message.length());
       messageBuffer.put(message.getBytes());
   }



}


