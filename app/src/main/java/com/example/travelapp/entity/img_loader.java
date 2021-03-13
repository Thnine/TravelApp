package com.example.travelapp.entity;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 *
 * 图像单元
 *
 */

public class img_loader implements Serializable {
    private File content;
    private String img_name;
    private int img_id;


    private class load_Thread extends Thread {
        private int img_id;
        private File content;
        private ImageView img;
        private Activity a;

        public load_Thread(int img_id,File content,ImageView img,Activity a){
            super();
            this.img_id = img_id;
            this.content = content;
            this.img = img;
            this.a = a;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            super.run();
            Log.d("iL","ok0");
            Log.d("iL",this.content.getPath());
            if (!this.content.exists()) {
                String url = "http://121.196.149.163:8080/dzwblog/GetSImg_server";
                //创建HTTP对象
                HttpPost httpRequset = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("img_id", Integer.toString(this.img_id)));
                Log.d("iL","ok1");
                try {
                    //装入数据和设置数据格式
                    httpRequset.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                    //发出请求
                    CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpRequset);
                    //获取服务器响应的资源
                    Log.d("iL","ok2");
                    String line = null;
                    JSONObject resultJson = null;
                    StringBuilder entityStringBuilder = new StringBuilder();
                    try {
                        BufferedReader b = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                        while ((line = b.readLine()) != null) {
                            entityStringBuilder.append(line + "/n");
                        }
                        JSONObject myjson = new JSONObject(entityStringBuilder.toString());
                        String temp_content = myjson.getString("img");
                        byte[] toT = new byte[0];
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            toT = Base64.getDecoder().decode(temp_content);
                        }
                        Log.d("iL","byte length : " + toT.length);
                        //byte数组转文件
                        //开始传输文件
                        BufferedOutputStream bos = null;
                        FileOutputStream fos = null;
                        fos = new FileOutputStream(this.content);
                        bos = new BufferedOutputStream(fos);
                        bos.write(toT);
                        bos.flush();
                        bos.close();
                        fos.close();

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Runnable t = new Runnable() {
                public void run() {
                    img.setImageURI(Uri.fromFile(content));
                }

            };
            a.runOnUiThread(t);

        }
    }

    public img_loader(int img_id,File content){
        this.img_id = img_id;
        this.content = content;
    }

    public File get_content() {
        return this.content;
    }

    public String get_name() {
        return this.img_name;
    }

    public int get_img_id() {
        return img_id;
    }

    //该图片是否已经加载
    public boolean is_loaded() {
        return content.exists();
    }

    //加载图片
    public void load_pic(ImageView img,Activity a) {

        new load_Thread(this.img_id,this.content,img,a).start();
    }
    private BitmapFactory.Options getBitmapOption(int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }
}
