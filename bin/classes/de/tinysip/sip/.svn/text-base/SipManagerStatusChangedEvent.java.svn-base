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
 * SipManager raises this event when the SipManager's status has changed.
 * 
 * @author Sebastian
 * 
 */
public class SipManagerStatusChangedEvent extends EventObject {
	private static final long serialVersionUID = -3146962567369231401L;
	private SipManagerState state;
	private String info;

	/**
	 * Create a new event to signal a status change in the SipManager.
	 * 
	 * @param source the sender of the event (the SipManager)
	 * @param state the new state if the SipManager
	 */
	public SipManagerStatusChangedEvent(Object source, SipManagerState state) {
		super(source);
		this.state = state;
		this.info = "";
	}

	/**
	 * Create a new event to signal a status change in the SipManager.
	 * 
	 * @param source the sender of the event (the SipManager)
	 * @param state the new state if the SipManager
	 * @param additional information about the current state
	 */
	public SipManagerStatusChangedEvent(Object source, SipManagerState state, String info) {
		this(source, state);
		this.info = info;
	}

	/**
	 * @return the SipManagerState
	 */
	public SipManagerState getState() {
		return state;
	}
	
	/**
	 * contains more information about the current state.
	 * 
	 * Cases:
	 * INCOMING and ESTABLISHED: contains the sip uri of the caller
	 * default: null
	 * 
	 * @return additional information about the state
	 */
	public String getInfo() {
		return info;
	}

}
