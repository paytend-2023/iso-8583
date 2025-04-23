package com.easy.iso8583.communicate;

import com.easy.iso8583.ProtocolFactory;
import com.gd.magic.rmi.RemoteInterceptor;
import com.gd.magic.rmi.SocketConnect;
import net.sf.cglib.proxy.MethodProxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/18 下午2:07
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class UPIInterceptor extends RemoteInterceptor {
    @Override
    public Object intercept(Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        SocketConnect socketConnect = getConnect();
        try {
            Message message = (Message) objects[0];
            OutputStream outputStream = socketConnect.getSocket().getOutputStream();
            TestServer.sendIsoMessage(message, outputStream);
            if (objects.length > 1 && objects[1] != null && objects[1] instanceof Boolean) {
                InputStream inputStream = socketConnect.getSocket().getInputStream();
                return TestServer.receiveMessage(inputStream, ProtocolFactory.createUPIFactory("conf/j8583.xml"));
            }
            return null;

        } finally {
            closeConnect(socketConnect);
        }


    }


}
