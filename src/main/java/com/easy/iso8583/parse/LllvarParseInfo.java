
package com.easy.iso8583.parse;

import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * This class is used to parse fields of type LLLVAR.
 *
 * @author Enrique Zamudio
 */
public class LllvarParseInfo extends FieldParseInfo {


    public LllvarParseInfo() {
        super(IsoType.LLLVAR, 0);
    }

    public <T> IsoValue<?> parse(final int field, final byte[] buf,
                                 final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid LLLVAR field %d pos %d",
                    field, pos), pos);
        } else if (pos + 3 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLVAR header field %d pos %d", field, pos), pos);
        }
        final int len = decodeLength(buf, pos, 3);
        if (len < 0) {
            throw new ParseException(String.format("Invalid LLLVAR length %d(%s) field %d pos %d",
                    len, new String(buf, pos, 3), field, pos), pos);
        } else if (len + pos + 3 > buf.length) {
            throw new ParseException(String.format("Insufficient data for LLLVAR field %d, pos %d len %d",
                    field, pos, len), pos);
        }
        String _v;
        try {
            _v = len == 0 ? "" : new String(buf, pos + 3, len, getCharacterEncoding());
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLVAR header, field %d pos %d len %d", field, pos, len), pos);
        }
        //This is new: if the String's length is different from the specified length in the
        //buffer, there are probably some extended characters. So we create a String from
        //the rest of the buffer, and then cut it to the specified length.
        if (_v.length() != len) {
            _v = new String(buf, pos + 3, buf.length - pos - 3,
                    getCharacterEncoding()).substring(0, len);
        }

        return new IsoValue<>(type, _v);

    }

    public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                       final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid bin LLLVAR field %d pos %d", field, pos), pos);
        } else if (pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLLVAR header, field %d pos %d", field, pos), pos);
        }
        final int len = ((buf[pos] & 0x0f) * 100) + Bcd.parseBcdLength(buf[pos + 1]);
        if (len < 0) {
            throw new ParseException(String.format(
                    "Invalid bin LLLVAR length %d, field %d pos %d", len, field, pos), pos);
        } else if (len + pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLLVAR field %d, pos %d", field, pos), pos);
        }

        return new IsoValue<>(type, new String(buf, pos + 2, len, getCharacterEncoding()));

    }

}
