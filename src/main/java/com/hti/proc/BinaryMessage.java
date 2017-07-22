/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hti.proc;

import java.util.Vector;

public class BinaryMessage {

    public int total = 0;
    public Vector vt = null;
    public int random = 100;
    Object obj = null;
    String msg = "";
    int nofmsg = 0;
    String type = "";
    public String RingToneHeaderMulti = "";//Withiout Number Of Messages And This no of Message.
    public String RingToneHeaderSingle = "";
    public String PicHeaderMulti = "";//Withiout Number Of Messages And This no of Message.
    public String PicHeaderSingle = "";
    public String OperatorHeader = "";
    public String OperatorHeaderMulti = "";
    public String WapPushHeaderSingle = "";
    public String WapPushHeaderMulti = "";
    public String VCardHeaderMulti = "";//Withiout Number Of Messages And This no of Message.
    public String VCardHeaderSingle = "";

    public BinaryMessage(Object obj, String msg) {
        this.obj = obj;
        if (obj instanceof nokiaMmsConstant) {
            nokiaMmsConstant nokia = (nokiaMmsConstant) obj;
            RingToneHeaderMulti = nokia.NokiaRingToneHeaderMulti;
            RingToneHeaderSingle = nokia.NokiaRingToneHeaderSingle;
            PicHeaderMulti = nokia.NokiaPicHeaderMulti;
            PicHeaderSingle = nokia.NokiaPicHeaderMulti;
            OperatorHeader = nokia.NokiaOperatorHeader;
            OperatorHeaderMulti = nokia.NokiaOperatorHeaderMulti;

            WapPushHeaderSingle = nokia.NokiaWapPushHeaderSingle;
            WapPushHeaderMulti = nokia.NokiaWapPushHeaderMulti;
            VCardHeaderMulti = nokia.NokiaVCardHeaderMulti;
            VCardHeaderSingle = nokia.NokiaVCardHeaderSingle;
        }
        vt = new Vector();
        this.msg = msg;
    }

    public String getCharFromHex(char[] st) {
        String toReturn = "";
        int code = 0;
        for (int i = 0; i < st.length; i += 2) {
            code += Character.digit(st[i], 16) * 16;
            code += Character.digit(st[i + 1], 16);
            toReturn += (char) code;
            code = 0;
        }
        return toReturn;
    }

    public String getNokiaOperatorHeaderMulti(int current) {
        String ret = "";
        if (total > 1) {
            ret = OperatorHeaderMulti + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = OperatorHeader;
        }
        return ret;
    }

    public String getNokiaOperatorHeaderSingle() {
        String ret = OperatorHeader;//+Integer.toHexString(random)+"0"+total+"0"+current;
        return ret;
    }

    public String getNokiaWapPushHeaderMulti(int current) {
        String ret = "";
        if (total > 1) {
            ret = WapPushHeaderMulti + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = WapPushHeaderSingle;
        }
        return ret;
    }

    public String getNokiaWapPushHeaderSingle() {
        String ret = WapPushHeaderSingle;//+Integer.toHexString(random)+"0"+total+"0"+current;
        return ret;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    public String getUserHeaderMulti(int current, String header) {
        String ret = "";
        if (total > 1) {
            ret = header + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = header;
        }
        return ret;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    public String getRingToneHeaderMultiHex(int current) {
        String ret = "";
        if (total > 1) {
            ret = RingToneHeaderMulti + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = RingToneHeaderSingle;//+Integer.toHexString(random)+"0"+total+"0"+current;
        }
        return ret;
    }

    public String getRingToneHeaderSingleHex() {
        String ret = RingToneHeaderSingle;//+Integer.toHexString(random)+"0"+total+"0"+current;
        return ret;
    }

    public String getPicHeaderMultiHex(int current) {
        String ret = "";
        if (total > 1) {
            ret = PicHeaderMulti + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = PicHeaderSingle;
        }
        return ret;
    }

    public String getPicHeaderSingleHex() {
        String ret = PicHeaderSingle;//+Integer.toHexString(random)+"0"+total+"0"+current;
        return ret;
    }

    public String getVCardHeaderMultiHex(int current) {
        String ret = "";
        if (total > 1) {
            ret = VCardHeaderMulti + Integer.toHexString(random) + "0" + total + "0" + current;
            if (random > 200) {
                random = 100;
            }
        } else {
            ret = VCardHeaderSingle;
        }
        return ret;
    }

    public String getVCardHeaderSingleHex() {
        String ret = VCardHeaderSingle;//+Integer.toHexString(random)+"0"+total+"0"+current;
        return ret;
    }

    public int getNumberOfMessage() {
        String str = msg;
        int ret = 1;
        if (str.length() > 266) {
            while (str.length() > 256) {
                ret += 1;
                str = str.substring(256, str.length());
            }
        }
        total = ret;
        return ret;
    }

    public Vector getMessageParts() {
        String str = msg;
        Vector vt = new Vector();
        if (str.length() > 266) // it was 266
        {
            int nofmsg = getNumberOfMessage();
            int i = 0;
            while (str.length() > 256) {
                vt.add(i, str.substring(0, 256));
                str = str.substring(256, str.length());
                //System.out.println("msg "+i+" ==== "+str);
                i++;
            }
            vt.add(i, str);
        } else {
            vt.add(0, str);
        }
        return vt;
    }

    public String getOperatorLogoString(String MCC, String MNC) {
        return getBigIndianFormat(MCC, MNC);
    }

    public String getBigIndianFormat(String temp, String mnc) {
        StringBuffer f = new StringBuffer(temp);
        String first = f.substring(0, 2);
        StringBuffer ff = new StringBuffer(first);
        ff = ff.reverse();
        first = ff.toString();
        String last = "F" + f.substring(2, f.length());
        StringBuffer mn = new StringBuffer(mnc);
        mn = mn.reverse();
        first = first + last + mn.toString();
        System.out.println(first);
        return first;
    }
}
