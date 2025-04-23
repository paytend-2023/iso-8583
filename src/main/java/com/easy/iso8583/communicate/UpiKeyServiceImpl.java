package com.easy.iso8583.communicate;

import com.easy.iso8583.UpiKeyService;
import com.gd.magic.MagicException;
import com.gd.magic.MagicFactory;
import com.gd.magic.encrypt.EncryptText;
import com.gd.magic.property.PropertyService;
import com.gd.magic.util.StringUtil;

/**
 * @author dy_gu king.gu@gmail.com
 * @version V1.0
 * @date 2021/6/25 上午10:47
 * @Copyright: 2021 wepay.mpay.cn Inc. All rights reserved.
 */
public class UpiKeyServiceImpl implements UpiKeyService {

    public static final String UPI_MAC_KEY = "UPI_MAC_KEY";
    public static final String UPI_PIN_KEY = "UPI_PIN_KEY";

    @Override
    public byte[] getMKey() {
        return getKeyByName("UPI_MK", "no upi mk");
    }

    private byte[] getKeyByName(String keyName, String errMsg) {
        PropertyService propertyService = MagicFactory.getProxy(PropertyService.class);
        String key = propertyService.getSystemPropertyValue(keyName);
        if (key == null) {
            throw new MagicException(errMsg);
        }
        return EncryptText.decrypt(StringUtil.hexStrToBytes(key));
    }


    private void saveKeyByName(String keyName, byte[] key) {
        PropertyService propertyService = MagicFactory.getProxy(PropertyService.class);
        String encKey = StringUtil.bytesToHexStr(EncryptText.encrypt(key));
        propertyService.setSystemProperty(keyName, encKey);
    }

    @Override
    public byte[] getMacKey() {
        return getKeyByName(UPI_MAC_KEY, "no upi MAC key ");
    }

    @Override
    public byte[] getPinKey() {
        return getKeyByName(UPI_PIN_KEY, "no upi PIN key ");
    }

    @Override
    public boolean savePinKey(byte[] pinKey) {
        saveKeyByName(UPI_PIN_KEY, pinKey);
        return true;
    }

    @Override
    public boolean saveMacKey(byte[] mKey) {
        saveKeyByName(UPI_MAC_KEY, mKey);
        return true;
    }
}
