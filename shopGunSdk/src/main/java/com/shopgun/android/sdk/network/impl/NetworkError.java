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

package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.network.ShopGunError;


public class NetworkError extends ShopGunError {

    public static final String TAG = Constants.getTag(NetworkError.class);

    private static final long serialVersionUID = 1L;

    public NetworkError(Throwable t) {
        super(t, Code.NETWORK_ERROR, "Networking error", "There was an error "
                + "establishing a connection to the API. Please check that the "
                + "device has a working internet connection.");
    }

}
