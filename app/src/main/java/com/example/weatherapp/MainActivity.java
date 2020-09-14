package com.example.weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private EditText city;
    private TextView showText;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            // This is where you do your work in the UI thread.
            // Your worker tells you in the message what to do.
            myToast(message.getData().getString("message"));
            showText.setVisibility(View.INVISIBLE);

        }
    };


    public void whatWeather(View view) {
        String result = "";


        try {
            String encoded = URLEncoder.encode(city.getText().toString().trim(), "UTF-8");
            result = new WebDownloader().execute("https://openweathermap.org/data/2.5/weather?q=" + encoded + "&appid=439d4b804bc8187953eb36d2a8c26a02").get();
            System.out.println(result);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(city.getWindowToken(), 0);


        } catch (Exception e) {


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city = (EditText) findViewById(R.id.city);
        showText = (TextView) findViewById(R.id.textView);

    }

    private void myToast(final String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private int takeCelsius(double kelvinValue) {


        return (int) (kelvinValue - 273.15);
    }

    public final class WebDownloader extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            String answer = "";
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int i = reader.read();
                while (i != -1) {
                    char ch = (char) i;
                    answer += ch;
                    i = reader.read();
                }

                return answer;
            } catch (Exception e) {
                e.printStackTrace();
                Message message = handler.obtainMessage(0);
                Bundle bundle = new Bundle();
                bundle.putString("message", "City doesn't found!");
                message.setData(bundle);
                message.sendToTarget();


                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                //myToast("no mutch found!");
            }
            try {
                String currespondant = "";

                JSONObject jsonObject = new JSONObject(result);
                String mainPart = jsonObject.getString("weather");
                String temperaturePart = jsonObject.getString("main");

                JSONObject temprObject = new JSONObject(temperaturePart);
                String realTemp = temprObject.getString("temp");


                JSONArray jsonArray = new JSONArray(mainPart);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonPart = jsonArray.getJSONObject(i);

                    Log.d("JSON", jsonPart.getString("main"));

                    currespondant += jsonPart.getString("main") + " : ";
                    Log.d("JSON", jsonPart.getString("description"));
                    currespondant += jsonPart.getString("description");
                    currespondant += "\n";
                }
                System.out.println(mainPart);
                System.out.println(realTemp);


             /*   for(int i=0;i<jsonArrayMain.length();i++){
                    JSONObject jsonPart=jsonArrayMain.getJSONObject(i);
                    currespondant+="\n"+takeCelsius(Integer.parseInt(jsonPart.getString("temp")))+" C";


                }

*/
                String backspace = "  ";
                for (int i = 0; i < currespondant.indexOf(":"); i++) {
                    backspace += " ";
                }
                currespondant += backspace + realTemp + " °C ";
                showText.setText(currespondant);
                showText.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();

                System.out.println(e);
            }
        }
    }


}
