package jay.media;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import jay.dencode.Decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public  class LanAudioPlay extends Thread{
	protected AudioTrack m_out_trk;
	private volatile Thread runner;
	private Decoder decoder;
	protected DatagramSocket udp_socket;
	protected RtpPacket rtp_packet;
	protected RtpSocket rtp_socket;
	protected int SampleRate=16000;
	protected int listenport;
	protected final int mFrameSize = 320;
	protected final int Rtphead = 12;
	protected final int GO_TIMEOUT=1000;
	protected int codectype=1;
	protected byte[] m_out_bytes;
	protected int m,vm=1;
	
	//speech preprocessor
	protected int gseq=0,currentseq=0,getseq,expseq,gap;
	public static float good, late, lost, loss, loss2;
	
	//used for echo calc
	
    public LanAudioPlay(DatagramSocket socket,int codectype,int SampleRate){
    	try{
    		this.SampleRate = SampleRate;
    		this.codectype = codectype;
    		int m_out_buf_size = AudioTrack.getMinBufferSize(SampleRate,
    									AudioFormat.CHANNEL_CONFIGURATION_MONO,
    									AudioFormat.ENCODING_PCM_16BIT);
    		m_out_trk = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
    									SampleRate,
    									AudioFormat.CHANNEL_CONFIGURATION_MONO,
    									AudioFormat.ENCODING_PCM_16BIT,
    									m_out_buf_size*10,
    									AudioTrack.MODE_STREAM);
    		udp_socket = socket;
    		
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    public void startThread(){
		if(runner == null){
		    runner = new Thread(this);
		    runner.start();
		  }
	}
	
	public void stopThread(){
		 if(runner != null){
			    Thread moribund = runner;
			    runner = null;
			    moribund.interrupt();
		  }
	}

	public void run(){
		byte[] buffer = new byte[mFrameSize+Rtphead];
		rtp_packet = new RtpPacket(buffer, 0);
		rtp_packet.setPayloadType(codectype);
		try {
			rtp_socket = new RtpSocket(this.udp_socket);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	decoder = new Decoder(codectype);
    	decoder.startThread();
    	try {
    		rtp_socket.receive(rtp_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	m_out_trk.play();
    	System.gc();
    	empty();

    	while(Thread.currentThread() == runner){
			try {
				rtp_socket.receive(rtp_packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		 gseq = rtp_packet.getSequenceNumber();
			 if (currentseq == gseq) {
				 m++;
				 continue;
			 }
			 lostandgood();
			 Log.d("LanAudioPlay","lost:"+ lost);
			if(decoder.isIdle()){
				decoder.putData(System.currentTimeMillis(),buffer,Rtphead,rtp_packet.getPayloadLength());
			}

			if(decoder.isGetData()==true){
				short[] s_bytes_pkg = decoder.getData().clone();
			m_out_trk.write(s_bytes_pkg, 0, s_bytes_pkg.length);
			}
    	}
    }

	void empty() {
		try {
			rtp_socket.getDatagramSocket().setSoTimeout(1);
			for (;;)
				rtp_socket.receive(rtp_packet);
		} catch (IOException e) {
		}
		try {
			rtp_socket.getDatagramSocket().setSoTimeout(GO_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		currentseq = 0;
	}
	
	
	void lostandgood(){
		if (currentseq != 0) {
			 getseq = gseq&0xff;
			 expseq = ++currentseq&0xff;
			 if (m == LanAudioRecord.m) vm = m;
			 gap = (getseq - expseq) & 0xff;
			 if (gap > 0) {
				 if (gap > 100) gap = 1;
				 loss += gap;
				 lost += gap;
				 good += gap - 1;
				 loss2++;
			 } else {
				 if (m < vm) {
					 loss++;
					 loss2++;
				 }
			 }
			 good++;
			 if (good > 110) {
				 good *= 0.99;
				 lost *= 0.99;
				 loss *= 0.99;
				 loss2 *= 0.99;
				 late *= 0.99;
			 }
		 }
		 m = 1;
		 currentseq = gseq;
	}
    public void free(){
    	m_out_trk.stop();
    	decoder.stopThread();
	}	
	
    
    
}