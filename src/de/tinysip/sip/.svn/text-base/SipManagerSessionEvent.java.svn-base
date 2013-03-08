/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;

import java.util.EventObject;

/**
 * SipManager raises this event when the sip session has changed.
 * 
 * @author Sebastian
 * 
 */
public class SipManagerSessionEvent extends EventObject {
	private static final long serialVersionUID = 1992448898141945529L;
	private SipSession session;

	/**
	 * Create a SipManagerSessionEvent.
	 * 
	 * @param source the sender of the event (the SipManager)
	 * @param session the current session information
	 */
	public SipManagerSessionEvent(Object source, SipSession session) {
		super(source);
		this.session = session;
	}

	/**
	 * @return the SipSession of this event
	 */
	public SipSession getSession() {
		return session;
	}

}
