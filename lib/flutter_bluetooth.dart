import 'dart:async';

import 'package:flutter/services.dart';

class FlutterBluetooth {
  static const MethodChannel _channel = const MethodChannel('flutter_bluetooth/method');
  static const EventChannel _eventChannel = const EventChannel('flutter_bluetooth/event');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static StreamSubscription listener(void onData(String data)) {
      return _eventChannel.receiveBroadcastStream().listen(onData);
  //    return _eventChannel.receiveBroadcastStream().map((event) => print(event));
  }


  //打开蓝牙
  static Future<bool> openBluetooth() async {
    return await _channel.invokeMethod<bool>("openBluetooth");
  }

  //关闭蓝牙
  static  Future<bool> closeBluetooth() async {
    final bool isClosed = await _channel.invokeMethod<bool>("closeBluetooth");
    return isClosed;
  }

  //打开服务端
  static  Future<bool> openBluetoothService() async {
    final bool service = await _channel.invokeMethod<bool>("openBluetoothService");
    return service;
  }

  //打开客户端
  static  Future<bool> openBluetoothClient(String address) async {
    return await _channel.invokeMethod("openBluetoothClient", address);
  }

  //开始搜索
  static  Future<List<Map<String, String>>> startDiscovery() async {
    List devices = await _channel.invokeMethod<List>("startDiscovery");
    return devices.map((e){
      return Map<String, String>.from(e);
    }).toList();
  }

  //停止搜索
  static Future<bool> stopDiscovery() async {
    final bool cancelDiscovery = await _channel.invokeMethod("stopDiscovery");
    return cancelDiscovery;
  }

  //获取已配对蓝牙
  static  Future<List> get pairedDevices async {
    final List pairedDevices = await _channel.invokeMethod<List>("getPairedDevices");
    return pairedDevices.map((e){
      return Map<String, String>.from(e);
    }).toList();
  }

  //客户端发送消息
  static Future sendMsg(String msg) async {
    await _channel.invokeMethod("sendMsg", msg);
  }

}
