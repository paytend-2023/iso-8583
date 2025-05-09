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


import com.easy.iso8583.util.Bcd;
import com.easy.iso8583.util.DateUtils;
import com.easy.iso8583.util.HexCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents a value that is stored in a field inside an ISO8583 message.
 * It can format the value when the message is generated.
 * Some values have a fixed length, other values require a length to be specified
 * so that the value can be padded to the specified length. LLVAR and LLLVAR
 * values do not need a length specification because the length is calculated
 * from the stored value.
 *
 * @author Enrique Zamudio
 */

public class IsoValue<T> implements Cloneable {

    private final IsoType type;
    private final T value;
    private int length;
    private String encoding;
    private TimeZone tz;


    /**
     * Creates a new instance that stores the specified value as the specified type.
     * Useful for storing LLVAR or LLLVAR types, as well as fixed-length value types
     * like DATE10, DATE4, AMOUNT, etc.
     *
     * @param t     the ISO type.
     * @param value The value to be stored.
     */
    public IsoValue(IsoType t, T value) {
        if (t.needsLength()) {
            throw new IllegalArgumentException("Fixed-value types must use constructor that specifies length");
        }
        type = t;
        this.value = value;
        if (type == IsoType.LLVAR || type == IsoType.LLLVAR || type == IsoType.LLLLVAR) {
            if (value instanceof byte[]) {
                length = ((byte[]) value).length;
            } else {
            	length = value.toString().length();
            }
            validateTypeWithVariableLength();
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            if (value instanceof byte[]) {
                length = ((byte[]) value).length;
            } else {
                length = value.toString().length() / 2 + (value.toString().length() % 2);
            }
            validateTypeWithVariableLength();
        } else if (type == IsoType.LLBCD || type == IsoType.LLLBCD || type == IsoType.LLLLBCD) {
            if (value instanceof byte[]) {
                length = ((byte[]) value).length * 2;
            } else {
                length = value.toString().length();
            }
            validateTypeWithVariableLength();
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            if (value instanceof byte[]) {
                length = ((byte[]) value).length;
            } else {
                length = (value.toString().length() + 1) / 2;
            }
            validateTypeWithVariableLength();
        } else {
            length = type.getLength();
        }
    }


    /**
     * Creates a new instance that stores the specified value as the specified type.
     * Useful for storing fixed-length value types.
     *
     * @param t   The ISO8583 type for this field.
     * @param val The value to store in the field.
     * @param len The length for the value.
     */
    public IsoValue(IsoType t, T val, int len) {
        type = t;
        value = val;
        length = len;
        if (length == 0 && t.needsLength()) {
            throw new IllegalArgumentException(String.format("Length must be greater than zero for type %s (value '%s')", t, val));
        } else if (t == IsoType.LLVAR || t == IsoType.LLLVAR || t == IsoType.LLLLVAR) {
            
            if (value instanceof byte[]) {
                length = ((byte[]) value).length;
            } else {
	            if (len == 0) {
	                length = val.toString().length();
	            }
            }
            validateTypeWithVariableLength();
        } else if (t == IsoType.LLBIN || t == IsoType.LLLBIN || t == IsoType.LLLLBIN) {
            if (len == 0) {
                length = ((byte[]) val).length;
            }
            validateTypeWithVariableLength();
        } else if (t == IsoType.LLBCD || t == IsoType.LLLBCD || t == IsoType.LLLLBCD) {
            if (len == 0) {
                length = val.toString().length();
            }
            validateTypeWithVariableLength();
        }
    }

    /**
     * Returns the ISO type to which the value must be formatted.
     */
    public IsoType getType() {
        return type;
    }

    /**
     * Returns the length of the stored value, of the length of the formatted value
     * in case of NUMERIC or ALPHA. It doesn't include the field length header in case
     * of LLVAR or LLLVAR.
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the stored value without any conversion or formatting.
     */
    public T getValue() {
        return value;
    }

