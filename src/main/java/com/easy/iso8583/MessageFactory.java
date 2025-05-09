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


import com.easy.iso8583.parse.ConfigParser;
import com.easy.iso8583.parse.DateTimeParseInfo;
import com.easy.iso8583.parse.FieldParseInfo;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

import static com.easy.iso8583.IsoType.LLLBIN;
import static com.easy.iso8583.IsoType.LLLVAR;

@SuppressWarnings("unchecked")
public class MessageFactory<T extends IsoMessage> {

    Logger log = Logger.getLogger(MessageFactory.class);

    /**
     * Stores the information needed to parse messages sorted by type.
     */
    protected Map<Integer, Map<Integer, FieldParseInfo>> parseMap = new HashMap<>();
    /**
     * Stores the field numbers to be parsed, in order of appearance.
     */
    protected Map<Integer, List<Integer>> parseOrder = new HashMap<>();


    /**
     * Indicates that the fields should be written/parsed as binary
     */
    private boolean binaryFields;


    /**
     * Flag to specify if missing fields should be ignored as long as they're at
     * the end of the message.
     */
    private boolean forceb2;
    private boolean forceStringEncoding;
    /* Flag specifying that variable length fields have the length header encoded in hexadecimal format */
    private boolean variableLengthFieldsInHex;
    private String encoding = Charset.forName("ISO8859-1").displayName();

    /**
     * This flag gets passed on to newly created messages and also sets this value for all
     * field parsers in parsing guides.
     */
    public void setForceStringEncoding(boolean flag) {
        forceStringEncoding = flag;
        for (Map<Integer, FieldParseInfo> pm : parseMap.values()) {
            for (FieldParseInfo parser : pm.values()) {
                parser.setForceStringDecoding(flag);
            }
        }
    }


    /**
     * This flag gets passed on to newly created messages and also sets this value for all
     * field parsers in parsing guides.
     */
    public void setVariableLengthFieldsInHex(boolean flag) {
        this.variableLengthFieldsInHex = flag;
        for (Map<Integer, FieldParseInfo> pm : parseMap.values()) {
            for (FieldParseInfo parser : pm.values()) {
                parser.setForceHexadecimalLength(flag);
            }
        }
    }

    public boolean isVariableLengthFieldsInHex() {
        return variableLengthFieldsInHex;
    }


    /**
     * Sets the character encoding used for parsing ALPHA, LLVAR and LLLVAR fields.
     */
    public void setCharacterEncoding(String value) {
        if (encoding == null) {
            throw new IllegalArgumentException("Cannot set null encoding.");
        }
        encoding = value;
        if (!parseMap.isEmpty()) {
            for (Map<Integer, FieldParseInfo> pt : parseMap.values()) {
                for (FieldParseInfo fpi : pt.values()) {
                    fpi.setCharacterEncoding(encoding);
                }
            }
        }

    }

    /**
     * Returns the encoding used to parse ALPHA, LLVAR and LLLVAR fields. The default is the
     * file.encoding system property.
     */
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * Sets or clears the flag to pass to new messages, to include a secondary bitmap
     * even if it's not needed.
     */
    public void setForceSecondaryBitmap(boolean flag) {
        forceb2 = flag;
    }

    public boolean isForceSecondaryBitmap() {
        return forceb2;
    }


    /**
     * Tells the receiver to read the configuration at the specified path. This just calls
     * ConfigParser.configureFromClasspathConfig() with itself and the specified path at arguments,
     * but is really convenient in case the MessageFactory is being configured from within, say, Spring.
     */
    public void setConfigPath(String path) throws IOException {
        ConfigParser.configureFromClasspathConfig(this, path);
        //Now re-set some properties that need to be propagated down to the recently assigned objects
        setCharacterEncoding(encoding);
        setForceStringEncoding(forceStringEncoding);
    }


    /**
     * fields portion of the message is written/parsed in binary, default is false
     */
    public void setBinaryFields(boolean flag) {
        binaryFields = flag;
    }

