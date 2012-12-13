package cz.cvut.fel.via.client;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ClientPreferences
{
    public static final String PREFS_FILE_NAME = "viaclient-preferences";

    public static final SharedPreferences getPreferences(Context applicationContext)
    {
      return applicationContext.getSharedPreferences(PREFS_FILE_NAME, Activity.MODE_PRIVATE) ;
    }
}
