package iskander.quadcopternav;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.hardware.SensorManager;
import android.hardware.Sensor;


public class QuadcopterActivity extends Activity implements LocationListener {
	private static final int REQUEST_ENABLE_BT = 1;
	private TextView latituteField;
	private TextView longitudeField;
	private LocationManager locationManager;
	private Location destination, start, curLocation;
	private Double curLatitude, curLongitude;
	private String provider;
	private Handler myHandler = new Handler();
	private SensorManager sManager;

	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadcopter);
        
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        BluetoothDevice arduino = mBluetoothAdapter.getRemoteDevice("HT-06");

        
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /* CAL METHOD requestLocationUpdates */
        provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 0, 0, this);
        start = locationManager.getLastKnownLocation(provider);

        //Get start location
        curLocation = start;
        if (curLocation != null) {
        	curLatitude = curLocation.getLatitude();
        	curLongitude = curLocation.getLongitude();
        	}
        
        sManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Read destination. Store received location in destination.
        readDestination();
        
        //Lift off: Send throttle command for 10 seconds via bluetooth.
        liftOff();
        //Orient towards destination.
        rotate(destination);
        //Move forward till you've reached destination.
        moveToDestination(destination);
        //Orient towards start location.
        rotate(start);
        //Move forward until start location has been reached.
        moveToDestination(start);
        //Land.
        land();
        
        // Turn off sensors and end program.
    }

    private void outputCommand(Commands command) {
    	
    	// 0 = -100%, 499 = -1%, 500 = 0%, 999 = 100%
    	switch(command) {
    	case ROLL:
    		break;
    	case FORWARD:
    		//send command pitch 25% : 2---
    		break;
    	case BACKWARD:
    		//send command pitch -25% : 2---
    	case LIFT:
    		//send command throttle 65% : 3---
    		break;
    	case LOCKHEIGHT:
    		//send command throttle 50% : 3---
    		break;
    	case LAND:
    		//send command throttle 40% : 3---
    		break;
    	case TURNLEFT:
    		//send command turn -15% : 4---
    		break;
    	case TURNRIGHT:
    		//send command turn 15% : 4---
    		break;
    	default:
    		break;
    	}
    }
    
    private void readDestination() {
    	//Wait until text message with destination is received.
    	//If 1 minute passes exit program.
    	//Parse text
    	//Create new location object and store in destination;
    	//FIXME
    	IncomingSms messageObj = new IncomingSms();
    	String message = messageObj.getMessage();
    	double[] coords = new double[2];
    	String pattern = "(\\d*\\.\\d*),*(\\d*\\.\\d*)";
    	Pattern r = Pattern.compile(pattern);
    	Matcher m = r.matcher(message);
    	if (m.find()) {
    		coords[0] = Integer.parseInt(m.group(0));
    		coords[1] = Integer.parseInt(m.group(1));
    	} else {
    		System.out.println("NO COORDINATES");
    	}
    	destination = new Location(provider);
    	destination.setLatitude(coords[0]);
    	destination.setLongitude(coords[1]);
    	//FIX DONT KNOW WHAT WILL HAPPEN WHEN NO MESSAGE
    	//Fix to wait 1 minute for message then output error.
    }
    
    private void liftOff() {
    	outputCommand(Commands.LIFT);
    	//Wait 6 seconds
    	myHandler.postDelayed(new Runnable() {
            public void run() {
            }
        }, 6000);
    	outputCommand(Commands.LOCKHEIGHT);
    }
    
    private void rotate(Location location) {
    	float[] orientation = new float[3];
    	float[] r = new float[3];
    	float[] i = new float[3];
    	float[] g = new float[3];
    	float[] gm = new float[3];
    	while (orientation[0] == -1 || (Math.abs(orientation[0] - curLocation.bearingTo(location)) >  0.0000005)) {
    		SensorManager.getRotationMatrix(r, i, g, gm);
        	SensorManager.getOrientation(r, orientation);
        	outputCommand(Commands.TURNLEFT);
        	myHandler.postDelayed(new Runnable() {
                public void run() {
                }
            }, 15);
    	}
 	
    }
    
    private void moveToDestination(Location location) {
    	while (curLocation.distanceTo(destination) >= 5 ) {
    		outputCommand(Commands.FORWARD);
    		myHandler.postDelayed(new Runnable() {
                public void run() {
                }
            }, 15);
    	}
    	outputCommand(Commands.LOCKHEIGHT);
    }
    
    private void land() {
    	outputCommand(Commands.LAND);
    }
    
	@Override
	public void onLocationChanged(Location arg0) {
		curLocation = arg0;
		curLatitude = arg0.getLatitude();
		curLongitude = arg0.getLongitude();	
		String str = "Lat: "+curLatitude+" Long: "+curLongitude;
		Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}



    
}
