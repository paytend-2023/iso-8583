package com.easy.iso8583.parse;


import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.MessageFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/14 上午11:00
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class ConfigParserTest {

    @Test
    public void testCreateDefault() throws IOException {
        MessageFactory<IsoMessage> factory = ConfigParser.createFromClasspathConfig("conf/j8583.xml");
        System.out.println(factory.getCharacterEncoding());
        System.out.println(factory.isBinaryFields());
    }


}