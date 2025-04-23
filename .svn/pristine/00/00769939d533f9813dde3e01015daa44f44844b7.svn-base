package com.easy.iso8583;

import com.gd.magic.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/15 下午6:34
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
class MsgHeadTest {
    @Test
    void toByteArray() {
        String str = "2e823030393534343139303434302020203030303130333434202020000000013130303030303030003030303030";
        byte[] bytes = StringUtil.hexStrToBytes(str);
        MsgHead head = new MsgHead(bytes);
        Assertions.assertEquals(head.getMsgLen(), 95);
        Assertions.assertEquals(head.getDestinationId(), "44190440");
        Assertions.assertEquals(head.getSource(), "00010344");
        Assertions.assertEquals(head.getBatchNumber(), 1);
        Assertions.assertEquals(head.getTransactionInfo(), "10000000");
        Assertions.assertEquals(head.getUserInfo(), 0);
        Assertions.assertEquals(head.getRejectCode(), "00000");
        Assertions.assertArrayEquals(head.toByteArray(), bytes);
        System.out.println(StringUtil.bytesToHexStr(head.toResponseByteArray(0)));
    }
}