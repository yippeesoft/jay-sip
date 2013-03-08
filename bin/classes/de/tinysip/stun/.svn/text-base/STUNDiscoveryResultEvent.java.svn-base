/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.stun;

import java.util.EventObject;

import de.javawi.jstun.test.DiscoveryInfo;

/**
 * STUNDiscoveryTask raises this event if new information about the NAT is available.
 * 
 * @author Sebastian
 * 
 */
public class STUNDiscoveryResultEvent extends EventObject {
	private static final long serialVersionUID = -6650144679437807701L;
	private DiscoveryInfo discoveryInfo;
	private STUNInfo stunInfo;

	/**
	 * Create a new STUNDiscoveryResult event to signal new information about the NAT.
	 * 
	 * @param source
	 *            the sender of the event (the STUNDiscoveryTask)
	 * @param discoveryInfo
	 *            the DiscoveryInfo containing the information about the NAT
	 * @param stunInfo
	 *            the STUNInfo containing the information about the test
	 */
	public STUNDiscoveryResultEvent(Object source, DiscoveryInfo discoveryInfo, STUNInfo stunInfo) {
		super(source);
		this.discoveryInfo = discoveryInfo;
		this.stunInfo = stunInfo;
	}

	/**
	 * @return the DiscoveryInfo containing the information about the NAT
	 */
	public DiscoveryInfo getDiscoveryInfo() {
		return discoveryInfo;
	}

	/**
	 * @return the STUNInfo containing the information about the test
	 */
	public STUNInfo getStunInfo() {
		return stunInfo;
	}

}
