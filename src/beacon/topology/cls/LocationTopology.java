package beacon.topology.cls;

import java.util.*;
import android.util.Pair;

public class LocationTopology {
	public class Coordinate {
		private double x, y;
		public Coordinate(double nX, double nY) { x = nX; y = nY; }
		public void set(double nX, double nY) { x = nX; y = nY; }
		public double getX() { return x; }
		public double getY() { return y; }
		public double getDist(Coordinate c) { 
			return Math.sqrt(Math.pow(x-c.x,2) + Math.pow(y-c.y,2));
		}
	}
	private BeaconVector bv;
	private Coordinate[] cd;
	private Coordinate cur; //Current coordinate 
	private BeaconTracker tracker;
	
	public LocationTopology(BeaconVector nBv, BeaconTracker nTracker)
	{
		bv = nBv;
		tracker = nTracker;
		int size = bv.getSize();
		cd = new Coordinate[size];
		for(int ind = 0; ind < size; ind++)
			cd[ind] = new Coordinate(0,0);
		cur = new Coordinate(0,0);
	}
	
	public Coordinate getCoordinate(Region beacon)
	{
		int ind = bv.indexOf(beacon);
		return (ind >= 0) ? cd[ind] : null;
	}
	
	public boolean setCoordinate(Region beacon, double x, double y)
	{
		int ind = bv.indexOf(beacon);
		if(ind == -1) return false;
		cd[ind].set(x, y);
		return true;
	}
	
	public Coordinate getResult()
	{
		DistanceVector dv = tracker.getAvgDist(bv);
		ArrayList<Coordinate> coord = new ArrayList<Coordinate>();
		ArrayList<Double> dist = new ArrayList<Double>();
		for(int ind = 0; ind < dv.getSize(); ind ++) {
			if(!dv.get(ind).isInfinite()) {
				coord.add(cd[ind]);
				dist.add(dv.get(ind));
			}
		}
		int size = dist.size(); 
		ArrayList<Double> xl = new ArrayList<Double>();
		ArrayList<Double> yl = new ArrayList<Double>();
		

	}
}
