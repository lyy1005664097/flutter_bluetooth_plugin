package com.lyy.flutter_bluetooth;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterBluetoothPlugin */
public class FlutterBluetoothPlugin implements MethodCallHandler, StreamHandler{

  private static String TAG = "FlutterBluetoothPlugin";
  private static BluetoothDelegate delegate;
  private Activity activity;
  private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
  final Registrar registrar;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_bluetooth/method");
    final EventChannel eventChannel = new EventChannel(registrar.messenger(), "flutter_bluetooth/event");
    final FlutterBluetoothPlugin instance = new FlutterBluetoothPlugin(registrar);
    Log.d("FlutterBluetoothPlugin", "registerWith: " + channel);
    Log.d("FlutterBluetoothPlugin", "registerWith: " + eventChannel);
    Log.d("FlutterBluetoothPlugin", "registerWith: " + instance);
    eventChannel.setStreamHandler(instance);
    channel.setMethodCallHandler(instance);

  }

  FlutterBluetoothPlugin(final Registrar registrar){
    Log.d(TAG, "FlutterBluetoothPlugin 构造方法" + registrar);

    this.registrar = registrar;
    this.activity = registrar.activity();
    this.delegate = new BluetoothDelegate(activity);
    registrar.addActivityResultListener(delegate);
    registrar.addRequestPermissionsResultListener(delegate);
    this.activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
      //  delegate.registerRec();
        Log.d(TAG, "onActivityCreated===============");
      }

      @Override
      public void onActivityStarted(Activity activity) {

      }

      @Override
      public void onActivityResumed(Activity activity) {

      }

      @Override
      public void onActivityPaused(Activity activity) {

      }

      @Override
      public void onActivityStopped(Activity activity) {

      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

      }

      @Override
      public void onActivityDestroyed(Activity activity) {
        if(activity instanceof FlutterActivity) {
          delegate.unregisterRec();
          if (activity == registrar.activity()) {
            ((Application) registrar.context()).unregisterActivityLifecycleCallbacks(this);
          }
          Log.d(TAG, "onActivityDestroyed===============");
        }
      }
    };
    ((Application)registrar.context()).registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {

    Log.d(TAG, "onMethodCall: " + call.method);

    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("getLocalAddress")){
      result.success(delegate.getLocalAddress());
    } else if(call.method.equals("openBluetooth")){
      delegate.openBluetooth(call, result);
    } else if(call.method.equals("closeBluetooth")){
      result.success(delegate.closeBluetooth());
    } else if(call.method.equals("openBluetoothService")){
      delegate.openBluetoothService();
      result.success(null);
    } else if(call.method.equals("openBluetoothClient")){
      Log.d(TAG, "onMethodCall: " + (String)call.arguments);
      delegate.openBluetoothClient((String)call.arguments);
      result.success(null);
    } else if(call.method.equals("startDiscovery")){
      delegate.startDiscovery(call, result);
    } else if(call.method.equals("stopDiscovery")){
      result.success(delegate.stopDiscovery());
    } else if(call.method.equals("getPairedDevices")){
      result.success(delegate.getPairedDevices());
    } else if(call.method.equals("sendMsg")){
      Log.d(TAG, "onMethodCall: " + call.arguments.toString());
      delegate.sendMsg(call.arguments.toString());
      result.success(true);
    } else {
      result.notImplemented();
    }
  }

  //注册成功后的回调
  @Override
  public void onListen(Object o, EventChannel.EventSink eventSink) {
    Log.d(TAG, "onListen: " + eventSink);
    delegate.setEventSink(eventSink);
  }

  @Override
  public void onCancel(Object o) {
    Log.d(TAG, "onListen: " + o.toString());
  }

}
