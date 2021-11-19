/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.supplementalprocess;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.supplementalprocess.IInitCodeCallback;
import android.supplementalprocess.ISupplementalProcessManager;
import android.supplementalprocess.SupplementalProcessManager;
import android.util.Log;

import com.android.server.SystemService;

/**
 * Implementation of Supplemental Process Manager service.
 *
 * @hide
 */
public class SupplementalProcessManagerService extends ISupplementalProcessManager.Stub {

    private static final String TAG = "SupplementalProcessManager";

    private final Context mContext;

    SupplementalProcessManagerService(Context context) {
        mContext = context;
    }

    @Override
    public void loadCode(String name, String version, Bundle params, IInitCodeCallback callback) {
        // Barebone logic for loading code. Still incomplete.

        // Step 1: fetch the installed code in device

        final ApplicationInfo info = getCodeInfo(name);
        if (info == null) {
            String errorMsg = name + " not found for loading";
            Log.w(TAG, errorMsg);
            sendLoadCodeError(SupplementalProcessManager.LOAD_CODE_NOT_FOUND, errorMsg, callback);
            return;
        }
        // TODO(b/204991850): ensure requested code is included in the AndroidManifest.xml

        // Step 2: create identity for the code
        //TODO(b/204991850): <app,code> unit should get unique token

        // Step 3: invoke CodeLoaderService to load the code
        // TODO(b/204991850): invoke code loader to actually load the code

        sendLoadCodeSuccess(new Binder(), callback);
    }

    private ApplicationInfo getCodeInfo(String packageName) {
        // TODO(b/204991850): code info should be version specific too
        try {
            // TODO(b/204991850): update this when PM provides better API for getting code info
            return mContext.getPackageManager().getApplicationInfo(packageName, /*flags=*/0);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }

    }

    private void sendLoadCodeSuccess(IBinder token, IInitCodeCallback callback) {
        try {
            //TODO(b/204991850): params should be returned from SupplementalProcessService
            callback.onInitCodeSuccess(token, new Bundle());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to send onInitCodeSuccess", e);
        }
    }

    private void sendLoadCodeError(int errorCode, String errorMsg, IInitCodeCallback callback) {
        try {
            callback.onInitCodeFailure(errorCode, errorMsg);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to send onInitCodeFailure", e);
        }
    }

    @Override
    public void requestSurfacePackage(int id, IBinder token, int displayId, Bundle params) {}

    @Override
    public void sendData(int id, Bundle params) {}

    @Override
    public void destroyCode(int id) {}

    /** @hide */
    public static class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
        }

        @Override
        public void onStart() {
            SupplementalProcessManagerService service =
                    new SupplementalProcessManagerService(getContext());
            publishBinderService(Context.SUPPLEMENTAL_PROCESS_SERVICE, service);
            Log.i(TAG, "SupplementalProcessManagerService started!");
        }
    }
}
