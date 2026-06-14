package com.solec.myapplication;

import android.util.Log;
import android.util.TimeUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Calendar;

public class Protocols {

    public Protocols(){

    }
    public ByteBuffer encodeString(String stringToEncode){
        ByteBuffer encodedString = ByteBuffer.allocate(2+stringToEncode.getBytes(StandardCharsets.UTF_8).length);
        encodedString.putShort((short)stringToEncode.getBytes(StandardCharsets.UTF_8).length);
        encodedString.put(stringToEncode.getBytes(StandardCharsets.UTF_8));
        return encodedString;
    }
    public int decodeBytesToInt(byte[] bytesToDecode){
        return new BigInteger(1, bytesToDecode).intValue();
    }

    public String decodeBytesToString(byte[] Bytes){
        return new String(Bytes,StandardCharsets.UTF_8);
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
    public ByteBuffer getHistory(ByteBuffer Address,long Timestamp,long count){
        int AddressLen = Address.limit() - 2;
        Address.rewind();
        Log.i("addresss", String.valueOf(Address)+AddressLen);
        ByteBuffer historyBuffer = ByteBuffer.allocate(1+2+2+AddressLen+8+8+8);
        historyBuffer.put((byte)0x08);
        historyBuffer.putShort((short)(2+AddressLen+8+8+8));
        historyBuffer.put(Address);
        historyBuffer.putLong(Timestamp);
        historyBuffer.putLong(count);
        historyBuffer.putLong(0);
        for(int i =0;i<historyBuffer.limit();i++) {
            Log.i("Historia", String.valueOf(historyBuffer.get(i)));
        }
        for(int i =0;i<Address.limit();i++){
            Log.i("Nickname", String.valueOf(Address.get(i)));
        }
        return historyBuffer;
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
    public ByteBuffer getList(){
        ByteBuffer listBuffer = ByteBuffer.allocate(1+2+8+8);
        listBuffer.put((byte)0x09);
        listBuffer.putShort((short) 16);
        listBuffer.putLong(20);
        listBuffer.putLong(0);
        return listBuffer;
    }
    public Instant getTimestamp(){
        Calendar calendar = Calendar.getInstance();
        return Instant.ofEpochMilli(calendar.getTimeInMillis());
    }
    public ByteBuffer getMessage(ByteBuffer source, ByteBuffer target, ByteBuffer content){
        int sourceLen = source.limit() - 2;
        int targetLen = target.limit() -2;
        int contentLen = content.limit() - 2;
        Instant Timestamp = Instant.now();
        long timestamp = Timestamp.toEpochMilli();
        ByteBuffer message = ByteBuffer.allocate((1+2+2+sourceLen+2+targetLen+8+2+contentLen));
        message.put((byte) 0x05);
        message.putShort((short) (2+source.limit()+2+target.limit()+8+2+content.limit()));
        message.put(source);
        message.put(target);
        message.putLong(timestamp);
        message.put(content);
        message.rewind();

        return message;
    }
    public ByteBuffer getJoinChannel(ByteBuffer user, ByteBuffer channelName, char mode){
        int userLen = user.limit() - 2;
        int channelLen = channelName.limit() -2;
        ByteBuffer joinChannel = ByteBuffer.allocate(1+2+2+userLen+2+channelLen+1);
        joinChannel.put((byte)0x07);
        joinChannel.putShort((short) (2+user.limit()+2+channelName.limit()+1));
        joinChannel.put(user);
        joinChannel.put(channelName);
        joinChannel.put((byte) mode);
        joinChannel.rewind();
        return joinChannel;
    }

}
