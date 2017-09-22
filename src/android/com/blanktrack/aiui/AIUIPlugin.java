package com.blanktrack.aiui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.SpeechUtility;

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


/**
 * Created by Blank on 2017-08-24.
 */

public class AIUIPlugin extends CordovaPlugin {
    public static final String TAG = "AIUIPlugin";
    private JSONObject params;
    private static CallbackContext pushContext;
    private AIUIAgent mAIUIAgent = null;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private String permission = Manifest.permission.RECORD_AUDIO;

    public static CallbackContext getCurrentCallbackContext() {
        return pushContext;
    }

    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }
    protected void getMicPermission(int requestCode)
    {
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
        Context context = this.cordova.getActivity().getApplicationContext();

        SpeechUtility.createUtility(context, "appid=57a016c4");
        super.initialize(cordova, webView);
    }

    private void registerNotifyCallback(CallbackContext callbackContext) {

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);

    }

    @Override
    public boolean execute(final String action,final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        JSONObject arg_object = args.getJSONObject(0);
        checkAIUIAgent();
        if("start".equals(action)){
            Log.i( TAG, "start voice nlp" );

            promptForRecord();

        }else if("stop".equals(action)){
            Log.i( TAG, "stop voice nlp" );
            // 停止录音
            String params = "sample_rate=16000,data_type=audio";
            AIUIMessage stopWriteMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);

            mAIUIAgent.sendMessage(stopWriteMsg);
        }else if("startText".equals(action)){
            Log.i( TAG, "start text nlp" );
            String text =arg_object.getString("text");
            String params = "data_type=text";
            if( TextUtils.isEmpty(text) ){
                text = "成都明天的天气怎么样？";
            }

            byte[] textData = text.getBytes();

            AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
            mAIUIAgent.sendMessage(msg);
        }
        else if("finish".equals(action)) {
            callbackContext.success();
        }
        else if("registerNotify".equals(action)){
            pushContext = callbackContext;
            registerNotifyCallback(callbackContext);
        }
        else{
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
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }
    private boolean checkAIUIAgent(){
        if( null == mAIUIAgent ){
            Log.i( TAG, "create aiui agent" );
            mAIUIAgent = AIUIAgent.createAgent( getApplicationContext(), getAIUIParams(), mAIUIListener );
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            mAIUIAgent.sendMessage( startMsg );
        }

        if( null == mAIUIAgent ){
            final String strErrorTip = "创建 AIUI Agent 失败！";
            Log.e(TAG,strErrorTip);
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
        if( null != this.mAIUIAgent ){
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null, null);
            mAIUIAgent.sendMessage( stopMsg );

            this.mAIUIAgent.destroy();
            this.mAIUIAgent = null;
        }
    }

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    Log.i( TAG,  "on event: "+ event.eventType );
                    sendEvent("wakeup","ok");
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    Log.i( TAG,  "on event: "+ event.eventType );
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            sendEvent("nlp",cntJson.toString());

                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                Log.i( TAG, resultStr );
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sendError(e.getLocalizedMessage());
                    }

                } break;

                case AIUIConstant.EVENT_ERROR: {
                    Log.i( TAG,  "on event: "+ event.eventType );
                    sendError("错误: "+event.arg1+"\n"+event.info );
                } break;

                case AIUIConstant.EVENT_VAD: {


                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        sendEvent("找到vad_bos","");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        sendEvent("找到vad_eos","ok");
                    } else {
                        sendEvent("volume",String.valueOf( event.arg2));
                    }


                } break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i( TAG,  "on event: "+ event.eventType );
                    sendEvent("start","ok");
                } break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i( TAG,  "on event: "+ event.eventType );
                    sendEvent("stop","ok");
                } break;

                case AIUIConstant.EVENT_STATE: {	// 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        sendEvent("state","STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        sendEvent("state","STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        sendEvent("state","STATE_WORKING");
                    }
                } break;

                case AIUIConstant.EVENT_CMD_RETURN:{
                    if( AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1 ){
                        //showTip( "上传"+ (0==event.arg2?"成功":"失败") );
                    }
                }break;

                default:
                    break;
            }
        }

    };

    private void sendEvent(String type,String msg ) {
        JSONObject response = new JSONObject();
        try {
            response.put("type",type);
            response.put("message",msg);

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
                                          int[] grantResults) throws JSONException
    {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                sendError("用户未授权录音机");
                return;
            }
        }
        promptForRecord();
    }

    private void promptForRecord()
    {
        if(PermissionHelper.hasPermission(this, permission)) {
            // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
            if( AIUIConstant.STATE_WORKING != 	this.mAIUIState ){
                AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
                mAIUIAgent.sendMessage(wakeupMsg);
            }
            // 打开AIUI内部录音机，开始录音
            String params = "sample_rate=16000,data_type=audio";
            AIUIMessage writeMsg = new AIUIMessage( AIUIConstant.CMD_START_RECORD, 0, 0, params, null );
            mAIUIAgent.sendMessage(writeMsg);
        }
        else
        {
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
