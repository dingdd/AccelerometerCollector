package beacon.accelerometer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import beacon.util.Storage;

public class CollectorActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor lSensor, gSensor, mSensor;
	private OutputStream lOs, gOs, mOs, bOs, gpsOs;
	private File lf, gf, mf, bf, gpsf;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TextView textview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collector);
		initSensors();
		initFolds();
		textview = (TextView) findViewById(R.id.textView1);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_collector, menu);
		return true;
	}

	public void stopRecord(View view) {
		this.finish();
	}

	public void bump(View view) {
		String s = "";
		s += System.currentTimeMillis() + "\n";
		try {
			bOs.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		initStreams();
		registSensors();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregistSensors();
		releaseStreams();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void initSensors() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		lSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		List<Sensor> l = sensorManager.getSensorList(Sensor.TYPE_ALL);
		String s;
		for (Iterator<Sensor> it = l.iterator(); it.hasNext();) {
			s = it.next().getName();
			Log.v("sensors", s);
		}
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				StringBuffer sb = new StringBuffer();
				sb.append("").append(location.getLatitude() + "\t")
						.append(location.getLongitude() + "\t")
						.append(location.getTime() + "\t")
						.append(System.currentTimeMillis() + "\n");
				textview.setText(sb.toString());
				try {
					gpsOs.write(sb.toString().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};
	}

	public void initFolds() {
		gf = Storage.getStorageDir("sensorData");
		lf = Storage.getStorageDir("sensorData");
		mf = Storage.getStorageDir("sensorData");
		bf = Storage.getStorageDir("bumps");
		gpsf = Storage.getStorageDir("gps");
	}

	public void initStreams() {
		long timestamp = System.currentTimeMillis();
		gOs = Storage.initStorageFiles(gf, "gra", timestamp);
		lOs = Storage.initStorageFiles(lf, "lin", timestamp);
		mOs = Storage.initStorageFiles(mf, "mag", timestamp);
		bOs = Storage.initStorageFiles(bf, "bump", timestamp);
		gpsOs = Storage.initStorageFiles(gpsf, "gps", timestamp);
	}

	public void releaseStreams() {
		try {
			lOs.close();
			gOs.close();
			mOs.close();
			bOs.close();
			gpsOs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registSensors() {
		sensorManager.registerListener(this, lSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, gSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
	}

	public void unregistSensors() {
		sensorManager.unregisterListener(this, lSensor);
		sensorManager.unregisterListener(this, gSensor);
		sensorManager.unregisterListener(this, mSensor);
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
		OutputStream os = null;
		if (type == Sensor.TYPE_LINEAR_ACCELERATION)
			os = lOs;
		else if (type == Sensor.TYPE_MAGNETIC_FIELD)
			os = mOs;
		else if (type == Sensor.TYPE_GRAVITY)
			os = gOs;
		if (os == null)
			return;
		String s = "" + event.values[0] + "\t\t" + event.values[1] + "\t\t"
				+ event.values[2] + "\t\t" + event.timestamp + "\t\t"
				+ System.currentTimeMillis() + "\n";
		try {
			os.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}