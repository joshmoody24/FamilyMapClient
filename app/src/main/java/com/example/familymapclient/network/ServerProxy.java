package com.example.familymapclient.network;

import android.os.Handler;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import request.*;

public class ServerProxy {

    String host;
    int port;

    public ServerProxy(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
    The clear method is used only in automated testing
     */
    public void clear(){
        postRequest("/clear", null, null);
    }

    public LoginResult login(LoginRequest request){
        String postBody = new Gson().toJson(request);
        String responseData = postRequest("/user/login", postBody, null);
        LoginResult result = new Gson().fromJson(responseData, LoginResult.class);
        return result;
    }

    public RegisterResult register(RegisterRequest request){
        String postBody = new Gson().toJson(request);
        String responseData = postRequest("/user/register", postBody, null);
        RegisterResult result = new Gson().fromJson(responseData, RegisterResult.class);
        return result;
    }

    public GetAllPersonsResult getPeople(String auth){
        String responseData = getRequest("/person", auth);
        GetAllPersonsResult result = new Gson().fromJson(responseData, GetAllPersonsResult.class);
        return result;
    }

    public GetAllEventsResult getEvents(String auth){
        String responseData = getRequest("/event", auth);
        GetAllEventsResult result = new Gson().fromJson(responseData, GetAllEventsResult.class);
        return result;
    }

    protected String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        sr.close();
        return sb.toString();
    }

    protected void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
        sw.close();
    }

    String getRequest(String urlPath, String auth){
        try{
            URL url = new URL("http://" + host + ":" + Integer.toString(port) + urlPath);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            if(auth != null) http.addRequestProperty("Authorization", auth);

            http.addRequestProperty("Accept", "application/json");
            http.connect();

            InputStream responseBody = http.getErrorStream();
            if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
                responseBody = http.getInputStream();
            }

            String responseData = readString(responseBody);
            return responseData;
        } catch(Exception e){
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    String postRequest(String urlPath, String payload, String auth){
        try{
            if(payload == null) payload = "";
            URL url = new URL("http://" + host + ":" + Integer.toString(port) + urlPath);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true); // include post data
            if(auth != null) http.addRequestProperty("Authorization", auth);

            http.addRequestProperty("Accept", "application/json");
            http.connect();

            OutputStream requestBody = http.getOutputStream();
            writeString(payload, requestBody);
            requestBody.close();

            InputStream responseBody = http.getErrorStream();
            if(http.getResponseCode() == HttpURLConnection.HTTP_OK){
                responseBody = http.getInputStream();
            }
            if(responseBody == null) responseBody = http.getInputStream(); // just in case
            String responseData = readString(responseBody);
            return responseData;
        } catch(Exception e){
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
