#讯飞语音AIUI识别服务
加入了百度唤醒.所以不提供外部调用了

## Installing the plugin

```
cordova plugin add cordova-plugin-aiui --variable APPID=your aiuiID --variable BAPPID=baiduid
```


## Using the plugin

-1.关闭唤醒,激活唤醒以后需要关闭唤醒来进行AIUI
```javascript
    window.aiuiPlugin.sleep();
```

0.开启唤醒,现在改了结构,必须先调用wakeup,让APP去请求权限,不然直接调用start有可能会报错
```javascript
    window.aiuiPlugin.wakeup();
```

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
* stop:     解析停止
* wakeup:   百度唤醒被激活//激活以后可以直接开启讯飞AIUI语音识别
* sleep:    百度唤醒睡眠
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