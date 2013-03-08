package de.tinysip.sip;


import jay.sipdemo.JaySipActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class Siphandler extends BroadcastReceiver{

	private JaySipActivity pta;
	@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		pta = (JaySipActivity) context;
		Intent intent = arg1;
		Bundle data =intent.getExtras();
		String state = data.getString("bread");
		String info = data.getString("info");
		Message message = new Message();
		message.setData(data);
		pta.mHandler.sendMessage(message);
	}
}