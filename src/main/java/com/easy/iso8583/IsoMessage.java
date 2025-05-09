
package com.easy.iso8583;


import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Map;

/**
 * Represents an ISO8583 message. This is the core class of the framework.
 * Contains the bitmap which is modified as fields are added/removed.
 * This class makes no assumptions as to what types belong in each field,
 * nor what fields should each different message type have; that is left
 * for the developer, since the different ISO8583 implementations can vary
 * greatly.
 *
 * @author Enrique Zamudio
 */

public class IsoMessage {
    static Logger logger = Logger.getLogger(IsoMessage.class);

    static final byte[] HEX = new byte[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * The message type.
     */
    private int type;
    private boolean binaryFields;

    /**
     * This is where the values are stored.
     */
    private IsoValue[] fields = new IsoValue[129];


    /**
     * Flag to enforce secondary bitmap even if empty.
     */
    private boolean forceb2;
    private boolean encodeVariableLengthFieldsInHex;
    private String encoding = "ISO-8859-1";

    /**
     * Creates a new empty message with no values set.
     */
    public IsoMessage() {
    }


    /**
     * If set, this flag will cause the secondary bitmap to be written even if it's not needed.
     */
    public void setForceSecondaryBitmap(boolean flag) {
        forceb2 = flag;
    }

    /**
     * Returns true if the secondary bitmap is always included in the message, even
     * if it's not needed. Default is false.
     */
    public boolean getForceSecondaryBitmap() {
        return forceb2;
    }

    /**
     * Sets the encoding to use.
     */
    public void setCharacterEncoding(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot set null encoding.");
        }
        encoding = value;
    }

    /**
     * Returns the character encoding for Strings inside the message. Default
     * is taken from the file.encoding system property.
     */
    public String getCharacterEncoding() {
        return encoding;
    }


    /**
     * Specified whether the variable-length fields should encode their length
     * headers using hexadecimal values. This is only useful for binary format.
     */
    public void setEncodeVariableLengthFieldsInHex(boolean flag) {
        this.encodeVariableLengthFieldsInHex = flag;
    }

    public boolean isEncodeVariableLengthFieldsInHex() {
        return encodeVariableLengthFieldsInHex;
    }


    /**
     * Sets the ISO message type. Common values are 0x200, 0x210, 0x400, 0x410, 0x800, 0x810.
     */
    public void setType(int value) {
        type = value;
    }

    /**
     * Returns the ISO message type.
     */
    public int getType() {
        return type;
    }

    /**
     * Indicates whether the message should be binary. Default is false.
     * To encode the message as text but the bitmap in binary format, you can set the
     * binaryBitmap flag.
     */
    public void setBinary(boolean flag) {
        binaryFields = flag;
    }

    /**
     * Returns true if the message is binary coded (both header and fields); default is false.
     *
     * @deprecated Use the new flags isBinaryHeader and isBinaryFields instead.
     */
    @Deprecated
    public boolean isBinary() {
        return binaryFields;
    }


    /**
     * field data is binary encoded
     */
    public void setBinaryFields(boolean flag) {
        binaryFields = flag;
    }

    /**
     * field data is binary encoded
     */
    public boolean isBinaryFields() {
        return binaryFields;
    }


    /**
     * Returns the stored value in the field, without converting or formatting it.
     *
     * @param field The field number. 1 is the secondary bitmap and is not returned as such;
     *              real fields go from 2 to 128.
     */
    public <T> T getObjectValue(int field) {
        @SuppressWarnings("unchecked")
        IsoValue<T> v = fields[field];
        return v == null ? null : v.getValue();
    }

    /**
     * Returns the IsoValue for the specified field. First real field is 2.
     */
    @SuppressWarnings("unchecked")
    public <T> IsoValue<T> getField(int field) {
        return fields[field];
    }

    /**
     * Stored the field in the specified index. The first field is the secondary bitmap and has index 1,
     * so the first valid value for index must be 2.
     *
     * @return The receiver (useful for setting several fields in sequence).
     */
    public IsoMessage setField(int index, IsoValue<?> field) {
        if (index < 2 || index > 128) {
            throw new IndexOutOfBoundsException("Field index must be between 2 and 128");
        }
        if (field != null) {
            field.setCharacterEncoding(encoding);
        }
        fields[index] = field;
        return this;
    }

    /**
     * Convenience method for setting several fields in one call.
     */
    public IsoMessage setFields(Map<Integer, IsoValue<?>> values) {
        for (Map.Entry<Integer, IsoValue<?>> e : values.entrySet()) {
            setField(e.getKey(), e.getValue());
        }
        return this;
    }


    /**
     * Sets the specified value in the specified field, creating an IsoValue internally.
     *
     * @param index  The field number (2 to 128)
     * @param value  The value to be stored.
     * @param t      The ISO type.
     * @param length The length of the field, used for ALPHA and NUMERIC values only, ignored
     *               with any other type.
     * @return The receiver (useful for setting several values in sequence).
     */
    public <T> IsoMessage setValue(int index, T value, IsoType t, int length) {
        if (index < 2 || index > 128) {
            throw new IndexOutOfBoundsException("Field index must be between 2 and 128");
        }
        if (value == null) {
            fields[index] = null;
        } else {
            IsoValue<T> v = null;
            if (t.needsLength()) {
                v = new IsoValue<>(t, value, length);
            } else {
                v = new IsoValue<>(t, value);
            }
            v.setCharacterEncoding(encoding);
            fields[index] = v;
        }
        return this;
    }

