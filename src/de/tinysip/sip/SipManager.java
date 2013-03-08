/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian R�sch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sip;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import jay.sipdemo.JaySipActivity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter.LengthFilter;

/**
 * SipManager handles the session establishment, teardown and authorization. Notifies state changes by raising SipManagerStatusChangedEvent, SipManagerCallStatusEvent,
 * SipManagerSessionEvent.
 * 
 * @author Sebastian
 * 
 */
public class SipManager {
	private static String TAG = "tSIP";
	private static SipManager sipManager = null;

	private Context context;
	private PendingIntent pendingIntent;
	private SipService mSipService;

	private int registerTryCount = 0;

	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        mSipService = ((SipService.JayBinder)service).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        mSipService = null;
	    }
	};
	
	void doBindService(Intent intent) {
	    context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        context.unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	/**
	 * Create a SipManager by specifying the LocalSipProfile and the STUN DiscoveryInfo for NAT traversal.
	 * 
	 * @param localSipProfile
	 *            the local user's SIP profile to register with the provider
	 * @param discoveryInfo
	 *            the STUN DiscoveryInfo for NAT traversal
	 */
	private SipManager(Context context,PendingIntent pt,LocalSipProfile localSipProfile, String localIPAddress, int localSipPort) {
		try {
			this.context = context;
			pendingIntent = pt;
			initService();
//			sipMessageHandler = SipMessageHandler.createInstance(localSipProfile, localIPAddress, localSipPort);
//			sipMessageHandler.getSipProvider().addSipListener(this);
			
		} catch (Exception e) {
		}
	}
	
	public void initService(){
		Bundle bundle = new Bundle();            
		bundle.putParcelable("pendingintent", pendingIntent);
		//we want to start our service (for handling our time-consuming operation)
		Intent serviceIntent = new Intent(context, SipService.class);
		serviceIntent.putExtras(bundle);
		//used to bind sipservice ,it could be destroyed when app was destroyed
		doBindService(serviceIntent);
	}

	public void test(LocalSipProfile localSipProfile, String localIPAddress, int localSipPort) throws PeerUnavailableException, TransportNotSupportedException, ObjectInUseException, InvalidArgumentException, TooManyListenersException{
		if(mIsBound){
			mSipService.MessageHandler("hello jay!",null);
			mSipService.createInstance(localSipProfile, localIPAddress, localSipPort);
		}
	}
	/**
	 * Create a singleton instance of SipManager by specifying the LocalSipProfile and the STUN DiscoveryInfo for NAT traversal.
	 * 
	 * @param localSipProfile
	 *            the local user's SIP profile to register with the provider
	 * @param discoveryInfo
	 *            the STUN DiscoveryInfo for NAT traversal
	 * @return the created instance of SipManager
	 */
	public static SipManager createInstance(Context context,PendingIntent pt,LocalSipProfile localSipProfile, String localIPAddress, int localSipPort) {
		if (sipManager == null)
			sipManager = new SipManager(context,pt ,localSipProfile, localIPAddress, localSipPort);

		return sipManager;
	}

	/**
	 * Get a singleton instance of the SipManager. Use createInstance() first to create SipManager.
	 * 
	 * @return an instance of SipManager or null if none was created
	 */
	public static SipManager getInstance() {
		return sipManager;
	}

	/**
	 * Register the local sip profile with the sip provider.
	 * 
	 * @throws InvalidArgumentException
	 * @throws TooManyListenersException
	 * @throws ParseException
	 * @throws SipException
	 */
	public void registerProfile() throws InvalidArgumentException, TooManyListenersException, ParseException, SipException {
//		if (sipMessageHandler.getLocalSipProfile().isLocalProfile())
//			setStatusChanged(SipManagerState.READY);
//		else {
//			setStatusChanged(SipManagerState.REGISTERING);
			mSipService.register();
//			sipMessageHandler.register(SipRequestState.REGISTER);
//		}
	}

	/**
	 * Unregister the local sip profile from the sip provider.
	 * 
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws SipException
	 */
	public void unregisterProfile() throws ParseException, InvalidArgumentException, SipException {
//		if (sipMessageHandler.getLocalSipProfile().isLocalProfile())
//			setStatusChanged(SipManagerState.IDLE);
//		else {
//			setStatusChanged(SipManagerState.UNREGISTERING);
			mSipService.unregister();
//			sipMessageHandler.register(SipRequestState.UNREGISTER);
//		}
	}

	/**
	 * Start calling the specified SipContact.
	 * 
	 * @param contact
	 *            the SIP contact to call
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 * @throws SipException
	 * @throws NullPointerException
	 * @throws SdpException
	 */
	public void sendInvite(SipContact contact) throws ParseException, InvalidArgumentException, SipException, NullPointerException, SdpException {
//		currentContact = contact; 
//		currentSession = null;
//		setStatusChanged(SipManagerState.CALLING);
//		sipMessageHandler.sendInvite(currentContact, SipRequestState.REGISTER);
		mSipService.sendInvite(contact);
	}

	

	/**
	 * Accept the incoming call.
	 * 
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 * @throws SdpException
	 */
	public void acceptCall() throws SipException, InvalidArgumentException, ParseException, SdpException {
//		if (currentRequest != null)
			mSipService.acceptCall();
	}

	/**
	 * Decline the incoming call and reset the SipManager.
	 * 
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 * @throws SdpException
	 */
	public void declineCall() throws SipException, InvalidArgumentException, ParseException, SdpException {
//		if (currentRequest != null)
//			sipMessageHandler.sendDecline(currentRequest);
			mSipService.declineCall();

		reset();
	}

	/**
	 * Confirm the BUSY, DECLINED and INVALID state and reset the SipManager.
	 */
	public void confirmStateAndReset() {
		reset();
	}

	/**
	 * End the current call and reset the SipManager.
	 * 
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 * @throws SdpException
	 */
	public void endCall() throws SipException, InvalidArgumentException, ParseException, SdpException {
//		if (currentState.equals(SipManagerState.ESTABLISHED))
//			sipMessageHandler.sendBye();
//			mSipService.endCall();
//		else
//			sipMessageHandler.sendCancel();
			mSipService.endCall();

		reset();
	}
	
	/** 
	 * @return the LocalSipProfile
	 */
	public LocalSipProfile getLocalSipProfile(){
		return mSipService.getLocalSipProfile();
	}

	/**
	 * Reset all connections.
	 */
	private void reset() {
////		currentContact = null;
////		currentRequest = null;
////		currentSession = null;
		registerTryCount = 0;
////		sipMessageHandler.reset();
		mSipService.reset();
//
//		if (currentState.equals(SipManagerState.ESTABLISHED))
//			setSessionChanged(null);
//
//		setStatusChanged(SipManagerState.READY);
	}

	/**
	 * Close the SipManager and reset all connections.
	 */
	public void close() {
		reset();
	}

	/**
	 * Returns the first Internet-facing InetAddress, or Localhost, if none was found
	 * 
	 * @return InetAddress the InetAddress of the interface
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static InetAddress getInetAddress(){
		try{
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
			while (iaddresses.hasMoreElements()) {
				InetAddress iaddress = iaddresses.nextElement();
				if (InetAddress.class.isInstance(iaddress)) {
					if ((!iaddress.isLoopbackAddress()) && (!iaddress.isLinkLocalAddress())) {
						return iaddress;
					}
				}
			}
		}
		}catch (Exception e) {
		}

		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
		return null;
	}

}
