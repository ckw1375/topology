package beacon.topology.cls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.Math;

public class BeaconTracker {
	private HashMap<Beacon,BeaconTrace> beaconMap;
	
	public class BeaconTrace {
		private double avgRssi;
		private double avgDist;
		private double lastUpdate;
		private double alpha;
		private double gain;
		private double pathCoef;
		
		public double getAvgRssi() { return avgRssi; }
		public double getAvgDist() { return avgDist; }
		public BeaconTrace()
		{
			avgRssi = 0;
			avgDist = 0;
			lastUpdate = 0;
			alpha = 0.001; //Update coefficient for 100 ms span
			gain = 0;
			pathCoef = 3; //Path loss coefficient
		}
		public void updateTrace(double rssi, double txPower)
		{
			//Calculate the average RSSI
			double curTime = (double)System.nanoTime();
			double timeDiff = (curTime - lastUpdate) * 0.00001; //in 100 ms
			double beta = 1 - Math.pow(1-alpha, timeDiff);
			if(lastUpdate == 0 || timeDiff < 0) {
				avgRssi = rssi;
			} else {
				avgRssi = beta*rssi + (1-beta)*avgRssi;
			}
			lastUpdate = curTime;
			//Calculate the distance
			double diff = txPower - rssi + gain;
			avgDist = Math.pow(10, diff/(10*pathCoef));
		}
	}
	
	public BeaconTracker()
	{
		beaconMap = new HashMap<Beacon,BeaconTrace>();
	}
	
	public void renewBeaconList(ArrayList<Beacon> beacons)
	{
		HashMap<Beacon,BeaconTrace> tmpMap = new HashMap<Beacon,BeaconTrace>();
		Iterator<Beacon> it = beacons.iterator();
		while(it.hasNext()) {
			Beacon x = it.next();
			if(beaconMap.containsKey(x))
				tmpMap.put(x, beaconMap.get(x));
			else
				tmpMap.put(x, new BeaconTrace());			
		}
		beaconMap = tmpMap;
	}
	
	public Double getAvgRssi(Beacon beacon)
	{
		return beaconMap.get(beacon).getAvgRssi();
	}
	
	public Double getAvgDist(Beacon beacon)
	{
		return beaconMap.get(beacon).getAvgDist();
	}

	public void update(ArrayList<Beacon> beacons) {
		Iterator<Beacon> it1 = beacons.iterator();
		Iterator<Beacon> it2 = beacons.iterator();
		while(it1.hasNext()) {
			Beacon x = it1.next();
			if(x != null && beaconMap.containsKey(x)) {
				double sum = x.getRssi();
				int count = 1;
				it2 = it1;
				while(it2.hasNext()) {
					Beacon y = it2.next();
					if(y == x) {
						sum = sum + (double)y.getRssi();
						count++;
					}
					it2.remove();				
				}			
				beaconMap.get(x).updateTrace(sum/(double)count, (double)x.getMeasuredPower());
			}
		}
	}
}
