package com.easy.iso8583.communicate;

import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.MsgHead;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/5/16 下午9:40
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class Message {
    public MsgHead head;
    public IsoMessage body;

    public Message(MsgHead head, IsoMessage body) {
        this.head = head;
        this.body = body;
    }

    final public static Message NullMessage = new Message(null, null) {
    };


}
