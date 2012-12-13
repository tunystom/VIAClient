package com.droidgesture.sensors;

/**
 * The Accelerometer Listener
 * 
 * @author Tomas Tunys
 */
public interface AccelerometerListener
{
	/**
	 * Invoked when a change in acceleration has been detected.
	 * 
	 *  @param x The change in x-axis.
	 *  @param y The change in y-axis.
	 *  @param z The change in z-axis.
	 *  @param t The timestamp of the event.
	 */
  public void onAccelerationChanged(float x, float y, float z, long t) ;
}
