package beacon.topology.cls;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Array;

public class LocationTopology {
	public class Coordinate {
		private double x, y;
		public Coordinate(double nX, double nY) { x = nX; y = nY; }
		public void set(double nX, double nY) { x = nX; y = nY; }
		public double getX() { return x; }
		public double getY() { return y; }
		public double getDist(Coordinate c) { 
			return Math.sqrt(Math.pow(this.x-c.x,2) + Math.pow(this.y-c.y,2));
		}
		public Coordinate minus(Coordinate c) {
			return new Coordinate(this.x-c.x,this.y-c.y);
		}
	}
	private BeaconVector bv;
	private Coordinate[] cd;
	private Coordinate cur; //Current coordinate 
	private BeaconTracker tracker;
	private long lastUpdate; //in nanoseconds
	
	private final static double ITER_PER_SEC = 10D; //Number of Quasi-Newton iterations for a second
	private final static int MAX_ITER = 100; //Maximum number of iterations in one update
	
	public LocationTopology(BeaconVector nBv, BeaconTracker nTracker)
	{
		bv = nBv;
		tracker = nTracker;
		int size = bv.getSize();
		cd = new Coordinate[size];
		for(int ind = 0; ind < size; ind++)
			cd[ind] = new Coordinate(0,0);
		cur = new Coordinate(0,0);
		lastUpdate = -1;
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
		if(dist.size() == 0) return null;
		int numIter;
		long curTime = System.nanoTime();
		if(lastUpdate == -1) {
			numIter = MAX_ITER;
		} else {
			double timeDiffMillis = (double)TimeUnit.NANOSECONDS.toMillis(curTime - lastUpdate);
			numIter = Math.min(MAX_ITER, Math.max(1, (int)(ITER_PER_SEC * timeDiffMillis / 1000D)));
		}
		for(int it = 0; it < numIter; it ++) {
			quasiNewton(coord, dist);
		}
		return new Coordinate(cur.getX(), cur.getY());
	}
	
	private void quasiNewton(ArrayList<Coordinate> coord, ArrayList<Double> dist) {
		int size = dist.size(); 
		double[] xJ = new double[size];
		double[] yJ = new double[size];
		double[] err = new double[size];
		for(int ind = 0; ind < size; ind ++) {
			double d = cur.getDist(coord.get(ind));
			Coordinate t = cur.minus(coord.get(ind));
			xJ[ind] = t.getX()/d;
			yJ[ind] = t.getY()/d;
			err[ind] = d - dist.get(ind);
		}
		//Calculating matrix [a b; b d]
		double a = vecMul(xJ,xJ);
		double b = vecMul(xJ,yJ);
		double d = vecMul(yJ,yJ);
		//Calculating vector [e f]
		double e = vecMul(xJ,err);
		double f = vecMul(yJ,err);
		//Calculating delta
		double det = a*d - b*b;
		double deltaX = (d*e - b*f)/det;
		double deltaY = (a*f - b*e)/det;
		//Update current position
		double newX = cur.getX() - deltaX;
		double newY = cur.getY() - deltaY;
		cur.set(newX, newY);		
	}
	//Vector multiplication
	private double vecMul(double[] a, double[] b) {
		int size = Array.getLength(a);
		double result = 0;
		for(int ind = 0; ind < size; ind ++) result += a[ind]*b[ind];
		return result;
	}
}





