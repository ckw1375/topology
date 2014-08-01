package beacon.topology.cls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class BeaconReceiver {
	private Context context;
	private BluetoothAdapter adapter;
	private BluetoothAdapter.LeScanCallback leScanCallback;
	private AlarmManager alarmManager;
	private Handler handler;
	private long scanTimeMillis;
	private long idleTimeMillis;
	private BroadcastReceiver bluetoothBR;
	private BroadcastReceiver scanBR;
	private BroadcastReceiver idleBR;
	private PendingIntent scanPendingIntent;
	private PendingIntent idlePendingIntent;
	private BeaconTracker tracker;
	private boolean active; //True when the ble scan is activated.
	private boolean bluetoothOn; //True when the bluetooth device is ON.
	private boolean scanning; //True when it is in the scanning state.
	
	public BeaconReceiver(Context nContext, Handler nHandler, BeaconTracker nTracker) {
		context = nContext;
		BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
		if(adapter == null || !adapter.isEnabled()) bluetoothOn = false;
		else bluetoothOn = true;
		leScanCallback = new ScanCallback();
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		handler = nHandler;
		scanTimeMillis = 1000; //default value
		idleTimeMillis = 0; //default value
		bluetoothBR = new BluetoothBroadcastReceiver();
		scanBR = new scanBroadcastReceiver();
		idleBR = new idleBroadcastReceiver();
		context.registerReceiver(bluetoothBR, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"), null, handler);
		context.registerReceiver(scanBR, new IntentFilter("beaconReceiver.SCAN"), null, handler);
		context.registerReceiver(idleBR, new IntentFilter("beaconReceiver.IDLE"), null, handler);
		scanPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("beaconReceiver.SCAN"), 0);
		idlePendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("beaconReceiver.IDLE"), 0);
		tracker = nTracker;
		active = false;
		scanning = false;
	}
	
	public boolean setScanIdleTimeMillis(long nScanTimeMillis, long nIdleTimeMillis) {
		if(nScanTimeMillis <= 0 || nIdleTimeMillis < 0) return false;
		scanTimeMillis = nScanTimeMillis;
		idleTimeMillis = nIdleTimeMillis;	
		return true;
	}
	
	private class ScanCallback implements BluetoothAdapter.LeScanCallback {
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			handler.post(new ScanProcessing(tracker, device, rssi, scanRecord));
		}
	}

	public static class ScanProcessing implements Runnable {
		BeaconTracker tracker;
		BluetoothDevice device;
		int rssi;
		byte[] scanRecord;
		public ScanProcessing(BeaconTracker nTracker, BluetoothDevice nDevice, 
				int nRssi, byte[] nScanRecord) {
			tracker = nTracker;
			device = nDevice;
			rssi = nRssi;
			scanRecord = nScanRecord;					
		}
		public void run() {
			Log.d("BluetoothReceiver", "ScanProcessing, " + "Thread:" + Thread.currentThread().getId());
			Beacon beacon = Utils.beaconFromLeScan(device, rssi, scanRecord);
			Region r = new Region(beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());			
			tracker.update(r, beacon.getMacAddress(), (double)rssi, (double)beacon.getMeasuredPower());					
		}
	}
	
	private class BluetoothBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
				int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
				if (state == 12) { //Bluetooth is ON
					bluetoothOn = true;
					removeAlarm();
					scanStart();
				} else if (state == 10) { //Bluetooth is OFF
					bluetoothOn = false;
					removeAlarm();
					idleStart();
				}
			}
		}
	}
	
	public boolean isActive() { return active; }
	
	public void activate() {
		active = true;
		removeAlarm();
		scanStart();
	}
	
	public void deactivate() {
		active = false;
		removeAlarm();
		idleStart();
	}
	
	void scanStart() {
		Log.d("BluetoothReceiver", "scanStart(), " + "Thread:" + Thread.currentThread().getId());
		handler.post(new Runnable() {
			public void run() {		
				if(bluetoothOn == true && active == true) {
					adapter.startLeScan(leScanCallback);
					scanning = true;
					if(idleTimeMillis > 0) setAlarm(idlePendingIntent, scanTimeMillis);
				}
			}
		});
	}
	
	void idleStart() {
		Log.d("BluetoothReceiver", "idleStart(), " + "Thread:" + Thread.currentThread().getId());
		handler.post(new Runnable() {
			public void run() {	
				adapter.stopLeScan(leScanCallback);
				scanning = false;
				if(bluetoothOn == true && active == true) setAlarm(scanPendingIntent, idleTimeMillis);
			}
		});
	}

	private class scanBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			scanStart();
		}
	}
	
	private class idleBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			idleStart();
		}
	}
	
	private void setAlarm(PendingIntent pendingIntent, long delayMillis)
	{
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
				SystemClock.elapsedRealtime() + delayMillis, pendingIntent);
	}
	
	private void removeAlarm()
	{
		alarmManager.cancel(scanPendingIntent);
		alarmManager.cancel(idlePendingIntent);
	}
	
	public void onDestroy()
	{
		removeAlarm();
		active = false;
		idleStart();	
		
		context.unregisterReceiver(this.bluetoothBR);
		context.unregisterReceiver(this.scanBR);
		context.unregisterReceiver(this.idleBR);
	}

	public boolean isScanning() {
		return scanning;
	}
}
