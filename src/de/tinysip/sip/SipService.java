package de.tinysip.sip;

import gov.nist.javax.sdp.MediaDescriptionImpl;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.sdp.Media;
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

import android.R.array;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class SipService extends Service implements SipListener{
	
	private String Tag = "SipService";
	private PendingIntent mPendingIntent;
	private SipMessageHandler sipMessageHandler;
	private SipManagerState currentState = null;
	private SipSession currentSession = null;
	private SipContact currentContact = null;
	private Request currentRequest = null;
	private SessionDescription currentsdp=null;
	private int registerTryCount=0;
    
	@Override
	public void onDestroy() {
		Log.d(Tag,"SipService is destroy");
		super.onDestroy();
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
		Log.d("sipservice", "create");	
		super.onCreate();
	}
	
	public class JayBinder extends Binder{
		SipService getService(){
			return SipService.this;
		}
	}

	private final IBinder mBinder = new JayBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(Tag, "bind");
		//get that PendingIntent come from Sipmanager
		Bundle bundle = intent.getExtras();
		this.mPendingIntent= bundle.getParcelable("pendingintent");
		return mBinder;
	}
	
	public void MessageHandler(String msg,String info)
	{
		try {            
	           //you can attach data from the operation in the intent.
	           Intent broadintent = new Intent();
	           Bundle b = new Bundle();
	           b.putString("bread", msg);
	           if(info != null)
	        	   b.putString("info",info);
	           broadintent.putExtras(b);
	           mPendingIntent.send(getApplicationContext(), 12341, broadintent);
	           
	        } catch (Exception e) {         
	        e.printStackTrace();
	        }  
	}
	
	public void createInstance(LocalSipProfile localSipProfile,String localIPAddress, int localSipPort) throws PeerUnavailableException, TransportNotSupportedException, ObjectInUseException, InvalidArgumentException, TooManyListenersException{
		sipMessageHandler = SipMessageHandler.createInstance(localSipProfile, localIPAddress, localSipPort);
		sipMessageHandler.getSipProvider().addSipListener(this);
		currentState = SipManagerState.IDLE;
	}
	public void setStatusChanged(SipManagerState state){
		currentState = state;
		MessageHandler(state+"",null);
	}
	public void setStatusChanged(SipManagerState state,String info){
		currentState = state;
		MessageHandler(state+"",info);
	}
	public void setCallStatus(String info){
		MessageHandler(SipManagerState.INCOMING+"",currentSession.getCallerNumber());
	}
	public void setSessionChanged(SipSession sipsession){
		this.currentSession = sipsession;
	}
	public void register() throws ParseException, InvalidArgumentException, SipException{
		setStatusChanged(SipManagerState.REGISTERING);
		sipMessageHandler.register(SipRequestState.REGISTER);
	}

	public void unregister() throws ParseException, InvalidArgumentException, SipException{
		setStatusChanged(SipManagerState.UNREGISTERING);
		sipMessageHandler.register(SipRequestState.UNREGISTER);
	}
	
	public void sendInvite(SipContact contact) throws NullPointerException, ParseException, InvalidArgumentException, SipException, SdpException{
		currentContact = contact; 
		currentSession = null;
		setStatusChanged(SipManagerState.CALLING);
		sipMessageHandler.sendInvite(contact, SipRequestState.REGISTER);
	}
	
	public void acceptCall() throws SipException, InvalidArgumentException, ParseException, SdpException{
		if (currentRequest != null){
			sipMessageHandler.sendOK(currentRequest, null);
		}
	}
	
	public void declineCall() throws ParseException, SipException, InvalidArgumentException{
		if (currentRequest != null)
			sipMessageHandler.sendDecline(currentRequest);
	}
	
	public void endCall() throws ParseException, InvalidArgumentException, SipException{
		if (currentState.equals(SipManagerState.ESTABLISHED))
			sipMessageHandler.sendBye();
		else sipMessageHandler.sendCancel();
	}
	
	public LocalSipProfile getLocalSipProfile(){
		return sipMessageHandler.getLocalSipProfile();
	}
	
	public void reset(){
		sipMessageHandler.reset();
		currentContact = null;
		currentRequest = null;
		currentSession = null;
		if (currentState.equals(SipManagerState.ESTABLISHED))
			setSessionChanged(null);
		setStatusChanged(SipManagerState.READY);
	}
	
	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {
	}

	@Override
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		String method = ((CSeqHeader) request.getHeader(CSeqHeader.NAME)).getMethod();
		System.out.println(Tag +": Incoming " + method + " request");

		if (method.equals(Request.INVITE)) {
			try {
				if (currentSession == null) 
				{
					SessionDescription sdpSession = SdpFactory.getInstance().createSessionDescription(new String(request.getRawContent()));
					currentsdp = sipMessageHandler.createSDP(request);
					//if there is no codec matched ,so sdp would return null
					if(currentsdp==null)
						sipMessageHandler.sendDecline(currentRequest);
					else{
						currentSession = SipMessageHandler.createSipSession(request, sdpSession);
						currentSession.setIncoming(true);
						//set the audio formats which has negotiated with caller
						currentSession.setAudioFormats(sipMessageHandler.getSipAudioFormats());
						String CallTO = currentSession.getToSipURI().toString();
						CallTO = CallTO.substring(CallTO.indexOf(':')+1, CallTO.indexOf('@'));
						if(CallTO.equals(sipMessageHandler.getLocalSipProfile().getUserName()))
						{
							currentRequest = request;
							sipMessageHandler.sendRinging(request);	
							
							setStatusChanged(SipManagerState.INCOMING,currentSession.getCallerNumber());
							setCallStatus("Incoming call from " + currentSession.getCallerNumber());
							System.out.println(Tag +": " + currentSession.toString());
						} else {
							sipMessageHandler.sendNotFound(request);
						}
					}
				} 
				else {
					sipMessageHandler.sendBusyHere(request);
				}
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}
		} else if (method.equals(Request.CANCEL)) {
			reset();
		} else if (method.equals(Request.ACK)) {
			if (currentSession != null) {
				sipMessageHandler.setDialog(requestEvent.getDialog());
				
				setStatusChanged(SipManagerState.ESTABLISHED, currentSession.getCallerNumber());
				setSessionChanged(currentSession);
			}
		} else if (method.equals(Request.BYE)) {
			try {
				sipMessageHandler.sendOK(request, requestEvent.getServerTransaction());
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}
			// no need to send 200 OK, SipStack should do that automatically
			setStatusChanged(SipManagerState.BYE);
			reset();
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		Response response = (Response) responseEvent.getResponse();
		int status = response.getStatusCode();
		System.out.println(Tag +": Response Status Code: " + status);

		switch (status) {
		case 180: // Ringing
			try {
				if (currentState.equals(SipManagerState.CALLING)) {
					setStatusChanged(SipManagerState.RINGING);
				}
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}
		case 200: // OK
			try {
				if ((currentState.equals(SipManagerState.RINGING) || currentState.equals(SipManagerState.CALLING)) && response.getRawContent() != null
						&& responseEvent.getDialog() != null) {
					SessionDescription sdpSession = SdpFactory.getInstance().createSessionDescription(new String(response.getRawContent()));
					currentSession = SipMessageHandler.createSipSession(response, sdpSession);
					Vector audioFormats = sipMessageHandler.getSipAudioFormats(sdpSession);
					audioFormats = sipMessageHandler.getSipAudioFormatsBump(audioFormats);
					ArrayList<SipAudioFormat> tempa = sipMessageHandler.getSipAudioFormatsList(audioFormats);
					currentSession.setAudioFormats(tempa);
					currentSession.setIncoming(false);
					sipMessageHandler.setDialog(responseEvent.getDialog());
					sipMessageHandler.sendAck();

					setStatusChanged(SipManagerState.ESTABLISHED);
					setSessionChanged(currentSession);
				} else if (currentState.equals(SipManagerState.REGISTERING)) {
					setStatusChanged(SipManagerState.READY);
					registerTryCount = 0;
				} else if (currentState.equals(SipManagerState.UNREGISTERING)) {
					setStatusChanged(SipManagerState.IDLE);
					registerTryCount = 0;
				}
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}

			break;

		case 401: // Unauthorized
			try {
				registerTryCount++;

				WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);
				sipMessageHandler.getLocalSipProfile().setNonce(authHeader.getNonce());
				sipMessageHandler.getLocalSipProfile().setRealm(authHeader.getRealm());

				if (currentState.equals(SipManagerState.REGISTERING))
					sipMessageHandler.register(SipRequestState.AUTHORIZATION);
				else if (currentState.equals(SipManagerState.UNREGISTERING))
					sipMessageHandler.register(SipRequestState.UNREGISTER_AUTHORIZATION);
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}

			break;

		case 404: // Not found
			setStatusChanged(SipManagerState.INVALID);
			break;

		case 407: // Proxy Authentication required
			try {
				registerTryCount++;
				System.out.println(Tag +": " + response.toString());

				if (currentSession == null && currentContact != null) {
					ProxyAuthenticateHeader authHeader = (ProxyAuthenticateHeader) response.getHeader(ProxyAuthenticateHeader.NAME);
					sipMessageHandler.getLocalSipProfile().setNonce(authHeader.getNonce());
					sipMessageHandler.getLocalSipProfile().setRealm(authHeader.getRealm());

					if (currentState.equals(SipManagerState.CALLING))
						sipMessageHandler.sendInvite(currentContact, SipRequestState.AUTHORIZATION);
				}
			} catch (Exception e) {
				setStatusChanged(SipManagerState.ERROR);
			}

			break;
		case 480:
			setStatusChanged(SipManagerState.DECLINED);
			break;
		case 486: // Busy
			setStatusChanged(SipManagerState.BUSY);
			break;

		case 603: // Decline
			setStatusChanged(SipManagerState.DECLINED);
			break;

		case 500: // Too many clients
			setStatusChanged(SipManagerState.ERROR);
			break;

		default:
			break;
		}

	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
	}
	
