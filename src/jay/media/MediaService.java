package jay.media;

import java.net.DatagramSocket;
import java.net.SocketException;

import jay.codec.EchoCancellation;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class MediaService extends Service{

	
	protected LanAudioPlay m_iPlay;
	protected LanAudioRecord m_iRecord;
	static public EchoCancellation m_ec;
	protected DatagramSocket udp_socket;
	protected int mAudioRtpPort = 5071;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class JayBinder extends Binder{
		MediaService getService(){
			return MediaService.this;
		}
	}

	private final IBinder mBinder = new JayBinder();
	
	public void startAudio(String Destaddr,int codecType ,int SampleRate,int RtpPort)
	{
		try {
			if(udp_socket==null)
				udp_socket = new DatagramSocket(5071);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if(m_ec==null){
			m_ec = new EchoCancellation();
			m_ec.startThread();
		}
		else{
			m_ec.stopThread();
			m_ec.startThread();
		}
		if(m_iRecord==null){
			m_iRecord = new LanAudioRecord(udp_socket,Destaddr,codecType,RtpPort,SampleRate);
			m_iRecord.startThread();
		}
		else{
			m_iRecord.stopThread();
			m_iRecord.setDestIP(Destaddr);
			m_iRecord.setDestPort(RtpPort);
			m_iRecord.startThread();
		}
		if(m_iPlay==null){
			m_iPlay = new LanAudioPlay(udp_socket,codecType,SampleRate);
			m_iPlay.startThread();
		}
		else{
			m_iPlay.stopThread();
			m_iPlay.startThread();
		}

	}
	
	public void stopAudio()
	{
		if(m_iRecord!=null)
			m_iRecord.stopThread();
		if(m_iPlay!=null)
			m_iPlay.stopThread();
		if(m_ec!=null)
			m_ec.stopThread();
	}
	
	public void startVideo()
	{
		
	}
	
	public void stopVideo()
	{
		
	}
	
	public void test(){
		Log.d("Media service", "this is only a test");
	}
	
}