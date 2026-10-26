package com.srikrishnamedicals.skmedcare;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;

public class MainActivity extends Activity {
  WebView web;
  private ValueCallback<Uri[]> fileChooserCallback;
  private static final int FILE_CHOOSER_REQUEST = 7314;
  static final String PREF="skmedcare_alarms";

  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    createChannel();
    if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
      requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},100);
    web=new WebView(this);
    WebSettings s=web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(true);
    web.setWebChromeClient(new WebChromeClient(){
      @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params){
        if(fileChooserCallback!=null){
          fileChooserCallback.onReceiveValue(null);
          fileChooserCallback=null;
        }
        fileChooserCallback=callback;
        try{
          Intent intent=params.createIntent();
          startActivityForResult(intent,FILE_CHOOSER_REQUEST);
          return true;
        }catch(Exception ex){
          fileChooserCallback=null;
          callback.onReceiveValue(null);
          return false;
        }
      }
    });
    web.setWebViewClient(new WebViewClient() {
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url == null) return false;

    if (url.startsWith("whatsapp://")) {
      try {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
      } catch (Exception e) {
        try {
          String fallback = url.replace(
              "whatsapp://send/", "https://wa.me/"
          ).replace("whatsapp://send", "https://wa.me/");
          Intent intent = new Intent(
              Intent.ACTION_VIEW, Uri.parse(fallback)
          );
          startActivity(intent);
        } catch (Exception ignored) {
          view.loadUrl("https://wa.me/");
        }
      }
      return true;
    }

    if (url.startsWith("https://wa.me/") ||
        url.startsWith("https://api.whatsapp.com/")) {
      try {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
      } catch (Exception e) {
        view.loadUrl(url);
      }
      return true;
    }

    return false;
  }

  @Override
  public boolean shouldOverrideUrlLoading(
      WebView view, android.webkit.WebResourceRequest request) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return shouldOverrideUrlLoading(
          view, request.getUrl().toString()
      );
    }
    return false;
  }
});
    web.addJavascriptInterface(new ReminderBridge(this),"AndroidReminderBridge");
    setContentView(web);
    web.loadUrl("file:///android_asset/index.html");
  }

  void createChannel(){
    if(Build.VERSION.SDK_INT>=26){
      NotificationChannel ch=new NotificationChannel("skmedcare_reminders","SKMedCARE Reminders",NotificationManager.IMPORTANCE_HIGH);
      ch.setDescription("Medicine-time and refill reminders");
      ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
    }
  }

  public static void scheduleAll(Context c,String json){
    try{
      JSONArray arr=new JSONArray(json);
      SharedPreferences sp=c.getSharedPreferences(PREF,MODE_PRIVATE);
      sp.edit().putString("payload",json).apply();
      AlarmManager am=(AlarmManager)c.getSystemService(ALARM_SERVICE);
      for(int i=0;i<arr.length();i++){
        JSONObject p=arr.getJSONObject(i);
        String id=p.optString("id","p"+i);
        JSONObject times=p.optJSONObject("times"), nt=p.optJSONObject("notifyTimes");
        if(times!=null && nt!=null){
          String[][] slots={{"morning","morning"},{"afternoon","afternoon"},{"evening","evening"},{"night","night"}};
          for(String[] slot:slots) if(times.optBoolean(slot[0],false))
            setDaily(c,am,id+"_"+slot[0],p.optString("customerName")+" • "+p.optString("medicine"),"மருந்து சாப்பிடும் நேரம்: "+slot[0],nt.optString(slot[1],"08:00"));
        }
        String next=p.optString("nextDate","");
        if(!next.isEmpty()) setDate(c,am,id+"_refill",p.optString("customerName")+" • Refill Reminder",p.optString("medicine")+" மீண்டும் வாங்க வேண்டிய நாள்",next,nt==null?"09:00":nt.optString("refill","09:00"));
      }
    }catch(Exception ignored){}
  }

  static void setDaily(Context c,AlarmManager am,String key,String title,String body,String hhmm){
    try{
      String[] t=hhmm.split(":");
      Calendar cal=Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(t[0]));
      cal.set(Calendar.MINUTE,Integer.parseInt(t[1]));
      cal.set(Calendar.SECOND,0);
      cal.set(Calendar.MILLISECOND,0);
      if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_YEAR,1);
      ReminderReceiver.schedule(c,key,title,body,cal.getTimeInMillis(),true);
    }catch(Exception ignored){}
  }

  static void setDate(Context c,AlarmManager am,String key,String title,String body,String date,String hhmm){
    try{
      String[] d=date.split("-"),t=hhmm.split(":");
      Calendar cal=Calendar.getInstance();
      cal.set(Integer.parseInt(d[0]),Integer.parseInt(d[1])-1,Integer.parseInt(d[2]),Integer.parseInt(t[0]),Integer.parseInt(t[1]),0);
      cal.set(Calendar.MILLISECOND,0);
      if(cal.getTimeInMillis()>System.currentTimeMillis())ReminderReceiver.schedule(c,key,title,body,cal.getTimeInMillis(),false);
    }catch(Exception ignored){}
  }

  public static class ReminderBridge {
    Context c;
    ReminderBridge(Context x){c=x.getApplicationContext();}
    @JavascriptInterface public void syncReminders(String json){scheduleAll(c,json);}
    @JavascriptInterface public void openExactAlarmSettings(){
      if(Build.VERSION.SDK_INT>=31){
        Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(i);
      }
    }
  }

  @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
    super.onActivityResult(requestCode,resultCode,data);
    if(requestCode==FILE_CHOOSER_REQUEST){
      if(fileChooserCallback==null)return;
      Uri[] results=null;
      if(resultCode==RESULT_OK && data!=null){
        if(data.getClipData()!=null){
          int count=data.getClipData().getItemCount();
          results=new Uri[count];
          for(int i=0;i<count;i++)results[i]=data.getClipData().getItemAt(i).getUri();
        }else if(data.getData()!=null){
          results=new Uri[]{data.getData()};
        }
      }
      fileChooserCallback.onReceiveValue(results);
      fileChooserCallback=null;
    }
  }

  @Override public void onBackPressed(){
    if(web!=null&&web.canGoBack())web.goBack();
    else super.onBackPressed();
  }
        }
