package jay.media;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;

import jay.dencode.Encoder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class LanAudioRecord extends Thread{
	
	protected AudioRecord m_in_rec;	
	protected DatagramSocket udp_socket;
	protected RtpSocket rtp_socket;
	private Encoder encoder ;
	private volatile Thread runner;
	
	protected boolean muteflag;
	protected int destport;	
	protected int mSampleRate=8000;	
	protected int mFrameSize = 160;
	protected int mFrameRate = 50;
	protected int codectype=1;
	protected int mFramePeriod = 20;
	//for Ring Buffer
	private final int Rtphead=12;
	private int readpos =0;
	private int writepos =0;
	protected String destip;
	
	//adjust time
	long last_tx_time = 0;
	long next_tx_delay;
	long now;
	int sync_adj = 2;
	
	public static int m;

	public LanAudioRecord(DatagramSocket socket ,String destip,int codectype,int destport,int SampleRate){
		this.destip = destip;
		this.destport = destport;
		this.codectype=codectype;
		this.mSampleRate = SampleRate;
		this.mFrameRate = SampleRate/mFrameSize;
		this.mFramePeriod = 1000/mFrameRate;
		this.udp_socket = socket;
		//*************record_init********************
		int m_in_buf_size = AudioRecord.getMinBufferSize(SampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO, 
						AudioFormat.ENCODING_PCM_16BIT);
		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC,
								  SampleRate, 
								  AudioFormat.CHANNEL_CONFIGURATION_MONO,
								  AudioFormat.ENCODING_PCM_16BIT,
								  m_in_buf_size*10);
		
		System.out.println("m_in_bytes="+m_in_buf_size);

		muteflag = false;
	}
	
	public void setCodec(int type){
		this.codectype = type;
	}
	
	public void setDestIP(String destip){
		this.destip = destip;
	}
	
	public void setDestPort(int port){
		this.destport = port;
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
		int seqn = 0;		
		long time = 0;
		short[] Audio_in = new short[mFrameSize*(mFrameRate+1)];  // this is a ring buffer
		byte[] buffer = new byte[mFrameSize+Rtphead];
		RtpPacket rtp_packet = new RtpPacket(buffer, 0);
		rtp_packet.setPayloadType(codectype);
		try {
			rtp_socket = new RtpSocket(this.udp_socket, InetAddress
					.getByName(this.destip), this.destport);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		encoder = new Encoder(this.codectype);
		encoder.startThread();
		m_in_rec.startRecording();
		while(Thread.currentThread() == runner){
			adjustTransferTime();
			if( m_in_rec.read(Audio_in, writepos, mFrameSize)<=0)
				continue;
			//Decline Volume to half
			calc2(Audio_in,writepos,mFrameSize);
			writepos = (writepos +mFrameSize)%(mFrameSize*(mFrameRate+1));
			if(encoder.isIdle()){
				encoder.putData(System.currentTimeMillis(),Audio_in,readpos, mFrameSize);
				readpos= (readpos +mFrameSize)%(mFrameSize*(mFrameRate+1));
			}
			if(muteflag == false){
				if(encoder.isGetData())
				{
					try {
						byte[] temp = encoder.getData();
						System.arraycopy(temp, Rtphead, buffer, Rtphead, temp.length-Rtphead);
						rtp_packet.setPayloadLength(temp.length-Rtphead);
						rtp_packet.setSequenceNumber(seqn++);
						rtp_packet.setTimestamp(time);
						rtp_socket.send(rtp_packet);
						if (codectype == 9)
			 				 time += mFrameSize/2;
			 			 else
			 				 time += mFrameSize;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		free();
			
	}
	
	public void mute(){
		muteflag = true;
	}
	
	public void demute(){
		muteflag = false;
	}
	
	//decline the volume
	void calc2(short[] lin,int off,int len) {
		int i,j;
		
		for (i = 0; i < len; i++) {
			j = lin[i+off];
			if (j > 16350)
				lin[i+off] = 16350<<1;
			else if (j < -16350)
				lin[i+off] = -16350<<1;
			else
				lin[i+off] = (short)(j<<1);
		}
	}
	
	void adjustTransferTime(){
		if(mFrameSize<480)
		{
			now = System.currentTimeMillis();
			next_tx_delay = mFramePeriod - (now - last_tx_time);
			last_tx_time = now;
			if(next_tx_delay>0)
			{
				try {
				    sleep(next_tx_delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				 last_tx_time += next_tx_delay-sync_adj;
			}
		}
	}
	
	private void free(){
		m_in_rec.stop();
		encoder.stopThread();
	}	
}