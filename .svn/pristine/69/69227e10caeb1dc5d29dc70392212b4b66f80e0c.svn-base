/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2011 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.easy.iso8583.parse;

import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * This class is used to parse fields of type BINARY.
 *
 * @author Enrique Zamudio
 */
public class BinaryParseInfo extends FieldParseInfo {


    public BinaryParseInfo(int len) {
        super(IsoType.BINARY, len);
    }

    @Override
    public IsoValue<byte[]> parse(final int field, final byte[] buf, final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid BINARY field %d position %d",
                    field, pos), pos);
        }
        if (pos + (length) > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for BINARY field %d of length %d, pos %d",
                    field, length, pos), pos);
        }
        byte[] binval = new byte[length];
        System.arraycopy(buf, pos, binval, 0, length);
        return new IsoValue<>(type, binval, binval.length);

    }

    @Override
    public IsoValue<byte[]> parseBinary(final int field, final byte[] buf, final int pos
                                        ) throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid BINARY field %d position %d",
                    field, pos), pos);
        }
        if (pos + length > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for BINARY field %d of length %d, pos %d",
                    field, length, pos), pos);
        }
        byte[] _v = new byte[length];
        System.arraycopy(buf, pos, _v, 0, length);
        return new IsoValue<>(type, _v, length);

    }

}
