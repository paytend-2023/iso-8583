/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * This class is used to parse a field from a message buffer. There are concrete subclasses for each IsoType.
 *
 * @author Enrique Zamudio
 */
public abstract class FieldParseInfo {

    protected IsoType type;
    protected final int length;
    private String encoding = System.getProperty("file.encoding");
    protected boolean forceStringDecoding;
    protected boolean forceHexadecimalLength;

    /**
     * Creates a new instance that parses a value of the specified type, with the specified length.
     * The length is only useful for ALPHA and NUMERIC types.
     *
     * @param t   The ISO type to be parsed.
     * @param len The length of the data to be read (useful only for ALPHA and NUMERIC types).
     */
    public FieldParseInfo(IsoType t, int len) {
        if (t == null) {
            throw new IllegalArgumentException("IsoType cannot be null");
        }
        type = t;
        length = len;
    }

    /**
     * Specified whether length headers for variable-length fields in text mode should
     * be decoded using proper string conversion with the character encoding. Default is false,
     * which means use the old behavior of decoding as ASCII.
     */
    public void setForceStringDecoding(boolean flag) {
        forceStringDecoding = flag;
    }

    /**
     * Specifies whether length headers for variable-length fields in binary mode should
     * be decoded as a hexadecimal values. Default is false, which means decoding the length as BCD.
     */
    public void setForceHexadecimalLength(boolean flag) {
        this.forceHexadecimalLength = flag;
    }

    public void setCharacterEncoding(String value) {
        encoding = value;
    }

    public String getCharacterEncoding() {
        return StandardCharsets.US_ASCII.displayName();
    }

    /**
     * Returns the specified length for the data to be parsed.
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the data type for the data to be parsed.
     */
    public IsoType getType() {
        return type;
    }


    /**
     * Parses the character data from the buffer and returns the
     * IsoValue with the correct data type in it.
     *
     * @param field The field index, useful for error reporting.
     * @param buf   The full ISO message buffer.
     * @param pos   The starting position for the field data.
     *              <p>
     *
     *              throws ParseException, UnsupportedEncodingException;
     *              <p>
     *              /** Parses binary data from the buffer, creating and returning an IsoValue of the configured
     *              type and length.
     *          */
    public abstract <T> IsoValue<?> parse(final int field, byte[] buf, int pos ) throws ParseException, UnsupportedEncodingException;
    /* *
     * @param field The field index, useful for error reporting.
     * @param buf   The full ISO message buffer.
     * @param pos   The starting position for the field data.
     *              public abstract <T> IsoValue<?> parseBinary(final int field, byte[] buf, int pos )
     *              throws ParseException, UnsupportedEncodingException;
     *              <p>
     *              /** Returns a new FieldParseInfo instance that can parse the specified type.
     */

    public abstract <T> IsoValue<?> parseBinary(final int field, byte[] buf, int pos ) throws ParseException, UnsupportedEncodingException;

    public static FieldParseInfo getInstance(IsoType t, int len, String encoding) {
        FieldParseInfo fpi = null;
        if (t == IsoType.ALPHA) {
            fpi = new AlphaParseInfo(len);
        }
//		else if (t == IsoType.AMOUNT) {
//			fpi = new AmountParseInfo();
//		}
        else if (t == IsoType.BINARY) {
            fpi = new BinaryParseInfo(len);
        } else if (t == IsoType.DATE10) {
            fpi = new Date10ParseInfo();
        } else if (t == IsoType.DATE12) {
            fpi = new Date12ParseInfo();
        } else if (t == IsoType.DATE14) {
            fpi = new Date14ParseInfo();
        } else if (t == IsoType.DATE4) {
            fpi = new Date4ParseInfo();
        } else if (t == IsoType.DATE_EXP) {
            fpi = new DateExpParseInfo();
        } else if (t == IsoType.DATE6) {
            fpi = new Date6ParseInfo();
        } else if (t == IsoType.LLBIN) {
            fpi = new LlbinParseInfo();
        } else if (t == IsoType.LLLBIN) {
            fpi = new LllbinParseInfo();
        } else if (t == IsoType.LLLVAR) {
            fpi = new LllvarParseInfo();
        } else if (t == IsoType.LLVAR) {
            fpi = new LlvarParseInfo();
        } else if (t == IsoType.NUMERIC) {
            fpi = new NumericParseInfo(len);
        } else if (t == IsoType.TIME) {
            fpi = new TimeParseInfo();
        } else if (t == IsoType.LLLLVAR) {
            fpi = new LlllvarParseInfo();
        } else if (t == IsoType.LLLLBIN) {
            fpi = new LlllbinParseInfo();
        } else if (t == IsoType.LLBCD) {
            fpi = new BcdLengthLlbinParseInfo();
        } else if (t == IsoType.LLLBCD) {
            fpi = new BcdLengthLllbinParseInfo();
        } else if (t == IsoType.LLLLBCD) {
            fpi = new BcdLengthLlllbinParseInfo();
        }
        if (fpi == null) {
            throw new IllegalArgumentException(String.format("Cannot parse type %s", t));
        }
        fpi.setCharacterEncoding(encoding);
        return fpi;
    }

    protected int decodeLength(byte[] buf, int pos, int digits) throws UnsupportedEncodingException {
        if (forceStringDecoding) {
            return Integer.parseInt(new String(buf, pos, digits, encoding), 10);
        } else {
            switch (digits) {
                case 2:
                    return ((buf[pos] - 48) * 10) + (buf[pos + 1] - 48);
                case 3:
                    return ((buf[pos] - 48) * 100) + ((buf[pos + 1] - 48) * 10)
                            + (buf[pos + 2] - 48);
                case 4:
                    return ((buf[pos] - 48) * 1000) + ((buf[pos + 1] - 48) * 100)
                            + ((buf[pos + 2] - 48) * 10) + (buf[pos + 3] - 48);
            }
        }
        return -1;
    }

}
