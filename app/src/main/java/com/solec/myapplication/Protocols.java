package com.solec.myapplication;

import android.util.Log;
import android.util.TimeUtils;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Calendar;

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
    public ByteBuffer getHistory(ByteBuffer Address,long Timestamp,long count){
        int AddressLen = Address.limit() - 2;
        ByteBuffer historyBuffer = ByteBuffer.allocate(1+2+AddressLen+8+8+8);
        historyBuffer.put((byte)0x08);
        historyBuffer.putShort((short)(2+Address.remaining()+8+8+8));
        historyBuffer.putLong(Timestamp);
        historyBuffer.putLong(count);
        historyBuffer.putLong(0);
        historyBuffer.rewind();
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
