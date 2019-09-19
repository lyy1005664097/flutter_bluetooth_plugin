package com.lyy.flutter_bluetooth.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Message;

import com.lyy.flutter_bluetooth.utils.BluetoothUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class AcceptThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mSocket;
//    private InputStream btIs;
//    private OutputStream btOs;
    private PrintWriter writer;
    private boolean canAccept;
    private boolean canRecv;
    public static final String PRE_BT_UUID = "00001101-0000-1000-8000-";// uuid前缀
    private String BT_UUID;
    public ConnectedThread connectedThread;

    public AcceptThread(BluetoothAdapter bluetoothAdapter) {
        canAccept = true;
        canRecv = true;
        mBluetoothAdapter = bluetoothAdapter;
        BT_UUID = PRE_BT_UUID + BluetoothUtils.getBtAddressByReflection().replaceAll(":", "");
    //    sendHandlerMsg("MY_UUID: " + BT_UUID, 0);
    }

    @Override
    public void run() {
        try {

            //获取套接字
            BluetoothServerSocket temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("TEST", UUID.fromString(BT_UUID));
            mServerSocket = temp;
            //监听连接请求 -- 作为测试，只允许连接一个设备
            if (mServerSocket != null) {
                while (canAccept) {
                    mSocket = mServerSocket.accept();
                //    sendHandlerMsg("有客户端连接", 1);

                    connectedThread = new ConnectedThread(mSocket);
                    new Thread(connectedThread).start();
                }
            }
//            //获取输入输出流
//            btIs = mSocket.getInputStream();
//            btOs = mSocket.getOutputStream();
//            //通讯-接收消息
//            BufferedReader reader = new BufferedReader(new InputStreamReader(btIs, "UTF-8"));
//            String content = null;
//            write("all_set");
//            while (canRecv) {
//                content = reader.readLine();
//                if(content.equals("")) continue;
//                sendHandlerMsg("收到消息：" + content, 2);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
//                // btIs.close();//两个输出流都依赖socket，关闭socket即可
//                // btOs.close();
            } catch (IOException e) {
                e.printStackTrace();
            //    sendHandlerMsg("错误：" + e.getMessage(), 3);
            }
        }
    }
//
//    private void sendHandlerMsg(String content, int arg) {
//        Message msg = BluetoothActivity.mHandler.obtainMessage();
//        msg.what = 1001;
//        msg.arg1 = arg;
//        msg.obj = content;
//        BluetoothActivity.mHandler.sendMessage(msg);
//    }
/*
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
                sendHandlerMsg("错误：" + e.getMessage(), 4);
            }
        }
    }*/
}