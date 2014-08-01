package beacon.topology.demo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import beacon.topology.R;
import beacon.topology.cls.*;

public class LeDeviceListAdapter extends BaseAdapter {

  private ArrayList<Beacon> beacons;
  private LayoutInflater inflater;
  int coloredPosition;

  public LeDeviceListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    this.beacons = new ArrayList<Beacon>();
    this.coloredPosition = -1;
  }

  public void setColor(int position) {
	  this.coloredPosition = position;
	  notifyDataSetChanged();
  }
  
  public void replaceWith(Collection<Beacon> newBeacons) {
    this.beacons.clear();
    this.beacons.addAll(newBeacons);
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return beacons.size();
  }

  @Override
  public Beacon getItem(int position) {
    return beacons.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    view = inflateIfRequired(view, position, parent);
    bind(getItem(position), position, view);
    return view;
  }

  private void bind(Beacon beacon, int position, View view) {
    ViewHolder holder = (ViewHolder) view.getTag();
    if(beacon != null)
    {
    	holder.macTextView.setText("MAC: " + beacon.getMacAddress());
    	holder.uuidTextView.setText("UUID: " + beacon.getProximityUUID());
    	holder.majorTextView.setText("Major: " + beacon.getMajor());
    	holder.minorTextView.setText("Minor: " + beacon.getMinor());
    	holder.distanceTextView.setText(String.format("Distance: %.2f", beacon.getDistance()));
    	holder.measuredPowerTextView.setText(String.format("TxPower: %.2f", beacon.getMeasuredPower()));
    	holder.rssiTextView.setText(String.format("RSSI: %.2f", beacon.getRssi()));
    } else {
    	holder.macTextView.setText("MAC: " + "Not Found");
    	holder.uuidTextView.setText("UUID: " + "Not Found");
    	holder.majorTextView.setText("Major: " + "Not Found");
    	holder.minorTextView.setText("Minor: " + "Not Found");
    	holder.distanceTextView.setText("Distance: " + "Not Found");
    	holder.measuredPowerTextView.setText("TxPower: " + "Not Found");
    	holder.rssiTextView.setText("RSSI: " + "Not Found");
    }
    if(position == coloredPosition) view.setBackgroundColor(Color.YELLOW);
    else view.setBackgroundColor(Color.WHITE);
  }

  private View inflateIfRequired(View view, int position, ViewGroup parent) {
    if (view == null) {
      view = inflater.inflate(R.layout.device_item, null);
      view.setTag(new ViewHolder(view));
    }
    return view;
  }

  static class ViewHolder {
    final TextView macTextView;
    final TextView uuidTextView;
    final TextView majorTextView;
    final TextView minorTextView;
    final TextView distanceTextView;
    final TextView measuredPowerTextView;
    final TextView rssiTextView;

    ViewHolder(View view) {
      macTextView = (TextView) view.findViewWithTag("mac");
      uuidTextView = (TextView) view.findViewWithTag("uuid");
      majorTextView = (TextView) view.findViewWithTag("major");
      minorTextView = (TextView) view.findViewWithTag("minor");
      distanceTextView = (TextView) view.findViewWithTag("distance");
      measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
      rssiTextView = (TextView) view.findViewWithTag("rssi");
    }
  }
}