    /**
     * A convenience method to set new values in fields that already contain values.
     * The field's type, length and custom encoder are taken from the current value.
     * This method can only be used with fields that have been previously set,
     * usually from a template in the MessageFactory.
     *
     * @param index The field's index
     * @param value The new value to be set in that field.
     * @return The message itself.
     * @throws IllegalArgumentException if there is no current field at the specified index.
     */
    public <T> IsoMessage updateValue(int index, T value) {
        IsoValue<T> current = getField(index);
        if (current == null) {
            throw new IllegalArgumentException("Value-only field setter can only be used on existing fields");
        } else {
            setValue(index, value, current.getType(), current.getLength());
            getField(index).setCharacterEncoding(current.getCharacterEncoding());
            getField(index).setTimeZone(current.getTimeZone());
        }
        return this;
    }

    /**
     * Returns true is the message has a value in the specified field.
     *
     * @param idx The field number.
     */
    public boolean hasField(int idx) {
        return fields[idx] != null;
    }

    /**
     * Writes a message to a stream, after writing the specified number of bytes indicating
     * the message's length. The message will first be written to an internal memory stream
     * which will then be dumped into the specified stream. This method flushes the stream
     * after the write. There are at most three write operations to the stream: one for the
     * length header, one for the message, and the last one with for the ETX.
     *
     * @param outs        The stream to write the message to.
     * @param lengthBytes The size of the message length header. Valid ranges are 0 to 4.
     * @throws IllegalArgumentException if the specified length header is more than 4 bytes.
     * @throws IOException              if there is a problem writing to the stream.
     */
    public void write(OutputStream outs, int lengthBytes) throws IOException {
        if (lengthBytes > 4) {
            throw new IllegalArgumentException("The length header can have at most 4 bytes");
        }
        byte[] data = writeData();

        if (lengthBytes > 0) {
            int l = data.length;
            byte[] buf = new byte[lengthBytes];
            int pos = 0;
            if (lengthBytes == 4) {
                buf[0] = (byte) ((l & 0xff000000) >> 24);
                pos++;
            }
            if (lengthBytes > 2) {
                buf[pos] = (byte) ((l & 0xff0000) >> 16);
                pos++;
            }
            if (lengthBytes > 1) {
                buf[pos] = (byte) ((l & 0xff00) >> 8);
                pos++;
            }
            buf[pos] = (byte) (l & 0xff);
            outs.write(buf);
        }
        outs.write(data);
        outs.flush();
    }

    /**
     * Creates and returns a ByteBuffer with the data of the message, including the length header.
     * The returned buffer is already flipped, so it is ready to be written to a Channel.
     */
    public ByteBuffer writeToBuffer(int lengthBytes) {
        if (lengthBytes > 4) {
            throw new IllegalArgumentException("The length header can have at most 4 bytes");
        }

        byte[] data = writeData();
        ByteBuffer buf = ByteBuffer.allocate(lengthBytes + data.length);
        if (lengthBytes > 0) {
            int l = data.length;
            if (lengthBytes == 4) {
                buf.put((byte) ((l & 0xff000000) >> 24));
            }
            if (lengthBytes > 2) {
                buf.put((byte) ((l & 0xff0000) >> 16));
            }
            if (lengthBytes > 1) {
                buf.put((byte) ((l & 0xff00) >> 8));
            }
            buf.put((byte) (l & 0xff));
        }
        buf.put(data);
        buf.flip();
        return buf;
    }

    /**
     * Creates a BitSet for the bitmap.
     */
    protected BitSet createBitmapBitSet() {
        BitSet bs = new BitSet(forceb2 ? 128 : 64);
        for (int i = 2; i < 129; i++) {
            if (fields[i] != null) {
                bs.set(i - 1);
            }
        }
        if (forceb2) {
            bs.set(0);
        } else if (bs.length() > 64) {
            //Extend to 128 if needed
            BitSet b2 = new BitSet(128);
            b2.or(bs);
            bs = b2;
            bs.set(0);
        }
        return bs;
    }

