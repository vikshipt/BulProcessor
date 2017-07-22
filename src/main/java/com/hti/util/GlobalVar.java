/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.logica.smpp.util.Queue;

/**
 *
 * @author Administrator
 */
public class GlobalVar {
	public static Map processmap = new ConcurrentHashMap();
	public static Map pausedBatch = new ConcurrentHashMap();
	public static Map suspendBatch = new ConcurrentHashMap();
	public static Map UserSession = new ConcurrentHashMap();
	// public static Map UserTimeout = new ConcurrentHashMap();
	// public static Map UserForceDelay = new ConcurrentHashMap();
	public static Map UserSequence = new ConcurrentHashMap(); // to contain User wise Sequence Map
	// public static Map HLRUserSession = new ConcurrentHashMap();
	public static Queue SubmitRespQueue = new Queue();
}
