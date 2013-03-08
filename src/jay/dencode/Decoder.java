package jay.dencode;

import java.util.LinkedList;

import jay.codec.Codec;
import jay.codec.EchoCancellation;
import jay.media.MediaService;

public class Decoder implements Runnable {

	private volatile int leftSize = 0;
	private final Object mutex = new Object();
	private Codec codec;
	private int frameSize =160;
	private long ts;
	private short[] processedData = new short[frameSize];
	private byte[] rawdata = new byte[frameSize*2];
	protected LinkedList<short[]> m_out_q=new LinkedList<short[]>();    //store processed data
	private EchoCancellation m_ec;
	static public int num_recv;
	protected int rtp_head = 12;
	private volatile Thread runner;

	public Decoder(int codeccode) {
		super();
		codec = new Codec(codeccode);
		codec.init();
		m_ec = MediaService.m_ec;
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
				byte[] raw_temp = new byte[leftSize+rtp_head];
				System.arraycopy(rawdata, 0, raw_temp, rtp_head, leftSize);
				getSize = codec.decode(raw_temp, processedData, leftSize);
				if(m_ec!=null){
					if(num_recv<20)
						num_recv++;
					else{
						m_ec.putData(false, processedData, processedData.length);
					}
				}
				m_out_q.add(processedData);
				setIdle();
			}
		}
	}

	public void putData(long ts, byte[] data,int offset, int size) {
		synchronized (mutex) {
			this.ts = ts;
			System.arraycopy(data, offset, rawdata, 0, size);
			this.leftSize = size;
			mutex.notify();
		}
	}
	
	public boolean isGetData()
	{
		return m_out_q.size() == 0 ?false : true; 
	}
	
	public short[] getData(){
		return m_out_q.removeFirst();
	}

	public boolean isIdle() {
		return leftSize == 0 ? true : false;
	}

	public void setIdle() {
		leftSize = 0;
	}
	
	public void free(){
		num_recv=0;
		codec.close();
	}
}