    /**
     * Writes the message to a memory stream and returns a byte array with the result.
     */
    public byte[] writeData() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            bout.write(String.format("%04x", type).getBytes(encoding));
        } catch (IOException ex) {
            //should never happen, writing to a ByteArrayOutputStream
        }
        //Bitmap
        BitSet bs = createBitmapBitSet();
        //Write bitmap to stream

        int pos = 128;
        int b = 0;
        for (int i = 0; i < bs.size(); i++) {
            if (bs.get(i)) {
                b |= pos;
            }
            pos >>= 1;
            if (pos == 0) {
                bout.write(b);
                pos = 128;
                b = 0;
            }


        }

        //Fields
        for (int i = 2; i < 129; i++) {
            IsoValue<?> v = fields[i];
            if (v != null) {
                try {
                    v.write(bout, binaryFields, false);
                } catch (IOException ex) {
                    //should never happen, writing to a ByteArrayOutputStream
                }
            }
        }
        return bout.toByteArray();
    }


    public byte[] createBytes(int field) {
        try {
            return getField(field).valueToBytes(binaryFields, false);
        } catch (IOException ioException) {
            return new byte[0];
        }
    }

    /**
     * Returns a string representation of the message, as if it were encoded
     * in ASCII with no binary bitmap.
     */
    public String debugString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04x", type));
        //Bitmap
        BitSet bs = createBitmapBitSet();
        int pos = 0;
        int lim = bs.size() / 4;
        for (int i = 0; i < lim; i++) {
            int nibble = 0;
            if (bs.get(pos++))
                nibble |= 8;
            if (bs.get(pos++))
                nibble |= 4;
            if (bs.get(pos++))
                nibble |= 2;
            if (bs.get(pos++))
                nibble |= 1;
            sb.append(new String(HEX, nibble, 1));
        }

        //Fields
        for (int i = 2; i < 129; i++) {
            IsoValue<?> v = fields[i];
            if (v != null) {
                String desc = v.toString();
                if (v.getType() == IsoType.LLBIN || v.getType() == IsoType.LLBCD || v.getType() == IsoType.LLVAR) {
                    sb.append(String.format("%02d", desc.length()));
                } else if (v.getType() == IsoType.LLLBIN || v.getType() == IsoType.LLLBCD || v.getType() == IsoType.LLLVAR) {
                    sb.append(String.format("%03d", desc.length()));
                } else if (v.getType() == IsoType.LLLLBIN || v.getType() == IsoType.LLLLBCD || v.getType() == IsoType.LLLLVAR) {
                    sb.append(String.format("%04d", desc.length()));
                }
                sb.append(desc);
            }
        }
        return sb.toString();
    }

    //These are for Groovy compat

    /**
     * Sets the specified value in the specified field, just like {@link #setField(int, IsoValue)}.
     */
    public <T> void putAt(int i, IsoValue<T> v) {
        setField(i, v);
    }

    /**
     * Returns the IsoValue in the specified field, just like {@link #getField(int)}.
     */
    public <T> IsoValue<T> getAt(int i) {
        return getField(i);
    }

    //These are for Scala compat

    /**
     * Sets the specified value in the specified field, just like {@link #setField(int, IsoValue)}.
     */
    public <T> void update(int i, IsoValue<T> v) {
        setField(i, v);
    }

    /**
     * Returns the IsoValue in the specified field, just like {@link #getField(int)}.
     */
    public <T> IsoValue<T> apply(int i) {
        return getField(i);
    }

    /**
     * Copies the specified fields from the other message into the recipient. If a specified field is
     * not present in the source message it is simply ignored.
     */
    public void copyFieldsFrom(IsoMessage src, int... idx) {
        for (int i : idx) {
            IsoValue<Object> v = src.getField(i);
            if (v != null) {
                setValue(i, v.getValue(), v.getType(), v.getLength());
            }
        }
    }

    public void copyAllFieldsFrom(IsoMessage src) {
        for (int i = 0; i < 128; i++) {
            IsoValue<Object> v = src.getField(i);
            if (v != null) {
                setValue(i, v.getValue(), v.getType(), v.getLength());
            }
        }
    }

    /**
     * Remove the specified fields from the message.
     */
    public void removeFields(int... idx) {
        for (int i : idx) {
            setField(i, null);
        }
    }

    /**
     * Returns true is the message contains all the specified fields.
     * A convenience for m.hasField(x) &amp;&amp; m.hasField(y) &amp;&amp; m.hasField(z) &amp;&amp; ...
     */
    public boolean hasEveryField(int... idx) {
        for (int i : idx) {
            if (!hasField(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true is the message contains at least one of the specified fields.
     * A convenience for m.hasField(x) || m.hasField(y) || m.hasField(z) || ...
     */
    public boolean hasAnyField(int... idx) {
        for (int i : idx) {
            if (hasField(i)) {
                return true;
            }
        }
        return false;
    }

    public void printMsg() {
        StringBuilder stringBuilder = new StringBuilder("\n-----------------------------------------------------").append('\n');
        stringBuilder.append("Message TypeID = [").append(Integer.toHexString(getType())).append("]").append('\n');
        for (int i = 2; i <= 128; i++) {
            if (hasField(i)) {
                String strType = getField(i).getType().toString();
                stringBuilder.append("FieldID: ").append(String.format("%03d", i)).append(" <");
                stringBuilder.append(String.format("%03d", getField(i).getLength()));
                stringBuilder.append(">\t<");
                stringBuilder.append(strType);
                String formatStr = "%-" + (8 + 1 - strType.length()) + "s";
                stringBuilder.append(String.format(formatStr, ">"));//">"
                stringBuilder.append("\t[");
                stringBuilder.append(getObjectValue(i).toString())
                        .append("]\t[")
                        .append(getField(i).toString())
                        .append("]").append('\n');
            }
        }
        logger.debug(stringBuilder.toString());
    }
}