//	private void initSessionDescription(){
//		SessionDescriptor sdp = new SessionDescriptor(
//				sipMessageHandler.getLocalSipProfile().getSipUri().toString(),
//				sipMessageHandler.getSipProvider().getSipStack().getIPAddress());
//		
//		localSdp = sdp.toString();
//		
//		//We will have at least one media line, and it will be 
//		//audio
//		if (sipMessageHandler.getLocalSipProfile().audio || !sipMessageHandler.getLocalSipProfile().video)
//		{
////			addMediaDescriptor("audio", user_profile.audio_port, c, user_profile.audio_sample_rate);
//			addMediaDescriptor("audio", sipMessageHandler.getLocalSipProfile().getLocalAudioRtpPort(), null);
//		}
//		
//		if (user_profile.video)
//		{
//			addMediaDescriptor("video", user_profile.video_port,
//					user_profile.video_avp, "h263-1998", 90000);
//		}
//	}
//	
//	/** Adds a set of media to the SDP */
////	private void addMediaDescriptor(String media, int port, Codecs.Map c,int rate) {
//	private void addMediaDescriptor(String media, int port, Codecs.Map c) {
//		SessionDescriptor sdp = new SessionDescriptor(local_session);
//	
//		Vector<String> avpvec = new Vector<String>();
//		Vector<AttributeField> afvec = new Vector<AttributeField>();
//		if (c == null) {
//			// offer all known codecs
//			for (int i : Codecs.getCodecs()) {
//				Codec codec = Codecs.get(i);
//				if (i == 0) codec.init();
//				avpvec.add(String.valueOf(i));
//				if (codec.number() == 9)
//					afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", i, codec.userName(), 8000))); // kludge for G722. See RFC3551.
//				else
//					afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", i, codec.userName(), codec.samp_rate())));
//			}
//		} else {
//			c.codec.init();
//			avpvec.add(String.valueOf(c.number));
//			if (c.codec.number() == 9)
//				afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", c.number, c.codec.userName(), 8000))); // kludge for G722. See RFC3551.
//			else
//				afvec.add(new AttributeField("rtpmap", String.format("%d %s/%d", c.number, c.codec.userName(), c.codec.samp_rate())));
//		}
//		if (user_profile.dtmf_avp != 0){
//			avpvec.add(String.valueOf(user_profile.dtmf_avp));
//			afvec.add(new AttributeField("rtpmap", String.format("%d telephone-event/%d", user_profile.dtmf_avp, user_profile.audio_sample_rate)));
//			afvec.add(new AttributeField("fmtp", String.format("%d 0-15", user_profile.dtmf_avp)));
//		}
//				
//		//String attr_param = String.valueOf(avp);
//		
//		sdp.addMedia(new MediaField(media, port, 0, "RTP/AVP", avpvec), afvec);
//		
//		local_session = sdp.toString();
//	}
}