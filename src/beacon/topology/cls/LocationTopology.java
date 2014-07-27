package beacon.topology.cls;

import java.util.*;

public class LocationTopology {
	public class Coordinate {
		private double x, y;
		public Coordinate(double nX, double nY) { x = nX; y = nY; }
		public void setX(double nX) { x = nX; }		
		public void setY(double nY) { y = nY; }
		public double getX() { return x; }
		public double getY() { return y; }
		public double getDist(Coordinate c) { 
			return Math.sqrt(Math.pow(x-c.x,2) + Math.pow(y-c.y,2));
		}
	}
	private HashMap<Region,Coordinate> coordMap; //Map for coordinates
	private BeaconTracker tracker;
	
	public LocationTopology(ArrayList<Region> beacons, BeaconTracker nTracker)
	{
		coordMap = new HashMap<Region,Coordinate>();
		for(Region it : beacons)
			coordMap.put(it,new Coordinate(0,0));
		tracker = nTracker;
	}
	
	public Coordinate getCoordinate(Region beacon)
	{
		return coordMap.containsKey(beacon) ? coordMap.get(beacon) : null;
	}
	
	public void setCoordinate(Region beacon, Coordinate coord)
	{
		if(coordMap.containsKey(beacon)) coordMap.put(beacon, coord);
	}
	
	public Coordinate getResult()
	{
		Coordinate result = new Coordinate(0,0);
		return result;
	}
}
