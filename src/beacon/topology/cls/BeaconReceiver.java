package beacon.topology.cls;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.estimote.sdk.service.BeaconService;
import com.estimote.sdk.utils.L;

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
	private boolean active; 
	private boolean bluetoothOn;
	
	public BeaconReceiver(Context nContext, Handler nHandler, BeaconTracker nTracker) {
		context = nContext;
		BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
		if(adapter == null || !adapter.isEnabled()) bluetoothOn = false;
		else bluetoothOn = true;
		leScanCallback = new ScanCallback();
		alarmManager = (AlarmManager)context.getSystemService("alarm");
		handler = nHandler;
		scanTimeMillis = 1000; //default value
		idleTimeMillis = 0; //default value
		bluetoothBR = new BluetoothBroadcastReceiver();
		scanBR = createScanBroadcastReceiver();
		idleBR = createIdleBroadcastReceiver();
		context.registerReceiver(bluetoothBR, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
		context.registerReceiver(scanBR, new IntentFilter("beaconReceiver.scan"));
		context.registerReceiver(idleBR, new IntentFilter("beaconReceiver.idle"));
		scanPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("beaconReceiver.scan"), 0);
		idlePendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("beaconReceiver.idle"), 0);
		tracker = nTracker;
		active = false;
	}
	
	public boolean setScanIdleTimeMillis(long nScanTimeMillis, long nIdleTimeMillis) {
		if(nScanTimeMillis <= 0 || nIdleTimeMillis < 0) return false;
		scanTimeMillis = nScanTimeMillis;
		idleTimeMillis = nIdleTimeMillis;	
		return true;
	}
	
	public boolean isActive() { return active; }
	
	public boolean activate() {
		
	}
	
	public boolean deactivate() {
		
	}
	
	private class ScanCallback implements BluetoothAdapter.LeScanCallback {
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Beacon beacon = Utils.beaconFromLeScan(device, rssi, scanRecord);
			Region r = new Region(null, beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());			
			tracker.update(r, (double)rssi, (double)beacon.getMeasuredPower());
		}
	}
	
	private class BluetoothBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
				int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
				if (state == 10) { //Bluetooth is OFF
					deactivate();           
				} else if (state == 12) { //Bluetooth is ON
					activate();
				}
			}
		}
	}
 
	

}
