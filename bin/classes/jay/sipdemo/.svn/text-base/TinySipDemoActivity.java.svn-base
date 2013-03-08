/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sipdemo;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;

import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;

import de.javawi.jstun.test.DiscoveryInfo;
import de.tinysip.sip.LocalSipProfile;
import de.tinysip.sip.SipAudioFormat;
import de.tinysip.sip.SipContact;
import de.tinysip.sip.SipManager;
import de.tinysip.sip.SipManagerCallStatusEvent;
import de.tinysip.sip.SipManagerSessionEvent;
import de.tinysip.sip.SipManagerState;
import de.tinysip.sip.SipManagerStatusChangedEvent;
import de.tinysip.sip.SipManagerStatusListener;
import de.tinysip.sip.SipSession;
import de.tinysip.sip.SipVideoFormat;
import de.tinysip.stun.STUNDiscoveryResultEvent;
import de.tinysip.stun.STUNDiscoveryResultListener;
import de.tinysip.stun.STUNDiscoveryTask;
import de.tinysip.stun.STUNInfo;
import de.tinysip.sipdemo.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TinySipDemoActivity extends Activity implements STUNDiscoveryResultListener, SipManagerStatusListener {
	private static String TAG = "tSIP";
	private TextView sipStatusText;

	private LocalSipProfile localSipProfile;
	private SipContact sipContact;
	private STUNDiscoveryTask sipPortTask;
	private DiscoveryInfo sipDiscoveryInfo;
	private SipManager sipManager;

	private SipManagerState state;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sipStatusText = (TextView) findViewById(R.id.sipstatustext);

		// the local user's credentials (change details in SettingsProvider)
		// localSipProfile = new LocalSipProfile(SettingsProvider.sipUserName, SettingsProvider.sipDomain, SettingsProvider.sipPassword, SettingsProvider.sipPort,
		// SettingsProvider.displayName);
		// the local user's credentials (change details in SettingsProvider)

		InetAddress address = SipManager.getInetAddress();
		localSipProfile = new LocalSipProfile("Sebastian", address);

		// create a list of supported audio formats for the local user agent
		ArrayList<SipAudioFormat> audioFormats = new ArrayList<SipAudioFormat>();
		audioFormats.add(new SipAudioFormat(SdpConstants.PCMA, "PCMA", 8000));
		localSipProfile.setAudioFormats(audioFormats);

		// set ports for rtp and rtcp for audio
		localSipProfile.setLocalAudioRtpPort(5071);
		localSipProfile.setLocalAudioRtcpPort(5072);

		// create a list of supported video formats for the local user agent
		ArrayList<SipVideoFormat> videoFormats = new ArrayList<SipVideoFormat>();
		videoFormats.add(new SipVideoFormat(SdpConstants.JPEG, "JPEG", 90000));
		localSipProfile.setVideoFormats(videoFormats);

		// set ports for rtp and rtcp for video
		localSipProfile.setLocalVideoRtpPort(5073);
		localSipProfile.setLocalVideoRtcpPort(5074);

		// the sip contact to call (change details in SettingsProvider)
		// sipContact = new SipContact(SettingsProvider.callContact, SettingsProvider.callDomain, true);
		sipContact = new SipContact("Sebastian", "192.168.178.20", 5060);

		if (!localSipProfile.isLocalProfile()) {
			// the STUN server and port for NAT traversal
			STUNInfo sipPortInfo = new STUNInfo(STUNInfo.TYPE_SIP, "stun.sipgate.net", 10000);
			sipPortInfo.setLocalPort(5070); // local port to use for SIP
			sipPortTask = new STUNDiscoveryTask();
			sipPortTask.addResultListener(this);
			sipPortTask.execute(sipPortInfo);
			sipStatusText.append("\n" + getString(R.string.STUNDiscovery));
		} else {
			startSipRegistration();
		}
	}

	@Override
	public void SipManagerStatusChanged(SipManagerStatusChangedEvent event) {
		final SipManagerStatusChangedEvent fevent = event;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				state = fevent.getState();

				switch (state) {
				case IDLE:
					break;
				case RINGING:
					break;
				case ESTABLISHED:
					break;
				case INCOMING:
					try {
						Thread.sleep(3000);
						sipManager.acceptCall();
						//Thread.sleep(6000);
						//sipManager.endCall();
					} catch (SipException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvalidArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SdpException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case ERROR:
					break;
				case UNREGISTERING:
					sipStatusText.append("\n" + getString(R.string.SIPUnregistering));
					break;
				case TIMEOUT:
					break;
				case READY:
					localSipProfile = sipManager.getLocalSipProfile();
					sipStatusText.append("\n" + getString(R.string.SIPReady));
					sipStatusText.append("\n" + localSipProfile.toString());
					if ((localSipProfile.isLocalProfile() && sipContact.isLocalContact()) || !(localSipProfile.isLocalProfile() && sipContact.isLocalContact())) {
						sipStatusText.append("\n" + getString(R.string.SIPInvite) + " " + sipContact.toString());
						try {
							sipManager.sendInvite(sipContact);
						} catch (Exception e) {
							sipStatusText.append("\n" + getString(R.string.SIPRegistering));
						}
					}
					break;
				case REGISTERING:
					sipStatusText.append("\n" + getString(R.string.SIPRegistering));
					break;
				default:
					break;
				}
			}
		});
	}

	@Override
	public void SipManagerCallStatusChanged(SipManagerCallStatusEvent event) {
		final SipManagerCallStatusEvent fevent = event;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				sipStatusText.append("\n" + fevent.getMessage());
			}
		});
	}

	@Override
	public void SipManagerSessionChanged(SipManagerSessionEvent event) {
		final SipManagerSessionEvent fevent = event;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SipSession session = fevent.getSession();

				if (session != null) {
					sipStatusText.append("\n" + getString(R.string.SIPCallConnected) + " " + session.getCallerNumber());
				}
			}
		});
	}

	@Override
	public void STUNDiscoveryResultChanged(STUNDiscoveryResultEvent event) {
		if (event.getDiscoveryInfo() != null) {
			Log.d(TAG, event.getDiscoveryInfo().toString());

			DiscoveryInfo discoveryInfo = event.getDiscoveryInfo();
			STUNInfo stunInfo = event.getStunInfo();

			switch (stunInfo.getType()) {
			case STUNInfo.TYPE_SIP:
				// STUN test was completed
				sipDiscoveryInfo = discoveryInfo;
				if (sipDiscoveryInfo.isBlockedUDP() || sipDiscoveryInfo.isSymmetric() || sipDiscoveryInfo.isSymmetricUDPFirewall()) {
					sipStatusText.append("\n" + getString(R.string.STUNNotSupported));
					Log.d(TAG, getString(R.string.STUNNotSupported));
				} else {
					// start SIP registration now
					startSipRegistration();
				}
				break;
			}
		} else {
			sipStatusText.append("\n" + getString(R.string.STUNError));
		}
	}

	private void startSipRegistration() {
		if (localSipProfile.isLocalProfile())
			sipManager = SipManager.createInstance(localSipProfile, localSipProfile.getSipDomain(), localSipProfile.getLocalSipPort());
		else
			sipManager = SipManager.createInstance(localSipProfile, sipDiscoveryInfo.getPublicIP().getHostAddress(), sipDiscoveryInfo.getPublicPort());
		sipManager.addStatusListener(this);

		try {
			sipManager.registerProfile();
		} catch (Exception e) {
			e.printStackTrace();
			sipStatusText.append("\n" + getString(R.string.SIPRegistrationError));
		}
	}
}