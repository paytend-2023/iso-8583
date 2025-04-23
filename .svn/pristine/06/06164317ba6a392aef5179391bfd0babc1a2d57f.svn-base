package com.easy.iso8583.communicate;

import com.easy.iso8583.BusinessHandle;
import com.easy.iso8583.ProtocolFactory;
import com.gd.magic.MagicFactory;
import com.gd.magic.MagicHelper;
import com.gd.magic.Service;
import com.gd.magic.kernel.ServiceHelper;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.Properties;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/18 上午9:33
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class UPIServerHandle implements Service {


    static Logger logger = Logger.getLogger(UPIServerHandle.class);
    MagicHelper helper = MagicFactory.getHelper();
    ServiceHelper serviceHelper = MagicFactory.getService(ServiceHelper.class);


    private static IoSession connSession;

    public void handleRequest(final IoSession session, final InputStream inStream,
                              final OutputStream out, final Properties properties) throws IOException, ParseException {
        connSession = session;
        long begin = System.currentTimeMillis();
        String protocolFile = properties.getProperty("protocol");
        String ipAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        helper.setLoginIP(ipAddress);
        BusinessHandle handle = MagicFactory.getProxy(BusinessHandle.class);
        TestServer.handle(inStream, out, ProtocolFactory.createUPIFactory(protocolFile), handle);
        logger.debug("cost " + (System.currentTimeMillis() - begin) + "ms");
    }


    public static IoSession getConnSession() {
        return connSession;
    }


}
