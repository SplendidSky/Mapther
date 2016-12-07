package com.example.administrator.mapther;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/7.
 */

public class WeatherActivity extends AppCompatActivity {

    private String url;
    private String city;
    private String temperature;

    private TextView weather_city;
    private TextView weather_temperature;
    private EditText weather_search;

    private String getData() {
        Log.i("key", "weather");
        city = weather_city.getText().toString();
        url = "http://api.map.baidu.com/telematics/v3/weather?location=" + city + "&output=json&ak=E4805d16520de693a3fe707cdc962045";
        String result = "";
        try {
            URL mURL = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(10000);

            InputStream inputStream = httpURLConnection.getInputStream();
            result = getStringFromInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean update() {
        String result = getData();
        Log.i("key", result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray results = jsonObject.getJSONArray("results");
            if (results != null)
                return false;
            JSONArray weather_data = results.getJSONArray(0).getJSONArray(3);
            JSONObject today = weather_data.getJSONObject(0);
            JSONObject tomorrow = weather_data.getJSONObject(1);
            JSONObject third_day = weather_data.getJSONObject(2);
            JSONObject forth_day = weather_data.getJSONObject(3);

            temperature = today.getString("temperature");
            weather_temperature.setText(temperature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getStringFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return state;
    }

    private boolean isEditTextLoseFocus(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom)
                return false;
            else
                return true;
        }
        return false;
    }

    private void editTextLoseFocus(IBinder token) {
        if (token != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            }).start();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isEditTextLoseFocus(v, motionEvent))
                editTextLoseFocus(v.getWindowToken());
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        weather_city = (TextView) findViewById(R.id.weather_city);
        weather_temperature = (TextView) findViewById(R.id.weather_temperature);
        weather_search = (EditText) findViewById(R.id.weather_search);

        weather_search.setText(getIntent().getStringExtra("city"));
        city = weather_city.getText().toString();

        handler.post(runnable);

    }
}
