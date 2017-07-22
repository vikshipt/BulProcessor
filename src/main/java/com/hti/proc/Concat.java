/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.proc;

import com.logica.smpp.*;
import com.logica.smpp.Data;
import com.logica.smpp.pdu.*;
import com.logica.smpp.util.ByteBuffer;

public class Concat {

    public static SubmitSM getUnicodeCon(ByteBuffer byteMessage, String sender, String destination_no, int ston, int snpi) {
        SubmitSM msg = new SubmitSM();
        try {
            msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination_no);
            msg.setSourceAddr((byte) ston, (byte) snpi, sender);
            msg.setShortMessage(byteMessage);
            msg.setEsmClass((byte) 0x40);
            msg.setDataCoding((byte) 8);
        } catch (Exception e) {
        }
        return msg;
    }
    
}
