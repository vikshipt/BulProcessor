/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hti.proc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 *
 * @author Administrator
 */
public class Converter {

    synchronized public static String getUTF8toHexDig(String str) {

        String dump = "";
        boolean status = true;
        int fina = 0;
        int big = 128;
        int first = 0;
        for (int i = 0; i < str.length();) {
            first = str.charAt(i);
            char ch = str.charAt(i);
            if (first > 128) {
                int count = 0;
                int noofmove = 0;
                int value = 128;
                i = i + 1;
                fina = first;
                value = big;
                while (fina >= value) {
                    count += 1;
                    fina = fina - value;
                    value = value / 2;
                    noofmove += 1;

                }
                if (fina == 0) {
                    first = 0;
                    value = big;
                    fina = 0;
                    count = 1;
                    first = str.charAt(i);
                    i = i + 1;
                    fina = first;
                    while (fina >= value) {
                        count += 1;
                        fina = fina - value;
                        value = value / 2;
                        noofmove += 1;

                    }
                }
                first = 0;
                if (count > 2) {
                    fina = fina << 12;
                } else {
                    fina = fina << 6;
                }
                while (count > 1) {
                    first = str.charAt(i);
                    first = first - big;
                    count = count - 1;
                    if (count > 1) {
                        first = first << 6;
                        fina += first;
                    } else {
                        fina += first;
                    }
                    i = i + 1;
                    first = 0;
                }
                String temp1 = "";
                String temp = Integer.toHexString(fina);
                for (int j = temp.length(); j < 4; j++) {
                    temp1 += "0";
                }
                dump += temp1 + temp;
            } /////////////////////////////////////////raj//////////////////
           else {
                String onechar = String.valueOf(ch);
                dump += getUnicodeTOHexaRaj(onechar);
                first = 0;
                i += 1;
            }

        }//for ends here
        return dump;
    }

      public static String getUnicodeTOHexaRaj(String unicode) {
        String hexa = "";
        try {
            Writer file_writer;
            FileOutputStream fos;
            fos = new FileOutputStream("Utf");
            file_writer = new OutputStreamWriter(fos, "UTF-8");
            file_writer.write(unicode);
            file_writer.close();
            InputStreamReader in = new InputStreamReader(new FileInputStream(new File("Utf")), "UTF-8");
            int a = 0;
            while ((a = in.read()) != -1) {
                if (a > 65000) {
                    continue;
                }
                String temp = Integer.toHexString(a);
                String put = "";
                for (int i = temp.length(); i < 4; i++) {
                    put += "0";
                }
                put += temp;
                hexa += put;
            }
        } catch (IOException e) {
        }
        return (hexa);
    }

      public static String getUTF8toHex(String str) {
        String dump = "";
        int fina = 0;
        int big = 128;
        int first = 0;
        for (int i = 0; i < str.length();) {
            first = str.charAt(i);
            if (first > 128) {
                int count = 0;
                int noofmove = 0;
                int value = 128;
                i = i + 1;
                fina = first;
                value = big;
                while (fina >= value) {
                    count += 1;
                    fina = fina - value;
                    value = value / 2;
                    noofmove += 1;

                }
                if (fina == 0) {
                    first = 0;
                    value = big;
                    fina = 0;
                    count = 1;
                    first = str.charAt(i);
                    i = i + 1;
                    fina = first;
                    while (fina >= value) {
                        count += 1;
                        fina = fina - value;
                        value = value / 2;
                        noofmove += 1;
                    }
                }
                first = 0;
                if (count > 2) {
                    fina = fina << 12;
                } else {
                    fina = fina << 6;
                }
                while (count > 1) {
                    first = str.charAt(i);
                    first = first - big;
                    count = count - 1;
                    if (count > 1) {
                        first = first << 6;
                        fina += first;
                    } else {
                        fina += first;
                    }
                    i = i + 1;
                    first = 0;
                }
                dump += (char) fina;
            } else {
                dump += (char) first;
                first = 0;
                i += 1;
            }
        }//for ends here
        return dump;
    }

         public static String getUnicode(char[] buffer) {
        String unicode = "";
        int code = 0;
        int j = 0;
        char[] unibuffer = new char[buffer.length / 4];
        try {
            for (int i = 0; i < buffer.length; i += 4) {
                code += Character.digit(buffer[i], 16) * 4096;
                code += Character.digit(buffer[i + 1], 16) * 256;
                code += Character.digit(buffer[i + 2], 16) * 16;
                code += Character.digit(buffer[i + 3], 16);
                unibuffer[j++] = (char) code;
                code = 0;
            }
            unicode = new String(unibuffer);

        } catch (Exception e) {
            System.out.println("Excepiton in getUnicode in the converter222 " + e);
        }
        return unicode;

    }
}
