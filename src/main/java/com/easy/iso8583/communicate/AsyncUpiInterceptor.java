package com.easy.iso8583.communicate;

import com.easy.iso8583.AsyncBusinessHandle;
import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.MessageFactory;
import com.easy.iso8583.ProtocolFactory;
import com.gd.magic.MagicFactory;
import com.gd.magic.rmi.RemoteInterceptor;
import com.gd.magic.rmi.SocketConnect;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.log4j.Logger;

public class AsyncUpiInterceptor extends RemoteInterceptor {
    private static Logger logger = org.apache.log4j.Logger.getLogger(AsyncUpiInterceptor.class);

    private boolean hasInit = false;
    private SocketConnect socketConnect;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger retryCount = new AtomicInteger(0);
    AsyncBusinessHandle handle = (AsyncBusinessHandle) MagicFactory.getProxy(AsyncBusinessHandle.class);
    private MessageFactory<IsoMessage> factory;

    public AsyncUpiInterceptor() {
        try {
            this.factory = ProtocolFactory.createUPIFactory("conf/j8583.xml");
        } catch (IOException e) {
            logger.debug("read file error :conf/j8583.xml not found");
        }

        this.startListen();
        this.startHeartbeat();
    }

    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                if (hasInit && outputStream != null) {
                    synchronized (this) {
                        outputStream.write(new byte[]{48, 48, 48, 48});
                        outputStream.flush();
                        logger.debug("Heartbeat packet sent.");
                    }
                }
            } catch (IOException e) {
                logger.error("Heartbeat packet delivery failure", e);
                resetConn();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    void startListen() {
        Thread t = new Thread("upi receive thread") {
            public void run() {
                while (true) {
                    if (!AsyncUpiInterceptor.this.hasInit) {
                        logger.warn("Connection not yet initialized, ready to wait for retry...");
                        AsyncUpiInterceptor.this.waitBeforeReconnect();
                        continue;
                    }

                    try {
                        final Message msg = TestServer.receiveMessage(AsyncUpiInterceptor.this.inputStream, AsyncUpiInterceptor.this.factory);
                        if (Message.NullMessage.equals(msg)) {
                            logger.warn("Heartbeat packet received");
                        } else if (msg != null) {
                            MagicFactory.getGlobalThreadPool().execute(() -> {
                                try {
                                    TestServer.innerHandler(AsyncUpiInterceptor.this.handle, msg);
                                } catch (Throwable e) {
                                    logger.error("Business Processing Exception:", e);
                                }
                            });
                        }
                    } catch (ParseException | IOException e) {
                        logger.error("Communication anomaly, preparing to reconnect...", e);
                        AsyncUpiInterceptor.this.resetConn();
                    } catch (Throwable ex) {
                        logger.error("Receive thread exit exception:", ex);
                    }
                }
            }
        };
        t.start();
        logger.debug("Start the upi receiver thread");
    }

    void initConn() {
        if (!this.hasInit) {
            synchronized (this) {
                if (!this.hasInit) {
                    try {
                        if (this.socketConnect != null) {
                            this.socketConnect.setInvalid(true);
                            this.closeConnect(this.socketConnect);
                        }

                        this.socketConnect = this.getConnect();
                        Socket socket = this.socketConnect.getSocket();
                        socket.setSoTimeout(0);
                        socket.setKeepAlive(true);
                        this.inputStream = socket.getInputStream();
                        this.outputStream = socket.getOutputStream();
                        this.hasInit = true;
                        retryCount.set(0);
                        logger.debug(">>>Connection initialized successfully: " + this.socketConnect);
                    } catch (Exception e) {
                        logger.error(">>>Connection initialization exception:", e);
                        this.hasInit = false;
                        if (this.socketConnect != null) {
                            this.socketConnect.setInvalid(true);
                            try {
                                this.closeConnect(this.socketConnect);
                            } catch (Exception ignored) {
                            }
                        }
                        this.socketConnect = null;
                    }
                }
            }
        }
    }

    private void resetConn() {
        this.hasInit = false;
        int count = retryCount.incrementAndGet();
        logger.warn("resetConn begin " + count + " count , presen socketConnect: " + this.socketConnect);
        if (this.socketConnect != null) {
            this.socketConnect.setInvalid(true);
            try {
                this.closeConnect(this.socketConnect);
            } catch (Exception e) {
                logger.error("resetConn closeConnect exceptions:", e);
            }
            this.socketConnect = null;
        }
    }

    private void waitBeforeReconnect() {
        int count = retryCount.get();
        int waitTime = Math.min((int) Math.pow(2, count), 60);
        try {
            logger.debug("wait for " + waitTime + " Retry connection in seconds...");
            Thread.sleep(waitTime * 1000L);
        } catch (InterruptedException ignored) {
        }
    }

    public Object intercept(Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Message message = (Message) objects[0];
        logger.info("intercept method = [" + method + "] message=" + message);
        this.initConn();

        try {
            synchronized (this) {
                if (this.hasInit) {
                    if (message == null) {
                        this.outputStream.write(new byte[]{48, 48, 48, 48});
                    } else {
                        TestServer.sendIsoMessage(message, this.outputStream);
                    }
                } else {
                    logger.error("intercept Abnormal connection. Please try again later.");
                }
            }
        } catch (IOException e) {
            logger.error("intercept Failed to send a message, triggering a reconnect:", e);
            this.resetConn();
        }

        return null;
    }

    public void closeConnect(SocketConnect conn) {
        try {
            if (conn != null) {
                Socket socket = conn.getSocket();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        } catch (IOException e) {
            logger.error("closeConnect Close Connection Exception:", e);
        }
    }
}