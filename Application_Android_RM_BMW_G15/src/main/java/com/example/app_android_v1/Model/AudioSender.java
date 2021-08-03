package com.example.app_android_v1.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class AudioSender implements Callable<String> {

    String outputFile;

    public AudioSender(String fileLocation){
        outputFile=fileLocation;
    }

    public String call(){

        File output=new File(outputFile);
        String response = new String ();
        try {
            //On definit le endPoint (ladreese de notre serveur node)
            MultipartUtility multipart = new MultipartUtility("http://192.168.1.20:3000/upload");
            multipart.addFilePart("audio", output);
            response = multipart.finish();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            Log.d("SR", "SERVER REPLIED:");
            return response;
        }

    }
}