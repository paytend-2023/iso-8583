package com.easy.iso8583.communicate;

import com.easy.iso8583.*;
import com.easy.iso8583.util.HexCodec;
import com.gd.magic.MagicFactory;
import com.gd.magic.util.StringUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/15  :39
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class TestServer {
    static MessageFactory<IsoMessage> factory;
    static Logger log = Logger.getLogger(TestServer.class);
    static InputStream inputStream;
    static OutputStream outputStream;

    final static boolean CHECK_UPI_MAC;

    static {
        CHECK_UPI_MAC = MagicFactory.getProperty("checkUPIMac") != null;
    }


    public static void main(String[] args) throws IOException {
        ProtocolFactory.createUPIFactory("conf/j8583.xml");
        ServerSocket ss = new ServerSocket(2889);
        while (true) {
            Socket socket = ss.accept();
            log.debug("main  createUPIFactory accept....");
            while (true) {
                try {
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();
                    handle(inputStream, outputStream, factory, new TestServerBusinessHandler());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void handle(InputStream inputStream,
                              OutputStream outputStream,
                              MessageFactory<IsoMessage> factory,
                              MesgHandle handler
    ) throws IOException, ParseException {
        Message isoMessage = receiveMessage(inputStream, factory);
        if (isoMessage == null) {
            // 
            return;
        }
        innerHandler(handler, isoMessage);

        // 
        if (isoMessage.body.getObjectValue(39) != null) {
            sendIsoMessage(isoMessage, outputStream);
        }
    }

    public static void innerHandler(MesgHandle handler, Message isoMessage) throws IOException {
        int type = isoMessage.body.getType();
        boolean macSt = true;
        switch (type) {
//            case 0x0820:
//                BaseHandle.h0820(isoMessage);
//                break;
//            case 0x0830:
//                BaseHandle.h0830(isoMessage);
//                break;
            case 0x0800:
                BaseHandle.h0800(isoMessage);
                log.debug("0800 business type: " + Integer.toHexString(type)+ " resp39="+isoMessage.body.getField(39));
                macSt = true;
                macSt = innerCheckMac(isoMessage, macSt);
                innerHandle(handler, isoMessage, macSt);
                break;
            default:
                log.debug("default business type: " + Integer.toHexString(type)+ " resp39="+isoMessage.body.getField(39));
                macSt = true;
                macSt = innerCheckMac(isoMessage, macSt);
                innerHandle(handler, isoMessage, macSt);
        }

    }

    private static void innerHandle(MesgHandle handler, Message isoMessage, boolean macSt) {
        if (macSt) {
            handler.handle(isoMessage);
        } else {
            isoMessage.body.setValue(39, "A0", IsoType.ALPHA, 2);
        }
        if (CHECK_UPI_MAC) {
            UpiKeyService keyService = MagicFactory.getService(UpiKeyService.class);
            BaseHandle.addMac(isoMessage.body, keyService.getMacKey());
        }
    }

    private static boolean innerCheckMac(Message isoMessage, boolean macSt) {
        if (CHECK_UPI_MAC) {
            UpiKeyService keyService = MagicFactory.getService(UpiKeyService.class);
            macSt = BaseHandle.checkMac(isoMessage.body, keyService.getMacKey());
            if (!macSt) {
                log.error("mac error");
            }
        }
        return macSt;
    }

    static Message receiveMessage(InputStream inputStream, MessageFactory<IsoMessage> factory) throws IOException, ParseException {
        byte[] lenBytes = new byte[4];
        int readLen = inputStream.read(lenBytes);
        if (readLen != 4) {
            return null;
        }
        log.info("receive msg:" + HexCodec.hexEncode(lenBytes));
        int len = Integer.parseInt(new String(lenBytes));
        if (len == 0) {
            // 
//            outputStream.write(lenBytes);
//            outputStream.flush();
            return Message.NullMessage;
        }
        byte[] msg = new byte[len];
        readLen = inputStream.read(msg);
        if (readLen != msg.length) {
            return null;
        }
        return createIsoMessage(msg, factory);
    }

    static void sendIsoMessage(Message isoMessage, OutputStream outputStream) throws IOException {
        byte[] respBytes = isoMessage.body.writeData();
        String lenStrLen = StringUtil.lengthFix(String.valueOf(respBytes.length + MsgHead.HEAD_LEN), 4, '0', false);
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        outs.write(lenStrLen.getBytes(StandardCharsets.US_ASCII));
        outs.write(isoMessage.head.toResponseByteArray(respBytes.length));
        outs.write(respBytes);
        byte[] rsp = outs.toByteArray();
        isoMessage.body.printMsg();
        log.debug("send msg:" + StringUtil.bytesToHexStr(rsp));
        outputStream.write(rsp);
        outputStream.flush();
    }


    public static Message createIsoMessage(byte[] bytes, MessageFactory<IsoMessage> factory) throws UnsupportedEncodingException, ParseException {
        log.debug("receive msg:" + HexCodec.hexEncode(bytes));
        MsgHead head = new MsgHead(bytes);
        if (!head.getRejectCode().equals("00000")) {
            // 
            log.error("rejectCode:" + head.getRejectCode());
            //return null;
        }
        IsoMessage isoMessage = factory.parseMessage(bytes, 0x2E);
        isoMessage.printMsg();
        return new Message(head, isoMessage);
    }

    public void sendMessage(MsgHead head, IsoMessage isoMessage) throws IOException {
        sendIsoMessage(new Message(head, isoMessage), outputStream);
    }

}
