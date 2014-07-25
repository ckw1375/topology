package beacon.topology.cls;

import java.util.*;
import java.lang.*;

public class ProximityTopology {
	private HashMap<Beacon,Double> prox;
	
	public ProximityTopology(ArrayList<Beacon> beaconList)
	{
		prox = new HashMap<Beacon,Double>();
		Iterator<Beacon> it = beaconList.iterator();
		while(it.hasNext()) {
			prox.put(it.next(),Double.POSITIVE_INFINITY);
		}
	}
	
	public Double getRange(Beacon beacon)
	{
		return prox.get(beacon);
	}
	
	public void putRange(Beacon beacon, Double range)
	{
		prox.put(beacon, range);
	}
	
	public Beacon getResult(ArrayList<Beacon> beaconList)
	{
		Iterator<Beacon> it = beaconList.iterator();
		Beacon selected;
		Double minRange;
		while(it.hasNext()) {
			Beacon beacon = it.next();
			if(prox.containsKey(beacon))
			{
				Double range = prox.get(beacon);
				if(beacon.)
				
				
				
			}
		}
		return selected;
	}
	
	
	

}
