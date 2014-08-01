package beacon.topology.cls;

import java.util.*;

public class BeaconVector {
	private Region[] beacons;
	private int size;
	public BeaconVector(int nSize) {
		size = nSize;
		beacons = new Region[size];
	}
	private boolean checkValidity(Region r) {
		if(r == null || r.getProximityUUID() == null 
				|| r.getMajor() == null || r.getMinor() == null) 
			return false;
		return true;
	}
	public int getSize() { return size; }
	public boolean setAll(ArrayList<Region> nBeacons) {
		if(nBeacons.size() != size) return false;
		for(Region it : nBeacons) 
			if(checkValidity(it) == false) return false;
		for(int ind = 0; ind < size; ind++)
			beacons[ind] = nBeacons.get(ind);
		return true;
	}
	public boolean set(int ind, Region nBeacon) {
		if(ind < 0 || ind > size) return false;
		if(checkValidity(nBeacon) == false) return false;
		beacons[ind] = nBeacon;		
		return true;
	}
	public Region get(int ind) {
		if(ind < 0 || ind > size) return null;
		return beacons[ind];
	}
	public int indexOf(Region r) {
		for(int ind = 0; ind < size; ind++)
			if(beacons[ind].equals(r)) return ind;
		return -1;
	}
}
