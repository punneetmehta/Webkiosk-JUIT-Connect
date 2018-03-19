package juit.webkiosk.helper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by puneet on 01/01/18.
 */

public class HTTPGetRequest extends AsyncTask<String, Void, String> {
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 30000;
    public static final int CONNECTION_TIMEOUT = 30000;
    @Override
    protected String doInBackground(String... params){
        String stringUrl = params[0];
        String result;
        String inputLine;
        try {
            URL myUrl = new URL(stringUrl);
            HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
            connection.setRequestMethod(REQUEST_METHOD);
            connection.connect();
            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            result = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }
    protected void onPostExecute(String result){
        Log.e("HTTPGetRequest",""+result);
        super.onPostExecute(result);
    }
}

