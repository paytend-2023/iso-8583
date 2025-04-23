
package com.easy.iso8583.parse;


import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.util.Bcd;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/** This class is used to parse fields of type DATE4.
 * 
 * @author Enrique Zamudio
 */
public class Date4ParseInfo extends DateTimeParseInfo {

	public Date4ParseInfo() {
		super(IsoType.DATE4, 4);
	}

	@Override
	public <T> IsoValue<Date> parse(final int field, final byte[] buf, final int pos )
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid DATE4 field %d position %d",
                    field, pos), pos);
		}
		if (pos+4 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for DATE4 field %d, pos %d", field, pos), pos);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//Set the month in the date
        if (forceStringDecoding) {
            cal.set(Calendar.MONTH, Integer.parseInt(new String(buf, pos, 2, getCharacterEncoding()), 10)-1);
            cal.set(Calendar.DATE, Integer.parseInt(new String(buf, pos+2, 2, getCharacterEncoding()), 10));
        } else {
            cal.set(Calendar.MONTH, ((buf[pos] - 48) * 10) + buf[pos + 1] - 49);
            cal.set(Calendar.DATE, ((buf[pos + 2] - 48) * 10) + buf[pos + 3] - 48);
        }
        return createValue(cal, true,length);
	}

	@Override
	public <T> IsoValue<Date> parseBinary(final int field, final byte[] buf, final int pos ) throws ParseException {
		int[] tens = new int[2];
		int start = 0;
        if (buf.length-pos < 2) {
            throw new ParseException(String.format(
                    "Insufficient data to parse binary DATE4 field %d pos %d",
                    field, pos), pos);
        }
		for (int i = pos; i < pos + tens.length; i++) {
			tens[start++] = Bcd.parseBcdLength(buf[i]);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		//Set the month in the date
		cal.set(Calendar.MONTH, tens[0] - 1);
		cal.set(Calendar.DATE, tens[1]);
		cal.set(Calendar.MILLISECOND,0);
        return createValue(cal, true,length);
	}

}
