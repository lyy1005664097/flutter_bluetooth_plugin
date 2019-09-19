package com.lyy.flutter_bluetooth.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import com.lyy.flutter_bluetooth.BluetoothDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static com.lyy.flutter_bluetooth.thread.AcceptThread.PRE_BT_UUID;

public class ConnectThread extends Thread {
    String TAG = "ConnectThread";
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream btIs;
    private OutputStream btOs;
    private boolean canRecv;
    private PrintWriter writer;
    private String BT_UUID;

    public ConnectThread(BluetoothDevice device) {
        mDevice = device;
        canRecv = true;
        BT_UUID = PRE_BT_UUID + device.getAddress().replaceAll(":", "");
    //    sendHandlerMsg(device.getName() + "_UUID: " + BT_UUID, 0);
        Log.d(TAG, "ConnectThread: " + BT_UUID);
    }

    @Override
    public void run() {
        if (mDevice != null) {
            try {
                //获取套接字
                BluetoothSocket temp = mDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                //mDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));//sdk 2.3以下使用
                mSocket = temp;
                try {
                    //发起连接请求
                    if (mSocket != null) {
                        mSocket.connect();
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                    try {
                        Log.e("", "trying fallback...");

                        mSocket = (BluetoothSocket) mDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mDevice, 1);
                        mSocket.connect();

                        Log.e("", "Connected");
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }


            //    sendHandlerMsg("连接 " + mDevice.getName() + " 成功！", 1);
                //获取输入输出流
                btIs = mSocket.getInputStream();
                btOs = mSocket.getOutputStream();

                //通讯-接收消息
                BufferedReader reader = new BufferedReader(new InputStreamReader(btIs, "UTF-8"));
                String content = null;
                while (canRecv) {
                    content = reader.readLine();
                    if(content.equals(""))continue;
                    sendHandlerMsg(content);
               //     sendMsg(content);
               //     if(content.equals("ok")) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            //    sendHandlerMsg("错误：" + e.getMessage(), 3);
            } finally {
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                    //btIs.close();//两个输出流都依赖socket，关闭socket即可
                    //btOs.close();
                } catch (IOException e) {
                    e.printStackTrace();
            //        sendHandlerMsg("错误：" + e.getMessage(), 4);
                }
            }
        }
    }

    private void sendMsg(String content){
        if(content.equals("all_set")){
    //        write(psd);
        }
    }

    private void sendHandlerMsg(String content) {
        Message msg = BluetoothDelegate.mHandler.obtainMessage();
        msg.what = 1002;
        msg.obj = content;
        BluetoothDelegate.mHandler.sendMessage(msg);
    }

    public void write(String msg) {
        if (btOs != null) {
            try {
                if (writer == null) {
                    writer = new PrintWriter(new OutputStreamWriter(btOs, "UTF-8"), true);
                }
                writer.println(msg + "\r\n");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                writer.close();
           //     sendHandlerMsg("错误：" + e.getMessage(), 5);
            }
        }
    }
}