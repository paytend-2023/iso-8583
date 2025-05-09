package com.easy.iso8583.communicate;

import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.IsoType;
import com.easy.iso8583.UpiKeyService;
import com.easy.iso8583.util.DESUtil;
import com.gd.magic.MagicException;
import com.gd.magic.MagicFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/16 上午9:35
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class BaseHandle {

    /**
     * 0820/0830 001  The Member signs on/UnionPay system informs the Member that UnionPay system is enabled.
     * 0820/0830 002  The Member signs off/UnionPay system informs the Member that GSCS is disabled.
     * 0800/0810 101  UnionPay resets the key.
     * 0820/0830 101  The Member requests to reset the key.
     * 0820/0830 201  UnionPay starts date switch.
     * 0820/0830 202  UnionPay ends date switch.
     * 0820/0830 301  Echo test
     * 0820/0830 401 Stand-in authorization echo message.
     * 0820/0830 501 Stand-in authorization transaction transmission activation.
     * 0820/0830 502 Stand-in authorization online transmission completion/termination. In UnionPay’s message it means transmission completion; in the institution’s message it means termination.
     */




    //UnionPay resets the key.
    static void h0800(Message isoMessage) {
        String field53 = isoMessage.body.getObjectValue(53);
        byte[] originKey = null;
        UpiKeyService keyService = MagicFactory.getService(UpiKeyService.class);
//        byte[] mmk = HexCodec.hexDecode("1023457689BADCEF0132546798ABCDFE");
        byte[] mmk = keyService.getMKey();

        if (field53.charAt(1) == '0') {
            //single length 8 byte
            byte[] tmp = isoMessage.body.getObjectValue(96);
            originKey = DESUtil.triDesDecrypt(mmk, tmp);

        } else if (field53.charAt(1) == '6') {
            //double length 16 byte
            byte[] key48 = isoMessage.body.getObjectValue(48);
            assert new String(key48, 0, 2).equals("NK");
            byte[] tmp = new byte[16];
            System.arraycopy(key48, 2, tmp, 0, 16);
            originKey = DESUtil.triDesDecrypt(mmk, tmp);
//            System.out.println("originKey:" + HexCodec.hexEncode(originKey));
//            System.out.println(" checkVal:" + HexCodec.hexEncode(DESUtil.triDesEncrypt(originKey, new byte[8])));
        } else {
            throw new MagicException("不支持的密钥长度");
        }
        isoMessage.body.setType(0x0810);
        isoMessage.body.removeFields(48, 96);
        isoMessage.body.setValue(33, isoMessage.head.getDestinationId(), IsoType.LLVAR, 0);
        //Why???  isoMessage.body.setValue(39, "00", IsoType.ALPHA, 2);

        if (field53.charAt(0) == '1') {
            //PIK
            keyService.savePinKey(originKey);
        } else if (field53.charAt(0) == '2') {
            //MAK
            keyService.saveMacKey(originKey);
        } else {
            throw new MagicException("不支持的密钥类型!");
        }
        //Why???   isoMessage.body.setValue(39, "00", IsoType.ALPHA, 2);
        addMac(isoMessage.body, originKey);
    }

    final static List<Integer> MGNT_FIELDS;
    final static List<Integer> TRANS_FIELDS;

    static {
        MGNT_FIELDS = Arrays.asList(7, 11, 39, 53, 70, 100);
        TRANS_FIELDS = Arrays.asList(2, 3, 4, 7, 11, 18, 25, 28, 32, 33, 38, 39, 41, 42, 10);
    }


    public static void addMac(IsoMessage isoMessage, byte[] macKey) {
        if (isResetKeyType(isoMessage)) {
            isoMessage.setValue(128, calcMac4resetKey(isoMessage, macKey), IsoType.BINARY, 8);
        } else {
            isoMessage.setValue(128, calcMac4trans(isoMessage, macKey), IsoType.BINARY, 8);
        }
    }

    public static byte[] calcMac4trans(IsoMessage isoMessage, byte[] seed) {
        byte[] macBlock = createMacBlock(isoMessage, TRANS_FIELDS);
        return DESUtil.mac4trans(seed, macBlock);
    }

    public static byte[] calcMac4resetKey(IsoMessage isoMessage, byte[] seed) {
        byte[] macBlock = createMacBlock(isoMessage, MGNT_FIELDS);
        return DESUtil.mac4resetKey(seed, macBlock);
    }

    public static byte[] createMacBlock(IsoMessage isoMessage, List<Integer> macFields) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(String.format("%04X", isoMessage.getType()).getBytes());
            if (isResetKeyType(isoMessage)) {
                for (int i : macFields) {
                    if (isoMessage.hasAnyField(i)) {
                        outputStream.write((byte) 0x20);
                        outputStream.write(isoMessage.createBytes(i));
                    }
                }
            }
            return outputStream.toByteArray();
        } catch (IOException ignore) {
        }
        return null;
    }


    private static boolean isResetKeyType(IsoMessage isoMessage) {
        return isoMessage.getType() == 0x0800 || isoMessage.getType() == 0x0810;
    }

    public static boolean checkMac(IsoMessage body, byte[] macKey) {
        return true;
    }
}
