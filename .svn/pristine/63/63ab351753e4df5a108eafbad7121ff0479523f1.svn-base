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
package com.easy.iso8583;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Defines the possible values types that can be used in the fields.
 * Some types required the length of the value to be specified (NUMERIC
 * and ALPHA). Other types have a fixed length, like dates and times.
 * Other types do not require a length to be specified, like LLVAR
 * and LLLVAR.
 *
 * @author Enrique Zamudio
 */
public enum IsoType {

    /**
     * A fixed-length numeric value. It is zero-filled to the left.
     */
    NUMERIC(true, 0),

    /**
     * A fixed-length alphanumeric value. It is filled with spaces to the right.
     */
    ALPHA(true,  0),

    /**
     * A variable length alphanumeric value with a 2-digit header length.
     */
    LLVAR(false, 0),

    /**
     * A variable length alphanumeric value with a 3-digit header length.
     */
    LLLVAR(false, 0),  //new String()

    /**
     * variable length with 4-digit header length.
     */
    LLLLVAR(false, 0),

    /**
     * Similar to ALPHA but holds byte arrays instead of strings.
     */
    BINARY(true, 0),
    /**
     * Similar to LLVAR but holds byte arrays instead of strings.
     */
    LLBIN(false, 0),  // hexDecode(new String ())
    /**
     * Similar to LLLVAR but holds byte arrays instead of strings.
     */
    LLLBIN(false, 0),

    /**
     * variable length byte array with 4-digit header length.
     */
    LLLLBIN(false, 0),

    /** An amount, expressed in cents with a fixed length of 12. */
    AMOUNT(false, 12),

    /**
     * A date in format YYYYMMddHHmmss
     */
    DATE14(false, 14),
    /**
     * A date in format MMddHHmmss
     */
    DATE10(false, 10),
    /**
     * A date in format MMdd
     */
    DATE4(false, 4),
    /**
     * A date in format yyMM
     */
    DATE_EXP(false, 4),
    /**
     * Time of day in format HHmmss
     */
    TIME(false, 6),


    /**
     * Similar to LLBIN but with a BCD encoded length.
     */
    LLBCD(false, 0),

    /**
     * Similar to LLLBIN but with a BCD encoded length.
     */
    LLLBCD(false, 0),
    LLLLBCD(false, 0),


    /**
     * Date in format yyMMddHHmmss.
     */
    DATE12(false, 12),
    /**
     * Date in format yyMMdd
     */
    DATE6(false, 6);



    public static final Set<IsoType> VARIABLE_LENGTH_BIN_TYPES = Collections.unmodifiableSet(EnumSet.of(LLBIN, LLLBIN, LLLLBIN));

    private final boolean needsLen;
    private final int length;

    IsoType(boolean flag, int l) {
        needsLen = flag;
        length = l;

    }

    IsoType(boolean flag, int l, boolean end) {
        needsLen = flag;
        length = l;
    }


    /**
     * Returns true if the type needs a specified length.
     */
    public boolean needsLength() {
        return needsLen;
    }

    /**
     * Returns the length of the type if it's always fixed, or 0 if it's variable.
     */
    public int getLength() {
        return length;
    }

    /**
     * Formats a Date if the receiver is DATE10, DATE4, DATE_EXP, DATE12, DATE14 or TIME; throws an exception
     * otherwise.
     */
    public String format(final Date value, TimeZone tz) {
        final SimpleDateFormat sdf;
        if (this == DATE10) {
            sdf = new SimpleDateFormat("MMddHHmmss");
        } else if (this == DATE4) {
            sdf = new SimpleDateFormat("MMdd");
        } else if (this == DATE_EXP) {
            sdf = new SimpleDateFormat("yyMM");
        } else if (this == TIME) {
            sdf = new SimpleDateFormat("HHmmss");
        } else if (this == DATE12) {
            sdf = new SimpleDateFormat("yyMMddHHmmss");
        } else if (this == DATE14) {
            sdf = new SimpleDateFormat("YYYYMMddHHmmss");
        } else if (this == DATE6) {
            sdf = new SimpleDateFormat("yyMMdd");
        } else {
            throw new IllegalArgumentException("Cannot format date as " + this);
        }
        if (tz != null) {
            sdf.setTimeZone(tz);
        }
        return sdf.format(value);
    }

    /**
     * Formats the string to the given length (length is only useful if type is ALPHA, NUMERIC or BINARY).
     */
    public String format(String value, int length) {
        if (this == ALPHA) {
            if (value == null) {
                value = "";
            }
            if (value.length() > length) {
                return value.substring(0, length);
            } else if (value.length() == length) {
                return value;
            } else {
                return String.format(String.format("%%-%ds", length), value);
            }
        } else if (this == LLVAR || this == LLLVAR || this == LLLLVAR) {
            return value;
        } else if (this == NUMERIC) {
            char[] c = new char[length];
            char[] x = value.toCharArray();
            if (x.length > length) {
                throw new IllegalArgumentException("Numeric value is larger than intended length: " + value + " LEN " + length);
            }
            int lim = c.length - x.length;
            for (int i = 0; i < lim; i++) {
                c[i] = '0';
            }
            System.arraycopy(x, 0, c, lim, x.length);
            return new String(c);
        } else if (this == BINARY) {

            if (value == null) {
                value = "";
            }
            if (value.length() > length) {
                return value.substring(0, length);
            }
            char[] c = new char[length];
            int end = value.length();
            if (value.length() % 2 == 1) {
                c[0] = '0';
                System.arraycopy(value.toCharArray(), 0, c, 1, value.length());
                end++;
            } else {
                System.arraycopy(value.toCharArray(), 0, c, 0, value.length());
            }
            for (int i = end; i < c.length; i++) {
                c[i] = '0';
            }
            return new String(c);

        } else if (VARIABLE_LENGTH_BIN_TYPES.contains(this)) {
            return value;
        }
        throw new IllegalArgumentException("Cannot format String as " + this);
    }

    /**
     * Formats the integer value as a NUMERIC, an AMOUNT, or a String.
     */
    public String format(long value, int length) {
        if (this == NUMERIC) {
            String x = String.format(String.format("%%0%dd", length), value);
            if (x.length() > length) {
                throw new IllegalArgumentException("Numeric value is larger than intended length: " + value + " LEN " + length);
            }
            return x;
        } else if (this == ALPHA || this == LLVAR || this == LLLVAR || this == LLLLVAR) {
               return String.format(String.format("%%-%ds", length), value);
        }
//        else if (this == AMOUNT) {
//            return String.format("%010d00", value);
//        } else if (this == BIN || VARIABLE_LENGTH_BIN_TYPES.contains(this)) {
//            //TODO
//        }
        throw new IllegalArgumentException("Cannot format number as " + this);
    }



//    public <T> IsoValue<T> value(T val, int len) {
//        return new IsoValue<>(this, val, len);
//    }
//
//    public <T> IsoValue<T> value(T val) {
//        return new IsoValue<>(this, val);
//    }
//
//    public <T> IsoValue<T> call(T val, int len) {
//        return new IsoValue<>(this, val, len);
//    }
//
//    public <T> IsoValue<T> call(T val) {
//        return new IsoValue<>(this, val);
//    }
//
//    public <T> IsoValue<T> apply(T val, int len) {
//        return new IsoValue<>(this, val, len);
//    }
//
//    public <T> IsoValue<T> apply(T val) {
//        return new IsoValue<>(this, val);
//    }

}
