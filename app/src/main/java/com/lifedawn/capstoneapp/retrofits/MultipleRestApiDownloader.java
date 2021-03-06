package com.lifedawn.capstoneapp.retrofits;

import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lifedawn.capstoneapp.retrofits.parameters.RequestParameter;
import com.lifedawn.capstoneapp.weather.DataProviderType;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public abstract class MultipleRestApiDownloader {
   private static final String tag = "MultipleJsonDownloader";
   private final ZonedDateTime requestDateTime = ZonedDateTime.now();
   private volatile int requestCount;
   private volatile int responseCount;
   private boolean responseCompleted;

   private Map<String, String> valueMap = new HashMap<>();
   private Map<RetrofitClient.ServiceType, Call<?>> callMap = new HashMap<>();

   protected Map<DataProviderType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> responseMap = new ArrayMap<>();

   public MultipleRestApiDownloader() {
   }

   public Map<RetrofitClient.ServiceType, Call<?>> getCallMap() {
      return callMap;
   }

   public Map<DataProviderType, ArrayMap<RetrofitClient.ServiceType, ResponseResult>> getResponseMap() {
      return responseMap;
   }

   public MultipleRestApiDownloader(int requestCount) {
      this.requestCount = requestCount;
   }

   public ZonedDateTime getRequestDateTime() {
      return requestDateTime;
   }

   public void setRequestCount(int requestCount) {
      this.requestCount = requestCount;
   }

   public int getRequestCount() {
      return requestCount;
   }

   public void setResponseCount(int responseCount) {
      this.responseCount = responseCount;
   }

   public int getResponseCount() {
      return responseCount;
   }

   public String get(@NonNull @NotNull String key) {
      return valueMap.get(key);
   }




   public void put(@NonNull @NotNull String key, @NonNull @NotNull String value) {
      valueMap.put(key, value);
   }

   public boolean isResponseCompleted() {
      return responseCompleted;
   }

   public Map<String, String> getValueMap() {
      return valueMap;
   }

   public abstract void onResult();

   public abstract void onCanceled();

   public void cancel() {
      responseCount = requestCount + 1000;

      if (!callMap.isEmpty()) {
         for (Call<?> call : callMap.values()) {
            call.cancel();
         }
      }

   }

   public void processResult(DataProviderType dataProviderType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType,
                             Response<?> response, Object responseObj, String responseText) {
      responseCount++;

      if (!responseMap.containsKey(dataProviderType)) {
         responseMap.put(dataProviderType, new ArrayMap<>());
      }
      responseMap.get(dataProviderType).put(serviceType, new ResponseResult(requestParameter, response, responseObj, responseText));
      Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);

      if (requestCount == responseCount) {
         responseCompleted = true;
         onResult();
      }
   }

   public void processResult(DataProviderType dataProviderType, RequestParameter requestParameter, RetrofitClient.ServiceType serviceType, Throwable t) {
      responseCount++;

      if (!responseMap.containsKey(dataProviderType)) {
         responseMap.put(dataProviderType, new ArrayMap<>());
      }

      responseMap.get(dataProviderType).put(serviceType, new ResponseResult(requestParameter, t));
      Log.e(tag, "requestCount : " + requestCount + ",  responseCount : " + responseCount);

      if (requestCount == responseCount) {
         responseCompleted = true;
         onResult();
      }
   }

   public RequestParameter getRequestParameter(DataProviderType dataProviderType, RetrofitClient.ServiceType serviceType) {
      return responseMap.get(dataProviderType).get(serviceType).getRequestParameter();
   }

   public static class ResponseResult {
      private final RequestParameter requestParameter;
      private boolean successful;

      private Response<?> response;
      private Throwable t;
      private String responseText;
      private Object responseObj;

      public ResponseResult(RequestParameter requestParameter, Throwable t) {
         this.t = t;
         successful = false;
         this.requestParameter = requestParameter;
      }

      public ResponseResult(RequestParameter requestParameter, Response<?> response, Object responseObj, String responseText) {
         this.response = response;
         successful = true;
         this.responseObj = responseObj;
         this.requestParameter = requestParameter;
         this.responseText = responseText;
      }

      public Response<?> getResponse() {
         return response;
      }

      public void setResponse(Response<?> response) {
         this.response = response;
      }

      public Throwable getT() {
         return t;
      }

      public void setT(Throwable t) {
         this.t = t;
      }

      public RequestParameter getRequestParameter() {
         return requestParameter;
      }

      public boolean isSuccessful() {
         return successful;
      }

      public String getResponseText() {
         return responseText;
      }

      public Object getResponseObj() {
         return responseObj;
      }
   }
}
