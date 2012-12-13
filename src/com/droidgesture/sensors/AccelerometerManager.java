package com.droidgesture.sensors ;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

/**
 * The Accelerometer Manager.
 *
 * @author Tomas Tunys
 */
public class AccelerometerManager
{
	/**
	 * The sensor event listener that listens to events from the accelerometer sensor.
	 */
	private SensorEventListener mSensorEventListener = new SensorEventListener()
	{			
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
			// Ignored!
		}

		public void onSensorChanged(SensorEvent event)
		{
			fireAccelerationChangedEvent(event.values[0], event.values[1], event.values[2], event.timestamp) ;
		}
		
		/**
		 * Invokes the {@link AccelerometerListener#onAccelerationChanged(float, float, float)} 
		 * in all registered accelerometer listeners with the specified parameters.
		 * 
		 * @param x The change in x-axis.
		 * @param y The change in y-axis.
		 * @param z The change in z-axis.
		 * @param t The timestamp of the event.
		 */
		private void fireAccelerationChangedEvent(float x, float y, float z, long t)
		{
			for (AccelerometerListener listener : mListenerList)
			{
				listener.onAccelerationChanged(x, y, z, t) ;
			}
		}
	} ;
	
	/** The application context in which the manager is created. */
	private Context mContext ;
		
	/** Indicates the manager is listening for acceleration events. */
	private boolean mListening = false ;
	
	/** The list of registered accelerometer listeners with this manager. */
	private ArrayList<AccelerometerListener> mListenerList = new ArrayList<AccelerometerListener>() ;

	/**
	 * Creates a new AccelerometerManager that listens for events from the specified
	 * accelerometer sensor.
	 * 
	 * @param context The context for which is the manager created.
	 * @param manager The sensor manager.
	 * @param sensor  The accelerometer sensor.
	 */
	private AccelerometerManager(Context context, SensorManager manager, Sensor sensor)
	{
		mContext = context.getApplicationContext() ;
		
		if (sensor == null)
		{
			throw new IllegalStateException("The accelerometer cannot be null.") ;
		}
	}

	/**
	 * Returns an instance of an AccelerometerManager that may
	 * be used to bind listeners to an accelerometer sensor. 
	 * 
	 * @param context An application context.
	 * 
	 * @return An instance of AccelerometerManager.
	 */
	public static AccelerometerManager getInstance(Context context)
	{
			if (context != null)
			{
				SensorManager manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE) ;
				
				if (manager != null)
				{
					List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER) ;
					
					if (!sensors.isEmpty())
					{
						try
						{
							return new AccelerometerManager(context, manager, sensors.get(0)) ;
						}
						catch (Exception exception)
						{
							return null ;
						}
					}
				}
			}
			
			return null ;
	}
	
	/**
	 * Returns an Sensor object representing either Sensor.TYPE_LINER_ACCELERATION, if
	 * linear is {@code true}, or Sensor.TYPE_ACCELEROMETER, in an other way.
	 * 
	 *  @param context The context.
	 *  @param linear  Indicates whether to retrieve a linear type acceleration 
	 *                 sensor, or not.
	 */
	public static Sensor getAccelerometerSensor(Context context, boolean linear)
	{
		if (linear && Integer.parseInt(Build.VERSION.SDK) <= 8)
		{
			IllegalStateException exception = new IllegalStateException("The linear acceleration is not supported in API Level " + Build.VERSION.SDK) ;
			
			Log.e("DroidGestureCollector", "The linear acceleration is not supported in API Level " + Build.VERSION.SDK, exception) ;
			
			throw exception ;
		}
		
		if (context != null)
		{
			SensorManager manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE) ;
			
			if (manager != null)
			{
				List<Sensor> sensors = manager.getSensorList(linear ? Sensor.TYPE_LINEAR_ACCELERATION : Sensor.TYPE_ACCELEROMETER) ;
				
				if (!sensors.isEmpty())
				{
					return sensors.get(0) ;
				}
			}
		}
		
		return null ;
	}
	
	/**
	 * Makes the manager listen to a Sensor.TYPE_LINEAR_ACCELERATION sensor, if linear
	 * is {@code true}, or to a Sensor.TYPE_ACCELEROMETER, otherwise. All listeners
	 * will be send samples from the sensor in the specified {@code rate}.
	 * 
	 * @param rate   The accelerometer sampling rate. The value must be one of SENSOR_DELAY_NORMAL,
	 *               SENSOR_DELAY_UI, SENSOR_DELAY_GAME, or SENSOR_DELAY_FASTEST, or the desired
	 *               delay between samples in microsecond.
	 * @param linear Indicates whether the manager will listen to a linear type 
	 *               acceleration sensor, or not.
	 */
	public boolean startListening(int rate, boolean linear)
	{
		if (linear && Integer.parseInt(Build.VERSION.SDK) <= 8)
		{
			IllegalStateException exception = new IllegalStateException("The linear acceleration is not supported in API Level " + Build.VERSION.SDK) ;
			
			Log.e(AccelerometerManager.class.getName(), "The linear acceleration is not supported in API Level " + Build.VERSION.SDK, exception) ;
			
			throw exception ;
		}
		
		if (!mListening)
		{
			SensorManager manager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE) ;
			
			if (manager != null)
			{
				List<Sensor> sensors = manager.getSensorList(linear ? Sensor.TYPE_LINEAR_ACCELERATION : Sensor.TYPE_ACCELEROMETER) ;
				
				if (!sensors.isEmpty())
				{
					mListening = manager.registerListener(mSensorEventListener, sensors.get(0), rate) ;
				}
			}
		}
		
		return mListening ;
	}
	
	/**
	 * Returns {@code true} if the manager listens to
	 * an accelerometer sensor, otherwise, returns {@code false}.
	 */
	public boolean isListening()
	{
		return mListening ;
	}
	
	/**
	 * Stops the manager from listening to an accelerometer sensor.
	 * No listener will be sent any more samples.
	 */
	public void stopListening()
	{
		if (mListening)
		{
			SensorManager manager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE) ;
			
			if (manager != null)
			{
				manager.unregisterListener(mSensorEventListener) ;
				
				mListening = false ;
			}
		}
	}
	
	/**
	 * Stops the listening to an accelerometer sensor 
	 * just in case it has not been already stopped.
	 */
	@Override protected void finalize() throws Throwable
	{
		stopListening() ;
		
		super.finalize() ;
	}

	/**
	 * Registers the specified accelerometer listener with this manager.
	 * 
	 * @param listener The accelerometer listener.
	 */
	public void addAccelerometerListener(AccelerometerListener listener)
	{
			mListenerList.add(listener) ;
	}
	
	/**
	 * Unregisters the specified accelerometer listener with this manager.
	 * 
	 * @param listner The accelerometer listener.
	 */
	public void removeAccelerometerListener(AccelerometerListener listener)
	{
			mListenerList.remove(listener) ;
	}
}
