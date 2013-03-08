/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian R�sch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package jay.sipdemo;

import gov.nist.javax.sip.SIPConstants;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import javawi.jstun.test.DiscoveryInfo;

import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipException;
import javax.sip.TransportNotSupportedException;

import jay.media.MediaManager;

import de.tinysip.sip.LocalSipProfile;
import de.tinysip.sip.SipAudioFormat;
import de.tinysip.sip.SipContact;
import de.tinysip.sip.SipManager;
import de.tinysip.sip.SipManagerCallStatusEvent;
import de.tinysip.sip.SipManagerState;
import de.tinysip.sip.SipSession;
import de.tinysip.sip.SipVideoFormat;
import de.tinysip.sip.Siphandler;
import de.tinysip.stun.STUNDiscoveryResultEvent;
import de.tinysip.stun.STUNDiscoveryResultListener;
import de.tinysip.stun.STUNDiscoveryTask;
import de.tinysip.stun.STUNInfo;
import de.tinysip.sipdemo.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class JaySipActivity extends Activity implements STUNDiscoveryResultListener {
	@Override
	protected void onPause() {
		unregisterReceiver(siphandler);
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}

	private static String TAG = "tSIP";
	private TextView sipStatusText;

	private LocalSipProfile localSipProfile;
	private SipContact sipContact;
	private STUNDiscoveryTask sipPortTask;
	private DiscoveryInfo sipDiscoveryInfo;
	private SipManager sipManager;
	public MediaManager mediaManager;
	
    private Siphandler siphandler;
    private PendingIntent pendingIntent;
    private final String IntentAction = "android.jsipdemo.detiny";
    public Handler mHandler;

	private SipManagerState state;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sipStatusText = (TextView) findViewById(R.id.sipstatustext);
		
		Intent intent = new Intent(); 
        intent.setAction(IntentAction); 
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);

		// the local user's credentials (change details in SettingsProvider)
		// localSipProfile = new LocalSipProfile(SettingsProvider.sipUserName, SettingsProvider.sipDomain, SettingsProvider.sipPassword, SettingsProvider.sipPort,
		// SettingsProvider.displayName);
		// the local user's credentials (change details in SettingsProvider)

        RegBroadcastRecv();
        
		InetAddress address = SipManager.getInetAddress();
//		localSipProfile = new LocalSipProfile("Sebastian", address);
		
		localSipProfile = new LocalSipProfile("100", "192.168.1.6", null, 5060, "100");

		// create a list of supported audio formats for the local user agent
		ArrayList<SipAudioFormat> audioFormats = new ArrayList<SipAudioFormat>();
		audioFormats.add(new SipAudioFormat(SdpConstants.PCMU, "PCMU", 8000));
		audioFormats.add(new SipAudioFormat(SdpConstants.PCMA, "PCMA", 8000));
		audioFormats.add(new SipAudioFormat(SdpConstants.G722,"G722",8000));
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
		sipContact = new SipContact("200", "192.168.1.6", 5060);

		if (!localSipProfile.isLocalProfile()) {
			// the STUN server and port for NAT traversal
			STUNInfo sipPortInfo = new STUNInfo(STUNInfo.TYPE_SIP, "10.3.5.5", 5060);
			sipPortInfo.setLocalPort(5070); // local port to use for SIP
			sipPortTask = new STUNDiscoveryTask();
			sipPortTask.addResultListener(this);
			sipPortTask.execute(sipPortInfo);
			sipStatusText.append("\n" + getString(R.string.STUNDiscovery));
		} else {
			startSipRegistration();
		}
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				String state = data.getString("bread");
				String uiDisplay;
				if(state.equals(SipManagerState.INCOMING+""))
					uiDisplay = state+"from:"+data.getString("info");
				else
					uiDisplay = state;
				sipStatusText.append(uiDisplay+'\n');
				
				super.handleMessage(msg);
			}
		};
		
		mediaManager= new MediaManager(getApplicationContext());
		mediaManager.initService();
	}


	
	
	public void onclick(View v){
		switch(v.getId()){
		case R.id.accept:
			try {
//				Thread.sleep(3000);
				sipManager.acceptCall();
				//Thread.sleep(6000);
				//sipManager.endCall();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case R.id.decline:
			try {
				sipManager.declineCall();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case R.id.call:
			try {
				sipManager.sendInvite(sipContact);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case R.id.endcall:
			try {
				sipManager.endCall();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case R.id.test:
			try {
				sipManager.test(localSipProfile,getlocalip(), localSipProfile.getLocalSipPort());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			break;
		case R.id.register:
			try {
				sipManager.registerProfile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			break;
			
		case R.id.MeidaService:
			mediaManager.startAudio();
			break;
		case R.id.stopMedia:
			mediaManager.stopAudio();
			break;

		}
		
	}
	
	
	private void RegBroadcastRecv(){
    	IntentFilter filter = new IntentFilter();
        filter.addAction(IntentAction);
        siphandler = new Siphandler();
        this.registerReceiver(siphandler, filter);
    }
    
    private void UnRegBroadcastRecv(){
    	this.unregisterReceiver(siphandler);
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
			sipManager = SipManager.createInstance(getApplicationContext(),pendingIntent,localSipProfile,getlocalip(), localSipProfile.getLocalSipPort());
		else
			sipManager = SipManager.createInstance(getApplicationContext(),pendingIntent,localSipProfile, sipDiscoveryInfo.getPublicIP().getHostAddress(), sipDiscoveryInfo.getPublicPort());
		try {
			sipManager.registerProfile();
		} catch (Exception e) {
			e.printStackTrace();
			sipStatusText.append("\n" + getString(R.string.SIPRegistrationError));
		}
	}
	private String getlocalip(){
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);  
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
		int ipAddress = wifiInfo.getIpAddress(); 
		if(ipAddress==0)return null;
		return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
				+(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
	}
	
	public void processMessage(String msg){
		final String fmsg = msg;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
					sipStatusText.append("\n" + fmsg);
			}
		});
	}
	
	 @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	            new AlertDialog.Builder(this)
	                    // .setIcon(R.drawable.services)
	                    .setTitle("exit")
	                    .setMessage("press ok to exit")
	                    .setNegativeButton("cancel",
	                            new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										
									}
//	                                @Override
//	                                public void onClick(DialogInterface dialog,
//	                                        int which) {
//	                                }
	                            })
	                    .setPositiveButton("ok",
	                            new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog,
	                                        int whichButton) {
	                                    finish();
	                                }
	                            }).show();
	            return true;
	        } else {
	            return super.onKeyDown(keyCode, event);
	        }
	    }
}