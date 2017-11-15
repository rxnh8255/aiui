/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blanktrack.wakeup.androidsystemimpl;

import android.content.Context;

import com.blanktrack.wakeup.systeminterface.IAudioRecord;
import com.blanktrack.wakeup.systeminterface.IPlatformFactory;
import com.blanktrack.wakeup.systeminterface.IWakeUp;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wuruisheng on 2017/6/7.
 */
public class PlatformFactoryImpl implements IPlatformFactory {
    private Context context;
    private IAudioRecord audioRecord;
    private LinkedBlockingDeque<byte[]> linkedBlockingDeque = new LinkedBlockingDeque<>();

    public PlatformFactoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public IAudioRecord getAudioRecord() {
        if (audioRecord == null) {
            audioRecord = new AudioRecordThread(linkedBlockingDeque);
        }
        return audioRecord;
    }

    @Override
    public IWakeUp getWakeUp() {
        return new WakeUpImpl(context, linkedBlockingDeque);
    }

}