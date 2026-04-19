package com.solec.myapplication;

import java.nio.ByteBuffer;

public class Protocols {

    public Protocols(){

    }
    public ByteBuffer encodeString(String stringToEncode){
        ByteBuffer encodedString = ByteBuffer.allocate(2+stringToEncode.length());
        encodedString.putShort((short)stringToEncode.length());
        encodedString.put(stringToEncode.getBytes());
        return encodedString;
    }
    public ByteBuffer getAuth(ByteBuffer login, ByteBuffer pass){
        ByteBuffer authBuffer = ByteBuffer.allocate(1+2+2+login.limit()+2+pass.limit());
        authBuffer.put((byte)0x04);
        authBuffer.putShort((short)(2+login.remaining()+2+pass.remaining()));
        authBuffer.put(login);
        authBuffer.put(pass);
        return authBuffer;
    }
}
