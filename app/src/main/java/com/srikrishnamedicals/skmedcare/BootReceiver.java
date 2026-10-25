package com.srikrishnamedicals.skmedcare;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){
   SharedPreferences sp=c.getSharedPreferences(MainActivity.PREF,Context.MODE_PRIVATE);
   String payload=sp.getString("payload","");
   if(!payload.isEmpty())MainActivity.scheduleAll(c,payload);
 }
}
