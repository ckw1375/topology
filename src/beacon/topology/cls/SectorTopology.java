package beacon.topology.cls;

import java.util.*;
import android.util.*;

public class SectorTopology {
	private class SampleList {
		private ArrayList<RssiVector> sp;
		public SampleList() {
			sp = new ArrayList<RssiVector>();
		}
		public void addSample(RssiVector s) {
			sp.add(s);
		}
		public RssiVector getSample(int ind) {
			if(ind < 0 || ind >= sp.size()) return null;
			return sp.get(ind);
		}
		public void clearSample() {
			sp.clear();
		}
		public int getSampleNumber() {
			return sp.size();
		}
	}
	private HashMap<String,SampleList> sectors;
	private BeaconVector bv;
	private BeaconTracker tracker;
	private static final int K = 5; //Parameter for KNN algorithm
	
	public SectorTopology(BeaconVector nBv, BeaconTracker nTracker)	{
		sectors = new HashMap<String,SampleList>();
		bv = nBv;
		tracker = nTracker;
	}
	//Add a new sector with the sector name 'sectName'. 
	//Successful (return true) only when there is no sector with the same name.
	public boolean addSector(String sectName) {
		if(sectors.containsKey(sectName) == false) {
			sectors.put(sectName,new SampleList());
			return true;
		} 
		return false;			
	}
	public void deleteAllSectors() {
		sectors.clear();
	}
	//Add a new sample to the sector with a given name. 
	public boolean addSample(String sectName) {
		SampleList sl = sectors.get(sectName);
		if(sl == null) return false;
		RssiVector rv = tracker.getAvgRssi(bv);
		sl.addSample(rv);
		return true;
	}
	//Clear all samples of the sector with a given name. 
	public boolean clearSample(String sectName) {
		SampleList sl = sectors.get(sectName);
		if(sl == null) return false;
		sl.clearSample();
		return true;
	}
	//Get the number of samples of the sector with a given name. 
	//In case of an error, return -1. 
	public int getSampleNumber(String sectName) {
		SampleList sl = sectors.get(sectName);
		if(sl == null) return -1;
		return sl.getSampleNumber();
	}
	//Return the sector name of the sector that the user is currently in (by using the KNN algorithm)
	//In case of an error, return null.
	public String getResult()
	{
		if(sectors.size() == 0) return null;
		RssiVector cv = tracker.getAvgRssi(bv);
		ArrayList<Pair<Double,String>> l = new ArrayList<Pair<Double,String>>();
		for(Map.Entry<String,SampleList> entry : sectors.entrySet()) {
			String sn = entry.getKey();
			SampleList sl = entry.getValue();
			for(int ind = 0; ind < sl.getSampleNumber(); ind ++) {
				Pair<Double,String> p = new Pair<Double,String>(cv.sqDist(sl.getSample(ind)),sn);
				l.add(p);
			}			
		}
		Collections.sort(l, new Comparator<Pair<Double,String>>() { 
			public int compare(Pair<Double,String> lhs, Pair<Double,String> rhs) {
				return Double.compare(lhs.first, rhs.first);
			}
		});
		if(l.get(0).first.isInfinite()) return null;
		String selected = null;
		int maxCount = 0;
		for(Map.Entry<String,SampleList> entry : sectors.entrySet()) {
			String sn = entry.getKey();
			int count = 0;
			for(int k = 0; k < Math.min(K, l.size()); k++) {
				if(l.get(k).first.isInfinite()) break;
				if(l.get(k).second == sn) count ++;
			}
			if(count >= maxCount) {
				selected = sn;
				maxCount = count;
			}
		}
		return selected;
	}
}
