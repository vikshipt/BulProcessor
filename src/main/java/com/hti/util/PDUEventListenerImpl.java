/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

import com.logica.smpp.Data;
import com.logica.smpp.ServerPDUEvent;
import com.logica.smpp.ServerPDUEventListener;
import com.logica.smpp.pdu.PDU;
import com.logica.smpp.pdu.SubmitSMResp;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class PDUEventListenerImpl implements ServerPDUEventListener {

    private PDU pdu = null;
    private Logger logger = Logger.getLogger(PDUEventListenerImpl.class);
    private Map map;
    private String systemid;

    public PDUEventListenerImpl() {
    }

    public PDUEventListenerImpl(Map map, String systemid) {
        this.map = map;
        this.systemid = systemid;
    }

    @Override
    public void handleEvent(ServerPDUEvent spdue) {
        pdu = spdue.getPDU();
        if (pdu.getCommandId() == Data.SUBMIT_SM_RESP) {
            int sequence = pdu.getSequenceNumber();
            int batch_id = -1;
            if (map.containsKey(sequence)) {
                batch_id = (Integer) map.remove(sequence);
            }
            if (((SubmitSMResp) pdu).getCommandStatus() == Data.ESME_ROK) {
                GlobalVar.SubmitRespQueue.enqueue(systemid+": "+batch_id + " : " + pdu.debugString());
            } else {
                logger.error(systemid + " SubmitResp(" + batch_id + "): " + pdu.debugString());
            }
        } else if (pdu.getCommandId() == Data.DELIVER_SM) {
            FileUtil.writeLog(IConstants.DLR_LOG_FILE, systemid + " : " + pdu.debugString());
        } else if (pdu.getCommandId() == Data.ENQUIRE_LINK_RESP) {
            logger.info(systemid + " : " + pdu.debugString());
        } else {
            logger.info(systemid+" Unknown PDU: " + pdu.debugString());
        }
    }

    public void stop() {
        logger.info("@@@ EventListner Stoping Internal Threads @@@");
        Thread.interrupted();
    }
}
