import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth/flutter_bluetooth.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp>{

  List<Map<String, String>> devices;
  List<Map<String, String>> pairedDevices;
  List<String> msgs;
  TextEditingController controller;
  StreamSubscription subscription;

  @override
  void initState() {
    super.initState();

    subscription = FlutterBluetooth.listener(onData);
    devices = List();
    pairedDevices = List();
    msgs = List();
    controller = TextEditingController();
  }

  onData(msg) {
    if(msg == "all_set"){
      print("发送消息：" + msg);
      msgs.add("发送消息：" + msg);
    }else {
      print("收到消息：" + msg);
      msgs.add("收到消息：" + msg);
    }
    setState(() {

    });
  }

  onError() {
    print("error");
  }

  @override
  void dispose() {
    super.dispose();
  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Bluetooth'),
        ),
        body: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  RaisedButton(
                    child: Text("打开蓝牙"),
                    onPressed: () async {
                      msgs.add("打开蓝牙");
                      await FlutterBluetooth.openBluetooth();
                      setState(() {

                      });
                    },
                  ),
                  RaisedButton(
                    child: Text("关闭蓝牙"),
                    onPressed: () async {
                      msgs.add("关闭蓝牙");
                      await FlutterBluetooth.closeBluetooth();
                      setState(() {

                      });
                      },
                  ),
                  RaisedButton(
                    child: Text("开始搜索"),
                    onPressed: () async {
                      msgs.add("开始搜索");
                      setState(() {

                      });
                      devices = await FlutterBluetooth.startDiscovery();
                      msgs.add("搜索结束");
                      setState(() {

                      });
                    },
                  ),
                  RaisedButton(
                    child: Text("停止搜索"),
                    onPressed: () async {
                      msgs.add("停止搜索");
                      await FlutterBluetooth.stopDiscovery();
                      setState(() {

                      });
                    },
                  ),
                ],
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: <Widget>[
                  RaisedButton(
                    child: Text("服务端"),
                    onPressed: () async {
                      await FlutterBluetooth.openBluetoothService();
                      msgs.add("开启服务端");
                      setState(() {

                      });
                    },
                  ),
                  RaisedButton(
                    child: Text("客户端"),
                    onPressed: null,
                  ),
                  RaisedButton(
                    child: Text("设置蓝牙密码"),
                    onPressed: null,
                  ),
                ],
              ),
              Row(
                children: <Widget>[
                  Expanded(
                    child: TextField(
                      controller: controller,
                    ),
                  ),
                  RaisedButton(
                    child: Text("发送"),
                    onPressed: () async {
                      String msg = controller.text;
                      msgs.add("发送消息：" + msg);
                      await FlutterBluetooth.sendMsg(msg);
                      controller.text = "";
                      setState(() {

                      });
                    },
                  ),
                ],
              ),
              ListTile(
                title: Text("已配对设备"),
                trailing: RaisedButton(
                  child: Text("获取已配对蓝牙"),
                  onPressed: () async {
                    msgs.add("开始获取");
                    setState(() {

                    });
                    pairedDevices = await FlutterBluetooth.pairedDevices;
                    msgs.add("获取完毕");
                    setState(() {

                    });
                  },
                ),
              ),
              ConstrainedBox(
                constraints: BoxConstraints(
                  maxHeight: 400,
                ),
                child: ListView(
                  shrinkWrap: true,
                  children: pairedDevices.map((device){
                    return ListTile(
                      title: Text(device["name"] ?? ""),
                      subtitle: Text(device["address"] ?? ""),
                      onTap: () async {
                        await FlutterBluetooth.openBluetoothClient(device["address"]);
                        msgs.add("连接" + device["name"]);
                        setState(() {

                        });
                      },
                    );
                  }).toList(),
                ),
              ),
              ListTile(
                title: Text("发现设备"),
              ),
              ConstrainedBox(
                constraints: BoxConstraints(
                  maxHeight: 400,
                ),
                child:ListView(
                    shrinkWrap: true,
                    children: devices.map((device){
                      return ListTile(
                        title: Text(device["name"] ?? ""),
                        subtitle: Text(device["address"] ?? ""),
                        onTap: () async {
                          await FlutterBluetooth.openBluetoothClient(device["address"]);
                          msgs.add("连接" + device["name"]);
                          setState(() {

                          });
                        },
                      );
                    }).toList(),
                  ),
              ),
              ListTile(
                title: Text("操作日志"),
                subtitle: ListView(
                  shrinkWrap: true,
                  children: msgs.map((msg){
                    return Text(msg);
                  }).toList(),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }


}
