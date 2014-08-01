package beacon.topology.cls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import android.util.Pair;

public class BeaconTracker {
	private HashMap<Region,BeaconTrace> beaconMap; //Region is an identifier of a single beacon in this map
	private Filter filter; //filter defines the set of tracked beacons
	
	public static class Filter {
		ArrayList<Region> filters;
		public Filter() {
			filters = new ArrayList<Region>();
		}
		public boolean add(Region r) {
			boolean eligible = true;
			for(Region it : filters) {
				if(it.includes(r)) { 
					eligible = false;
					break;
				}
			}
			if(eligible == false) return false;
			Iterator<Region> it = filters.iterator();
			while(it.hasNext()) {
				Region x = it.next();
				if(r.includes(x)) it.remove();
			}
			filters.trimToSize();				
			filters.add(r);
			return true;			
		}
		public void clear() {
			filters.clear();
		}
		public boolean test(Region beacon) {
			boolean pass = false;
			for(Region selected : filters) {
				if(selected.includes(beacon)) {
					pass = true;
					break;
				}
			}
			return pass;
		}
	}
	
	private static class BeaconTrace {
		private double rssi; //in dB
		private double txPower; //in dB
		private double avgRssi; //in dB
		private String macAddress;
		private long lastUpdate; //in nanoseconds
		//Constant
		private static final double MV_AVG_COEF = 0.1; //Update coefficient for moving avg. for 100 ms span
		private static final double GAIN = 18; //Gain in dB for path loss calculation
		private static final double PL_COEF = 3; //Path loss coefficient
		private static final long TIME_OUT_SECONDS = 5; //Time out for a nearby state in seconds
		//Constructor
		public BeaconTrace(String nMacAddress, double nRssi, double nTxPower) {
			lastUpdate = -1;
			updateTrace(nMacAddress, nRssi, nTxPower);
		}
		//Update average RSSI
		public void updateTrace(String nMacAddress, double nRssi, double nTxPower) {
			macAddress = nMacAddress;
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
		public String getMacAddress() { return macAddress; }
		public double getRssi() { return rssi; }
		public double getAvgRssi() { return avgRssi; }
		public double getAvgDist() {
			//Calculate the distance
			double diff = txPower - avgRssi + GAIN; //Path loss (in dB) calculation
			return Math.pow(10, diff/(10*PL_COEF));			
		}
		public double getTxPower() { return txPower; }
		public boolean isNearby() {
			long timeDiffSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastUpdate); //in seconds
			return timeDiffSeconds < TIME_OUT_SECONDS ? true : false; 
		}
	}
	
	//Constructor
	public BeaconTracker()
	{
		beaconMap = new HashMap<Region,BeaconTrace>();
		filter = new Filter();
	}
	
	//Set a new filter. And remove all beacons not included in a new filter.
	public void setFilter(Filter nFilter)
	{
		filter = nFilter;
		//Remove all beacons not included in a new filter.
		for(Map.Entry<Region,BeaconTrace> x : beaconMap.entrySet()) {
			Region r = x.getKey();
			if(!filter.test(r)) beaconMap.remove(r);				
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
		for(int ind = 0; ind < size; ind ++) {
			Region beacon = b.get(ind);
			Boolean nearby = beaconMap.containsKey(beacon) ? 
					Boolean.valueOf(beaconMap.get(beacon).isNearby()) : false;
			n.add(nearby);
		}
		return n;
	}
	//Get Beacon objects corresponding to BeaconVector b. (null is returned for not-found beacons)
	public ArrayList<Beacon> getBeacon(BeaconVector b) {
		int size = b.getSize();
		ArrayList<Beacon> beacons = new ArrayList<Beacon>(size);
		for(int ind = 0; ind < size; ind ++) {
			Region r = b.get(ind);
			if(beaconMap.containsKey(r) && beaconMap.get(r).isNearby()) {
				BeaconTrace t = beaconMap.get(r);
				String proximityUUID = r.getProximityUUID();
				int major = r.getMajor();
				int minor = r.getMinor();
				String macAddress = t.getMacAddress();
				double measuredPower = t.getTxPower();
				double rssi = t.getAvgRssi();
				double distance = t.getAvgDist();
				beacons.add(new Beacon(proximityUUID, null, macAddress, major, minor,
						measuredPower, rssi, distance));
			} else {
				beacons.add(null);
			}
		}
		return beacons;
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
	
	public ArrayList<Beacon> getAllNearbyBeacons() {
		ArrayList<Beacon> beacons = new ArrayList<Beacon>();
		for(Map.Entry<Region,BeaconTrace> entry : beaconMap.entrySet()) {
			if(entry.getValue().isNearby()) {
				Region r = entry.getKey();
				BeaconTrace t = entry.getValue();
				String proximityUUID = r.getProximityUUID();
				int major = r.getMajor();
				int minor = r.getMinor();
				String macAddress = t.getMacAddress();
				double measuredPower = t.getTxPower();
				double rssi = t.getAvgRssi();
				double distance = t.getAvgDist();
				beacons.add(new Beacon(proximityUUID, null, macAddress, major, minor,
						measuredPower, rssi, distance));
			}
		}
		return beacons;
	}

	public void update(Region beacon, String nMacAddress, double nRssi, double nTxPower) {
		if(beaconMap.containsKey(beacon)) {
			beaconMap.get(beacon).updateTrace(nMacAddress, nRssi, nTxPower);
		} else {
			if(filter.test(beacon))
				beaconMap.put(beacon, new BeaconTrace(nMacAddress, nRssi, nTxPower));			
		}
	}
}
