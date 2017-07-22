/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hti.rmi.LookupObject;

/**
 *
 * @author Administrator
 */
public class BulkProcessThread implements Runnable {
	private Logger logger = Logger.getLogger(BulkProcessThread.class);
	private boolean stop;
	private List list;
	private String systemid;
	private LookupObject lookup;

	public BulkProcessThread(LookupObject lookup) {
		this.lookup = lookup;
		this.systemid = lookup.getSystemid();
		this.list = lookup.getList();
		logger.info(systemid + " BulkProcessThread Started.");
	}

	public void run() {
		while (!stop) {
			if (list.size() > 1000) {
				long split_size = list.size() / 5;
				int thread_count = 0;
				logger.info("Split Size:----> " + split_size);
				while (!list.isEmpty()) {
					List split_list = new ArrayList();
					LookupObject split_object = new LookupObject();
					int count = 0;
					while (!list.isEmpty()) {
						String destination = (String) list.remove(0);
						split_list.add(destination);
						if (++count >= split_size) {
							if (list.size() >= split_size) {
								break;
							}
						}
					}
					split_object.setSystemid(systemid);
					split_object.setPassword(lookup.getPassword());
					split_object.setList(split_list);
					split_object.setBatchid(lookup.getBatchid());
					try {
						new Thread(new LookupProcessing(split_object, ++thread_count)).start();
					} catch (Exception ex) {
						logger.error("Unable to Start Thread For Lookup Processing: " + ex);
					}
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException ex) {
					}
				}
				if (list.isEmpty()) {
					break;
				}
			} else {
				try {
					new Thread(new LookupProcessing(lookup, 1)).start();
				} catch (Exception ex) {
					logger.error("Unable to Start Thread For Lookup Processing: " + ex);
				}
				break;
			}
		}
		logger.info(systemid + " BulkProcessThread Stopping.");
	}
}
