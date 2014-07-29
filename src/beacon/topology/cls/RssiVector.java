package beacon.topology.cls;

import java.util.ArrayList;

public class RssiVector {
	private Double[] rssi;
	private int size;
	public RssiVector(int nSize) { 
		size = nSize;
		rssi = new Double[size];			
	}
	public int getSize() { return size; }
	public boolean setAll(ArrayList<Double> nRssi) {
		if(nRssi.size() != size) return false;
		for(int ind = 0; ind < size; ind++)
			rssi[ind] = nRssi.get(ind);
		return true;
	}
	public boolean set(int ind, Double nRssi) {
		if(ind < 0 || ind > size) return false;
		rssi[ind] = nRssi;		
		return true;
	}
	public Double get(int ind) {
		if(ind < 0 || ind > size) return null;
		return rssi[ind];
	}
	//Calculate a squared distance to another rssi vector
	public Double sqDist(RssiVector x) {
		if(this.getSize() != x.getSize()) return null;
		Double result = 0D;
		for(int i = 0; i < size; i++) {
			Double a = this.get(i);
			Double b = x.get(i);
			if(a != null && b != null) {
				result += Math.pow(a-b, 2);
			} else if(a == null && b == null) {
				continue;
			} else {
				return Double.valueOf(Double.POSITIVE_INFINITY);
			}
		}
		return result;			
	}		
}
