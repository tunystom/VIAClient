package cz.cvut.fel.via.client;

/* The following code is based on the code written 
 * by Matthew Wiggins and so as it's original it is
 * released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import com.droidgesture.sensors.AccelerometerListener;

public class ShakeDetector implements AccelerometerListener
{
    private static final int FORCE_THRESHOLD = 350;
    private static final int TIME_THRESHOLD  = 100;
    private static final int SHAKE_TIMEOUT   = 500;
    private static final int SHAKE_DURATION  = 1000;
    private static final int SHAKE_COUNT     = 3;
    
    private float mLastX = -1.0f ;
    private float mLastY = -1.0f ;
    private float mLastZ = -1.0f;
    private long mLastTime;
   
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;
    
    ClientMainActivity mMainActivity ;
    
    public ShakeDetector(ClientMainActivity activity)
    {
        mMainActivity = activity ;
    }

    @Override public void onAccelerationChanged(float x, float y, float z, long t)
    {
        long now = System.currentTimeMillis();

        if ((now - mLastForce) > SHAKE_TIMEOUT)
        {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD)
        {
            long diff = now - mLastTime;
            float speed = Math.abs(x - mLastX + y - mLastY + z - mLastZ) / diff * 10000;
            if (speed > FORCE_THRESHOLD)
            {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION))
                {
                    mLastShake = now;
                    mShakeCount = 0;
                    
                    mMainActivity.syncMessagesImediately();
                }
                mLastForce = now;
            }
            
            mLastTime = now;
            mLastX = x;
            mLastY = y;
            mLastZ = z;
        }
    }
}