    public void setCharacterEncoding(String value) {
        encoding = value;
    }

    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * Sets the timezone, useful for date fields.
     */
    public void setTimeZone(TimeZone value) {
        tz = value;
    }

    public TimeZone getTimeZone() {
        return tz;
    }

    /**
     * Returns the formatted value as a String. The formatting depends on the type of the
     * receiver.
     */
    public String toString() {
        if (value == null) {
            return "ISOValue<null>";
        }
        if (type == IsoType.NUMERIC) {
            return type.format(value.toString(), length);
        } else if (type == IsoType.ALPHA) {
            return type.format(value.toString(), length);
        } else if (type == IsoType.LLVAR || type == IsoType.LLLVAR || type == IsoType.LLLLVAR) {
            return getStringEncoded();
        } else if (value instanceof Date) {
            return type.format((Date) value, tz);
        } else if (value instanceof LocalDateTime) {
            return type.format((DateUtils.asDate((LocalDateTime) value)), tz);
        } else if (value instanceof LocalDate) {
            return type.format((DateUtils.asDate((LocalDate) value)), tz);
        } else if (type == IsoType.BINARY) {
            if (value instanceof byte[]) {
                final byte[] _v = (byte[]) value;
                return type.format(HexCodec.hexEncode(_v, 0, _v.length), length * 2);
            } else {
                return type.format(value.toString(), length);
            }
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            if (value instanceof byte[]) {
                final byte[] _v = (byte[]) value;
                return HexCodec.hexEncode(_v, 0, _v.length);
            } else {
                final String _s = getStringEncoded();
                return (_s.length() % 2 == 1) ? String.format("0%s", _s) : _s;
            }
        } else if (type == IsoType.LLBCD || type == IsoType.LLLBCD || type == IsoType.LLLLBCD) {
            if (value instanceof byte[]) {
                final byte[] _v = (byte[]) value;
                final String val = HexCodec.hexEncode(_v, 0, _v.length);
                if (length == val.length() - 1) {
//					return val.substring(1);
                    //BCD 国内后补0
                    return val.substring(0, length);
                }
                return val;
            } else {
                return getStringEncoded();
            }
        }
        return getStringEncoded();
    }

    private String getStringEncoded() {
        return value.toString();
    }

