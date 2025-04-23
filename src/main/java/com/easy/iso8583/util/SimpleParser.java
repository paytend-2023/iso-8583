
package com.easy.iso8583.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

/** A simple command-line program that reads a configuration file to set up a MessageFactory
 * and parse messages read from STDIN.
 *
 * @author Enrique Zamudio
 *         Date: 20/06/12 02:11
 */
public class SimpleParser {

    private static BufferedReader reader;

    private static String getMessage() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        System.out.println("Paste your ISO8583 message here (no ISO headers): ");
        return reader.readLine();
    }

    public static void main(String [] args) throws IOException, ParseException {
//
//        final MessageFactory<IsoMessage> mf = new MessageFactory<IsoMessage>();
//        if (args.length == 0) {
//            ConfigParser.configureFromDefault(mf);
//        } else {
//            if (System.console() != null) {
//                System.console().printf("Attempting to configure MessageFactory from %s...%n", args[0]);
//            }
//            String url = args[0];
//            if (url.contains("://")) {
//                ConfigParser.configureFromUrl(mf, new URL(args[0]));
//            } else {
//                ConfigParser.configureFromUrl(mf, new File(url).toURI().toURL());
//            }
//        }
//        //Now read messages in a loop
//        String line = getMessage();
//        while (line != null && line.length() > 0) {
//            IsoMessage m = mf.parseMessage(line.getBytes(), 0);
//            if (m != null) {
//                System.out.printf("Message type: %04x%n", m.getType());
//                System.out.println("FIELD TYPE    VALUE");
//                for (int i = 2; i <= 128; i++) {
//                    IsoValue<?> f = m.getField(i);
//                    if (f != null) {
//                        System.out.printf("%5d %-7s (%4d) [", i, f.getType(), f.getLength());
//                        System.out.print(f.toString());
//                        System.out.println(']');
//                    }
//                }
//            }
//            line = getMessage();
//        }
    }
}
