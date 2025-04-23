package com.easy.iso8583;


import com.gd.magic.util.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/12 上午10:41
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
//    Field 1  Header Length 1
//    Field 2  Header Flag and Version  1
//    Field 3  Total Message Length 4
//    Field 4  Destination ID 11
//    Field 5  Source ID 11
//    Field 6  Reserved for Use 3
//    Field 7  Batch Number 1
//    Field 8  Transaction Information 8
//    Field 9  User Information 1
//    Field 10 Reject Code 5
//00015= invalid value   HeadLen Error Not 0x2e
//00025=invalid value  Version Error bit  1  (0 Product  1 Test)  The left 7bit identy version  from 000 0001 to 000 0010.
//00035=invalid value   >46  <46+1892  Message Len Error
//00045=invalid value   destinationId error
//00055=invalid value  source error
public class MsgHead {
    final public static int HEAD_LEN = 0x2E;


    byte[] head = new byte[HEAD_LEN];


    byte headLen = 0x2E;
    byte headVersion = (byte) 0x82;

    //The length of the rectification message includes the first 4 bytes number
    int msgLen;
    // Domestic members are 000010000
    //Foreign members are 000010344
    String destinationId;

    String source;
    //    String reserved;
    byte batchNumber;
    String transactionInfo;
    byte userInfo;
    String rejectCode;


    public MsgHead(byte[] head) {
        parse(head);
    }

    private void parse(byte[] head) {
        //head length
        int pos = 0;
        assert head[pos] == HEAD_LEN : "Abnormal message header length";
        pos += 1;

        // version
        headVersion = head[pos];
        pos += 1;

        //msg length
        msgLen = Integer.parseInt(new String(head, pos, 4));
        pos += 4;


        //destinationId
        destinationId = new String(head, pos, 11).trim();
        pos += 11;

        //source
        source = new String(head, pos, 11).trim();
        pos += 11;

        //reserve  00 00 00
        pos += 3;

        //batchNumber
        batchNumber = head[pos];
        pos += 1;

        //transactionInfo
        transactionInfo = new String(head, pos, 8).trim();
        pos += 8;

        //user info
        userInfo = head[pos];
        pos += 1;

        // rejectCode
        rejectCode = new String(head, pos, 5).trim();
    }

    public byte[] toResponseByteArray(int length) {

        //head length
        int pos = 0;
        head[pos] = HEAD_LEN;
        pos += 1;

        // version
        head[pos] = headVersion;
        pos += 1;

        //msg length
        String strLen = StringUtil.lengthFix(String.valueOf(length + headLen), 4, '0', false);
        System.arraycopy(strLen.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 4);
        pos += 4;

        String tmp = "";

//       The purpose and resource are exchanged when returning

        //source
        tmp = StringUtil.lengthFix(source, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.UTF_8), 0, head, pos, 11);
        pos += 11;


        //destinationId
        tmp = StringUtil.lengthFix(destinationId, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 11);
        pos += 11;


        //reserve  00 00 00
        pos += 3;

        //batchNumber
        head[pos] = batchNumber;
        pos += 1;

        //transactionInfo
        tmp = StringUtil.lengthFix(transactionInfo, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 8);
        pos += 8;

        //user info
        head[pos] = userInfo;
        pos += 1;

        // rejectCode  Members cannot generate but may receive the Reject Code field with non-all-zero.

        System.arraycopy("00000".getBytes(StandardCharsets.US_ASCII), 0, head, pos, 5);
        return head;
    }

    public byte[] toByteArray() {

        //head length
        int pos = 0;
        head[pos] = headLen;
        pos += 1;

        // version
        head[pos] = headVersion;
        pos += 1;

        //msg length
        String strLen = StringUtil.lengthFix(String.valueOf(msgLen), 4, '0', false);
        System.arraycopy(strLen.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 4);
        pos += 4;


        //destinationId
        String tmp = StringUtil.lengthFix(destinationId, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 11);
        pos += 11;

        //source
        tmp = StringUtil.lengthFix(source, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.UTF_8), 0, head, pos, 11);
        pos += 11;

        //reserve  00 00 00
        pos += 3;

        //batchNumber
        head[pos] = batchNumber;
        pos += 1;

        //transactionInfo
        tmp = StringUtil.lengthFix(transactionInfo, 11, ' ', true);
        System.arraycopy(tmp.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 8);
        pos += 8;

        //user info
        head[pos] = userInfo;
        pos += 1;

        // rejectCode  Members cannot generate but may receive the Reject Code field with non-all-zero.

        System.arraycopy(rejectCode.getBytes(StandardCharsets.US_ASCII), 0, head, pos, 5);
        return head;
    }


    public byte getHeadVersion() {
        return headVersion;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public String getSource() {
        return source;
    }

    public byte getBatchNumber() {
        return batchNumber;
    }

    public String getTransactionInfo() {
        return transactionInfo;
    }

    public byte getUserInfo() {
        return userInfo;
    }

    public String getRejectCode() {
        return rejectCode;
    }
}
