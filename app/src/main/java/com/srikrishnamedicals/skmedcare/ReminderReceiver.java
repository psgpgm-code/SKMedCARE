package com.srikrishnamedicals.skmedcare;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
  public static void schedule(Context c,String key,String title,String body,long at,boolean repeatDaily){
    AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    Intent i=new Intent(c,ReminderReceiver.class);i.putExtra("key",key);i.putExtra("title",title);i.putExtra("body",body);i.putExtra("repeat",repeatDaily);
    int req=key.hashCode()&0x7fffffff;
    PendingIntent pi=PendingIntent.getBroadcast(c,req,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    try{
      if(Build.VERSION.SDK_INT>=23)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);
      else am.set(AlarmManager.RTC_WAKEUP,at,pi);
    }catch(SecurityException ex){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);}
  }
  @Override public void onReceive(Context c,Intent i){
    String key=i.getStringExtra("key"),title=i.getStringExtra("title"),body=i.getStringExtra("body");
    Notification n;
    if(Build.VERSION.SDK_INT>=26)n=new Notification.Builder(c,"skmedcare_reminders").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(body).setAutoCancel(true).build();
    else n=new NotificationCompat.Builder(c).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(body).setAutoCancel(true).build();
    ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(key.hashCode(),n);
    if(i.getBooleanExtra("repeat",false))schedule(c,key,title,body,System.currentTimeMillis()+24L*60*60*1000,true);
  }
}
