package beacon.topology.demo;

import beacon.topology.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.listing_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(MainActivity.this, BeaconListingActivity.class);
              startActivity(intent);
            }
          });  
        findViewById(R.id.proximity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(MainActivity.this, ProximityTopologyActivity.class);
              startActivity(intent);
            }
          });    
        findViewById(R.id.sector_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(MainActivity.this, SectorTopologyActivity.class);
              startActivity(intent);
            }
          });
        findViewById(R.id.location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(MainActivity.this, LocationTopologyActivity.class);
              startActivity(intent);
            }
          });
    }
}