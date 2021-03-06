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

package com.shopgun.android.sdk.network;

import com.shopgun.android.sdk.log.EventLog;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public abstract class Request<T> implements Comparable<Request<T>> {

    public static final String TAG = Constants.getTag(Request.class);

    /** Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}. */
    protected static final String DEFAULT_PARAMS_ENCODING = "utf-8";

    /** Default cache time in milliseconds */
    protected static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(3);

    /** Default connection timeout, this is for both connection and socket */
    private static final int CONNECTION_TIME_OUT = (int) TimeUnit.SECONDS.toMillis(20);

    /** Listener interface, for responses */
    private final Listener<T> mListener;

    /** Request method of this request.  Currently supports GET, POST, PUT, and DELETE. */
    private final Method mMethod;
    /** Log of this request */
    private final EventLog mEventLog;
    private final JSONObject mNetworkLog;
    /** URL of this request. */
    private String mUrl;
    /** Headers to be used in this request */
    private Map<String, String> mHeaders = new HashMap<String, String>();
    /** Sequence number used for prioritizing the queue */
    private int mSequence = 0;
    /** Item for containing cache items */
    private Map<String, Cache.Item> mCache = new HashMap<String, Cache.Item>();
    /** Parameters to add to request */
    private Map<String, String> mParameters = new HashMap<String, String>();
    /** Should this request use location in the query */
    private boolean mUseLocation = true;
    private boolean mExcludeRadius = false;
    /** If true Request will return data from cache if exists */
    private boolean mIgnoreCache = false;
    /** Whether or not responses to this request should be cached. */
    private boolean mIsCacheable = true;
    /** Whether or not this request has been canceled. */
    private boolean mCanceled = false;
    /** Indication if the request is finished */
    private boolean mFinished = false;
    private int mTimeout = CONNECTION_TIME_OUT;
    private boolean mCacheHit = false;

    private RequestDebugger mDebugger;

    /** Boolean deciding if logs should be enabled */
    private boolean mSaveNetworkLog = true;

    /**  */
    private RequestQueue mRequestQueue;

    /** A tag to identify the request, useful for bulk operations */
    private Object mTag;

    private Delivery mDelivery;

    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     * @param method A {@link com.shopgun.android.sdk.network.Request.Method}
     * @param url An url
     * @param listener A callback listener
     */
    public Request(Method method, String url, Listener<T> listener) {
        mMethod = method;
        mUrl = url;
        mListener = listener;
        mEventLog = new EventLog();
        mNetworkLog = new JSONObject();
    }

    protected void resetstate() {
        mEventLog.add("request-state-reset");
        mFinished = false;
        mCanceled = false;
        mCacheHit = false;
    }

    /**
     * Adds event to a request, for later debugging purposes
     * @param event An event
     */
    public void addEvent(String event) {
        mEventLog.add(event);
    }

    /**
     * Get the log for this request, log contains actions, and timings that have been performed on this request
     * @return the EventLog for this request
     */
    public EventLog getLog() {
        return mEventLog;
    }

    /** Mark this request as canceled.  No callback will be delivered. */
    public synchronized void cancel() {
        mCanceled = true;
    }

    /**
     * Returns true if this request has been canceled.
     * @return {@code true} if the request is canceled, else {@code false}
     */
    public synchronized boolean isCanceled() {
        return mCanceled;
    }

    /**
     * Method for determining if the request is finished.
     * Whether the request was successful or not, it <b>NOT</b> reflected here.
     * @return true if the SDK, has finished this request
     */
    public synchronized boolean isFinished() {
        return mFinished;
    }

    /**
     * Method for marking a request as finished
     * @param reason A reason for finishing the request
     * @return this object
     */
    public synchronized Request finish(String reason) {

        if (mFinished) {
            SgnLog.d(TAG, getClass().getSimpleName() + " - Request already finished, " + toString());
        }

        addEvent(reason);

        try {
            mNetworkLog.put("duration", mEventLog.getTotalDuration());
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }

        if (mSaveNetworkLog) {
            // Append the request summary to the debugging log
//            SgnLog.getLogger().getLog().add(Event.TYPE_REQUEST, mNetworkLog);
        }

        mFinished = true;
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }

        if (mDebugger != null) {
            mDebugger.onFinish(this);
        }

        return Request.this;
    }

    public JSONObject getNetworkLog() {
        return mNetworkLog;
    }

    public void stats(int in, int out) {
        mRequestQueue.dataIn += in;
        mRequestQueue.dataOut += out;
    }

    /**
     * Get the connection timeout for this request.
     * <p>The timeout will be the same for connecting, and for reading data</p>
     * @return The timeout in milliseconds
     */
    public int getTimeOut() {
        return mTimeout;
    }

    /**
     * Set the timeout value for this request.
     * <p>The timeout will be the same for connecting, and for reading data</p>
     * @param timeout A timeout in millis
     * @return this object
     */
    public Request setTimeOut(int timeout) {
        mTimeout = timeout;
        return this;
    }

    /**
     * Returns a list of headers for this request.
     * @return A map of headers
     */
    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    /**
     * Set any headers wanted in the request
     * @param headers to include
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders.putAll(headers);
    }

    /**
     * Return the method for this request.  Can be one of the values in {@link Method}.
     * @return A method
     */
    public Method getMethod() {
        return mMethod;
    }

    /**
     * returns whether this request is cacheable or not
     * @return true if the request is cacheable
     */
    public boolean isCacheable() {
        return mIsCacheable;
    }

    /**
     * Whether this request should be added to cache.
     * @param cacheable {@code true} if the response is cacheable, else {@code false}
     */
    protected void setCacheable(boolean cacheable) {
        mIsCacheable = cacheable;
    }

    /**
     * A tag to identify this request (or its origin) for performing batch operations.
     * @return An object. This requests tag or null.
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Set a tag on this request. This can later be used for bulk operations.
     * @param tag An object to identify this request buy
     * @return This object
     */
    public Request setTag(Object tag) {
        mTag = tag;
        return this;
    }

    protected RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    /**
     * Set the executing request queue, in order to later inform the RequestQueue
     * if this requests finished execution.
     * @param requestQueue The RequestQueue that is performing this Request
     * @return This object
     */
    public synchronized Request setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    /**
     * Find out if the response from this request is from cache or not.
     * @return true if response is from cache, else false
     */
    public boolean isCacheHit() {
        return mCacheHit;
    }

    /**
     * Set whether this was from cache
     * @param cacheHit true is response is cache hit, else false
     * @return this object
     */
    public Request setCacheHit(boolean cacheHit) {
        mCacheHit = cacheHit;
        return this;
    }

    /**
     * The time-to-live for a given Cache.Item this request may create
     * @return request time-to-live in milliseconds
     */
    public long getCacheTTL() {
        return DEFAULT_CACHE_TTL;
    }

    /**
     * Method determining is cache should be ignored
     * @return true, if this request should query the cache for data
     */
    public boolean ignoreCache() {
        return mIgnoreCache;
    }

    /**
     * Set whether this request may use data from cache or not
     * @param skip true if cache should not be used
     * @return this object
     */
    public Request setIgnoreCache(boolean skip) {
        mIgnoreCache = skip;
        return Request.this;
    }

    /**
     * Get the {@link Delivery} for this request, if any exists.
     * @return A delivery, or null
     */
    public Delivery getDelivery() {
        return mDelivery;
    }

    /**
     * Set a specific {@link Delivery} interface for handling the response once a {@link Request} has finished.
     * @param d Delivery interface to handle the response once this {@link Request} has finished
     * @return this object
     */
    public Request setDelivery(Delivery d) {
        mDelivery = d;
        return this;
    }

    /**
     * Get the cache item that this request have generated
     * @return an Cache.Item, or null is no Cache.Item have been generated
     */
    public Map<String, Cache.Item> getCache() {
        return mCache;
    }

    /**
     * Set the Cache.Item that have been generated from this request.
     * @param cache the generated Cache.Item
     * @return this object
     */
    public Request putCache(Map<String, Cache.Item> cache) {
        mCache.putAll(cache);
        return this;
    }

    /**
     * Determining if this request should include location data.
     * @return true if location data should be used in this request, else false.
     */
    public boolean useLocation() {
        return mUseLocation;
    }

    /**
     * Enable or disable the usage of location data in this request.<br>
     * Please use with care, <b>most API v2 endpoints require location data</b>
     * @param useLocation true to include, and false exclude location data in request parameters
     * @return this object
     */
    public Request setUseLocation(boolean useLocation) {
        mUseLocation = useLocation;
        return Request.this;
    }

    /**
     * Exclude the radius info from the request.
     * This is optional, by default the radius will be included in each request that include location,
     * so set it to true only if you want to send just lat and long
     * @param excludeRadius true to exclude radius from request parameters
     * @return this object
     */
    public Request setExcludeRadius(boolean excludeRadius) {
        mExcludeRadius = excludeRadius;
        return Request.this;
    }

    /**
     * Determine if the radius should be excluded
     * @return true if radius should be excluded
     */
    public boolean excludeRadius() {
        return mExcludeRadius;
    }

    /**
     * Get the url for this request
     * @return the url for this request
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Set the url of this request
     * @param url to use in this request
     * @return this object
     */
    public Request setUrl(String url) {
        mUrl = url;
        return Request.this;
    }

    /**
     * Get the query parameters that will be used to perform this query.<br>
     * @return the query parameters
     */
    public Map<String, String> getParameters() {
        return mParameters;
    }

    /**
     * Add request parameters to this request. The parameters will be appended
     * as HTTP query parameters to the URL, when the SDK executes the request.
     * Therefore you should <b>not</b> do nested structures, only simple key-value-pairs.
     * This is <b>not the same as appending a body</b> to the request, when doing a PUT or POST request.
     * @param query A map of parameters to bundle along with this request
     * @return this object
     */
    public Request putParameters(Map<String, String> query) {
        if (query != null) {
            mParameters.putAll(query);
        }
        return Request.this;
    }

    /**
     * Get the priority of which this request has.
     * @return the request priority
     */
    protected Priority getPriority() {
        return Priority.MEDIUM;
    }

    /**
     * Get the sequence number that this request have been given.
     * The sequence number reflects the order of which the request was handed to the
     * {@link RequestQueue RequestQueue}.
     * and can partially be used to determine the order of execution.
     * @return the sequence number (a non-negative number)
     */
    protected int getSequence() {
        return mSequence;
    }

    /**
     * Set a sequence number for when the request was received by
     * {@link RequestQueue RequestQueue}
     * in order to partially determine the order of execution of requests.
     * @param seq A sequence number
     */
    protected void setSequence(int seq) {
        mSequence = seq;
    }

    /**
     * Get parameter encoding of the request. Useful for decoding data.
     * @return the encoding
     */
    public String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    /**
     * Get content type of the body. Useful for setting headers.
     * @return A string
     */
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    /**
     * Get the body of this request.
     * @return body, if body have been set, else null
     */
    public byte[] getBody() {
        return null;
    }

    /**
     * Method to be implemented, should handle parsing of network data, and simultaneously create
     * a Cache.Item (or several Cache.Item) if such a item(s) can and shall be created.
     * @param response NetworkResponse to parse into <b>both</b> a Response, and Cache.Item
     * @return a valid Response is possible, or null
     */
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    /**
     * Method to be implemented in subclasses, which will be able to parse a Cache.Item,
     * previously generated by this request in {@link #parseNetworkResponse(NetworkResponse) parseNetworkResponse()}.
     * @param c item to parse
     * @return a valid Response is possible, or null
     */
    abstract protected Response<T> parseCache(Cache c);

    /**
     * Method for easily delivering the response to the user, via the given callback-listener.
     * @param response to deliver, may be null
     * @param error to deliver, may be null
     */
    public void deliverResponse(T response, ShopGunError error) {
        if (mDebugger != null) {
            mDebugger.onDelivery(this, response, error);
        }
        if (mListener != null) {
            mListener.onComplete(response, error);
        }
    }

    public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();
        return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
    }

    /**
     * Get the {@link RequestDebugger} associated with this request
     * @return A {@link RequestDebugger}, or null
     */
    public RequestDebugger getDebugger() {
        return mDebugger;
    }

    /**
     * Set a debugger to perform debugging when {@link Request} finishes.
     *
     * @param debugger A {@link RequestDebugger} to print/debug the {@link Request}
     * @return this object
     */
    public Request setDebugger(RequestDebugger debugger) {
        mDebugger = debugger;
        return this;
    }

    /**
     * Use this method, to enable/disable saving summary to
     * {@link SgnLog#getLogger()}.
     * @param saveNetworkLog True to save logs to global log
     */
    public void setSaveNetworkLog(boolean saveNetworkLog) {
        mSaveNetworkLog = saveNetworkLog;
    }

    /**
     * Returns a complete printable representation of this Request, e.g:
     *
     * <code>GET: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2</code>
     *
     * <p>The SDK/API parameters are not added to the
     * {@link Request#getParameters()}  query parameters}, before the request
     * is handed to the {@link RequestQueue}. So if you want to have the SDK/API
     * parameters appended as well in the string do:</p>
     * <code>ShopGun.getInstance().add(Request)</code>
     * <p>and then call: </p>
     * <code>toString()</code>
     */
    @Override
    public String toString() {
        return mMethod.toString() + ": " + SgnUtils.requestToUrlAndQueryString(this);
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    /** Supported request methods. */
//	public interface Method {
//		int GET = 0;
//		int POST = 1;
//		int PUT = 2;
//		int DELETE = 3;
//	}

    public enum Method {
        GET, POST, PUT, DELETE
    }

}