    /**
     * fields portion of the message is written/parsed in binary, default is false
     */
    public boolean isBinaryFields() {
        return binaryFields;
    }


    /**
     * Creates a new message of the specified type, with optional trace and date values as well
     * as any other values specified in a message template. If the factory is set to use binary
     * messages, then the returned message will be written using binary coding.
     *
     * @param type The message type, for example 0x200, 0x400, etc.
     */
    public T newMessage(int type) {
        T m = createIsoMessage();
        m.setType(type);
        m.setBinaryFields(isBinaryFields());
        m.setForceSecondaryBitmap(forceb2);
        m.setCharacterEncoding(encoding);
        m.setEncodeVariableLengthFieldsInHex(variableLengthFieldsInHex);
        return m;
    }

    /**
     * Creates a response message by calling {@link #createResponse(IsoMessage, boolean)}
     * with true as the second parameter.
     */
    public T createResponse(T request) {
        return createResponse(request, true);
    }

    /**
     * Creates a message to respond to a request. Increments the message type by 16,
     * sets all fields from the template if there is one,
     * and either copies all values from the request or only the ones already in the template,
     * depending on the value of copyAllFields flag.
     *
     * @param request       An ISO8583 message with a request type (ending in 00).
     * @param copyAllFields If true, copies all fields from the request to the response,
     *                      overwriting any values already set from the template; otherwise
     *                      it only overwrites values for existing fields from the template.
     *                      If the template for a response does not exist, then all fields from
     *                      the request are copied even in this flag is false.
     */
    public T createResponse(T request, boolean copyAllFields) {
        T resp = createIsoMessage();
        resp.setCharacterEncoding(request.getCharacterEncoding());
        resp.setBinaryFields(request.isBinaryFields());
        resp.setType(request.getType() + 16);
        resp.setForceSecondaryBitmap(forceb2);
        resp.setEncodeVariableLengthFieldsInHex(request.isEncodeVariableLengthFieldsInHex());
        //Copy the values from the template or the request (request has preference)

        for (int i = 2; i < 128; i++) {
            if (request.hasField(i)) {
                resp.setField(i, request.getField(i).clone());
            }
        }
        if (copyAllFields) {
            for (int i = 2; i < 128; i++) {
                if (request.hasField(i)) {
                    resp.setField(i, request.getField(i).clone());
                }
            }
        }
        return resp;
    }

    /**
     * Sets the timezone for the specified FieldParseInfo, if it's needed for parsing dates.
     */
    public void setTimezoneForParseGuide(int messageType, int field, TimeZone tz) {
        if (field == 0) {
            DateTimeParseInfo.setDefaultTimeZone(tz);
        }
        Map<Integer, FieldParseInfo> guide = parseMap.get(messageType);
        if (guide != null) {
            FieldParseInfo fpi = guide.get(field);
            if (fpi instanceof DateTimeParseInfo) {
                ((DateTimeParseInfo) fpi).setTimeZone(tz);
            }
        }
    }