    /**
     * Returns a copy of the receiver that references the same value object.
     */
    @SuppressWarnings("unchecked")
    public IsoValue<T> clone() {
        try {
            return (IsoValue<T>) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    /**
     * Returns true of the other object is also an IsoValue and has the same type and length,
     * and if other.getValue().equals(getValue()) returns true.
     */
    public boolean equals(Object other) {
        if (other == null || !(other instanceof IsoValue<?>)) {
            return false;
        }
        IsoValue<?> comp = (IsoValue<?>) other;
        return (comp.getType() == getType() && comp.getValue().equals(getValue())
                && comp.getLength() == getLength());
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : toString().hashCode();
    }


    protected void writeLengthHeader(final int l, final OutputStream outs, final IsoType type,
                                     final boolean binary,
                                     final boolean forceHexadecimalLength)
            throws IOException {
        final int digits;
        if (type == IsoType.LLLLBIN || type == IsoType.LLLLVAR || type == IsoType.LLLLBCD) {
            digits = 4;
        } else if (type == IsoType.LLLBIN || type == IsoType.LLLVAR || type == IsoType.LLLBCD) {
            digits = 3;
        } else {
            digits = 2;
        }


        if (binary) {
            if (forceHexadecimalLength) {
                if (digits > 2) {
                    outs.write(new byte[]{(byte) (l >>> 8), (byte) l});
                } else {
                    outs.write(new byte[]{(byte) l});
                }
            } else {
                if (digits == 4) {
                    outs.write((((l % 10000) / 1000) << 4) | ((l % 1000) / 100));
                } else if (digits == 3) {
                    outs.write(l / 100); //00 to 09 automatically in BCD
                }
                //BCD encode the rest of the length
                outs.write((((l % 100) / 10) << 4) | (l % 10));
            }

        } else {
            //write the length in ASCII
            if (digits == 4) {
                outs.write((l / 1000) + 48);
                outs.write(((l % 1000) / 100) + 48);
            } else if (digits == 3) {
                outs.write((l / 100) + 48);
            }
            if (l >= 10) {
                outs.write(((l % 100) / 10) + 48);
            } else {
                outs.write(48);
            }
            outs.write((l % 10) + 48);
        }
    }


    /**
     * Writes the formatted value to a stream, with the length header
     * if it's a variable length type.
     *
     * @param outs   The stream to which the value will be written.
     * @param binary Specifies whether the value should be written in binary or text format.
     */
    public void write(final OutputStream outs, final boolean binary,
                      final boolean forceHexadecimalLength) throws IOException {
        outs.write(valueToBytes(binary, forceHexadecimalLength));
    }


    public byte[] valueToBytes(final boolean binary,
                               final boolean forceHexadecimalLength) throws IOException {
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        if (type == IsoType.LLLVAR || type == IsoType.LLVAR || type == IsoType.LLLLVAR) {
            writeLengthHeader(length, outs, type, binary, forceHexadecimalLength);
        } else if (type == IsoType.LLBCD || type == IsoType.LLLBCD || type == IsoType.LLLLBCD) {
            writeLengthHeader(length, outs, type, binary, forceHexadecimalLength);
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            writeLengthHeader(length, outs, type, binary, forceHexadecimalLength);
        } else if (binary) {
            //numeric types in binary are coded like this
            byte[] buf = null;
            if (type == IsoType.NUMERIC) {
                buf = new byte[(length / 2) + (length % 2)];
            } else if (type == IsoType.DATE10 || type == IsoType.DATE4 ||
                    type == IsoType.DATE_EXP || type == IsoType.TIME ||
                    type == IsoType.DATE12 || type == IsoType.DATE14) {
                buf = new byte[length / 2];
            }
            //Encode in BCD if it's one of these types
            if (buf != null) {
                Bcd.encode(toString(), buf);
                outs.write(buf);
                return outs.toByteArray();
            }
        }
        if (binary && (type == IsoType.BINARY || IsoType.VARIABLE_LENGTH_BIN_TYPES.contains(type))) {
            int missing;
            if (value instanceof byte[]) {
                outs.write((byte[]) value);
                missing = length - ((byte[]) value).length;
            } else {
                byte[] binval;
                if (value.toString().length() % 2 == 1) {
                    binval = HexCodec.hexDecode(value.toString() + "0");
                } else {
                    binval = HexCodec.hexDecode(value.toString());
                }
                outs.write(binval);
                missing = length - binval.length;
            }
            if (type == IsoType.BINARY && missing > 0) {
                for (int i = 0; i < missing; i++) {
                    outs.write(0);
                }
            }
        } else {

            if (value instanceof byte[]) {
                outs.write((byte[]) value);
                //missing = length - ((byte[]) value).length;
            } 
            else
            {
            	outs.write(encoding == null ? toString().getBytes() : toString().getBytes(encoding));
            }
        }
        return outs.toByteArray();
    }

    private void validateTypeWithVariableLength() {

        validateDecimalVariableLength();

    }


    private void validateDecimalVariableLength() {
        switch (type) {
            case LLVAR:
            case LLBIN:
            case LLBCD:
                validateMaxLength(99);
                break;
            case LLLVAR:
            case LLLBCD:
            case LLLBIN:
                validateMaxLength(999);
                break;
            case LLLLVAR:
            case LLLLBIN:
            case LLLLBCD:
                validateMaxLength(9999);
                break;
        }
    }

    private void validateMaxLength(int maxLength) {
        if (length > maxLength) {
            throw new IllegalArgumentException(type.name() + " can only hold values up to " + maxLength + " chars");
        }
    }

}
