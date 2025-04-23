package com.easy.iso8583;

import com.easy.iso8583.communicate.BaseHandle;
import com.easy.iso8583.parse.ConfigParser;
import com.easy.iso8583.util.DESUtil;
import com.easy.iso8583.util.HexCodec;
import com.gd.magic.MagicException;
import com.gd.magic.encrypt.Des;
import com.gd.magic.util.StringUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/15 上午11:23
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
class MessageFactoryTest {
    static MessageFactory<IsoMessage> factory;

    @BeforeAll
    static void init() throws IOException {
        factory = ConfigParser.createFromClasspathConfig("conf/j8583.xml");
        factory.setBinaryFields(false);
        factory.setForceStringEncoding(true);
        factory.setVariableLengthFieldsInHex(false);
    }

    @Test
    void testBytes() throws UnsupportedEncodingException, ParseException {
        byte[] bytes = StringUtil.hexStrToBytes("2E82303039353434313930343430202020303030313033343420202000000001313030303030303000303030303030383230822000000000000004000000100000003035313531313136333131303637353333303130383434313930343430");
        IsoMessage msg = factory.parseMessage(bytes, 0x2e);
        msg.printMsg();
        msg.setValue(39, "00", IsoType.ALPHA, 2);
        System.out.println(StringUtil.bytesToHexStr(msg.writeData()));
    }

    @Test
    void test0830() throws UnsupportedEncodingException, ParseException {
        byte[] bytes = StringUtil.hexStrToBytes("2E82303134303434313930343430202020303030313033343420202000000001313030303030303000303030303030383330822000008200000004000000100000803035313531393332313331303637383830383434313930343430303033303130383434313930343430303330353131202020202020202020202020202020202020202020202020202020");
        IsoMessage msg = factory.parseMessage(bytes, 0x2e);
        msg.printMsg();
    }


    byte[] mmk = HexCodec.hexDecode("1023457689BADCEF0132546798ABCDFE");

    @Test
    void testRestMacKey() throws UnsupportedEncodingException, ParseException {
        byte[] bytes = StringUtil.hexStrToBytes("2E823031323734343139303434302020203030303130333434202020000000013130303030303030003030303030303830308220000000000800040000011000000130353137313630373236303030353335323030303030303030303030303030303130310FE5B03EC604CB6B30383434313930343430FFAB6DBF8945EAB7");
        IsoMessage msg = factory.parseMessage(bytes, 0x2e);
        msg.printMsg();
        byte[] tmp = msg.getObjectValue(96);
        byte[] originKey = DESUtil.triDesDecrypt(mmk, tmp);
        System.out.println("originKey:" + HexCodec.hexEncode(originKey));
        System.out.println(" checkVal:" + HexCodec.hexEncode(Des.encrypt(originKey, new byte[8])));
        System.out.println("calcMac:" + HexCodec.hexEncode(BaseHandle.calcMac4resetKey(msg, originKey)));
    }


    @Test
    void testRestPinKey() throws UnsupportedEncodingException, ParseException {
        byte[] bytes = StringUtil.hexStrToBytes("2E8230313438343431393034343020202030303031303334342020200000000131303030303030300030303030303038303082200000000108000400000110000001303531373039343635333030303532323031384E4B4393590FF157B782DDBE18332531120C31363030303030303030303030303030313031000000000000000030383434313930343430ED5F4AE970AF4B76");
        IsoMessage msg = factory.parseMessage(bytes, 0x2e);
        msg.printMsg();
        String field53 = msg.getObjectValue(53);

        byte[] originKey = null;
        if (field53.charAt(0) == '1') {
            //PIK
        } else if (field53.charAt(0) == '2') {
            //MAK
        } else {
            throw new MagicException("不支持的密钥类型!");
        }
        if (field53.charAt(1) == '0') {
            String field96 = msg.getObjectValue(96);
            //single length 8 byte
        } else if (field53.charAt(1) == '6') {
            byte[] key48 = msg.getObjectValue(48);
            assert new String(key48, 0, 2).equals("NK");
            //double length 16 byte
            byte[] tmp = new byte[16];
            System.arraycopy(key48, 2, tmp, 0, 16);
            originKey = DESUtil.triDesDecrypt(mmk, tmp);
            System.out.println("originKey:" + HexCodec.hexEncode(originKey));
            System.out.println(" checkVal:" + HexCodec.hexEncode(DESUtil.triDesEncrypt(originKey, new byte[8])));
        } else {
            throw new MagicException("不支持的密钥长度");
        }
//        byte[] testKey = Arrays.copyOf(originKey, 8);
//        System.out.println("calcMac:" + HexCodec.hexEncode(TestHandle.calcMac4resetKey(msg, testKey)));
        System.out.println("calcMac:" + HexCodec.hexEncode(BaseHandle.calcMac4resetKey(msg, originKey)));
    }


    @Test
    public void test0200() throws UnsupportedEncodingException, ParseException {
        byte[] bytes = StringUtil.hexStrToBytes("2E82303334313434313930343430202020303030313033343420202000000001313030303030303000303030303030323030E23E64C1A8E09810000000001000008031363632313039343730303030303030323133303030303030353138313330383537313036383739313330383537303531383330313030393032363031313135363032313032303630383030303030303030303830303030303030303036313131313131303030303030303035323733303030313030303130303135383430353331313030303154657374696E67206D65726368616E742032202020202020205368656E5A68656E2020202043484E3434363C77B8E654636C383236303030303030303030303030303030323730303030303230303031303030303030303030303030323131303230383434313930343430303330353131202020202020202020202020202020202020202020202020202020");
        IsoMessage msg = factory.parseMessage(bytes, 0x2e);
        msg.printMsg();
        byte[] tmp = msg.getObjectValue(96);
    }
}