package com.easy.iso8583.util;

import com.gd.magic.encrypt.Des;
import com.gd.magic.encrypt.TriDes;
import com.gd.magic.util.ByteArrayUtil;

import java.util.Arrays;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/17 上午10:14
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public abstract class DESUtil {

    static public byte[] triDesEncrypt(byte[] key, byte[] data) {
        return triDes(key, data, true);
    }


    static public byte[] triDesDecrypt(byte[] key, byte[] data) {
        return triDes(key, data, false);
    }

    static public byte[] triDes(byte[] key, byte[] data, boolean isEnc) {
        assert key.length == 16;
        assert (data.length % 8) == 0;
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length / 8; i++) {
            byte[] tmp = new byte[8];
            System.arraycopy(data, i * 8, tmp, 0, 8);
            if (isEnc) {
                System.arraycopy(TriDes.encrypt(key, tmp), 0, result, i * 8, 8);
            } else {
                System.arraycopy(TriDes.decrypt(key, tmp), 0, result, i * 8, 8);
            }
        }
        return result;
    }

    static public byte[] mac4trans(byte[] seed, byte[] data) {
        byte[] mac99 = nMac99(seed, data);
        return HexCodec.hexEncode(mac99, 0, 4).getBytes();
    }

    static public byte[] mac4resetKey(byte[] seed, byte[] data) {
        byte[] mac99 = nMac99(seed, data);
        byte[] checkValue = des(seed, new byte[8], true);
        System.arraycopy(checkValue, 0, mac99, 4, 4);
        return mac99;
    }


    static public byte[] nMac99(byte[] seed, byte[] data) {
        if (seed.length <= 8) {
            return nMac99Single(seed, data);
        } else {
            return nMac99Double(seed, data);
        }
    }

    static public byte[] nMac99Single(byte[] seed, byte[] data) {
        seed = ByteArrayUtil.forceLength(seed, 8);
        byte[] source;
        int dataLen = data.length;
        if (dataLen % 8 == 0)
            source = new byte[dataLen];
        else
            source = new byte[(dataLen / 8 + 1) * 8];
        System.arraycopy(data, 0, source, 0, dataLen);

        for (int i = dataLen; i < source.length; i++) {
            source[i] = (byte) 0x00;
        }
        int block = source.length / 8;
        byte[] temp = new byte[8];
        for (int i = 0; i < block; i++) {
            for (int j = 0; j < 8; j++) {
                temp[j] = (byte) (temp[j] ^ source[i * 8 + j]);
            }
            temp = des(seed, temp, true);
        }

        byte[] macResult = new byte[8];
        System.arraycopy(temp, 0, macResult, 0, 8);
        return macResult;
    }

    static public byte[] nMac99Double(byte[] seed, byte[] data) {
        seed = ByteArrayUtil.forceLength(seed, 16);
        byte[] keyLeft = Arrays.copyOf(seed, 8);
        byte[] keyRight = Arrays.copyOfRange(seed, 8, 16);
        byte[] source;
        int dataLen = data.length;
        if (dataLen % 8 == 0) {
            source = new byte[dataLen];
        } else {
            source = new byte[(dataLen / 8 + 1) * 8];
        }
        System.arraycopy(data, 0, source, 0, dataLen);

        for (int i = dataLen; i < source.length; i++) {
            source[i] = (byte) 0x00;
        }
        int block = source.length / 8;
        byte[] temp = new byte[8];
        for (int i = 0; i < block; i++) {
            for (int j = 0; j < 8; j++) {
                temp[j] = (byte) (temp[j] ^ source[i * 8 + j]);
            }
            temp = des(keyLeft, temp, true);
        }
        temp = des(keyRight, temp, false);
        temp = des(keyLeft, temp, true);
        byte[] macResult = new byte[8];
        System.arraycopy(temp, 0, macResult, 0, 8);
        return macResult;
    }

    private static byte[] des(byte[] seed, byte[] data, boolean isEnc) {
        if (isEnc) {
            if (seed.length != 8) {
                return TriDes.encrypt(seed, data);
            } else {
                return Des.encrypt(seed, data);
            }
        } else {
            if (seed.length != 8) {
                return TriDes.decrypt(seed, data);
            } else {
                return Des.decrypt(seed, data);
            }
        }
    }
}
