package com.lyy.flutter_bluetooth.thread;

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

public class ConnectedThread extends Thread {

    private BluetoothSocket mSocket;
    private InputStream btIs;
    private OutputStream btOs;
    private boolean canRecv;
    private PrintWriter writer;

    ConnectedThread(BluetoothSocket mSocket){
        this.mSocket = mSocket;
        canRecv = true;
    }

    @Override
    public void run() {
        try {
            Log.d("当前线程", "run: " + android.os.Process.myTid());
            //获取输入输出流
            btIs = mSocket.getInputStream();
            btOs = mSocket.getOutputStream();
            //通讯-接收消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(btIs, "UTF-8"));
            String content = null;
            write("all_set");  //准备就绪
            sendHandlerMsg("all_set");
            while (canRecv) {
                content = reader.readLine();
                if(content.equals("")) continue;
                sendHandlerMsg(content);
          //      sendMsg(content);
            }
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
                // btIs.close();//两个输出流都依赖socket，关闭socket即可
                // btOs.close();
            } catch (IOException e) {
                e.printStackTrace();
            //    sendHandlerMsg("错误：" + e.getMessage(), 3);
            }
        }

    }

    /*private void sendMsg(String content){
        if(content.equals(BluetoothActivity.PASSWORD)){
            write("ok");
        }else {
            write("wwrong");
        }
    }*/

    private void sendHandlerMsg(String content) {
        Message msg = BluetoothDelegate.mHandler.obtainMessage();
        msg.what = 1001;
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
            //    sendHandlerMsg("错误：" + e.getMessage(), 4);
            }
        }
    }
}
