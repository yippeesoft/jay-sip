/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;



import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sip.PeerUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;

/**
 * Represents the remote contact's information, which is needed to start or receive a call.
 * 
 * @author Sebastian
 * 
 */
public class SipContact {
	private String sipUserName;
	private String sipDomain;
	private int sipPort = 5060;
	private boolean isLocalContact = false;
	private boolean isSipURI = true;

	/**
	 * Create a SipContact by specifying the user name and the contact's sip domain.
	 * 
	 * @param sipUserName the SIP username of the contact
	 * @param sipDomain the SIP domain of the contact
	 */
	public SipContact(String sipUserName, String sipDomain) {
		this.sipUserName = sipUserName;
		this.sipDomain = sipDomain;
	}

	/**
	 * Create a SipContact that is a local contact by specifying the user name, contact's IP address and sip port.
	 * 
	 * @param sipUserName the SIP username of the contact
	 * @param ipAddress the IP address of the contact
	 * @param sipPort the SIP port of the contact
	 */
	public SipContact(String sipUserName, String ipAddress, int sipPort) {
		this.sipUserName = sipUserName;
		this.sipDomain = ipAddress;
		this.sipPort = sipPort;
		isLocalContact = true;
	}

	/**
	 * Create a SipContact by specifying the user name and the contact's sip domain. Specify if this is a sip uri or a phone number.
	 * 
	 * @param sipUserName the SIP username of the contact
	 * @param sipDomain the SIP domain of the contact
	 * @param isSipURI
	 *            true if this is a sip uri or false if this is a phone number
	 */
	public SipContact(String sipUserName, String sipDomain, boolean isSipURI) {
		this(sipUserName, sipDomain);
		this.isSipURI = isSipURI;
	}

	/**
	 * @return the SipContact's user name
	 */
	public String getSipUserName() {
		return sipUserName;
	}

	/**
	 * @return the SipContact's sip domain
	 */
	public String getSipDomain() {
		return sipDomain;
	}

	/**
	 * Create a javax.sip.header.ToHeader for this SipContact
	 * 
	 * @param addressFactory an instance of an AddressFactory, created by the SipManager
	 * @param headerFactory an instance of a HeaderFactory, created by the SipManager
	 * @return the ToHeader created for this SipContact
	 * @throws PeerUnavailableException
	 * @throws ParseException
	 */
	public ToHeader getToHeader(AddressFactory addressFactory, HeaderFactory headerFactory) throws PeerUnavailableException, ParseException {
		SipURI fromAddress = addressFactory.createSipURI(this.sipUserName, this.sipDomain);
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(this.sipUserName);
		return headerFactory.createToHeader(fromNameAddress, null);
	}

	/**
	 * Create a SipContact by parsing the given String. Tests weather the contact is a sip contact or a phone number. Phone numbers as sip contacts have the same format as sip
	 * contacts (sip:bob@company.com), but use the number as user name and the local profile domain as the sip domain.
	 * 
	 * @param contact
	 *            a sip contact string to be parsed as a SipContact (for example: "sip:bob@company.com", where bob is the user name and company.com the sip domain)
	 * @param localProfileDomain
	 *            the sip domain of the local profile, used for non-sip numbers
	 * @return the parsed SipContact
	 */
	public static SipContact parse(String contact, String localProfileDomain) {
		contact = contact.replace(" ", "");
		String numberRegex = "(\\+|0)?\\d([/ -]?\\d)+";
		Pattern numberPattern = Pattern.compile(numberRegex, Pattern.CASE_INSENSITIVE);
		Matcher numberMatcher = numberPattern.matcher(contact);

		// contact is a phone number
		if (numberMatcher.matches()) {
			return new SipContact(contact, localProfileDomain, false);
		}
		// contact is a sip contact
		else if (contact.contains("@")) {
			String user = contact.split("@")[0];
			String domain = contact.split("@")[1];

			return new SipContact(user, domain, true);
		}

		return null;
	}
	
	/**
	 * @return the SIP port for a local user
	 */
	public int getSipPort() {
		return sipPort;
	}
	
	/**
	 * @return if this contact is a local contact
	 */
	public boolean isLocalContact(){
		return isLocalContact;
	}

	@Override
	public String toString() {
		if (isSipURI) {
			return "sip:" + this.sipUserName + "@" + this.sipDomain;
		} else {
			return this.sipUserName;
		}
	}
}
