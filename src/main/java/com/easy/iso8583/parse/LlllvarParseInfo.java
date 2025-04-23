
package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Blabla.
 *
 * @author Enrique Zamudio
 * Date: 19/02/15 18:30
 */
public class LlllvarParseInfo extends FieldParseInfo {

    public LlllvarParseInfo() {
        super(IsoType.LLLLVAR, 0);
    }

    @Override
    public IsoValue<String> parse(final int field, final byte[] buf,
                                  final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format(
                    "Invalid LLLLVAR field %d %d", field, pos), pos);
        } else if (pos + 4 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLLVAR header, pos %d", pos), pos);
        }
        final int len = decodeLength(buf, pos, 4);
        if (len < 0) {
            throw new ParseException(String.format(
                    "Invalid LLLLVAR length %d, field %d pos %d", len, field, pos), pos);
        } else if (len + pos + 4 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLLVAR field %d, pos %d", field, pos), pos);
        }
        String _v;
        try {
            _v = len == 0 ? "" : new String(buf, pos + 4, len, getCharacterEncoding());
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLLVAR header, field %d pos %d", field, pos), pos);
        }
        //This is new: if the String's length is different from the specified
        // length in the buffer, there are probably some extended characters.
        // So we create a String from the rest of the buffer, and then cut it to
        // the specified length.
        if (_v.length() != len) {
            _v = new String(buf, pos + 4, buf.length - pos - 4,
                    getCharacterEncoding()).substring(0, len);
        }

        return new IsoValue<String>(type, _v, len);

    }

    @Override
    public IsoValue<String> parseBinary(final int field, final byte[] buf, final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid bin LLLLVAR field %d pos %d",
                    field, pos), pos);
        } else if (pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLLLVAR header, field %d pos %d",
                    field, pos), pos);
        }
        final int len = Bcd.parseBcdLength2bytes(buf, pos);
        if (len < 0) {
            throw new ParseException(String.format(
                    "Invalid bin LLLLVAR length %d, field %d pos %d", len, field, pos), pos);
        }
        if (len + pos + 2 > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin LLLLVAR field %d, pos %d", field, pos), pos);
        }
        return new IsoValue<String>(type, new String(buf, pos + 2, len, getCharacterEncoding()), 0);
    }

}
