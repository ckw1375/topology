package beacon.topology.cls;

import java.util.ArrayList;

public class DistanceVector {
	private Double[] dist;
	private int size;
	public DistanceVector(int nSize) { 
		size = nSize;
		dist = new Double[size];			
	}
	public int getSize() { return size; }
	public boolean setAll(ArrayList<Double> nDist) {
		if(nDist.size() != size) return false;
		for(int ind = 0; ind < size; ind++)
			dist[ind] = nDist.get(ind);
		return true;
	}
	public boolean set(int ind, Double nDist) {
		if(ind < 0 || ind > size) return false;
		dist[ind] = nDist;		
		return true;
	}
	public Double get(int ind) {
		if(ind < 0 || ind > size) return null;
		return dist[ind];
	}
	//Calculate a squared distance to another distance vector
	public Double sqDist(DistanceVector x) {
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
