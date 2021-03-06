/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.requests;

import android.os.Handler;
import android.os.Looper;

import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class LoaderDelivery<T> {

    public static final String TAG = Constants.getTag(LoaderDelivery.class);

    Handler mHandler = new Handler(Looper.getMainLooper());
    LoaderRequest.Listener<T> mListener;

    public LoaderDelivery(LoaderRequest.Listener<T> listener) {
        this.mListener = listener;
    }

    public void deliver(Request request, Response response, T data, List<ShopGunError> errors, boolean intermediate) {
        deliverFinishRequestAndPostBack(request, response, data, errors, intermediate);
    }

    public void deliver(Request request, Response response, T data, ShopGunError error, boolean intermediate) {
        ArrayList<ShopGunError> errors = new ArrayList<ShopGunError>();
        errors.add(error);
        deliverFinishRequestAndPostBack(request, response, data, errors, intermediate);
    }

    private void deliverFinishRequestAndPostBack(Request<?> request, Response response, T data, List<ShopGunError> errors, boolean intermediate) {
        new Delivery.DeliveryRunnable(request, response).run();
        mHandler.post(new LoaderRequestPostBackRunnable(request, data, errors, intermediate));
    }

    private class LoaderRequestPostBackRunnable implements Runnable {

        private final Request<?> mRequest;
        private final T mData;
        private final List<ShopGunError> mErrors;
        private final boolean mIntermediate;

        public LoaderRequestPostBackRunnable(Request<?> request, T mData, List<ShopGunError> mErrors, boolean intermediate) {
            this.mRequest = request;
            this.mData = mData;
            this.mErrors = mErrors;
            this.mIntermediate = intermediate;
        }

        @Override
        public void run() {

            if (mRequest.isCanceled()) {
                // This should finish all sub-requests in a nice manor
                mRequest.finish("cancelled-at-delivery");
            } else if (mIntermediate) {
                mListener.onRequestIntermediate(mData, mErrors);
            } else {
                mListener.onRequestComplete(mData, mErrors);
            }

        }
    }

}
