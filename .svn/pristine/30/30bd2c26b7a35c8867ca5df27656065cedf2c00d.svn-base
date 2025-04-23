package com.easy.iso8583;

import com.easy.iso8583.parse.ConfigParser;

import java.io.IOException;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/18 上午9:58
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public abstract class ProtocolFactory {
    static MessageFactory<IsoMessage> factory;
    static public MessageFactory<IsoMessage> createUPIFactory(String protocol) throws IOException {
        if (factory == null) {
            synchronized (ProtocolFactory.class) {
                if (factory == null) {
                    factory = ConfigParser.createFromClasspathConfig(protocol);
                    factory.setBinaryFields(false);
                    factory.setForceStringEncoding(true);
                    factory.setVariableLengthFieldsInHex(false);
                }
            }
        }
        return factory;
    }
}
