package com.gdg.z_meet.global.Util;


import com.fasterxml.uuid.Generators;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.UUID;

@Component
public class UUIDUtil {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] createUUID() {
        UUID uuidV1 = Generators.timeBasedGenerator().generate();
        String[] uuidV1Parts = uuidV1.toString().split("-");
        String sequentialUUID = uuidV1Parts[2]+uuidV1Parts[1]+uuidV1Parts[0]+uuidV1Parts[3]+uuidV1Parts[4];

        String sequentialUuidV1 = String.join("", sequentialUUID);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(Long.parseUnsignedLong(sequentialUuidV1.substring(0, 16), 16));
        bb.putLong(Long.parseUnsignedLong(sequentialUuidV1.substring(16), 16));
        return bb.array();
    }

    //사람이 식별 가능한 문자열로 변환
    public static String bytesToHex(UUID bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ( (Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16) );
        }
        return data;
    }
}