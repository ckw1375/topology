package beacon.topology.cls;

import java.util.*;

public class ProximityTopology {
	private HashMap<Region,Double> rangeMap; //Map for ranges in meters
	private BeaconTracker tracker;
	
	public ProximityTopology(ArrayList<Region> beacons, BeaconTracker nTracker)
	{
		rangeMap = new HashMap<Region,Double>();
		for(Region it : beacons)
			rangeMap.put(it,Double.valueOf(Double.POSITIVE_INFINITY));
		tracker = nTracker;
	}
	
	public Double getRange(Region beacon)
	{
		return rangeMap.containsKey(beacon) ? rangeMap.get(beacon) : null;
	}
	
	public void setRange(Region beacon, Double range)
	{
		if(rangeMap.containsKey(beacon)) rangeMap.put(beacon, range);
	}
	
	public Region getResult()
	{
		Double minDist = Double.valueOf(Double.POSITIVE_INFINITY); 
		Region result = null;
		for(Map.Entry<Region,Double> entry : rangeMap.entrySet()) {
			Region beacon = entry.getKey();
			Double range = entry.getValue();
			if(tracker.isNearby(beacon)) {
				Double dist = tracker.getAvgDist(beacon);
				if(dist <= range && dist <= minDist) {
					result = beacon;
					minDist = dist;
				}				
			}
		}
		return result;
	}
}
