package beacon.topology.cls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.util.Pair;

public class BeaconTracker {
	private HashMap<Region,BeaconTrace> beaconMap; //Region is an identifier of a single beacon in this map
	private ArrayList<Region> filter; //filter defines the set of tracked beacons
	
	public class BeaconTrace {
		private double rssi; //in dB
		private double txPower; //in dB
		private double avgRssi; //in dB
		private long lastUpdate; //in nanoseconds
		//Constant
		private static final double MV_AVG_COEF = 0.01; //Update coefficient for moving avg. for 100 ms span
		private static final double GAIN = 0; //Gain in dB for path loss calculation
		private static final double PL_COEF = 3; //Path loss coefficient
		private static final long TIME_OUT_SECONDS = 5; //Time out for a nearby state in seconds
		//Constructor
		public BeaconTrace(double nRssi, double nTxPower) {
			lastUpdate = -1;
			updateTrace(nRssi, nTxPower);
		}
		//Update average RSSI
		public void updateTrace(double nRssi, double nTxPower) {
			rssi = nRssi;
			txPower = nTxPower;
			//Calculate the average RSSI
			long curTime = System.nanoTime();
			double timeDiff100Millis = (double)TimeUnit.NANOSECONDS.toMillis(curTime - lastUpdate)/100D; //in 100 ms
			if(lastUpdate == -1 || timeDiff100Millis < 0) {
				avgRssi = rssi;
			} else {
				double alpha = 1 - Math.pow(1 - MV_AVG_COEF, timeDiff100Millis);
				avgRssi = alpha*rssi + (1-alpha)*avgRssi;
			}
			lastUpdate = curTime;
		}
		//Getter
		public double getRssi() { return rssi; }
		public double getAvgRssi() { return avgRssi; }
		public double getAvgDist() {
			//Calculate the distance
			double diff = txPower - avgRssi + GAIN; //Path loss calculation
			return Math.pow(10, diff/(10*PL_COEF));			
		}
		public boolean isNearby() {
			long timeDiffSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastUpdate); //in seconds
			return timeDiffSeconds < TIME_OUT_SECONDS ? true : false; 
		}
	}
	
	//Constructor
	public BeaconTracker()
	{
		beaconMap = new HashMap<Region,BeaconTrace>();
		filter = new ArrayList<Region>();
	}
	
	//Return true only when beacon is included in filter.
	private boolean filterTest(Region beacon)
	{
		boolean pass = false;
		for(Region selected : filter) {
			if(selected.includes(beacon)) pass = true;
		}
		return pass;
	}
	
	//Set a new filter. And remove all beacons not included in a new filter.
	public void setFilter(ArrayList<Region> regions)
	{
		//Only non-overlapping regions are added to filter
		filter.clear();
		for(Region selected : regions) {
			Iterator<Region> it = filter.iterator();
			boolean eligible = true;
			while(it.hasNext()) {
				Region tmp = it.next();
				if(tmp.includes(selected)) { 
					eligible = false;
					break;
				}
				if(selected.includes(tmp)) it.remove();
			}
			if(eligible == true)
				filter.add(selected);
		}
		//Remove all beacons not included in a new filter.
		Iterator<Map.Entry<Region,BeaconTrace>> it = beaconMap.entrySet().iterator();
		while(it.hasNext()) {
			if(!filterTest(it.next().getKey())) it.remove();				
		}
	}
	
	public RssiVector getRssi(BeaconVector b) {
		int size = b.getSize();
		RssiVector r = new RssiVector(size);
		for(int ind = 0; ind < size; ind++) {
			Region beacon = b.get(ind);
			Double rssi;
			if(beaconMap.containsKey(beacon) && beaconMap.get(beacon).isNearby()) {
				rssi = Double.valueOf(beaconMap.get(beacon).getRssi());
			} else {
				rssi = null;
			}			
			r.set(ind, rssi);
		}
		return r;
	}
		
	public RssiVector getAvgRssi(BeaconVector b) {
		int size = b.getSize();
		RssiVector r = new RssiVector(size);
		for(int ind = 0; ind < size; ind++) {
			Region beacon = b.get(ind);
			Double rssi;
			if(beaconMap.containsKey(beacon) && beaconMap.get(beacon).isNearby()) {
				rssi = Double.valueOf(beaconMap.get(beacon).getAvgRssi());
			} else {
				rssi = null;
			}
			r.set(ind, rssi);
		}
		return r;
	}
	
	public DistanceVector getAvgDist(BeaconVector b) {
		int size = b.getSize();
		DistanceVector d = new DistanceVector(size);
		for(int ind = 0; ind < size; ind++) {
			Region beacon = b.get(ind);
			Double distance;
			if(beaconMap.containsKey(beacon) && beaconMap.get(beacon).isNearby()) {
				distance = Double.valueOf(beaconMap.get(beacon).getAvgDist());
			} else {
				distance = null;
			}
			d.set(ind, distance);
		}
		return d;
	}
	
	public ArrayList<Boolean> isNearby(BeaconVector b) {
		int size = b.getSize();
		ArrayList<Boolean> n = new ArrayList<Boolean>(size);
		for(int ind = 0; ind < size; ind++) {
			Region beacon = b.get(ind);
			Boolean nearby = beaconMap.containsKey(beacon) ? 
					Boolean.valueOf(beaconMap.get(beacon).isNearby()) : false;
			n.set(ind, nearby);
		}
		return n;
	}
	
	public Pair<BeaconVector,DistanceVector> getAllNearbyAvgDist() {
		ArrayList<Region> b = new ArrayList<Region>();
		ArrayList<Double> d = new ArrayList<Double>(); 
		for(Map.Entry<Region,BeaconTrace> entry : beaconMap.entrySet()) {
			if(entry.getValue().isNearby()) {
				b.add(entry.getKey());
				d.add(Double.valueOf(entry.getValue().getAvgDist()));
			}
		}
		int size = b.size();
		BeaconVector bv = new BeaconVector(size);
		DistanceVector dv = new DistanceVector(size);
		bv.setAll(b);
		dv.setAll(d);
		return new Pair<BeaconVector,DistanceVector>(bv,dv);
	}

	public void update(Region beacon, double nRssi, double nTxPower) {
		if(beaconMap.containsKey(beacon)) {
			beaconMap.get(beacon).updateTrace(nRssi, nTxPower);
		} else {
			if(filterTest(beacon))
				beaconMap.put(beacon, new BeaconTrace(nRssi, nTxPower));			
		}
	}
}
