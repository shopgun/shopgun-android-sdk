package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.Executor;

import android.os.Handler;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class Delivery {
	
	/** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;
    public RequestQueue mRequestQueue;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public Delivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
        	
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }
    
    public void postResponse(Request<?> request, Response<?> response) {
    	request.addEvent("post-response");
    	post(request, response);
    }

    public void postError(Request<?> request, EtaError error) {
    	request.addEvent("post-error");
        Response<?> response = Response.fromError(error);
    	post(request, response);
    }
    
    private void post(Request<?> request, Response<?> response) {
    	
    	if (mRequestQueue != null) {
        	mRequestQueue.finish(request, response);
    	}
    	
    	if (request.getHandler() != null) {
    		request.getHandler().post(new DeliveryRunnable(request, response));
    	} else {
            mResponsePoster.execute(new DeliveryRunnable(request, response));
    	}
    	
    }
    
    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class DeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;

        public DeliveryRunnable(Request request, Response response) {
            mRequest = request;
            mResponse = response;
        }

        @SuppressWarnings("unchecked")
        public void run() {
        	
            // If this request has canceled, finish it and don't deliver.
            if (mRequest.isCanceled()) {
            	mRequest.addEvent("canceled-at-delivery");
                return;
            }
            
            mRequest.deliverResponse(mResponse.isCache, mResponse.result, mResponse.error);
            
       }
    }
}