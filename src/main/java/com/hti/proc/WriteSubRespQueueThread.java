/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.proc;

import com.hti.util.FileUtil;
import com.hti.util.GlobalVar;
import com.hti.util.IConstants;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class WriteSubRespQueueThread extends com.logica.smpp.util.ProcessingThread {

    private Logger logger = Logger.getLogger(WriteSubRespQueueThread.class);

    public WriteSubRespQueueThread() {
        logger.info("WriteSubRespQueueThread Starting");
    }

    @Override
    public void process() {
        if (GlobalVar.SubmitRespQueue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        } else {
            String respContent = null;
            String content = "";
            int counter = 0;
            while (!GlobalVar.SubmitRespQueue.isEmpty()) {
                respContent = (String) GlobalVar.SubmitRespQueue.dequeue();
                if (respContent != null) {
                    content += new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + " : " + respContent + "\n";
                }
                if (++counter >= 1000) {
                    break;
                }
            }
            if (content.length() > 0) {
                try {
                    FileUtil.writeContent(IConstants.RESP_LOG_FILE, content, true);
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stopProcessing(null);
        logger.info("WriteSubRespQueueThread Stopping ");
    }
}
