package beacon.topology.cls;

import java.util.ArrayList;

public class ProximityTopology {
	private BeaconVector bv;
	private Double[] range; //Ranges in meters
	private int size;
	private BeaconTracker tracker;
	
	public ProximityTopology(BeaconVector nBv, BeaconTracker nTracker)
	{
		bv = nBv;
		size = bv.getSize();
		tracker = nTracker;
		range = new Double[size];
		for(int ind = 0; ind < size; ind++)
			range[ind] = Double.valueOf(Double.POSITIVE_INFINITY);
	}
	
	public Double getRange(Region beacon)
	{
		int ind = bv.indexOf(beacon);
		return (ind >= 0) ? range[ind] : null;
	}
	
	public boolean setRange(Region beacon, Double nRange)
	{
		int ind = bv.indexOf(beacon);
		if(ind == -1) return false;
		range[ind] = nRange;
		return true;
	}
	
	public Region getResult()
	{
		Double minDist = Double.valueOf(Double.POSITIVE_INFINITY); 
		Region result = null;
		DistanceVector dv = tracker.getAvgDist(bv);
		ArrayList<Boolean> nb = tracker.isNearby(bv);
				
		for(int ind = 0; ind < size; ind++) {
			if(nb.get(ind)) {
				Double dist = dv.get(ind);
				if(dist.compareTo(range[ind]) <= 0 && dist.compareTo(minDist) <= 0) {
					result = bv.get(ind);					
					minDist = dist;
				}				
			}
		}
		return result;
	}
}
