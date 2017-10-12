#讯飞语音AIUI识别服务

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
window.pushPlugin.startText("message",function () {
    console.log("ok");
},function (err){
    console.log(err);
});
```

5.TTS合成语音
```javascript
window.pushPlugin.ttsPlay("message",function () {
    console.log("ok");
},function (err){
    console.log(err);
});
```