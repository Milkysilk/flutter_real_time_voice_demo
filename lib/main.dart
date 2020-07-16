import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: Scaffold(
          appBar: AppBar(
            title: Text('Flutter Real Time Voice Demo'),
          ),
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Voice()
              ],
            ),
          )
      ),
    );
  }
}

class Voice extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => VoiceState();
}

class VoiceState extends State<Voice> {
  static const channel = MethodChannel("flutter_real_time_voice_demo");
  static const eventChannel = EventChannel("flutter_real_time_voice_demo");
  StreamSubscription _streamSubscription;
  bool hasPermission = false;

  @override
  void initState() {
    getPermission();
    super.initState();
  }

  @override
  void dispose() {
    (() async => print(await channel.invokeMethod("release")))();
    super.dispose();
  }

  Future<void> getPermission() async {
    PermissionStatus status = await Permission.microphone.request();
    if (status != PermissionStatus.granted) {
      Scaffold.of(context).showSnackBar(SnackBar(content: Text('未获得麦克风权限'),));
    } else {
      setState(() => hasPermission = true);
    }
  }

  void start() async {
    if (!hasPermission) {
      await getPermission();
      return;
    }
    print('开始语音');
    _streamSubscription = eventChannel.receiveBroadcastStream().listen((event) async {
      // send voice data
      print(event);
    });
  }

  void stop() async {
    print('停止语音');
    if (_streamSubscription != null) {
      _streamSubscription.cancel();
      _streamSubscription = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Center(
        child: GestureDetector(
          child: Container(
            alignment: Alignment.center,
            width: 200.0,
            height: 100.0,
            color: Colors.blueAccent,
            child: Text('按住说话', style: TextStyle(color: Colors.white),),
          ),
          onLongPress: start,
          onLongPressUp: hasPermission ? stop : null,
        )
    );
  }
}