    /**
     * Creates a new message instance from the buffer, which must contain a valid ISO8583
     * message. If the factory is set to use binary messages then it will try to parse
     * a binary message.
     *
     * @param buf The byte buffer containing the message. Must not include the length header.
     */
    public T parseMessage(byte[] buf, int begin)
            throws ParseException, UnsupportedEncodingException {
        final int minlength = (binaryFields ? 2 : 4) + 8;
        if (buf.length < minlength) {
            throw new ParseException("Insufficient buffer length, needs to be at least " + minlength, 0);
        }
        final T m = createIsoMessage();
        m.setCharacterEncoding(encoding);
        final int type;
        if (binaryFields) {
            type = ((buf[begin] & 0xff) << 8) | (buf[begin + 1] & 0xff);
        } else {
            type = Integer.parseInt(new String(buf, begin, 4, encoding), 16);
        }
        m.setType(type);
        //Parse the bitmap (primary first)
        final BitSet bitMap = new BitSet(128);
        int pos = parseBitMap(buf, begin, minlength, bitMap);
        log.warn(String.format("parseMap {%s} parseOrder {%s} ", parseMap, parseOrder));
           
        //Parse each field
        Map<Integer, FieldParseInfo> parseGuide = parseMap.get(type);
        List<Integer> index = parseOrder.get(type);
        if (index == null) {
            throw new ParseException(String.format(
                    "ISO8583 MessageFactory has no parsing guide for message type %04x [%s]",
                    type,
                    new String(buf)), 0);
        }
        //First we check if the message contains fields not specified in the parsing template
        boolean abandon = false;
        for (int i = 1; i < bitMap.length(); i++) {
            if (bitMap.get(i) && !index.contains(i + 1)) {
                log.warn(String.format("ISO8583 MessageFactory cannot parse field {%d}: unspecified in parsing guide for type {%s}",
                        i + 1, Integer.toString(type, 16)));
                abandon = true;
            }
        }
        if (abandon) {
            throw new ParseException("ISO8583 MessageFactory cannot parse fields", 0);
        }
        //Now we parse each field
        if (binaryFields) {
            parseBinaryMsg(buf, m, bitMap, pos, parseGuide, index);
        } else {
            innerParseMsg(buf, m, bitMap, pos, parseGuide, index);
        }

        m.setBinaryFields(binaryFields);
        m.setEncodeVariableLengthFieldsInHex(variableLengthFieldsInHex);
        return m;
    }

    private void innerParseMsg(byte[] buf, T m, BitSet bs, int pos, Map<Integer,
            FieldParseInfo> parseGuide, List<Integer> index) throws ParseException, UnsupportedEncodingException {
        for (Integer i : index) {
            FieldParseInfo fpi = parseGuide.get(i);
            if (bs.get(i - 1)) {
                if (pos >= buf.length && i.intValue() == index.get(index.size() - 1)) {
                    log.warn(String.format("Field {%d} is not really in the message even though it's in the bitmap", i));
                    bs.clear(i - 1);
                } else {
                    IsoValue<?> val = fpi.parse(i, buf, pos);
                    m.setField(i, val);
                    if (val != null) {
                        pos += val.getLength();
                        if (val.getType() == IsoType.LLVAR || val.getType() == IsoType.LLBIN || val.getType() == IsoType.LLBCD) {
                            pos += 2;
                        } else if (val.getType() == LLLVAR || val.getType() == LLLBIN || val.getType() == IsoType.LLLBCD) {
                            pos += 3;
                        } else if (val.getType() == IsoType.LLLLVAR || val.getType() == IsoType.LLLLBIN || val.getType() == IsoType.LLLLBCD) {
                            pos += 4;
                        }
                    }

                    if (log.isTraceEnabled() && val != null) {
                        testLog(val, i);
                    }
                }
            }
        }
    }

    private void testLog(IsoValue<?> value, int num) {
        StringBuilder stringBuilder = new StringBuilder();
        String strType = value.getType().toString();
        stringBuilder.append("FieldID: ").append(String.format("%03d", num)).append(" <");
        stringBuilder.append(String.format("%03d", value.getLength()));
        stringBuilder.append(">\t<");
        stringBuilder.append(strType);
        String formatStr = "%-" + (8 + 1 - strType.length()) + "s";
        stringBuilder.append(String.format(formatStr, ">"));//">"
        stringBuilder.append("\t[");
        stringBuilder.append(value.getValue().toString())
                .append("]\t[")
                .append(value.toString())
                .append("]").append('\n');
        log.debug(stringBuilder);
    }

    private boolean halfLen(IsoValue<?> val) {// 
        return val.getType() == IsoType.NUMERIC
                || val.getType() == IsoType.DATE10
                || val.getType() == IsoType.DATE4
                || val.getType() == IsoType.DATE12
                || val.getType() == IsoType.DATE14
                || val.getType() == IsoType.DATE6
                || val.getType() == IsoType.DATE_EXP
                || val.getType() == IsoType.AMOUNT
                || val.getType() == IsoType.TIME;

    }

