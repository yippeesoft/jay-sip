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
 * SipManager raises this event when the call status has changed.
 * 
 * @author Sebastian
 * 
 */
public class SipManagerCallStatusEvent extends EventObject {
	private static final long serialVersionUID = -1892714337210427054L;
	private String message;

	/**
	 * Create a new event to signal a call status change in the SipManager.
	 * 
	 * @param source the sender of the event (the SipManager)
	 * @param message the message to send
	 */
	public SipManagerCallStatusEvent(Object source, String message) {
		super(source);
		this.message = message;
	}

	/**
	 * @return the message of the event
	 */
	public String getMessage() {
		return message;
	}

}
