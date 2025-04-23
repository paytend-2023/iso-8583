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

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * This is the common abstract superclass to parse ALPHA and NUMERIC field types.
 *
 * @author Enrique Zamudio
 */
public abstract class AlphaNumericFieldParseInfo extends FieldParseInfo {

    public AlphaNumericFieldParseInfo(IsoType t, int len) {
        super(t, len);
    }

    @Override
    public IsoValue<String> parse(final int field, final byte[] buf, final int pos) throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid ALPHA/NUM field %d position %d",
                    field, pos), pos);
        } else if (pos + length > buf.length) {
            throw new ParseException(String.format("Insufficient data for %s field %d of length %d, pos %d", type, field, length, pos), pos);
        }
        try {
            String _v = new String(buf, pos, length, StandardCharsets.US_ASCII);
            if (_v.length() != length) {
                _v = new String(buf, pos, buf.length - pos, StandardCharsets.US_ASCII).substring(0, length);
            }
            return new IsoValue<>(type, _v, length);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for %s field %d of length %d, pos %d",
                    type, field, length, pos), pos);
        }
    }

}
