
package com.easy.iso8583.parse;

import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.HexCodec;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * This class is used to parse AMOUNT fields.
 *
 * @author Enrique Zamudio
 */
public class AmountParseInfo extends FieldParseInfo {


    public AmountParseInfo() {
        super(IsoType.AMOUNT, 12);
    }

    @Override
    public <T> IsoValue<Long> parse(final int field, final byte[] buf,
                                    final int pos)
            throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid AMOUNT field %d position %d",
                    field, pos), pos);
        }
        if (pos + 12 > buf.length) {
            throw new ParseException(String.format("Insufficient data for AMOUNT field %d, pos %d",
                    field, pos), pos);
        }
        String c = new String(buf, pos, 12, getCharacterEncoding());
        try {
            return new IsoValue<>(type, Long.parseLong(c));
        } catch (NumberFormatException ex) {
            throw new ParseException(String.format("Cannot read amount '%s' field %d pos %d",
                    c, field, pos), pos);
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for AMOUNT field %d, pos %d", field, pos), pos);
        }
    }

    @Override
    public <T> IsoValue<Long> parseBinary(final int field, final byte[] buf,
                                          final int pos)
            throws ParseException {

        String str = HexCodec.hexEncode(buf, pos, 6);

        try {
            return new IsoValue<>(IsoType.AMOUNT, Long.parseLong(str));
        } catch (NumberFormatException ex) {
            throw new ParseException(String.format("Cannot read amount '%s' field %d pos %d",
                    str, field, pos), pos);
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for AMOUNT field %d, pos %d", field, pos), pos);
        }
    }

}
