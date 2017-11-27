package com.blanktrack.aiui;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Blank on 2017-08-24.
 */

public class AIUIPlugin extends CordovaPlugin {
    public static final String TAG = "AIUIPlugin";
    private static CallbackContext pushContext;
    private AIUIAgent mAIUIAgent = null;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private String permission = Manifest.permission.RECORD_AUDIO;
    private EventManager wakeup;

    private SpeechSynthesizer mTts;
    private String voicer = "xiaoyan";

    public static CallbackContext getCurrentCallbackContext() {
        return pushContext;
    }

    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    protected void getMicPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, permission);
    }
    /**
     * Called after plugin construction and fields have been initialized.
     * Prefer to use pluginInitialize instead since there is no value in
     * having parameters on the initialize() function.
     *
     * @param cordova
     * @param webView
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Context context = this.cordova.getActivity().getApplicationContext();

        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SpeechUtility.createUtility(context, "appid="+applicationInfo.metaData.getString("com.blanktrack.appid"));
        //SpeechUtility.createUtility(context, "appid=57a016c4");

        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

    }


    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                //showTip("初始化失败,错误码："+code);
            } else {
                // 清空参数
                mTts.setParameter(SpeechConstant.PARAMS, null);
                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
                //设置合成语速
                mTts.setParameter(SpeechConstant.SPEED, "50");
                //设置合成音调
                mTts.setParameter(SpeechConstant.PITCH, "50");
                //设置合成音量
                mTts.setParameter(SpeechConstant.VOLUME, "50");
                //设置播放器音频流类型
                mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
                // 设置播放合成音频打断音乐播放，默认为true
                mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

                // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
                // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
                mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
                mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");

                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private void registerNotifyCallback(CallbackContext callbackContext) {

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);

    }
    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (null != this.mAIUIAgent) {
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
            mAIUIAgent.sendMessage(stopMsg);

            this.mAIUIAgent.destroy();
            this.mAIUIAgent = null;
        }
    }

    EventListener wakeupListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            Log.d(TAG, String.format("event: name=%s, params=%s", name, params));

            //唤醒成功
            if(name.equals("wp.data")){
                try {
                    JSONObject json = new JSONObject(params);
                    int errorCode = json.getInt("errorCode");
                    if(errorCode == 0){
                        //唤醒成功
                        sendEvent("wakeup",json.toString());
                        Log.i(TAG,"baidu唤醒成功了了了了");
                    } else {
                        //唤醒失败
                        Log.i(TAG,"baidu唤醒失败l了了了了");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if("wp.exit".equals(name)){
                //唤醒已停止
                Log.i(TAG,"baidu唤醒停止停止");
                sendEvent("sleep","true");
            }

        }
    };

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final JSONObject arg_object = args.getJSONObject(0);
        checkAIUIAgent();
        if ("wakeup".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.i("wakeup","唤醒kaishi了");
                    promptForRecord();
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                }
            });
        }
        else if("sleep".equals(action)){

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.i("wakeup","唤醒停止了");
                    if(wakeup!= null) {
                        wakeup.send(com.baidu.speech.asr.SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
                        wakeup.unregisterListener(wakeupListener);
                        wakeup = null;
                    }
                }
            });

        }
        else if ("start".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    Log.i(TAG, "start voice nlp");

                    // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
                    if (AIUIConstant.STATE_WORKING != mAIUIState) {
                        AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                        mAIUIAgent.sendMessage(wakeupMsg);
                    }
                    // 打开AIUI内部录音机，开始录音
                    String params1 = "sample_rate=16000,data_type=audio";
                    AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params1, null);
                    mAIUIAgent.sendMessage(writeMsg);

                    //promptForRecord();
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                }
            });
        }else if("registFamily".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        String familyId = arg_object.getString("familyId");
                        String setParams = "\"audioparams\":{\"pers_param\":\"{\"family\":\""+familyId+"\"}\"}";
                        AIUIMessage setMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0 , 0, setParams, null);
                        mAIUIAgent.sendMessage(setMsg);
                        Log.i(TAG,"registFamily success");
                        callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                    } catch (JSONException e) {
                        callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR,e.toString()) );
                    }
                }
            });
        }
        else if ("stop".equals(action)) {
            Log.i(TAG, "stop voice nlp");
            // 停止录音
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    String params = "sample_rate=16000,data_type=audio";
                    AIUIMessage stopWriteMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);
                    mAIUIAgent.sendMessage(stopWriteMsg);
                    callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                }
            });

        } else if ("startText".equals(action)) {
            Log.i(TAG, "start text nlp");

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        String text = arg_object.getString("text");
                        String params = "data_type=text";
                        if (TextUtils.isEmpty(text)) {
                            text = "成都明天的天气怎么样？";
                        }
                        byte[] textData = text.getBytes();
                        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
                        mAIUIAgent.sendMessage(msg);
                        callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                    } catch (JSONException e) {
                        callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR,e.toString()) );
                    }

                }
            });
        } else if ("finish".equals(action)) {
            callbackContext.success();
        } else if ("registerNotify".equals(action)) {
            pushContext = callbackContext;
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    registerNotifyCallback(callbackContext);
                }
            });
        }else if("ttsPlay".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        String text = arg_object.getString("text");
                        int code = mTts.startSpeaking(text, mTtsListener);
                        if (code != ErrorCode.SUCCESS) {
                            if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                                //未安装则跳转到提示安装页面
                                callbackContext.error("未安装语记");
                            }else {
                                callbackContext.error("语音合成失败,错误码: " + code);
                            }
                        }else{
                            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
                        }
                    } catch (JSONException e) {
                        callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.ERROR,e.toString()) );
                    }
                }
            });
        }else if("ttsPause".equals(action)){
            mTts.pauseSpeaking();
            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
        }else if("ttsStop".equals(action)){
            mTts.stopSpeaking();
            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
        }else if("ttsResume".equals(action)){
            mTts.resumeSpeaking();
            callbackContext.sendPluginResult( new PluginResult(PluginResult.Status.OK) );
        }
        else {
            Log.e(TAG, "Invalid action : " + action);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return false;
        }

        return true;
    }

    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getApplicationContext().getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }

    private boolean checkAIUIAgent() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "create aiui agent");
            mAIUIAgent = AIUIAgent.createAgent(getApplicationContext(), getAIUIParams(), mAIUIListener);
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            mAIUIAgent.sendMessage(startMsg);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建 AIUI Agent 失败！";
            Log.e(TAG, strErrorTip);
//            showTip( strErrorTip );
//            this.mNlpText.setText( strErrorTip );
        }

        return null != mAIUIAgent;
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeup.send(com.baidu.speech.asr.SpeechConstant.WAKEUP_STOP, "{}", null, 0, 0);
        if (null != this.mAIUIAgent) {
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
            mAIUIAgent.sendMessage(stopMsg);

            this.mAIUIAgent.destroy();
            this.mAIUIAgent = null;
        }
        if( null != mTts ){
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }

    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
            sendEvent("ttsState","begin");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
            sendEvent("ttsState","paused");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
            sendEvent("ttsState","resumed");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            //sendEvent("ttsBufferProgress", String.valueOf( percent));
            // 合成进度
//            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            //sendEvent("ttsSpeakProgress", String.valueOf( percent));
            // 播放进度
//            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                sendEvent("ttsState","completed");
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void showTip(final String str) {
        Log.i(TAG,str);
    }

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    Log.i(TAG, "on event: " + event.eventType);
                    sendEvent("wakeupAiui", "ok");
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    Log.i(TAG, "on event: " + event.eventType);
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            sendEvent("nlp", cntJson.toString());

                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                Log.i(TAG, resultStr);
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sendError(e.getLocalizedMessage());
                    }

                }
                break;

                case AIUIConstant.EVENT_ERROR: {
                    Log.i(TAG, "on event: " + event.eventType);
                    sendError("错误: " + event.arg1 + "\n" + event.info);
                }
                break;

                case AIUIConstant.EVENT_VAD: {


                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        sendEvent("找到vad_bos", "");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        sendEvent("找到vad_eos", "ok");
                    } else {
                        sendEvent("volume", String.valueOf(event.arg2));
                    }


                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
                    sendEvent("start", "ok");
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
                    sendEvent("stop", "ok");
                }
                break;

                case AIUIConstant.EVENT_STATE: {    // 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        sendEvent("state", "STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        sendEvent("state", "STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        sendEvent("state", "STATE_WORKING");
                    }
                }
                break;

                case AIUIConstant.EVENT_CMD_RETURN: {
                    if (AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1) {
                        //showTip( "上传"+ (0==event.arg2?"成功":"失败") );
                    }
                }
                break;

                default:
                    break;
            }
        }

    };

    private void sendEvent(String type, String msg) {
        JSONObject response = new JSONObject();
        try {
            response.put("type", type);
            response.put("message", msg);

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, response);
            pluginResult.setKeepCallback(true);
            CallbackContext pushCallback = getCurrentCallbackContext();
            if (pushCallback != null) {
                pushCallback.sendPluginResult(pluginResult);
            }

        } catch (JSONException e) {
            sendError(e.getMessage());
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                sendError("用户未授权录音机");
                return;
            }
        }
        promptForRecord();
    }

    private void promptForRecord() {
        if (PermissionHelper.hasPermission(this, permission)) {
            Log.i(TAG,"开启唤醒");

            wakeup = EventManagerFactory.create(getApplicationContext(), "wp");
            wakeup.registerListener(wakeupListener);

            Map<String, Object> params = new LinkedHashMap<String, Object>();
            params.put(com.baidu.speech.asr.SpeechConstant.APP_ID, "10099877");
            params.put(com.baidu.speech.asr.SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
            params.put(com.baidu.speech.asr.SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
            String json = null; // 这里可以替换成你需要测试的json
            json = new JSONObject(params).toString();
            wakeup.send(com.baidu.speech.asr.SpeechConstant.WAKEUP_START, json, null, 0, 0);

        } else {
            getMicPermission(0);
        }

    }

    public void sendError(String message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
        pluginResult.setKeepCallback(true);
        CallbackContext pushCallback = getCurrentCallbackContext();
        if (pushCallback != null) {
            pushCallback.sendPluginResult(pluginResult);
        }
    }

}
