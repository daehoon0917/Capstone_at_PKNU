package com.lifedawn.capstoneapp.retrofits.parameters.kma;

import com.lifedawn.capstoneapp.retrofits.parameters.RequestParameter;

import java.util.HashMap;
import java.util.Map;

public class KmaCurrentConditionsParameters extends RequestParameter {
   private final String unit = "m%2Fs";
   private final String aws = "N";
   private final String code;
   private double latitude;
   private double longitude;

   public KmaCurrentConditionsParameters(String code) {
      this.code = code;
   }

   public String getCode() {
      return code;
   }

   public Map<String, String> getParametersMap() {
      Map<String, String> map = new HashMap<>();
      map.put("unit", unit);
      map.put("aws", aws);
      map.put("code", code);
      return map;
   }

   public double getLatitude() {
      return latitude;
   }

   public KmaCurrentConditionsParameters setLatitude(double latitude) {
      this.latitude = latitude;
      return this;
   }

   public double getLongitude() {
      return longitude;
   }

   public KmaCurrentConditionsParameters setLongitude(double longitude) {
      this.longitude = longitude;
      return this;
   }
}
