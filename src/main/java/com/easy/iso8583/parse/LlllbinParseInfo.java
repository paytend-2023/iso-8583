/*
 * j8583 A Java implementation of the ISO8583 protocol
 * Copyright (C) 2007 Enrique Zamudio Lopez
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;
import com.easy.iso8583.util.HexCodec;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * This class is used to parse fields of type LLLLBIN.
 *
 * @author Enrique Zamudio
 */
public class LlllbinParseInfo extends FieldParseInfo {

    public LlllbinParseInfo(IsoType t, int len) {
        super(t, len);
    }

    public LlllbinParseInfo() {
        super(IsoType.LLLLBIN, 0);
    }

    @Override
    public <T> IsoValue<?> parse(final int field, final byte[] buf,
                                 final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid LLLLBIN field %d pos %d",
                    field, pos), pos);
        } else if (pos + 4 > buf.length) {
            throw new ParseException(String.format("Insufficient LLLLBIN header field %d",
                    field), pos);
        }
        final int l = decodeLength(buf, pos, 4);
        if (l < 0) {
            throw new ParseException(String.format("Invalid LLLLBIN length %d field %d pos %d",
                    l, field, pos), pos);
        } else if (l + pos + 4 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLLBIN field %d, pos %d", field, pos), pos);
        }
        byte[] binval = l == 0 ? new byte[0] : HexCodec.hexDecode(new String(buf, pos + 4, l));

        return new IsoValue<>(type, binval, binval.length);

    }

    @Override
    public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                       final int pos)
            throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid bin LLLLBIN field %d pos %d",
                    field, pos), pos);
        } else if (pos + 2 > buf.length) {
            throw new ParseException(String.format("Insufficient LLLLBIN header field %d",
                    field), pos);
        }
        final int l = getLengthForBinaryParsing(buf, pos);
        if (l < 0) {
            throw new ParseException(String.format("Invalid LLLLBIN length %d field %d pos %d",
                    l, field, pos), pos);
        }
        if (l + pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLLLBIN field %d, pos %d requires %d, only %d available",
                    field, pos, l, buf.length - pos + 1), pos);
        }
        byte[] _v = new byte[l];
        System.arraycopy(buf, pos + 2, _v, 0, l);

        int len = getFieldLength(buf, pos);
        return new IsoValue<>(type, _v, len);

    }

    protected int getLengthForBinaryParsing(final byte[] buf, final int pos) {
        return getFieldLength(buf, pos);
    }

    private int getFieldLength(byte[] buf, int pos) {
        return forceHexadecimalLength ?
                ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff)
                :
                Bcd.parseBcdLength2bytes(buf, pos);
    }
}
