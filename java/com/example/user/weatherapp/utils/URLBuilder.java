package com.example.user.weatherapp.utils;


import android.net.Uri;


public class URLBuilder {

    final String FORECAST_BASE_URL ="http://api.openweathermap.org/data/2.5/forecast/daily?";
    final String QUERY_PARAM = "q";
    final String FORMAT_PARAM = "mode";
    final String UNITS_PARAM = "units";
    final String DAYS_PARAM = "cnt";
    final String LAT_PARAM = "lat";
    final String LON_PARAM = "lon";


    private String format = "json";
    private String units = "metric";



    private int numDays = 7;
    private String locationName =null;
    private String lattitude =null;
    private String longitude =null;

    public URLBuilder( String lattitude, String longitude){
        this.lattitude = lattitude;
        this.longitude =longitude;

    }
    public URLBuilder( String LocationName){
        this.locationName =locationName;
    }

    public String getURL(){
        if (locationName == null) {
            if (lattitude==null) return null;
            else {
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LAT_PARAM, lattitude)
                        .appendQueryParameter(LON_PARAM, longitude)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();
                return builtUri.toString();
            }
        }
        else {
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationName)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();
            return builtUri.toString();
        }
    }
    public void setNumDays(int numDays) {
        this.numDays = numDays;
    }
    public void setUnits(String units) {
        this.units = units;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public void setCoordinates(String coord_x,String coord_y){
        this.lattitude =coord_x;
        this.longitude =coord_y;
    }
    public int getNumDays() {
        return numDays;
    }
}
