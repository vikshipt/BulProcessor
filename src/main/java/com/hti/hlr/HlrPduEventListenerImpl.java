/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import com.hti.util.FileUtil;
import com.hti.util.IConstants;
import com.logica.smpp.Data;
import com.logica.smpp.ServerPDUEvent;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.pdu.PDU;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class HlrPduEventListenerImpl implements ServerPDUEventListener {

    private PDU pdu = null;
    private Logger logger = Logger.getLogger(HlrPduEventListenerImpl.class);
    private String systemid;

    public HlrPduEventListenerImpl(String systemid) {
        this.systemid = systemid;
    }

    @Override
    public void handleEvent(ServerPDUEvent spdue) {
        pdu = spdue.getPDU();
        if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
            FileUtil.writeLog(IConstants.HLR_RESP_LOG_FILE, systemid + " : " + pdu.debugString());
        } else {
            logger.info(systemid + " (HLR): " + pdu.debugString());
        }
    }

}