    private void parseBinaryMsg(byte[] buf, T m, BitSet bs, int pos, Map<Integer, FieldParseInfo> parseGuide, List<Integer> index) throws ParseException, UnsupportedEncodingException {
        for (Integer i : index) {
            FieldParseInfo fpi = parseGuide.get(i);
            if (bs.get(i - 1)) {
                if (pos >= buf.length && i.intValue() == index.get(index.size() - 1)) {
                    log.warn(String.format("Field {%d} is not really in the message even though it's in the bitmap", i));
                    bs.clear(i - 1);
                } else {
                    IsoValue<?> val = fpi.parseBinary(i, buf, pos);
                    m.setField(i, val);
                    if (val != null) {
                        if (halfLen(val)) {
                            pos += (val.getLength() / 2) + (val.getLength() % 2);
                        } else if (val.getType() == IsoType.LLBCD || val.getType() == IsoType.LLLBCD || val.getType() == IsoType.LLLLBCD) {
                            pos += val.getLength() / 2 + (val.getLength() % 2);
                        } else {
                            pos += val.getLength();
                        }
                        if (val.getType() == IsoType.LLVAR || val.getType() == IsoType.LLBIN || val.getType() == IsoType.LLBCD) {
                            pos++;
                        } else if (val.getType() == LLLVAR
                                || val.getType() == LLLBIN
                                || val.getType() == IsoType.LLLBCD
                                || val.getType() == IsoType.LLLLVAR
                                || val.getType() == IsoType.LLLLBIN
                                || val.getType() == IsoType.LLLLBCD
                        ) {
                            pos += 2;
                        }
                    }
                }
            }
        }
    }

    private int parseBitMap(byte[] buf, int begin, int minlength, BitSet bs) throws ParseException {
        int pos = 0;
        final int bitmapStart = begin + (binaryFields ? 2 : 4);
        for (int i = bitmapStart; i < 8 + bitmapStart; i++) {
            int bit = 128;
            for (int b = 0; b < 8; b++) {
                bs.set(pos++, (buf[i] & bit) != 0);
                bit >>= 1;
            }
        }
        //Check for secondary bitmap and parse if necessary
        if (bs.get(0)) {
            if (buf.length < minlength + 8) {
                throw new ParseException("Insufficient length for secondary bitmap", minlength);
            }
            for (int i = 8 + bitmapStart; i < 16 + bitmapStart; i++) {
                int bit = 128;
                for (int b = 0; b < 8; b++) {
                    bs.set(pos++, (buf[i] & bit) != 0);
                    bit >>= 1;
                }
            }
            pos = minlength + 8;
        } else {
            pos = minlength;
        }
        return pos + begin;
    }


    protected T createIsoMessage() {
        return (T) new IsoMessage();
    }

    /**
     * Creates a Iso message with the specified binary ISO header.
     * Override this method in the subclass to provide your
     * own implementations of IsoMessage.
     *
     * @param binHeader The optional ISO header that goes before the message type
     * @return IsoMessage
     */
    @SuppressWarnings("unchecked")
    protected T createIsoMessageWithBinaryHeader(byte[] binHeader) {
        return (T) new IsoMessage();
    }


    /**
     * Invoke this method in case you want to freeze the configuration, making message and parsing
     * templates, as well as iso headers and custom fields, immutable.
     */
    public void freeze() {
        parseMap = Collections.unmodifiableMap(parseMap);
        parseOrder = Collections.unmodifiableMap(parseOrder);
    }

    /**
     * Sets a map with the fields that are to be expected when parsing a certain type of
     * message.
     *
     * @param type The message type.
     * @param map  A map of FieldParseInfo instances, each of which define what type and length
     *             of field to expect. The keys will be the field numbers.
     */
    public void setParseMap(int type, Map<Integer, FieldParseInfo> map) {
        parseMap.put(type, map);
        ArrayList<Integer> index = new ArrayList<>(map.keySet());
        Collections.sort(index);
        log.warn(String.format(" type {%d} index {%s} ",   type, index));
        parseOrder.put(type, index);
    }


}
