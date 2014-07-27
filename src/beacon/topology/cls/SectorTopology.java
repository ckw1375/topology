package beacon.topology.cls;

import java.util.*;

public class SectorTopology {
	public class Sample {
		private ArrayList<Double> rssiList;
		private int size;
		public Sample(int nSize) { 
			size = nSize;
			rssiList = new ArrayList<Double>(size);			
		}
		public int getSize() { return size; }
		public Double getRssi(int index) { return rssiList.get(index); }
		public void setRssi(int index, Double rssi) { rssiList.set(index, rssi); }
		public Double sqDist(Sample s) {
			if(this.getSize() != s.getSize()) return null;
			Double result = 0D;
			for(int i=0; i<size; i++) {
				result += Math.pow(this.getRssi(i)-s.getRssi(i), 2);
			}
			return result;			
		}		
	}
	public class SampleList {
		private ArrayList<Sample> spList;
		public SampleList() {
			spList = new ArrayList<Sample>();
		}
		public void addSample(Sample s) {
			spList.add(s);
		}
		public void clearSample() {
			spList.clear();
		}
		public int getSampleNumber() {
			return spList.size();
		}
	}
	public class Sector {
		private String name;
		public SampleList samples;
		public Sector(String nName) { 
			name = nName;
			samples = new SampleList();
		}
		public String getName() { return name; }
		public void setName(String nName) { name = nName; }
	}
	private ArrayList<Sector> sectorList;
	private ArrayList<Region> beaconList;
	private int beaconNumber;
	private BeaconTracker tracker;
	
	public SectorTopology(ArrayList<Region> beacons, ArrayList<String> sectorNames, BeaconTracker nTracker)	{
		sectorList = new ArrayList<Sector>();
		for(String str : sectorNames) {
			sectorList.add(new Sector(str));
		}
		beaconList = beacons;
		beaconNumber = beaconList.size();			
		tracker = nTracker;
	}
	
	public void addSample(int sectIdx) {
	}
	
	public void clearSample(int sectIdx) {
		sectorList.get(sectIdx).samples.clearSample();
	}
	
	public int getSampleNumber(int sectIdx) {
		return sectorList.get(sectIdx).samples.getSampleNumber();
	}
		
	public int getResult()
	{

	}
}
