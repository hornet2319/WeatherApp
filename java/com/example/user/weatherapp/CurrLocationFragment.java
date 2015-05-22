package com.example.user.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.weatherapp.dialogs.SaveLocationDialog;
import com.example.user.weatherapp.dialogs.WarningDialog;
import com.example.user.weatherapp.utils.GPSUtil;
import com.example.user.weatherapp.utils.InternetUtil;
import com.example.user.weatherapp.utils.URLBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class CurrLocationFragment extends Fragment {

    ArrayAdapter<String> mAdapter;
    List<String> weekForecast;
    static final String ARG_SECTION_NUMBER = "section_number";
    private static final String LOG_TAG=CurrLocationFragment.class.getSimpleName();

    // Will contain the raw JSON response as a string.
    private String forecastJsonStr = null;
    private String locationName = null;
    Context context;
    Location location;
    WarningDialog dialog;

        public CurrLocationFragment(String locationName) {
        this.locationName =locationName;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_refresh:{
                new FetchWeatherTask().execute();
                return true;
            }
            case R.id.action_save:{
                //Save location code goes here
               Log.i(LOG_TAG, "Saving location, Starting SaveLocationDialog");
                SaveLocationDialog dialog=new SaveLocationDialog(context);
                dialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context=getActivity();
         weekForecast=new ArrayList<String>();
        weekForecast.add("Today-Sunny-88/63");
        weekForecast.add("Tomorrow-Foggy-70/46");
        weekForecast.add("Weds-Cloudy-72/63");
        weekForecast.add("Thurs-Rainy-64/51");
        weekForecast.add("Fri-Foggy-70/46");
        weekForecast.add("Sat-Sunny-76/68");

        mAdapter=
                new ArrayAdapter<String>(
                        context,
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview,
                        weekForecast);


        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        ListView list=(ListView)rootView.findViewById(R.id.listview_forecast);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context,mAdapter.getItem(position),Toast.LENGTH_SHORT).show();
            }
        });

        InternetUtil internet=new InternetUtil(context);
        if (internet.isConnected())
        new FetchWeatherTask().execute();
        else {
            dialog=new WarningDialog(context);
            dialog.showMessage(context.getString(R.string.warning_internet));

        }
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, String[]> {
        private final String LOG_TAG =FetchWeatherTask.class.getSimpleName();



        /* The date/time conversion code is going to be moved outside the asynctask later,
                 * so for convenience we're breaking it out into its own method now.
                 */
               private String getReadableDateString(long time){
                   // Because the API returns a unix timestamp (measured in seconds),
                   // it must be converted to milliseconds in order to be converted to valid date.
                   SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE dd MMM");
                   return shortenedDateFormat.format(time);
                   }

                     /**
                  * Prepare the weather high/lows for presentation.*/
                   private String formatHighLows(double high, double low) {
                   // For presentation, assume the user doesn't care about tenths of a degree.
                      long roundedHigh = Math.round(high);
                      long roundedLow = Math.round(low);

                       String highLowStr = roundedHigh + "/" + roundedLow;
                       return highLowStr;
                    }

                        /**
                   * Take the String representing the complete forecast in JSON Format and
                   * pull out the data we need to construct the Strings needed for the wireframes.
                   *
         +         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         +         * into an Object hierarchy for us.
         +         */
                        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                        throws JSONException {

                                // These are the names of the JSON objects that need to be extracted.
                            final String OWM_LIST = "list";
                        final String OWM_WEATHER = "weather";
                        final String OWM_TEMPERATURE = "temp";
                        final String OWM_MAX = "max";
                        final String OWM_MIN = "min";
                        final String OWM_DESCRIPTION = "main";
                            JSONObject forecastJson = new JSONObject(forecastJsonStr);
                            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                            // OWM returns daily forecasts based upon the local time of the city that is being
                            // asked for, which means that we need to know the GMT offset to translate this data
                            // properly.

                            // Since this data is also sent in-order and the first day is always the
                            // current day, we're going to take advantage of that to get a nice
                            // normalized UTC date for all of our weather.

                            Time dayTime = new Time();
                            dayTime.setToNow();

                            // we start at the day returned by local time. Otherwise this is a mess.
                            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                            // now we work exclusively in UTC
                            dayTime = new Time();
                            String[] resultStrs = new String[numDays];
                            for(int i = 0; i < weatherArray.length(); i++) {
                            // For now, using the format "Day, description, hi/low"
                                String day;
                                String description;
                                String highAndLow;

                                // Get the JSON object representing the day
                                JSONObject dayForecast = weatherArray.getJSONObject(i);

                                // The date/time is returned as a long.  We need to convert that
                                // into something human-readable, since most people won't read "1400356800" as
                                // "this saturday".
                                long dateTime;
                                // Cheating to convert this to UTC time, which is what we want anyhow
                                dateTime = dayTime.setJulianDay(julianStartDay+i);
                                day = getReadableDateString(dateTime);

                                // description is in a child array called "weather", which is 1 element long.
                                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                                description = weatherObject.getString(OWM_DESCRIPTION);

                                // Temperatures are in a child object called "temp".  Try not to name variables
                                // "temp" when working with temperature.  It confuses everybody.
                                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                                double high = temperatureObject.getDouble(OWM_MAX);
                                double low = temperatureObject.getDouble(OWM_MIN);

                                highAndLow = formatHighLows(high, low);resultStrs[i] = day + " - " + description + " - " + highAndLow;}

                            for (String s : resultStrs) {
                                Log.v(LOG_TAG, "Forecast entry: " + s);
                            }
                            return resultStrs;

                        }
        @Override
        protected String[] doInBackground(Void... params) {
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;
            URLBuilder urlBuilder=null;
            GPSUtil gpsUtil=null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
             /*
                */
                if (locationName != null) {
                    urlBuilder=new URLBuilder(locationName);
                }
                else{
                    gpsUtil=new GPSUtil(context);
                    location=gpsUtil.getLocation();
                    //Check location
                    if (location==null) {
                        dialog.showMessage(context.getString(R.string.warning_gps));
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                    else urlBuilder=new URLBuilder(""+location.getLatitude(),""+location.getLongitude());
                }
                Log.i(LOG_TAG,urlBuilder.getURL());
                URL url = new URL(urlBuilder.getURL());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                 forecastJsonStr= buffer.toString();
                Log.i(LOG_TAG, "Json Data loaded succesfully");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Asynctask Error", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, urlBuilder.getNumDays());
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
        @Override
        protected void onPostExecute(String[] data) {
            super.onPostExecute(data);
            if(data!=null){
                mAdapter.clear();
                for(String dayForecastStr:data){
                    mAdapter.add(dayForecastStr);
                }
            }
        }
        }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
