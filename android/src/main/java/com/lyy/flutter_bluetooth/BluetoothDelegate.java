package com.lyy.flutter_bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.lyy.flutter_bluetooth.thread.AcceptThread;
import com.lyy.flutter_bluetooth.thread.ConnectThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION_CODES.M;

public class BluetoothDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    private String TAG = "BluetoothDelegate";
    private MethodCall methodCall;
    private MethodChannel.Result result;
    private Activity activity;
    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_BT_ENABLE_CODE = 200;
    public static final int REQUEST_CODE = 300;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private List<Object> devices;
    private static EventChannel.EventSink eventSink;
    private BluetoothStateReceiver mReceiver;

    BluetoothDelegate(final Activity activity){
        Log.d(TAG, "BluetoothDelegate 构造方法");
        this.activity = activity;
        this.devices = new ArrayList<>();
        registerRec();
    }

    public void registerRec() {

        Log.d(TAG, "registerRec: 注册蓝牙广播======================");
        //3.注册蓝牙广播
        mReceiver = new BluetoothStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//搜多到蓝牙
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索结束
        activity.registerReceiver(mReceiver, filter);
    }

    public void unregisterRec(){
        Log.d(TAG, "registerRec: 注销蓝牙广播======================");
        activity.unregisterReceiver(mReceiver);
    }

    public void openBluetooth(MethodCall call, MethodChannel.Result result){
        this.methodCall = call;
        this.result = result;

        openBluetooth();
    }

    private void openBluetooth(){
        if(Build.VERSION.SDK_INT >= M){
            if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
        }

        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        //1.设备不支持蓝牙，结束应用
        if (mBluetoothAdapter == null) {
            activity.finish();
            return;
        }
        //2.判断蓝牙是否打开
        if (!mBluetoothAdapter.isEnabled()) {
            //没打开请求打开
            Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(btEnable, REQUEST_BT_ENABLE_CODE);
        }
    }

    public boolean closeBluetooth(){
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter.disable();
    }

    public void openBluetoothService(){
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            openBluetooth();
        }
        if (mAcceptThread == null && mBluetoothAdapter != null) {
            mAcceptThread = new AcceptThread(mBluetoothAdapter);
            mAcceptThread.start();
        }
    }

    public void openBluetoothClient(String address){
        Log.d(TAG, "openBluetoothClient: " + address);
        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            openBluetooth();
        }
        if (mConnectThread == null && mBluetoothAdapter != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
    }

    public void startDiscovery(MethodCall call, MethodChannel.Result result){
        Log.d(TAG, "onMethodCall: else if " + call.method);
        this.methodCall = call;
        this.result = result;

        if(mBluetoothAdapter == null){
            openBluetooth();
        }
        devices.clear();
        mBluetoothAdapter.startDiscovery();
    }

    public boolean stopDiscovery(){
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            return mBluetoothAdapter.cancelDiscovery();
        }
        return true;
    }

    public List<Object> getPairedDevices(){

        List<Object> pairedDevices = new ArrayList<>();
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if(mBluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> sets = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : sets){
                Map<String, String> map = new HashMap<>();
                map.put("name", device.getName());
                map.put("address", device.getAddress());
                pairedDevices.add(map);
                Log.d(TAG, "getPairedDevices: " + map.get("name"));
                Log.d(TAG, "getPairedDevices: " + map.get("address"));
            }
        }
        return pairedDevices;
    }

    //客户端发送消息
    public void sendMsg(String msg){
        if(!msg.equals("") || msg != null){
            if(mConnectThread != null){
                mConnectThread.write(msg);
            }else if(mAcceptThread != null){
                mAcceptThread.connectedThread.write(msg);
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_BT_ENABLE_CODE) {
            if (resultCode == RESULT_OK) {
                //用户允许打开蓝牙
        //        mMessageAdapter.addMessage("用户同意打开蓝牙");
                result.success(true);
            } else if (resultCode == RESULT_CANCELED) {
                //用户取消打开蓝牙
         //       mMessageAdapter.addMessage("用户拒绝打开蓝牙");
                result.error("open bluetooth", "用户拒绝打开蓝牙",null);
            }
        }
        return true;
    }

    @Override
    public boolean onRequestPermissionsResult(int i, String[] strings, int[] ints) {
        if(i == REQUEST_CODE){
            for(int grant: ints){
                if(grant != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity, "程序需要请求权限，否则无法正常工作", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               //     mMessageAdapter.addMessage("找到设备：" + device.getName() + "\t\t\t" + device.getAddress());
               //     if (mRvAdapter != null) {
               //         mRvAdapter.addDevice(device);
               //     }
                    Map<String, String> map = new HashMap<>();
                    map.put("name", intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
                    map.put("address", device.getAddress());
                    devices.add(map);
                    Log.d("BlueToothStateReceiver", "onReceive: " + map.get("name"));
                    Log.d("BlueToothStateReceiver", "onReceive: " + map.get("address"));
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                //    mMessageAdapter.addMessage("搜索结束");
                    Log.d("BlueToothStateReceiver", "onReceive: 搜索结束");
                    result.success(devices);

                    break;
            }
        }
    }

    public static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d("Handler", "handleMessage: " + msg.what + "***" + (String) msg.obj);
            switch (msg.what){
                case 1001:
                    eventSink.success((String)msg.obj);
                    break;
                case 1002:
                    eventSink.success((String)msg.obj);
                    break;
            }
        }
    };

    public void setEventSink(EventChannel.EventSink eventSink){
        this.eventSink = eventSink;
    }
}
