package beacon.topology.demo;

import java.util.ArrayList;

import beacon.topology.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ListView;
import beacon.topology.cls.*;

public class BeaconListingActivity extends Activity {

	private BeaconTracker tracker;
	private BeaconReceiver receiver;	
	private LeDeviceListAdapter adapter;
	
	private int interval = 100; //ms
	private Handler mainHandler;
	private Handler subHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.beacon_listing);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    adapter = new LeDeviceListAdapter(this);
	    ListView list = (ListView) findViewById(R.id.device_list);
	    list.setAdapter(adapter);
	    
	    mainHandler = new Handler();
	    HandlerThread subThread = new HandlerThread("subThread");
	    subThread.start();
	    subHandler = new Handler(subThread.getLooper());	    
	    
	    tracker = new BeaconTracker();
	    BeaconTracker.Filter filter = new BeaconTracker.Filter();
	    filter.add(new Region(null, null, null));
	    tracker.setFilter(filter);
	    receiver = new BeaconReceiver(this, subHandler, tracker);
	    receiver.setScanIdleTimeMillis(100, 0);
	}
	
	public void onResume() {
		super.onResume();
	    receiver.activate();	
	    mainHandler.post(updateBeacon);
	}
	
	public void onPause() {
		super.onPause();
		receiver.deactivate();
		mainHandler.removeCallbacks(updateBeacon);
	}
	
	private Runnable updateBeacon = new Runnable() {
		public void run() {
			ArrayList<Beacon> beacons = tracker.getAllNearbyBeacons();
			adapter.replaceWith(beacons);
			mainHandler.postDelayed(updateBeacon, interval);
		}
	};
}
