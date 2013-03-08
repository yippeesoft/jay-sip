/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.stun;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

/**
 * Executes an asynchronous task for testing the NAT and NAT traversal. Raises a STUNDiscoveryResultEvent when new information is available.
 * 
 * @author Sebastian
 * 
 */
public class STUNDiscoveryTask extends AsyncTask<STUNInfo, DiscoveryInfo, DiscoveryInfo> {
	private static String TAG = "tSIP";
	private STUNInfo stunInfo;
	private InetAddress inetAddress;

	private List<STUNDiscoveryResultListener> listeners = new ArrayList<STUNDiscoveryResultListener>();

	@Override
	protected DiscoveryInfo doInBackground(STUNInfo... params) {
		this.stunInfo = params[0];
		try {
			inetAddress = getInetAddress();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Log.d(TAG, "Starting STUN test for " + stunInfo.getStunAddress() + ":" + stunInfo.getStunPort() + " from " + stunInfo.getLocalPort());
			DiscoveryTest test = new DiscoveryTest(inetAddress, stunInfo.getLocalPort(), stunInfo.getStunAddress(), stunInfo.getStunPort());
			DiscoveryInfo info = test.test();

			return info;
		} catch (Exception e) {
			try {
				DiscoveryTest test = new DiscoveryTest(inetAddress, stunInfo.getStunAddress(), stunInfo.getStunPort());
				DiscoveryInfo info = test.test();

				return info;
			} catch (Exception ex) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(DiscoveryInfo result) {
		STUNDiscoveryResultEvent event = new STUNDiscoveryResultEvent(this, result, stunInfo);

		synchronized (listeners) {
			for (STUNDiscoveryResultListener item : listeners) {
				item.STUNDiscoveryResultChanged(event);
			}
		}

		super.onPostExecute(result);
	}

	/**
	 * Returns the first Internet-facing InetAddress, or Localhost, if none was found
	 * 
	 * @return InetAddress the InetAddress of the interface
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	private InetAddress getInetAddress() throws SocketException, UnknownHostException {
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

		return InetAddress.getLocalHost();
	}

	/**
	 * Add a STUNDiscoveryResultListener to this STUNDiscoveryTask.
	 * 
	 * @param listener
	 *            the listener to register for the raised events
	 */
	public synchronized void addResultListener(STUNDiscoveryResultListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a STUNDiscoveryResultListener from this STUNDiscoveryTask.
	 * 
	 * @param listener
	 *            the listener to unregister from the events
	 */
	public synchronized void removeResultListener(STUNDiscoveryResultListener l) {
		listeners.remove(l);
	}
}
