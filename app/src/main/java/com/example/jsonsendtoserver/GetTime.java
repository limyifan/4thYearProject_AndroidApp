package com.example.jsonsendtoserver;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetTime extends AppCompatActivity {
    int time = 0;
    static final int GET_TIME_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_time);



        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText timeNum = (EditText) findViewById(R.id.time);


                time = Integer.parseInt(timeNum.getText().toString());
                //     new SendDeviceDetails().execute("https://www.201.team/tripit-http.php/?location=" + location + "&time=" + time);
                new SendDeviceDetails().execute("https://201.team/time.php/?timespent="+time);
                Log.d("TAG", "TIME SENT IS"+time);


                Intent returnIntent = new Intent(GetTime.this, MapsDisplay.class);
                returnIntent.putExtra("result", time);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) { //what you want done in the thread

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                //make httpurlconnection object by casting url object to it. This is where the connection opens to url.
                httpURLConnection.setRequestMethod("POST");    // post request

                httpURLConnection.setDoOutput(true);          // connection outputs - true

                Log.d("test", params[0]);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                //make dataoutputstream using getOutputStream, we are going to write to it
                // getOutputStream opens a stream with intention to write to server
                wr.writeBytes("request="); // + params[1]
                //sending data
                wr.flush();
                wr.close(); //outputStream is closed, not connection

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                //read data from stream, from server

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) { //adding each character from input to data string
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();

                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // receiving response code from server

        }
    }
}





