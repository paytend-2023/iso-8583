
package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * This class is used to parse fields of type LLVAR.
 *
 * @author Enrique Zamudio
 */
public class LlvarParseInfo extends FieldParseInfo {

    public LlvarParseInfo() {
        super(IsoType.LLVAR, 0);
    }

    @Override
    public IsoValue<String> parse(final int field, final byte[] buf, final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format(
                    "Invalid LLVAR field %d %d", field, pos), pos);
        } else if (pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLVAR header, pos %d", pos), pos);
        }
        final int len = decodeLength(buf, pos, 2);
        if (len < 0) {
            throw new ParseException(String.format(
                    "Invalid LLVAR length %d, field %d pos %d", len, field, pos), pos);
        } else if (len + pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLVAR field %d, pos %d len %d",
                    field, pos, len), pos);
        }
        String _v;
        try {
            _v = len == 0 ? "" : new String(buf, pos + 2, len, getCharacterEncoding());
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for LLVAR header, field %d pos %d len %d",
                    field, pos, len), pos);
        }
        //This is new: if the String's length is different from the specified
        // length in the buffer, there are probably some extended characters.
        // So we create a String from the rest of the buffer, and then cut it to
        // the specified length.
        if (_v.length() != len) {
            _v = new String(buf, pos + 2, buf.length - pos - 2,
                    getCharacterEncoding()).substring(0, len);
        }

        return new IsoValue<String>(type, _v, len);

    }

    @Override
    public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                       final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid bin LLVAR field %d pos %d",
                    field, pos), pos);
        } else if (pos + 1 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLVAR header, field %d pos %d",
                    field, pos), pos);
        }
        final int len = Bcd.parseBcdLength(buf[pos]);
        if (len < 0) {
            throw new ParseException(String.format(
                    "Invalid bin LLVAR length %d, field %d pos %d", len, field, pos), pos);
        }
        if (len + pos + 1 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLVAR field %d, pos %d", field, pos), pos);
        }

        return new IsoValue<String>(type, new String(buf, pos + 1, len, getCharacterEncoding()));

    }

}
