package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;

/**
 * Custom class to parse fields of type LLBCDBIN with BCD length.
 */
public class BcdLengthLlbinParseInfo extends LlbinParseInfo {

    public BcdLengthLlbinParseInfo() {
        super(IsoType.LLBCD, 0);
    }

    @Override
    protected int getLengthForBinaryParsing(byte b) {
        final int length = super.getLengthForBinaryParsing(b);
        return length % 2 == 0 ? length / 2 : (length / 2) + 1;
    }

}
