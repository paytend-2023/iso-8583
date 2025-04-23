package com.easy.iso8583.communicate;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/6/29 下午2:12
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */

import com.easy.iso8583.ProtocolFactory;
import com.gd.magic.kernel.ServiceInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.stream.StreamIoHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
*Long connection of multiplex service

*There is a bug in the long connection mode of the system

*Closed connections will not be removed from the connection pool
 */
public class UPIServerInterceptor extends ServiceInterceptor {
    private static final AttributeKey KEY_IN = new AttributeKey(StreamIoHandler.class, "in");
    private static final AttributeKey KEY_OUT = new AttributeKey(StreamIoHandler.class, "out");
    static Logger log = Logger.getLogger(UPIServerInterceptor.class);

    @Override
    public Object intercept(Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        IoSession session = UPIServerHandle.getConnSession();
        if (session == null) {
            log.error("not connects");
            return null;
        }
        InputStream in = (InputStream) session.getAttribute(KEY_IN);
        OutputStream out = (OutputStream) session.getAttribute(KEY_OUT);
        Message message = (Message) objects[0];
        TestServer.sendIsoMessage(message, out);
        return TestServer.receiveMessage(in, ProtocolFactory.createUPIFactory("conf/j8583.xml"));
    }
}
