package com.solec.myapplication;

import android.util.Log;

import java.nio.ByteBuffer;
import java.time.LocalTime;

public class Protocols {

    public Protocols(){

    }
    public ByteBuffer encodeString(String stringToEncode){
        ByteBuffer encodedString = ByteBuffer.allocate(2+stringToEncode.length());
        encodedString.putShort((short)stringToEncode.length());
        encodedString.put(stringToEncode.getBytes());
        return encodedString;
    }
    public int decodeBytesToInt(byte[] bytesToDecode){
        int intToReturn = 0;
        for(int i=0;i<bytesToDecode.length;i++){
            intToReturn += bytesToDecode[i];
        }
        return intToReturn;
    }
    public String decodeBytesToString(byte[] Bytes){
        int[] intTable = new int[Bytes.length];
        String[] stringTable = new String[Bytes.length];
        String stringToReturn = "";
        for(int i = 0; i<Bytes.length; i++){
            intTable[i] = Bytes[i];
            stringTable[i] = Character.toString((char) intTable[i]);
        }
        for (String c : stringTable) {
            stringToReturn = String.join("",stringToReturn,c);
        }

        return stringToReturn;
    }
    public ByteBuffer getAuth(ByteBuffer login, ByteBuffer pass){
        int loginLen = login.limit() - 2;
        int passLen = pass.limit() -2;
        ByteBuffer authBuffer = ByteBuffer.allocate(1+2+2+loginLen+2+passLen);
        authBuffer.put((byte)0x04);
        authBuffer.putShort((short)(2+login.remaining()+2+pass.remaining()));
        authBuffer.put(login);
        authBuffer.put(pass);
        return authBuffer;
    }
    public ByteBuffer getHandshake(){
        ByteBuffer handshakeBuffer = ByteBuffer.allocate(1+2+1+1+1);
        handshakeBuffer.put((byte)0x03);
        handshakeBuffer.putShort((short)3);
        handshakeBuffer.put((byte)0x00);
        handshakeBuffer.put((byte)0x02);
        handshakeBuffer.put((byte) 0x01);
        return handshakeBuffer;
    }
    public ByteBuffer getMessage(ByteBuffer source, ByteBuffer target, ByteBuffer content){
        int sourceLen = source.limit() - 2;
        int targetLen = target.limit() -2;
        int contentLen = content.limit() - 2;
        LocalTime time = LocalTime.now();
        String second = String.valueOf(time.getSecond());
        String minute = String.valueOf(time.getMinute());
        String hour = String.valueOf(time.getHour());
        String timestampString =String.join("",hour,minute,second);
        ByteBuffer timestamp = encodeString(timestampString);
        timestamp.rewind();
        ByteBuffer message = ByteBuffer.allocate((1+2+2+sourceLen+2+targetLen+2+timestampString.length()+2+contentLen));
        message.put((byte) 0x05);
        message.putShort((short) (2+source.limit()+2+target.limit()+2+timestampString.length()+2+content.limit()));
        message.put(source);
        message.put(target);
        message.put(timestamp);
        message.put(content);
        message.rewind();
        for(int i=0; i<message.limit();i++){
            Log.i("auth Authentication" + i, String.valueOf(message.get()));
        }
        return message;
    }

}
