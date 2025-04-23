package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;

import java.text.ParseException;

/**
 * This class is used to parse NUMERIC fields.
 *
 * @author Enrique Zamudio
 */
public class NumericParseInfo extends AlphaNumericFieldParseInfo {

    public NumericParseInfo(int len) {
        super(IsoType.NUMERIC, len);
    }

    @Override
    public <T> IsoValue<Number> parseBinary(final int field, final byte[] buf,
                                            final int pos)
            throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid bin NUMERIC field %d pos %d", field, pos), pos);
        } else if (pos + (length / 2) > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for bin %s field %d of length %d, pos %d",
                    type, field, length, pos), pos);
        }
        //A long covers up to 18 digits
        if (length < 19) {
            return new IsoValue<Number>(IsoType.NUMERIC, Bcd.decodeToLong(buf, pos, length), length);
        } else {
            try {
                return new IsoValue<Number>(IsoType.NUMERIC, Bcd.decodeToBigInteger(buf, pos, length), length);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for bin %s field %d of length %d, pos %d",
                        type, field, length, pos), pos);
            }
        }
    }
}
