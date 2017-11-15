#讯飞语音AIUI识别服务

Using this plugin requires [Cordova iOS](https://github.com/apache/cordova-ios) and [Cordova Android](https://github.com/apache/cordova-android).
## Installing the plugin

```
cordova plugin add cordova-plugin-aiui --variable APPID=your aiuiID
```


## Using the plugin

1.开启录音
```javascript
  window.aiuiPlugin.start(function (data) {
                    msg.text(angular.toJson(data));
                }, function (err) {
                    msg.error(err);
                });
```
2.停止录音
```javascript
window.aiuiPlugin.stop(function (res) {
    console.log("ok");
},function (err){
    console.log(err);
});
```

3.使用registerNotify接收来通知的回调函数
```
//type说明
* volume:       录音时的音量
* nlp:   解析出来的语义
//未完待续...还有其他类型的接收
```

```javascript
window.aiuiPlugin.registerNotify(function (res) {
    //res参数都带有一个type
    console.log(res);
},function(err){
    console.log(err);
});
```

4.解析文字信息
```javascript
window.aiuiPlugin.startText("message",function () {
    console.log("ok");
},function (err){
    console.log(err);
});
```

5.TTS合成语音
```javascript
window.aiuiPlugin.ttsPlay("message",function () {
    console.log("ok");
},function (err){
    console.log(err);
});

window.aiuiPlugin.ttsPause(function () {},function (){});

window.aiuiPlugin.ttsStop(function () {},function (){});

window.aiuiPlugin.ttsResume(function () {},function (){});

```

6.Finish
调用后将不再发送注册消息
```
window.aiuiPlugin.finish();
```