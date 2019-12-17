package com.example.jsonsendtoserver;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkCall {

    String data = "";

    HttpURLConnection httpURLConnection = null;
    String makeServiceCall(String url) {
        try {
            URL callUrl = new URL (url);
            httpURLConnection = (HttpURLConnection) callUrl.openConnection();
            //make httpurlconnection object by casting url object to it. This is where the connection opens to url.
            httpURLConnection.setRequestMethod("POST");    // post request


            httpURLConnection.setDoOutput(true);          // connection outputs - true


           // Log.d("test", params[0]);


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
}

