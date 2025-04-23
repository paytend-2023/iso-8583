package com.easy.iso8583.communicate;

import com.easy.iso8583.AsyncBusinessHandle;
import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.MessageFactory;
import com.easy.iso8583.ProtocolFactory;
import com.gd.magic.MagicFactory;
import com.gd.magic.rmi.RemoteInterceptor;
import com.gd.magic.rmi.SocketConnect;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.ParseException;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/9/14 上午9:03
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class AsyncUpiInterceptor extends RemoteInterceptor {
    private static Logger logger = org.apache.log4j.Logger.getLogger(AsyncUpiInterceptor.class);

    private boolean hasInit = false;
    private SocketConnect socketConnect;
    private OutputStream outputStream;
    private InputStream inputStream;
    AsyncBusinessHandle handle = MagicFactory.getProxy(AsyncBusinessHandle.class);


    private MessageFactory<IsoMessage> factory;

    public AsyncUpiInterceptor() {
        try {
            factory = ProtocolFactory.createUPIFactory("conf/j8583.xml");
        } catch (IOException e) {
            logger.debug("read file error :conf/j8583.xml not found");
        }
        startListen();
    }

    void startListen() {
        Thread t = new Thread("upi receive thread") {
            public void run() {
                while (true) {
                    if (!hasInit) {
                        logger.error("startListen socketConnect: " +socketConnect);
                        logger.error("no init ");
                        try {
                            Thread.sleep(1000);
                            logger.error("no init~waited for 1s");
                            continue;
                        } catch (InterruptedException ignore) {

                            //logger.warn("startListen ignore>>"+ignore.);
                            logger.error("startListen thread :", ignore);
                        }
                    }
                    try {
                        final Message msg = TestServer.receiveMessage(inputStream, factory);

                        if (Message.NullMessage.equals(msg)) {
                            logger.warn("receive   heartBeat;");
                            continue;
                        }
                        if (msg == null) {
                            //logger.warn("receive null message,read error!  ");
                            //test//todo  resetConn();
                            continue;
                        }
                        MagicFactory.getGlobalThreadPool().execute(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            TestServer.innerHandler(handle, msg);
                                        } catch (Throwable e) {
                                            logger.error("business error :", e);
                                        }
                                    }
                                }
                        );
                    } catch (IOException | ParseException e) {
                        logger.error(" Communication error ", e);
                        resetConn();
                    } catch (Throwable e) {
                        logger.error("exit receive thread :", e);
                    }
                }

            }
        };
        t.start();
        logger.debug("start upi receive thread");
    }

    void initConn() {
        if (hasInit) {
            return;
        }
        synchronized (this) {
            if (!hasInit) {
                try {
                    //Close the original connection
                    if (socketConnect != null) {
                        socketConnect.setInvalid(true);
                        closeConnect(socketConnect);
                    }
                    //Reacquire Connection
                    socketConnect = getConnect();
                    Socket socket = socketConnect.getSocket();
                    socket.setSoTimeout(0);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    hasInit = true;
                    logger.debug(">>>initConn sucessfull socketConnect:"+socketConnect);
                } catch (Exception e) {

                    e.printStackTrace();

                    logger.debug(">>>Exception initConn:"+e);

                    hasInit = false;
                    if (socketConnect != null) {
                        socketConnect.setInvalid(true);
                        try {
                            closeConnect(socketConnect);
                        } catch (Exception ignore) {
                        }
                    }
                    socketConnect = null;
                    logger.error("set socketConnect =  " +socketConnect);

                }
            }
        }

    }

    private void resetConn() {
        hasInit = false;
        logger.error("resetConn socketConnect:"+socketConnect);
        //
        if (socketConnect != null) {
            socketConnect.setInvalid(true);
            try {
				closeConnect(socketConnect);
				socketConnect = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
		        logger.error("resetConn closeConnect:"+e);
				e.printStackTrace();
			}
        }

    }


    @Override
    public Object intercept(Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Message message = (Message) objects[0];
        logger.error("intercept method = ["+method +"] message=" + message);
        initConn();
        try {
            synchronized (this) {
                if (hasInit) {
                    if (message == null) {//heartbeat
                        outputStream.write(new byte[]{0x30, 0x30, 0x30, 0x30});
                    } else {
                        TestServer.sendIsoMessage(message, outputStream);
                    }

                } else {
                    logger.error("Connection exception, please resend later!");
                }
            }
        } catch (IOException e) {
        	logger.error("intercept write, IOException"+e.getMessage());

            resetConn();
        }
        return null;
    }
}
