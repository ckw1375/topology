package beacon.topology.demo;

import java.util.ArrayList;

import beacon.topology.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import beacon.topology.cls.*;

public class ProximityTopologyActivity extends Activity {

	private BeaconTracker tracker;
	private BeaconReceiver receiver;	
	private ProximityTopology prox;
	private LeDeviceListAdapter adapter;
	
	private int interval = 100; //ms
	private Handler mainHandler;
	private Handler subHandler;
	
    Region r1; 
    Region r2; 
    Region r3; 
    BeaconVector bv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.beacon_listing);
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    adapter = new LeDeviceListAdapter(this);
	    ListView list = (ListView) findViewById(R.id.device_list);
	    list.setAdapter(adapter);
	    list.setOnItemClickListener(createOnItemClickListener());
	    
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
	    
	    r1 = new Region("00000000-0000-0000-0000-000000000000", 0, 1); 
	    r2 = new Region("00000000-0000-0000-0000-000000000000", 0, 2); 
	    r3 = new Region("00000000-0000-0000-0000-000000000000", 0, 3); 
	    bv = new BeaconVector(3);
	    bv.set(0, r1);
	    bv.set(1, r2);
	    bv.set(2, r3);
	    prox = new ProximityTopology(bv, tracker);
	}
	
	public void onResume() {
		super.onResume();
		startUpdate();
	}
	
	public void onPause() {
		super.onPause();
		stopUpdate();
	}
	
	private void startUpdate() {
	    receiver.activate();	
	    mainHandler.post(updateBeacon);
	}
	
	private void stopUpdate() {
		receiver.deactivate();
		mainHandler.removeCallbacksAndMessages(updateBeacon);
	}
	
	private Runnable updateBeacon = new Runnable() {
		public void run() {
			ArrayList<Beacon> beacons = tracker.getBeacon(bv);
			adapter.replaceWith(beacons);
			Region selected = prox.getResult();
			int ind = bv.indexOf(selected);
			adapter.setColor(ind);
			mainHandler.postDelayed(updateBeacon, interval);
		}
	};
	
	private AdapterView.OnItemClickListener createOnItemClickListener() {
		return new AdapterView.OnItemClickListener() {
			Region r;
			EditText input;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Beacon b = adapter.getItem(position);
				r = new Region(b.getProximityUUID(), b.getMajor(), b.getMinor());
				AlertDialog.Builder builder = new AlertDialog.Builder(ProximityTopologyActivity.this);
				input = new EditText(ProximityTopologyActivity.this);
				input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
				input.setText(String.valueOf(prox.getRange(r)));
				builder.setView(input);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						prox.setRange(r, Double.valueOf(input.getText().toString()));
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				});
				builder.show();
			}
		};
	}
}
