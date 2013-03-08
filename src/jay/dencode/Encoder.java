package jay.dencode;

import java.util.LinkedList;

import jay.codec.Codec;
import jay.codec.EchoCancellation;
import jay.media.MediaService;

public class Encoder implements Runnable {
	private EchoCancellation m_ec;
	private Codec codec ;
	private volatile Thread runner;
	private final Object mutex = new Object();
	protected LinkedList<byte[]> m_in_q=new LinkedList<byte[]>();    //store processed data
	private int frameSize = 160;
	private volatile int leftSize = 0;
	public static int num_send;
	private int dataLen=0;
	private int Rtp_head =12 ;
	private long ts;
	private byte[] processedData = new byte[frameSize+12];
	private short[] rawdata = new short[frameSize];
	private short[] output = new short[frameSize];

	public Encoder(int codeccode) {
		codec =new Codec(codeccode);
		codec.init();
		frameSize = codec.getFrameSize();
		this.m_ec=MediaService.m_ec;
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
	public void run() {

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (Thread.currentThread() == runner) {

			synchronized (mutex) {
				while (isIdle()) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			synchronized (mutex) {
				output=rawdata.clone();
				if(m_ec!=null){
					if(Decoder.num_recv>0){ 
						if(num_send<20)   
						{
							num_send++;
						}
						else{
							m_ec.putData(true, output, output.length);
							if(m_ec.isGetData())
							{ 
								output=m_ec.getshortData();
							}
						}
					}
				}
				getSize = codec.encode(output, 0, processedData, leftSize);
				this.dataLen = getSize;
				byte tempdata[] =new byte[getSize+Rtp_head];
				System.arraycopy(processedData, Rtp_head, tempdata, Rtp_head, getSize);
				m_in_q.add(tempdata);
				setIdle();
			}
		}
		free();
	}

	public void putData(long ts, short[] data, int off ,int size) {
		synchronized (mutex) {
			this.ts = ts;
			System.arraycopy(data, off, rawdata, 0, size);
			this.leftSize = size;
			mutex.notify();
		}
	}
	
	public byte[] getData(){
		return m_in_q.removeFirst();
	}

	public int getdataLen(){
		return this.dataLen;
	}
	public boolean isGetData()
	{
		return m_in_q.size() == 0 ?false : true; 
	}
	public boolean isIdle() {
		return leftSize == 0 ? true : false;
	}

	public void setIdle() {
		leftSize = 0;
	}

	private void free(){
		num_send=0;
		codec.close();
	}
}
