package com.easy.iso8583;

import com.gd.magic.Service;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/6/24 上午11:37
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public interface UpiKeyService extends Service {

    byte[] getMKey();

    byte[] getMacKey();

    byte[] getPinKey();

    boolean savePinKey(byte[] pinKey);

    boolean saveMacKey(byte[] mKey);
}
